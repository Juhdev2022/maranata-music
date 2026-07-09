package br.com.maranatamusic.presentation.substituicao;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.MusicoInstrumentoId;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import br.com.maranatamusic.domain.enums.CultoTipo;
import br.com.maranatamusic.domain.enums.EscalaStatus;
import br.com.maranatamusic.domain.enums.MotivoSubstituicao;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.SolicitacaoSubstituicaoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.substituicao.dto.AprovarSubstituicaoRequest;
import br.com.maranatamusic.presentation.substituicao.dto.SolicitarSubstituicaoRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubstituicaoControllerIT {

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
    private SolicitacaoSubstituicaoRepository solicitacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBase() {
        solicitacaoRepository.deleteAll();
        escalaRepository.deleteAll();
        cultoRepository.deleteAll();
        musicoInstrumentoRepository.deleteAll();
        usuarioRepository.deleteAll();
        instrumentoRepository.deleteAll();
    }

    // ---- S1 ----

    @Test
    void solicitar_comoDono_deveRetornar201() throws Exception {
        Usuario musico = criarUsuario("Musico S1", "musico1-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S1", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.VIAGEM, "Vou viajar", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.escalaId").value(escalaId));
    }

    // ---- S2 ----

    @Test
    void solicitar_comoOutroMusico_deveRetornar403() throws Exception {
        Usuario dono = criarUsuario("Musico Dono S2", "musico2dono-sub@maranata.com", Papel.MUSICO);
        Usuario outro = criarUsuario("Musico Outro S2", "musico2outro-sub@maranata.com", Papel.MUSICO);
        String tokenOutro = login(outro.getEmail());
        Long instrumentoId = criarInstrumento("Violão S2", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, dono.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + tokenOutro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(escalaId, MotivoSubstituicao.VIAGEM, null, null))))
                .andExpect(status().isForbidden());
    }

    // ---- S3 ----

    @Test
    void solicitar_escalaRecusada_deveRetornar409() throws Exception {
        Usuario musico = criarUsuario("Musico S3", "musico3-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S3", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.RECUSADA);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(escalaId, MotivoSubstituicao.VIAGEM, null, null))))
                .andExpect(status().isConflict());
    }

    // ---- S4 ----

    @Test
    void solicitar_duplicadaMesmaEscala_deveRetornar409() throws Exception {
        Usuario musico = criarUsuario("Musico S4", "musico4-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S4", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        solicitar(token, escalaId, MotivoSubstituicao.VIAGEM, null, null);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(escalaId, MotivoSubstituicao.SAUDE, null, null))))
                .andExpect(status().isConflict());
    }

    // ---- S5 ----

    @Test
    void solicitar_substitutoSugeridoInelegivel_deveRetornar409() throws Exception {
        Usuario musico = criarUsuario("Musico S5", "musico5-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Usuario semVinculo = criarUsuario("Sem Vinculo S5", "semvinculo5-sub@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão S5", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.VIAGEM, null, semVinculo.getId()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Substituto não toca o instrumento desta escala"));
    }

    // ---- S6 ----

    @Test
    void listarPendentes_comoLider_deveRetornar200() throws Exception {
        String tokenLider = obterTokenLider("lider6-sub@maranata.com");
        Usuario musico = criarUsuario("Musico S6", "musico6-sub@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S6", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        solicitar(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, null);

        mockMvc.perform(get("/api/substituicoes/pendentes")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ---- S7 ----

    @Test
    void listarPendentes_comoMusico_deveRetornar403() throws Exception {
        Usuario musico = criarUsuario("Musico S7", "musico7-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());

        mockMvc.perform(get("/api/substituicoes/pendentes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---- S8 ----

    @Test
    void aprovar_comSugerido_deveTrocarEscala() throws Exception {
        String tokenLider = obterTokenLider("lider8-sub@maranata.com");
        Usuario musico = criarUsuario("Musico S8", "musico8-sub@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Usuario substituto = criarUsuario("Substituto S8", "substituto8-sub@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão S8", CategoriaInstrumento.CORDA);
        criarMusicoInstrumento(substituto.getId(), instrumentoId);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, substituto.getId());

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/aprovar")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AprovarSubstituicaoRequest(null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.substitutoFinal.id").value(substituto.getId()));

        mockMvc.perform(get("/api/cultos/" + cultoId)
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipe.length()").value(2))
                .andExpect(jsonPath("$.equipe[0].status").value("SUBSTITUIDA"))
                .andExpect(jsonPath("$.equipe[1].status").value("PENDENTE"))
                .andExpect(jsonPath("$.equipe[1].usuario.id").value(substituto.getId()));
    }

    // ---- S9 ----

    @Test
    void aprovar_semSugeridoComSubstitutoFinalId_deveRetornar200() throws Exception {
        String tokenLider = obterTokenLider("lider9-sub@maranata.com");
        Usuario musico = criarUsuario("Musico S9", "musico9-sub@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Usuario substituto = criarUsuario("Substituto S9", "substituto9-sub@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão S9", CategoriaInstrumento.CORDA);
        criarMusicoInstrumento(substituto.getId(), instrumentoId);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, null);

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/aprovar")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AprovarSubstituicaoRequest(substituto.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));
    }

    // ---- S10 ----

    @Test
    void aprovar_semSugeridoESemSubstitutoFinalId_deveRetornar400() throws Exception {
        String tokenLider = obterTokenLider("lider10-sub@maranata.com");
        Usuario musico = criarUsuario("Musico S10", "musico10-sub@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S10", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, null);

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/aprovar")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AprovarSubstituicaoRequest(null))))
                .andExpect(status().isBadRequest());
    }

    // ---- S11 ----

    @Test
    void rejeitar_deveManterEscalaInalterada() throws Exception {
        String tokenLider = obterTokenLider("lider11-sub@maranata.com");
        Usuario musico = criarUsuario("Musico S11", "musico11-sub@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S11", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, null);

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"));

        mockMvc.perform(get("/api/cultos/" + cultoId)
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipe.length()").value(1))
                .andExpect(jsonPath("$.equipe[0].status").value("PENDENTE"));
    }

    // ---- S12 ----

    @Test
    void aprovar_solicitacaoJaResolvida_deveRetornar409() throws Exception {
        String tokenLider = obterTokenLider("lider12-sub@maranata.com");
        Usuario musico = criarUsuario("Musico S12", "musico12-sub@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S12", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, null);

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/aprovar")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AprovarSubstituicaoRequest(null))))
                .andExpect(status().isConflict());
    }

    // ---- S13 ----

    @Test
    void minhasEscalas_comSolicitacaoAberta_deveRetornarFlagTrue() throws Exception {
        Usuario musico = criarUsuario("Musico S13", "musico13-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S13", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        solicitar(token, escalaId, MotivoSubstituicao.VIAGEM, null, null);

        mockMvc.perform(get("/api/escalas/minhas?mes=2026-08")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].solicitacaoAberta").value(true));
    }

    // ---- helpers ----

    private String obterTokenLider(String email) throws Exception {
        Usuario lider = criarUsuario("Lider Teste", email, Papel.LIDER);
        return login(lider.getEmail());
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

    private void criarMusicoInstrumento(Long usuarioId, Long instrumentoId) {
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Instrumento instrumento = instrumentoRepository.getReferenceById(instrumentoId);
        MusicoInstrumento musicoInstrumento = new MusicoInstrumento();
        musicoInstrumento.setId(new MusicoInstrumentoId(usuarioId, instrumentoId));
        musicoInstrumento.setUsuario(usuario);
        musicoInstrumento.setInstrumento(instrumento);
        musicoInstrumentoRepository.save(musicoInstrumento);
    }

    private Long criarCulto(LocalDateTime dataHora, CultoTipo tipo) {
        Culto culto = new Culto();
        culto.setDataHora(dataHora);
        culto.setTipo(tipo);
        culto.setRepertorioTrancado(false);
        return cultoRepository.save(culto).getId();
    }

    private Long criarEscala(Long cultoId, Long usuarioId, Long instrumentoId, EscalaStatus status) {
        Escala escala = new Escala();
        escala.setCulto(cultoRepository.getReferenceById(cultoId));
        escala.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        escala.setInstrumento(instrumentoRepository.getReferenceById(instrumentoId));
        escala.setStatus(status);
        return escalaRepository.save(escala).getId();
    }

    private void solicitar(String token, Long escalaId, MotivoSubstituicao motivo, String observacao, Long substitutoSugeridoId)
            throws Exception {
        mockMvc.perform(post("/api/substituicoes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new SolicitarSubstituicaoRequest(escalaId, motivo, observacao, substitutoSugeridoId))));
    }

    private Long solicitarERetornarId(
            String token, Long escalaId, MotivoSubstituicao motivo, String observacao, Long substitutoSugeridoId) throws Exception {
        String resposta = mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(escalaId, motivo, observacao, substitutoSugeridoId))))
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
