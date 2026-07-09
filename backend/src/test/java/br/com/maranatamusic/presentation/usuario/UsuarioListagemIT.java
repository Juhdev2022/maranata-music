package br.com.maranatamusic.presentation.usuario;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicaCultoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioListagemIT {

    private static final String SENHA_PADRAO = "senha123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstrumentoRepository instrumentoRepository;

    @Autowired
    private MusicoInstrumentoRepository musicoInstrumentoRepository;

    @Autowired
    private CultoRepository cultoRepository;

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private MusicaCultoRepository musicaCultoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBase() {
        escalaRepository.deleteAll();
        musicaCultoRepository.deleteAll();
        cultoRepository.deleteAll();
        musicoInstrumentoRepository.deleteAll();
        usuarioRepository.deleteAll();
        instrumentoRepository.deleteAll();
    }

    // ---- cenário 1 ----

    @Test
    void listar_semParametros_deveRetornarApenasAtivos() throws Exception {
        String token = obterTokenLider("lider1@maranata.com");
        criarUsuario("Ativo Um", "ativo1@maranata.com", Papel.MUSICO, true);
        criarUsuario("Inativo Um", "inativo1@maranata.com", Papel.MUSICO, false);

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(hasItem("ativo1@maranata.com")))
                .andExpect(jsonPath("$[*].email").value(not(hasItem("inativo1@maranata.com"))));
    }

    // ---- cenário 2 ----

    @Test
    void listar_ativosFalse_deveRetornarApenasInativos() throws Exception {
        String token = obterTokenLider("lider2@maranata.com");
        criarUsuario("Ativo Dois", "ativo2@maranata.com", Papel.MUSICO, true);
        criarUsuario("Inativo Dois", "inativo2@maranata.com", Papel.MUSICO, false);

        mockMvc.perform(get("/api/usuarios")
                        .param("ativos", "false")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(hasItem("inativo2@maranata.com")))
                .andExpect(jsonPath("$[*].email").value(not(hasItem("ativo2@maranata.com"))));
    }

    // ---- cenário 3 ----

    @Test
    void listar_filtradoPorPapel_deveRetornarApenasComEssePapel() throws Exception {
        String token = obterTokenLider("lider3@maranata.com");
        criarUsuario("Musico Tres", "musico3@maranata.com", Papel.MUSICO, true);

        mockMvc.perform(get("/api/usuarios")
                        .param("papel", "LIDER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email").value(hasItem("lider3@maranata.com")))
                .andExpect(jsonPath("$[*].email").value(not(hasItem("musico3@maranata.com"))));
    }

    // ---- cenário 4 ----

    @Test
    void listar_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    // ---- cenário 5 ----

    @Test
    void listar_response_nuncaExpoeCamposSensiveis() throws Exception {
        String token = obterTokenLider("lider5@maranata.com");
        criarUsuario("Sensivel Cinco", "sensivel5@maranata.com", Papel.MUSICO, true);

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senhaHash").doesNotExist())
                .andExpect(jsonPath("$[0].telefone").doesNotExist())
                .andExpect(jsonPath("$[0].fcmToken").doesNotExist());
    }

    // ---- helpers ----

    private String obterTokenLider(String email) throws Exception {
        criarUsuario("Líder Teste", email, Papel.LIDER, true);
        return login(email);
    }

    private Usuario criarUsuario(String nome, String email, Papel papel, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        usuario.setAtivo(ativo);
        usuario.getPapeis().add(papel);
        return usuarioRepository.save(usuario);
    }

    private String login(String email) throws Exception {
        String resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(email, SENHA_PADRAO))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
