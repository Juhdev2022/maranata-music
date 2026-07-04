package br.com.maranatamusic.presentation.auth;

import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void limparBase() {
        usuarioRepository.deleteAll();
    }

    // ---- cenário 1 ----

    @Test
    void registro_dadosValidos_deveRetornar201ComToken() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RegistroRequest("Maria", "maria@maranata.com", "senha123", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // ---- cenário 2 ----

    @Test
    void registro_emailDuplicado_deveRetornar409() throws Exception {
        registrar("Maria", "maria@maranata.com", "senha123");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RegistroRequest("Maria2", "maria@maranata.com", "senha999", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Email já cadastrado"));
    }

    // ---- cenário 3 ----

    @Test
    void registro_emailInvalido_deveRetornar400ComCampoEmail() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RegistroRequest("Maria", "nao-e-um-email", "senha123", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    // ---- cenário 4 ----

    @Test
    void registro_senhaCurta_deveRetornar400ComCampoSenha() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RegistroRequest("Maria", "maria@maranata.com", "123", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    // ---- cenário 5 ----

    @Test
    void login_credenciaisValidas_deveRetornar200ComTokenEPapeis() throws Exception {
        registrar("Maria", "maria@maranata.com", "senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest("maria@maranata.com", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.papeis[0]").value("MUSICO"));
    }

    // ---- cenário 6 ----

    @Test
    void login_senhaErrada_deveRetornar401ComMensagemGenerica() throws Exception {
        registrar("Maria", "maria@maranata.com", "senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest("maria@maranata.com", "senhaErrada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Credenciais inválidas"));
    }

    // ---- cenário 7 ----

    @Test
    void login_emailInexistente_deveRetornar401ComMensagemGenerica() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest("naoexiste@maranata.com", "senha123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Credenciais inválidas"));
    }

    // ---- cenário 8 ----

    @Test
    void rotaProtegida_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    // ---- cenário 9 ----

    @Test
    void rotaProtegida_tokenValido_deveRetornar200() throws Exception {
        registrar("Maria", "maria@maranata.com", "senha123");
        String token = extrairToken("maria@maranata.com", "senha123");

        mockMvc.perform(get("/actuator/info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ---- cenário 10 ----

    @Test
    void rotaProtegida_tokenExpirado_deveRetornar401() throws Exception {
        long passado = System.currentTimeMillis() - 1000;
        String tokenExpirado = Jwts.builder()
                .subject("qualquer@maranata.com")
                .issuedAt(new Date(passado - 1000))
                .expiration(new Date(passado))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                .compact();

        mockMvc.perform(get("/actuator/info")
                        .header("Authorization", "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    // ---- helpers ----

    private void registrar(String nome, String email, String senha) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RegistroRequest(nome, email, senha, null))));
    }

    private String extrairToken(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(email, senha))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
