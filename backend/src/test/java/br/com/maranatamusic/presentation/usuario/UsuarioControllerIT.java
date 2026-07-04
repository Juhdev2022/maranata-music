package br.com.maranatamusic.presentation.usuario;

import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.usuario.dto.AlterarPapelRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerIT {

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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBase() {
        escalaRepository.deleteAll();
        cultoRepository.deleteAll();
        musicoInstrumentoRepository.deleteAll();
        usuarioRepository.deleteAll();
        instrumentoRepository.deleteAll();
    }

    // ---- cenário 1 ----

    @Test
    void promover_musicoALider_deveRetornar200() throws Exception {
        String tokenLider = obterTokenLider("lider1@maranata.com");
        Long musicoId = criarUsuarioComPapel("Musico Um", "musico1@maranata.com", Papel.MUSICO);

        mockMvc.perform(post("/api/usuarios/" + musicoId + "/papeis")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AlterarPapelRequest(Papel.LIDER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papeis").isArray())
                .andExpect(jsonPath("$.papeis", org.hamcrest.Matchers.hasItem("LIDER")));
    }

    // ---- cenário 2 ----

    @Test
    void promover_quemJaTemOPapel_deveRetornar200Idempotente() throws Exception {
        String tokenLider = obterTokenLider("lider2@maranata.com");
        Long outroLiderId = criarUsuarioComPapel("Outro Lider", "lider2b@maranata.com", Papel.LIDER);

        mockMvc.perform(post("/api/usuarios/" + outroLiderId + "/papeis")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AlterarPapelRequest(Papel.LIDER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papeis", org.hamcrest.Matchers.hasItem("LIDER")));
    }

    // ---- cenário 3 ----

    @Test
    void promover_comoMusico_deveRetornar403() throws Exception {
        String tokenMusico = obterTokenMusico("musico3@maranata.com");
        Long outroId = criarUsuarioComPapel("Outro Tres", "outro3@maranata.com", Papel.MUSICO);

        mockMvc.perform(post("/api/usuarios/" + outroId + "/papeis")
                        .header("Authorization", "Bearer " + tokenMusico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AlterarPapelRequest(Papel.LIDER))))
                .andExpect(status().isForbidden());
    }

    // ---- cenário 4 ----

    @Test
    void rebaixar_outroLider_comMaisDeUmLiderAtivo_deveRetornar204() throws Exception {
        String tokenLider = obterTokenLider("lider4@maranata.com");
        Long outroLiderId = criarUsuarioComPapel("Outro Lider Quatro", "lider4b@maranata.com", Papel.LIDER);

        mockMvc.perform(delete("/api/usuarios/" + outroLiderId + "/papeis/LIDER")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isNoContent());
    }

    // ---- cenário 5 ----
    // Cobre também o cenário "executor é o último líder do sistema" do prompt original —
    // a checagem é por contagem agregada (countByPapelEAtivo), não depende de quem executa.

    @Test
    void rebaixar_seMesmo_sendoUnicoLider_deveRetornar409() throws Exception {
        Usuario lider = criarUsuario("Lider Unico", "lider5@maranata.com", Papel.LIDER);
        String token = login(lider.getEmail());

        mockMvc.perform(delete("/api/usuarios/" + lider.getId() + "/papeis/LIDER")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Não é possível remover o último líder do sistema"));
    }

    // ---- cenário 6 ----

    @Test
    void promover_usuarioInexistente_deveRetornar404() throws Exception {
        String tokenLider = obterTokenLider("lider6@maranata.com");

        mockMvc.perform(post("/api/usuarios/9999/papeis")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AlterarPapelRequest(Papel.LIDER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado"));
    }

    // ---- cenário 7 ----

    @Test
    void removerPapel_queUsuarioNaoTem_deveRetornar204Idempotente() throws Exception {
        String tokenLider = obterTokenLider("lider7@maranata.com");
        Long musicoId = criarUsuarioComPapel("Musico Sete", "musico7@maranata.com", Papel.MUSICO);

        mockMvc.perform(delete("/api/usuarios/" + musicoId + "/papeis/MINISTRO")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isNoContent());
    }

    // ---- helpers ----

    private String obterTokenLider(String email) throws Exception {
        criarUsuario("Líder Teste", email, Papel.LIDER);
        return login(email);
    }

    private String obterTokenMusico(String email) throws Exception {
        criarUsuario("Músico Teste", email, Papel.MUSICO);
        return login(email);
    }

    private Long criarUsuarioComPapel(String nome, String email, Papel papel) {
        return criarUsuario(nome, email, papel).getId();
    }

    private Usuario criarUsuario(String nome, String email, Papel papel) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        usuario.setAtivo(true);
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
