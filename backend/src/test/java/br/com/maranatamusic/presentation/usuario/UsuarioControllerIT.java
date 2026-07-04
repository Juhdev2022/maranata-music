package br.com.maranatamusic.presentation.usuario;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import br.com.maranatamusic.domain.enums.CultoTipo;
import br.com.maranatamusic.domain.enums.EscalaStatus;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    // ---- cenário 9 ----

    @Test
    void desativar_musicoSemEscalasFuturas_deveRetornar204() throws Exception {
        String tokenLider = obterTokenLider("lider9@maranata.com");
        Long musicoId = criarUsuarioComPapel("Musico Nove", "musico9@maranata.com", Papel.MUSICO);

        mockMvc.perform(delete("/api/usuarios/" + musicoId)
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isNoContent());
    }

    // ---- cenário 10 ----

    @Test
    void desativar_comEscalaFuturaConfirmada_deveRetornar409() throws Exception {
        String tokenLider = obterTokenLider("lider10@maranata.com");
        Usuario musico = criarUsuario("Musico Dez", "musico10@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão M10", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.now().plusDays(5), CultoTipo.DOMINGO_NOITE);
        criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.CONFIRMADA);

        mockMvc.perform(delete("/api/usuarios/" + musico.getId())
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(
                        "Usuário possui 1 escala(s) futura(s) confirmada(s) — não pode ser desativado"));
    }

    // ---- cenário 11 ----

    @Test
    void desativar_unicoLider_deveRetornar409() throws Exception {
        Usuario lider = criarUsuario("Lider Onze", "lider11@maranata.com", Papel.LIDER);
        String token = login(lider.getEmail());

        mockMvc.perform(delete("/api/usuarios/" + lider.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Não é possível remover o último líder do sistema"));
    }

    // ---- cenário 12 ----

    @Test
    void reativar_usuarioDesativado_deveRetornar200() throws Exception {
        String tokenLider = obterTokenLider("lider12@maranata.com");
        Usuario musico = criarUsuario("Musico Doze", "musico12@maranata.com", Papel.MUSICO);
        musico.setAtivo(false);
        usuarioRepository.save(musico);

        mockMvc.perform(post("/api/usuarios/" + musico.getId() + "/reativar")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
    }

    // ---- cenário 13 ----

    @Test
    void login_usuarioDesativado_deveRetornar401() throws Exception {
        Usuario musico = criarUsuario("Musico Treze", "musico13@maranata.com", Papel.MUSICO);
        musico.setAtivo(false);
        usuarioRepository.save(musico);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(musico.getEmail(), SENHA_PADRAO))))
                .andExpect(status().isUnauthorized());
    }

    // ---- cenário 14 ----

    @Test
    void tokenValido_usuarioDesativadoDepoisDeGerarToken_deveRetornar401() throws Exception {
        Usuario musico = criarUsuario("Musico Quatorze", "musico14@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());

        musico.setAtivo(false);
        usuarioRepository.save(musico);

        mockMvc.perform(get("/api/instrumentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    // ---- cenário 15 ----

    @Test
    void login_depoisDeReativado_deveRetornar200() throws Exception {
        Usuario musico = criarUsuario("Musico Quinze", "musico15@maranata.com", Papel.MUSICO);
        musico.setAtivo(false);
        usuarioRepository.save(musico);
        musico.setAtivo(true);
        usuarioRepository.save(musico);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LoginRequest(musico.getEmail(), SENHA_PADRAO))))
                .andExpect(status().isOk());
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

    private Long criarInstrumento(String nome, CategoriaInstrumento categoria) {
        Instrumento instrumento = new Instrumento();
        instrumento.setNome(nome);
        instrumento.setCategoria(categoria);
        return instrumentoRepository.save(instrumento).getId();
    }

    private Long criarCulto(LocalDateTime dataHora, CultoTipo tipo) {
        Culto culto = new Culto();
        culto.setDataHora(dataHora);
        culto.setTipo(tipo);
        culto.setRepertorioTrancado(false);
        return cultoRepository.save(culto).getId();
    }

    private void criarEscala(Long cultoId, Long usuarioId, Long instrumentoId, EscalaStatus status) {
        Escala escala = new Escala();
        escala.setCulto(cultoRepository.getReferenceById(cultoId));
        escala.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        escala.setInstrumento(instrumentoRepository.getReferenceById(instrumentoId));
        escala.setStatus(status);
        escalaRepository.save(escala);
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
