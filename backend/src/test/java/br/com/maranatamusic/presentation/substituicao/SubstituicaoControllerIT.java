package br.com.maranatamusic.presentation.substituicao;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import br.com.maranatamusic.domain.enums.CultoTipo;
import br.com.maranatamusic.domain.enums.EscalaStatus;
import br.com.maranatamusic.domain.enums.MotivoSubstituicao;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicaCultoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.SolicitacaoSubstituicaoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.substituicao.dto.RejeitarSubstituicaoRequest;
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

/**
 * Fase 1.1 Pack 4.1: substituto de livre escolha do músico (sem exigir vínculo
 * musico_instrumento, sem checar conflito de horário do candidato). Líder aprova a
 * indicação ou rejeita e escolhe outra pessoa — nos dois casos a escala é trocada.
 */
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
    private MusicaCultoRepository musicaCultoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBase() {
        solicitacaoRepository.deleteAll();
        escalaRepository.deleteAll();
        musicaCultoRepository.deleteAll();
        cultoRepository.deleteAll();
        musicoInstrumentoRepository.deleteAll();
        usuarioRepository.deleteAll();
        instrumentoRepository.deleteAll();
    }

    // ---- cenário 1 ----

    @Test
    void solicitar_comoDono_deveRetornar201() throws Exception {
        Usuario musico = criarUsuario("Musico Um", "musico1-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido Um", "sugerido1-sub@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão S1", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.VIAGEM, "Vou viajar", sugerido.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.substitutoSugerido.id").value(sugerido.getId()));
    }

    // ---- cenário 2 ----

    @Test
    void solicitar_comoOutroMusico_deveRetornar403() throws Exception {
        Usuario dono = criarUsuario("Musico Dono S2", "musico2dono-sub@maranata.com", Papel.MUSICO);
        Usuario outro = criarUsuario("Musico Outro S2", "musico2outro-sub@maranata.com", Papel.MUSICO);
        Usuario sugerido = criarUsuario("Sugerido S2", "sugerido2-sub@maranata.com", Papel.MUSICO);
        String tokenOutro = login(outro.getEmail());
        Long instrumentoId = criarInstrumento("Violão S2", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, dono.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + tokenOutro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId()))))
                .andExpect(status().isForbidden());
    }

    // ---- cenário 3 ----

    @Test
    void solicitar_escalaRecusada_deveRetornar409() throws Exception {
        Usuario musico = criarUsuario("Musico S3", "musico3-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido S3", "sugerido3-sub@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão S3", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.RECUSADA);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId()))))
                .andExpect(status().isConflict());
    }

    // ---- cenário 4 ----

    @Test
    void solicitar_duplicadaMesmaEscala_deveRetornar409() throws Exception {
        Usuario musico = criarUsuario("Musico S4", "musico4-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido S4", "sugerido4-sub@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão S4", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        solicitar(token, escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId());

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.SAUDE, null, sugerido.getId()))))
                .andExpect(status().isConflict());
    }

    // ---- cenário 5 ----

    @Test
    void solicitar_semSubstitutoSugerido_deveRetornar400() throws Exception {
        Usuario musico = criarUsuario("Musico S5", "musico5-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão S5", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.VIAGEM, null, null))))
                .andExpect(status().isBadRequest());
    }

    // ---- T1: substituto sem vínculo no instrumento deve ser aceito ----

    @Test
    void solicitar_substitutoSemVinculoNoInstrumento_deveRetornar201() throws Exception {
        Usuario musico = criarUsuario("Musico T1", "musico-t1@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Usuario semVinculo = criarUsuario("Sem Vinculo T1", "semvinculo-t1@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão T1", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        // semVinculo não tem nenhum musico_instrumento cadastrado — antes bloqueava, agora deve passar.
        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.VIAGEM, null, semVinculo.getId()))))
                .andExpect(status().isCreated());
    }

    // ---- T2: substituto já escalado no mesmo culto (outro instrumento) deve ser aceito ----

    @Test
    void solicitar_substitutoJaEscaladoNoMesmoCultoOutroInstrumento_deveRetornar201() throws Exception {
        Usuario musico = criarUsuario("Musico T2", "musico-t2@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Usuario candidato = criarUsuario("Candidato T2", "candidato-t2@maranata.com", Papel.MUSICO);
        Long instrumentoOriginalId = criarInstrumento("Violão T2", CategoriaInstrumento.CORDA);
        Long instrumentoCandidatoId = criarInstrumento("Teclado T2", CategoriaInstrumento.TECLA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoOriginalId, EscalaStatus.PENDENTE);
        // candidato já está escalado no MESMO culto, em outro instrumento.
        criarEscala(cultoId, candidato.getId(), instrumentoCandidatoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/substituicoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new SolicitarSubstituicaoRequest(
                                escalaId, MotivoSubstituicao.VIAGEM, null, candidato.getId()))))
                .andExpect(status().isCreated());
    }

    // ---- T3: substitutos-elegiveis lista todos ativos exceto o solicitante ----

    @Test
    void listarSubstitutosElegiveis_deveListarTodosAtivosExcetoSolicitante() throws Exception {
        Usuario musico = criarUsuario("Musico T3", "musico-t3@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        criarUsuario("Outro Ativo T3", "outro-t3@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão T3", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(get("/api/substituicoes/substitutos-elegiveis?escalaId=" + escalaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Outro Ativo T3"));
    }

    // ---- cenário 6 ----

    @Test
    void listarPendentes_comoLider_deveRetornar200() throws Exception {
        String tokenLider = obterTokenLider("lider6-sub@maranata.com");
        Usuario musico = criarUsuario("Musico S6", "musico6-sub@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido S6", "sugerido6-sub@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão S6", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        solicitar(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId());

        mockMvc.perform(get("/api/substituicoes/pendentes")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ---- cenário 7 ----

    @Test
    void listarPendentes_comoMusico_deveRetornar403() throws Exception {
        Usuario musico = criarUsuario("Musico S7", "musico7-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());

        mockMvc.perform(get("/api/substituicoes/pendentes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ---- T4: aprovar usa o substituto sugerido ----

    @Test
    void aprovar_usaSugerido_deveTrocarEscala() throws Exception {
        String tokenLider = obterTokenLider("lider-t4@maranata.com");
        Usuario musico = criarUsuario("Musico T4", "musico-t4@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido T4", "sugerido-t4@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão T4", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId());

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/aprovar")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.substitutoFinal.id").value(sugerido.getId()));

        mockMvc.perform(get("/api/cultos/" + cultoId)
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipe.length()").value(2))
                .andExpect(jsonPath("$.equipe[0].status").value("SUBSTITUIDA"))
                .andExpect(jsonPath("$.equipe[1].status").value("PENDENTE"))
                .andExpect(jsonPath("$.equipe[1].usuario.id").value(sugerido.getId()));
    }

    // ---- cenário: aprovar solicitação já resolvida ----

    @Test
    void aprovar_solicitacaoJaResolvida_deveRetornar409() throws Exception {
        String tokenLider = obterTokenLider("lider-aprovresolv@maranata.com");
        Usuario musico = criarUsuario("Musico Resolv", "musico-aprovresolv@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido Resolv", "sugerido-aprovresolv@maranata.com", Papel.MUSICO);
        Usuario escolhaLider = criarUsuario("Escolha Lider Resolv", "escolha-aprovresolv@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão Resolv", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId());

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RejeitarSubstituicaoRequest(escolhaLider.getId()))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/aprovar")
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isConflict());
    }

    // ---- T5: rejeitar com substitutoFinalId diferente do sugerido troca com a escolha do líder ----

    @Test
    void rejeitar_comSubstitutoFinalDiferente_deveTrocarComEscolhaDoLider() throws Exception {
        String tokenLider = obterTokenLider("lider-t5@maranata.com");
        Usuario musico = criarUsuario("Musico T5", "musico-t5@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido T5", "sugerido-t5@maranata.com", Papel.MUSICO);
        Usuario escolhaLider = criarUsuario("Escolha Lider T5", "escolha-t5@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão T5", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId());

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RejeitarSubstituicaoRequest(escolhaLider.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.substitutoFinal.id").value(escolhaLider.getId()));

        mockMvc.perform(get("/api/cultos/" + cultoId)
                        .header("Authorization", "Bearer " + tokenLider))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipe.length()").value(2))
                .andExpect(jsonPath("$.equipe[0].status").value("SUBSTITUIDA"))
                .andExpect(jsonPath("$.equipe[1].status").value("PENDENTE"))
                .andExpect(jsonPath("$.equipe[1].usuario.id").value(escolhaLider.getId()));
    }

    // ---- T6: rejeitar sem body/substitutoFinalId retorna 400 ----

    @Test
    void rejeitar_semSubstitutoFinalId_deveRetornar400() throws Exception {
        String tokenLider = obterTokenLider("lider-t6@maranata.com");
        Usuario musico = criarUsuario("Musico T6", "musico-t6@maranata.com", Papel.MUSICO);
        String tokenMusico = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido T6", "sugerido-t6@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão T6", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        Long solicitacaoId = solicitarERetornarId(tokenMusico, escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId());

        mockMvc.perform(post("/api/substituicoes/" + solicitacaoId + "/rejeitar")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new RejeitarSubstituicaoRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Informe quem vai substituir"));
    }

    // ---- S13: minhasEscalas reflete solicitação aberta ----

    @Test
    void minhasEscalas_comSolicitacaoAberta_deveRetornarFlagTrue() throws Exception {
        Usuario musico = criarUsuario("Musico S13", "musico13-sub@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Usuario sugerido = criarUsuario("Sugerido S13", "sugerido13-sub@maranata.com", Papel.MUSICO);
        Long instrumentoId = criarInstrumento("Violão S13", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);
        solicitar(token, escalaId, MotivoSubstituicao.VIAGEM, null, sugerido.getId());

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
