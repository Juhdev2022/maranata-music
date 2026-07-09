package br.com.maranatamusic.presentation.repertorio;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.CultoTipo;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicaCultoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicaRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.SolicitacaoSubstituicaoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.repertorio.dto.AtualizarMusicaCultoRequest;
import br.com.maranatamusic.presentation.repertorio.dto.CriarMusicaCultoRequest;
import br.com.maranatamusic.presentation.repertorio.dto.RevisaoLiderRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RepertorioControllerIT {

    private static final String SENHA_PADRAO = "senha123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CultoRepository cultoRepository;

    @Autowired
    private MusicaRepository musicaRepository;

    @Autowired
    private MusicaCultoRepository musicaCultoRepository;

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private MusicoInstrumentoRepository musicoInstrumentoRepository;

    @Autowired
    private InstrumentoRepository instrumentoRepository;

    @Autowired
    private SolicitacaoSubstituicaoRepository solicitacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBase() {
        // Mesma base H2 é compartilhada entre classes de teste na mesma JVM — limpa tudo que
        // pode ter sobrado de outras classes antes de apagar usuario (referenciado por FK).
        solicitacaoRepository.deleteAll();
        musicaCultoRepository.deleteAll();
        escalaRepository.deleteAll();
        musicoInstrumentoRepository.deleteAll();
        cultoRepository.deleteAll();
        usuarioRepository.deleteAll();
        instrumentoRepository.deleteAll();
        musicaRepository.deleteAll();
    }

    // ---- R1: repertório vazio ----

    @Test
    void listar_cultoSemRepertorio_deveRetornar200ComListaVazia() throws Exception {
        Usuario lider = criarUsuario("Lider R1", "lider-r1@maranata.com", Papel.LIDER);
        String token = login(lider.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), lider.getId());

        mockMvc.perform(get("/api/cultos/" + cultoId + "/repertorio")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---- R2: ministro do culto adiciona música ----

    @Test
    void adicionar_comoMinistroDoCulto_deveRetornar201() throws Exception {
        Usuario ministro = criarUsuario("Ministro R2", "ministro-r2@maranata.com", Papel.MUSICO);
        String token = login(ministro.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());

        mockMvc.perform(post("/api/cultos/" + cultoId + "/repertorio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarMusicaCultoRequest(
                                "Grande é o Senhor", "https://youtube.com/watch?v=abc", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Grande é o Senhor"))
                .andExpect(jsonPath("$.linkVideo").value("https://youtube.com/watch?v=abc"))
                .andExpect(jsonPath("$.ordem").value(1));
    }

    // ---- R3: líder (não ministro) adiciona música ----

    @Test
    void adicionar_comoLiderNaoMinistro_deveRetornar201() throws Exception {
        Usuario ministro = criarUsuario("Ministro R3", "ministro-r3@maranata.com", Papel.MUSICO);
        Usuario lider = criarUsuario("Lider R3", "lider-r3@maranata.com", Papel.LIDER);
        String tokenLider = login(lider.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());

        mockMvc.perform(post("/api/cultos/" + cultoId + "/repertorio")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarMusicaCultoRequest("Ousado Amor", null, null))))
                .andExpect(status().isCreated());
    }

    // ---- R4: músico comum (nem ministro, nem líder) → 403 ----

    @Test
    void adicionar_comoMusicoComum_deveRetornar403() throws Exception {
        Usuario ministro = criarUsuario("Ministro R4", "ministro-r4@maranata.com", Papel.MUSICO);
        Usuario outroMusico = criarUsuario("Outro Musico R4", "outro-r4@maranata.com", Papel.MUSICO);
        String tokenOutro = login(outroMusico.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());

        mockMvc.perform(post("/api/cultos/" + cultoId + "/repertorio")
                        .header("Authorization", "Bearer " + tokenOutro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarMusicaCultoRequest("Ousado Amor", null, null))))
                .andExpect(status().isForbidden());
    }

    // ---- R5: culto sem ministro designado → 409 ----

    @Test
    void adicionar_cultoSemMinistro_deveRetornar409() throws Exception {
        Usuario lider = criarUsuario("Lider R5", "lider-r5@maranata.com", Papel.LIDER);
        String token = login(lider.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), null);

        mockMvc.perform(post("/api/cultos/" + cultoId + "/repertorio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarMusicaCultoRequest("Ousado Amor", null, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Culto sem ministro designado"));
    }

    // ---- R6: observação privada não aparece pra músico comum ----

    @Test
    void listar_comoMusicoComum_naoDeveVerObservacaoPrivada() throws Exception {
        Usuario ministro = criarUsuario("Ministro R6", "ministro-r6@maranata.com", Papel.MUSICO);
        Usuario lider = criarUsuario("Lider R6", "lider-r6@maranata.com", Papel.LIDER);
        Usuario musicoComum = criarUsuario("Musico Comum R6", "musico-r6@maranata.com", Papel.MUSICO);
        String tokenMinistro = login(ministro.getEmail());
        String tokenLider = login(lider.getEmail());
        String tokenMusico = login(musicoComum.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long musicaCultoId = adicionarERetornarId(tokenMinistro, cultoId, "Ousado Amor", null);

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/repertorio/" + musicaCultoId + "/revisao-lider")
                .header("Authorization", "Bearer " + tokenLider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RevisaoLiderRequest(
                        br.com.maranatamusic.domain.enums.RevisaoLider.COM_OBSERVACAO, "Cuidado com a tonalidade"))));

        mockMvc.perform(get("/api/cultos/" + cultoId + "/repertorio")
                        .header("Authorization", "Bearer " + tokenMusico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Ousado Amor"))
                .andExpect(jsonPath("$[0].observacaoLiderPrivada").doesNotExist());
    }

    // ---- R7: ministro do culto vê a observação privada ----

    @Test
    void listar_comoMinistroDoCulto_deveVerObservacaoPrivada() throws Exception {
        Usuario ministro = criarUsuario("Ministro R7", "ministro-r7@maranata.com", Papel.MUSICO);
        Usuario lider = criarUsuario("Lider R7", "lider-r7@maranata.com", Papel.LIDER);
        String tokenMinistro = login(ministro.getEmail());
        String tokenLider = login(lider.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long musicaCultoId = adicionarERetornarId(tokenMinistro, cultoId, "Ousado Amor", null);

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/repertorio/" + musicaCultoId + "/revisao-lider")
                .header("Authorization", "Bearer " + tokenLider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RevisaoLiderRequest(
                        br.com.maranatamusic.domain.enums.RevisaoLider.COM_OBSERVACAO, "Cuidado com a tonalidade"))));

        mockMvc.perform(get("/api/cultos/" + cultoId + "/repertorio")
                        .header("Authorization", "Bearer " + tokenMinistro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].observacaoLiderPrivada").value("Cuidado com a tonalidade"));
    }

    // ---- R8: PATCH revisao-lider APROVADA limpa observação ----

    @Test
    void revisar_comAprovada_deveLimparObservacao() throws Exception {
        Usuario ministro = criarUsuario("Ministro R8", "ministro-r8@maranata.com", Papel.MUSICO);
        Usuario lider = criarUsuario("Lider R8", "lider-r8@maranata.com", Papel.LIDER);
        String tokenMinistro = login(ministro.getEmail());
        String tokenLider = login(lider.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long musicaCultoId = adicionarERetornarId(tokenMinistro, cultoId, "Ousado Amor", null);

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/repertorio/" + musicaCultoId + "/revisao-lider")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RevisaoLiderRequest(
                                br.com.maranatamusic.domain.enums.RevisaoLider.APROVADA, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisaoLider").value("APROVADA"))
                .andExpect(jsonPath("$.observacaoLiderPrivada").doesNotExist());
    }

    // ---- R9: COM_OBSERVACAO sem texto → 400 ----

    @Test
    void revisar_comObservacaoSemTexto_deveRetornar400() throws Exception {
        Usuario ministro = criarUsuario("Ministro R9", "ministro-r9@maranata.com", Papel.MUSICO);
        Usuario lider = criarUsuario("Lider R9", "lider-r9@maranata.com", Papel.LIDER);
        String tokenMinistro = login(ministro.getEmail());
        String tokenLider = login(lider.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long musicaCultoId = adicionarERetornarId(tokenMinistro, cultoId, "Ousado Amor", null);

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/repertorio/" + musicaCultoId + "/revisao-lider")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RevisaoLiderRequest(
                                br.com.maranatamusic.domain.enums.RevisaoLider.COM_OBSERVACAO, null))))
                .andExpect(status().isBadRequest());
    }

    // ---- R10: revisar como não-líder → 403 ----

    @Test
    void revisar_comoMinistroNaoLider_deveRetornar403() throws Exception {
        Usuario ministro = criarUsuario("Ministro R10", "ministro-r10@maranata.com", Papel.MUSICO);
        String tokenMinistro = login(ministro.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long musicaCultoId = adicionarERetornarId(tokenMinistro, cultoId, "Ousado Amor", null);

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/repertorio/" + musicaCultoId + "/revisao-lider")
                        .header("Authorization", "Bearer " + tokenMinistro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RevisaoLiderRequest(
                                br.com.maranatamusic.domain.enums.RevisaoLider.APROVADA, null))))
                .andExpect(status().isForbidden());
    }

    // ---- R11: título já existente reaproveita música central, com override de link ----

    @Test
    void adicionar_tituloJaExistente_deveReaproveitarMusicaComOverrideDeLink() throws Exception {
        Usuario ministro = criarUsuario("Ministro R11", "ministro-r11@maranata.com", Papel.MUSICO);
        String token = login(ministro.getEmail());
        Long cultoAId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long cultoBId = criarCulto(LocalDateTime.of(2026, 8, 9, 19, 0), ministro.getId());

        adicionarERetornarId(token, cultoAId, "Grande é o Senhor", "https://youtube.com/watch?v=original");
        long totalMusicasAntes = musicaRepository.count();

        mockMvc.perform(post("/api/cultos/" + cultoBId + "/repertorio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarMusicaCultoRequest(
                                "grande é o senhor", "https://youtube.com/watch?v=ao-vivo", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.linkVideo").value("https://youtube.com/watch?v=ao-vivo"));

        // Não deve ter criado uma segunda Musica — reaproveitou a existente com override pontual.
        org.assertj.core.api.Assertions.assertThat(musicaRepository.count()).isEqualTo(totalMusicasAntes);
    }

    // ---- R12: PATCH atualizar link/ordem como ministro ----

    @Test
    void atualizar_comoMinistro_deveAtualizarLinkEOrdem() throws Exception {
        Usuario ministro = criarUsuario("Ministro R12", "ministro-r12@maranata.com", Papel.MUSICO);
        String token = login(ministro.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long musicaCultoId = adicionarERetornarId(token, cultoId, "Ousado Amor", "https://youtube.com/watch?v=x");

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/repertorio/" + musicaCultoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AtualizarMusicaCultoRequest("https://youtube.com/watch?v=novo", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkVideo").value("https://youtube.com/watch?v=novo"))
                .andExpect(jsonPath("$.ordem").value(5));
    }

    // ---- R13: DELETE como ministro ----

    @Test
    void remover_comoMinistro_deveRetornar204() throws Exception {
        Usuario ministro = criarUsuario("Ministro R13", "ministro-r13@maranata.com", Papel.MUSICO);
        String token = login(ministro.getEmail());
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long musicaCultoId = adicionarERetornarId(token, cultoId, "Ousado Amor", null);

        mockMvc.perform(delete("/api/cultos/" + cultoId + "/repertorio/" + musicaCultoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ---- R14: cultoId errado no path → 404 ----

    @Test
    void atualizar_cultoIdErradoNoPath_deveRetornar404() throws Exception {
        Usuario ministro = criarUsuario("Ministro R14", "ministro-r14@maranata.com", Papel.MUSICO);
        String token = login(ministro.getEmail());
        Long cultoRealId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), ministro.getId());
        Long outroCultoId = criarCulto(LocalDateTime.of(2026, 8, 9, 19, 0), ministro.getId());
        Long musicaCultoId = adicionarERetornarId(token, cultoRealId, "Ousado Amor", null);

        mockMvc.perform(patch("/api/cultos/" + outroCultoId + "/repertorio/" + musicaCultoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AtualizarMusicaCultoRequest(null, 2))))
                .andExpect(status().isNotFound());
    }

    // ---- helpers ----

    private Usuario criarUsuario(String nome, String email, Papel papel) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        usuario.setAtivo(true);
        usuario.getPapeis().add(papel);
        return usuarioRepository.save(usuario);
    }

    private Long criarCulto(LocalDateTime dataHora, Long ministroId) {
        Culto culto = new Culto();
        culto.setDataHora(dataHora);
        culto.setTipo(CultoTipo.DOMINGO_NOITE);
        culto.setRepertorioTrancado(false);
        if (ministroId != null) {
            culto.setMinistro(usuarioRepository.getReferenceById(ministroId));
        }
        return cultoRepository.save(culto).getId();
    }

    private Long adicionarERetornarId(String token, Long cultoId, String titulo, String linkVideo) throws Exception {
        String resposta = mockMvc.perform(post("/api/cultos/" + cultoId + "/repertorio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarMusicaCultoRequest(titulo, linkVideo, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
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
