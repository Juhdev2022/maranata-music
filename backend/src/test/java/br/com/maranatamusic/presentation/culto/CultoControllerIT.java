package br.com.maranatamusic.presentation.culto;

import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.MusicoInstrumentoId;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import br.com.maranatamusic.domain.enums.CultoTipo;
import br.com.maranatamusic.domain.enums.EscalaStatus;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicaCultoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.auth.RegistroRequest;
import br.com.maranatamusic.presentation.culto.dto.AtualizarObservacoesRequest;
import br.com.maranatamusic.presentation.culto.dto.CriarCultoRequest;
import br.com.maranatamusic.presentation.culto.dto.EscalarMusicoRequest;
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

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CultoControllerIT {

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
    void criarCulto_comoLider_deveRetornar201() throws Exception {
        String token = obterTokenLider("lider1@maranata.com");

        mockMvc.perform(post("/api/cultos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarCultoRequest(
                                LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.repertorioTrancado").value(false));
    }

    // ---- cenário 2 ----

    @Test
    void criarCulto_comoMusico_deveRetornar403() throws Exception {
        String token = obterTokenMusico("musico2@maranata.com");

        mockMvc.perform(post("/api/cultos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarCultoRequest(
                                LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE, null, null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    // ---- cenário 3 ----

    @Test
    void criarCulto_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(post("/api/cultos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarCultoRequest(
                                LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE, null, null))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    // ---- cenário 4 ----

    @Test
    void criarCulto_dataHoraNoPassado_deveRetornar400ComCampoDataHora() throws Exception {
        String token = obterTokenLider("lider4@maranata.com");

        mockMvc.perform(post("/api/cultos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarCultoRequest(
                                LocalDateTime.of(2020, 1, 1, 19, 0), CultoTipo.DOMINGO_NOITE, null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.dataHora").exists());
    }

    // ---- cenário 5 ----

    @Test
    void criarCulto_ministroInexistente_deveRetornar404() throws Exception {
        String token = obterTokenLider("lider5@maranata.com");

        mockMvc.perform(post("/api/cultos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarCultoRequest(
                                LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE, 9999L, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado"));
    }

    // ---- cenário 6 ----

    @Test
    void listarCultos_doMes_deveRetornarListaComCultoCriado() throws Exception {
        String token = obterTokenLider("lider6@maranata.com");
        criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);

        mockMvc.perform(get("/api/cultos?mes=2026-08")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].totalEscalados").value(0));
    }

    // ---- cenário 7 ----

    @Test
    void listarCultos_mesInvalido_deveRetornar400() throws Exception {
        String token = obterTokenMusico("musico7@maranata.com");

        mockMvc.perform(get("/api/cultos?mes=abc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.mes").exists());
    }

    // ---- cenário 8 ----

    @Test
    void detalharCulto_inexistente_deveRetornar404() throws Exception {
        String token = obterTokenMusico("musico8@maranata.com");

        mockMvc.perform(get("/api/cultos/9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Culto não encontrado"));
    }

    // ---- cenário 9 ----

    @Test
    void detalharCulto_existente_deveRetornar200ComEquipeVazia() throws Exception {
        String token = obterTokenLider("lider9@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);

        mockMvc.perform(get("/api/cultos/" + cultoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipe").isArray())
                .andExpect(jsonPath("$.equipe.length()").value(0));
    }

    // ---- cenário 10 ----

    @Test
    void escalarMusico_comoLider_dadosValidos_deveRetornar201() throws Exception {
        String token = obterTokenLider("lider10@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M10", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Dez", "musico10@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.id").value(usuarioId))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    // ---- cenário 11 ----

    @Test
    void escalarMusico_comoMusico_deveRetornar403() throws Exception {
        String tokenLider = obterTokenLider("lider11@maranata.com");
        Long cultoId = criarCulto(tokenLider, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M11", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Onze", "musico11-alvo@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);

        String tokenMusico = obterTokenMusico("musico11@maranata.com");

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + tokenMusico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId))))
                .andExpect(status().isForbidden());
    }

    // ---- cenário 12 ----

    @Test
    void escalarMusico_semMusicoInstrumentoCadastrado_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider12@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long usuarioId = criarMusicoAtivo("Musico Doze", "musico12@maranata.com");

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Este músico não toca este instrumento"));
    }

    // ---- cenário 13 ----

    @Test
    void escalarMusico_instrumentoJaEscaladoNoCulto_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider13@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M13", CategoriaInstrumento.CORDA);
        Long usuario1Id = criarMusicoAtivo("Musico Treze A", "musico13a@maranata.com");
        Long usuario2Id = criarMusicoAtivo("Musico Treze B", "musico13b@maranata.com");
        criarMusicoInstrumento(usuario1Id, instrumentoId);
        criarMusicoInstrumento(usuario2Id, instrumentoId);

        escalar(token, cultoId, usuario1Id);

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuario2Id))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Já há alguém escalado neste instrumento neste culto"));
    }

    // ---- cenário 14 ----

    @Test
    void escalarMusico_usuarioInativo_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider14@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M14", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusico("Musico Quatorze", "musico14@maranata.com", false);
        criarMusicoInstrumento(usuarioId, instrumentoId);

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Usuário inativo"));
    }

    // ---- cenário 15 ----

    @Test
    void escalarMusico_jaEscaladoEmOutroCultoNoMesmoHorario_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider15@maranata.com");
        Long cultoAId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long cultoBId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 20, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoAId = criarInstrumento("Violão M15A", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Quinze", "musico15@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoAId);

        escalar(token, cultoAId, usuarioId);

        mockMvc.perform(post("/api/cultos/" + cultoBId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Músico já escalado em outro culto próximo a este horário"));
    }

    // ---- cenário 16 ----

    @Test
    void detalharCulto_depoisDeEscalar_deveRetornarEquipeComEscala() throws Exception {
        String token = obterTokenLider("lider16@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Bateria M16", CategoriaInstrumento.PERCUSSAO);
        Long usuarioId = criarMusicoAtivo("Musico Dezesseis", "musico16@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);

        escalar(token, cultoId, usuarioId);

        mockMvc.perform(get("/api/cultos/" + cultoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipe.length()").value(1))
                .andExpect(jsonPath("$.equipe[0].usuario.id").value(usuarioId))
                .andExpect(jsonPath("$.equipe[0].instrumento.id").value(instrumentoId));
    }

    // ---- cenário 17 ----

    @Test
    void listarCultos_doMes_deveRefletirTotalEscaladosReal() throws Exception {
        String token = obterTokenLider("lider17@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoAId = criarInstrumento("Violão M17", CategoriaInstrumento.CORDA);
        Long instrumentoBId = criarInstrumento("Teclado M17", CategoriaInstrumento.TECLA);
        Long usuario1Id = criarMusicoAtivo("Musico Dezessete A", "musico17a@maranata.com");
        Long usuario2Id = criarMusicoAtivo("Musico Dezessete B", "musico17b@maranata.com");
        criarMusicoInstrumento(usuario1Id, instrumentoAId);
        criarMusicoInstrumento(usuario2Id, instrumentoBId);

        escalar(token, cultoId, usuario1Id);
        escalar(token, cultoId, usuario2Id);

        mockMvc.perform(get("/api/cultos?mes=2026-08")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalEscalados").value(2));
    }

    // ---- cenário 18 ----

    @Test
    void criarCulto_comNovoTipoTerca_deveRetornar201() throws Exception {
        String token = obterTokenLider("lider18@maranata.com");

        mockMvc.perform(post("/api/cultos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarCultoRequest(
                                LocalDateTime.of(2026, 8, 4, 19, 0), CultoTipo.TERCA, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
    }

    // ---- cenário 19 (O1) ----

    @Test
    void atualizarObservacoes_comoLider_deveRetornar200ComTextoAtualizado() throws Exception {
        String token = obterTokenLider("lider19@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/observacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AtualizarObservacoesRequest("Levar cabo extra"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observacoes").value("Levar cabo extra"));
    }

    // ---- cenário 20 (O2) ----

    @Test
    void atualizarObservacoes_comNull_deveLimparCampo() throws Exception {
        String token = obterTokenLider("lider20@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        mockMvc.perform(patch("/api/cultos/" + cultoId + "/observacoes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new AtualizarObservacoesRequest("Texto inicial"))));

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/observacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AtualizarObservacoesRequest(null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observacoes").value(nullValue()));
    }

    // ---- cenário 21 (O3) ----

    @Test
    void atualizarObservacoes_comoMusico_deveRetornar403() throws Exception {
        String tokenLider = obterTokenLider("lider21-setup@maranata.com");
        Long cultoId = criarCulto(tokenLider, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        String tokenMusico = obterTokenMusico("musico21@maranata.com");

        mockMvc.perform(patch("/api/cultos/" + cultoId + "/observacoes")
                        .header("Authorization", "Bearer " + tokenMusico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AtualizarObservacoesRequest("Tentativa"))))
                .andExpect(status().isForbidden());
    }

    // ---- cenário 22 (O4) ----

    @Test
    void atualizarObservacoes_cultoInexistente_deveRetornar404() throws Exception {
        String token = obterTokenLider("lider22@maranata.com");

        mockMvc.perform(patch("/api/cultos/99999/observacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new AtualizarObservacoesRequest("Texto"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Culto não encontrado"));
    }

    // ---- cenário 23 (R1) ----

    @Test
    void removerEscala_pendente_comoLider_deveRetornar204() throws Exception {
        String token = obterTokenLider("lider23@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M23", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Vinte e Tres", "musico23@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);
        Long escalaId = escalarERetornarId(token, cultoId, usuarioId);

        mockMvc.perform(delete("/api/cultos/" + cultoId + "/escalas/" + escalaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ---- cenário 24 (R2) ----

    @Test
    void removerEscala_confirmada_comoLider_deveRetornar204() throws Exception {
        String token = obterTokenLider("lider24@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M24", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Vinte e Quatro", "musico24@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);
        Long escalaId = escalarERetornarId(token, cultoId, usuarioId);
        definirStatusEscala(escalaId, EscalaStatus.CONFIRMADA);

        mockMvc.perform(delete("/api/cultos/" + cultoId + "/escalas/" + escalaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ---- cenário 25 (R3) ----

    @Test
    void removerEscala_comoMusico_deveRetornar403() throws Exception {
        String tokenLider = obterTokenLider("lider25@maranata.com");
        Long cultoId = criarCulto(tokenLider, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M25", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Vinte e Cinco", "musico25@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);
        Long escalaId = escalarERetornarId(tokenLider, cultoId, usuarioId);
        String tokenMusico = obterTokenMusico("musico25-caller@maranata.com");

        mockMvc.perform(delete("/api/cultos/" + cultoId + "/escalas/" + escalaId)
                        .header("Authorization", "Bearer " + tokenMusico))
                .andExpect(status().isForbidden());
    }

    // ---- cenário 26 (R4) ----

    @Test
    void removerEscala_recusada_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider26@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M26", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Vinte e Seis", "musico26@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);
        Long escalaId = escalarERetornarId(token, cultoId, usuarioId);
        definirStatusEscala(escalaId, EscalaStatus.RECUSADA);

        mockMvc.perform(delete("/api/cultos/" + cultoId + "/escalas/" + escalaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    // ---- cenário 27 (R5) ----

    @Test
    void removerEscala_cultoIdErradoNoPath_deveRetornar404() throws Exception {
        String token = obterTokenLider("lider27@maranata.com");
        Long cultoRealId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long outroCultoId = criarCulto(token, LocalDateTime.of(2026, 8, 3, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M27", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Vinte e Sete", "musico27@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);
        Long escalaId = escalarERetornarId(token, cultoRealId, usuarioId);

        mockMvc.perform(delete("/api/cultos/" + outroCultoId + "/escalas/" + escalaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ---- cenário 28 (R6) ----

    @Test
    void removerEscala_duasVezes_segundaDeveRetornar404() throws Exception {
        String token = obterTokenLider("lider28@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M28", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Vinte e Oito", "musico28@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);
        Long escalaId = escalarERetornarId(token, cultoId, usuarioId);

        mockMvc.perform(delete("/api/cultos/" + cultoId + "/escalas/" + escalaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/cultos/" + cultoId + "/escalas/" + escalaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ---- cenário 29 (R7) ----

    @Test
    void removerEscala_depoisPermiteEscalarOutroMusicoNoMesmoInstrumento_deveRetornar201() throws Exception {
        String token = obterTokenLider("lider29@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M29", CategoriaInstrumento.CORDA);
        Long usuario1Id = criarMusicoAtivo("Musico Vinte e Nove A", "musico29a@maranata.com");
        Long usuario2Id = criarMusicoAtivo("Musico Vinte e Nove B", "musico29b@maranata.com");
        criarMusicoInstrumento(usuario1Id, instrumentoId);
        criarMusicoInstrumento(usuario2Id, instrumentoId);
        Long escalaId = escalarERetornarId(token, cultoId, usuario1Id);

        mockMvc.perform(delete("/api/cultos/" + cultoId + "/escalas/" + escalaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuario2Id))))
                .andExpect(status().isCreated());
    }

    // ---- cenário 30 (V1+V2) ----

    @Test
    void escalarMusico_doisVocaisFemininosNoMesmoCulto_ambosDevemRetornar201() throws Exception {
        String token = obterTokenLider("lider30@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Vocal Feminino M30", CategoriaInstrumento.VOCAL);
        Long usuario1Id = criarMusicoAtivo("Musico Trinta A", "musico30a@maranata.com");
        Long usuario2Id = criarMusicoAtivo("Musico Trinta B", "musico30b@maranata.com");
        criarMusicoInstrumento(usuario1Id, instrumentoId);
        criarMusicoInstrumento(usuario2Id, instrumentoId);

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuario1Id))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuario2Id))))
                .andExpect(status().isCreated());
    }

    // ---- cenário 31 (E1) ----

    @Test
    void escalarMusico_comPrincipalDefinido_deveResolverInstrumentoPrincipal() throws Exception {
        String token = obterTokenLider("lider31@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoSecundarioId = criarInstrumento("Teclado M31", CategoriaInstrumento.TECLA);
        Long instrumentoPrincipalId = criarInstrumento("Violão M31", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Trinta e Um", "musico31@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoSecundarioId, false);
        criarMusicoInstrumento(usuarioId, instrumentoPrincipalId, true);

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.instrumento.id").value(instrumentoPrincipalId));
    }

    // ---- cenário 32 (E3) ----

    @Test
    void escalarMusico_comDoisVinculosSemPrincipal_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider32@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoAId = criarInstrumento("Teclado M32", CategoriaInstrumento.TECLA);
        Long instrumentoBId = criarInstrumento("Violão M32", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Trinta e Dois", "musico32@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoAId);
        criarMusicoInstrumento(usuarioId, instrumentoBId);

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Músico tem mais de um instrumento — defina um principal no cadastro"));
    }

    // ---- helpers ----

    private String obterTokenLider(String email) throws Exception {
        return criarUsuarioComPapelELogar("Líder Teste", email, Papel.LIDER, true);
    }

    private String obterTokenMinistro(String email) throws Exception {
        return criarUsuarioComPapelELogar("Ministro Teste", email, Papel.MINISTRO, true);
    }

    private String obterTokenMusico(String email) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RegistroRequest("Músico Teste", email, SENHA_PADRAO, null))));
        return login(email);
    }

    private String criarUsuarioComPapelELogar(String nome, String email, Papel papel, boolean ativo) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        usuario.setAtivo(ativo);
        usuario.getPapeis().add(papel);
        usuarioRepository.save(usuario);
        return login(email);
    }

    private Long criarMusicoAtivo(String nome, String email) {
        return criarMusico(nome, email, true);
    }

    private Long criarMusico(String nome, String email, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        usuario.setAtivo(ativo);
        usuario.getPapeis().add(Papel.MUSICO);
        return usuarioRepository.save(usuario).getId();
    }

    private Long criarInstrumento(String nome, CategoriaInstrumento categoria) {
        Instrumento instrumento = new Instrumento();
        instrumento.setNome(nome);
        instrumento.setCategoria(categoria);
        return instrumentoRepository.save(instrumento).getId();
    }

    private void criarMusicoInstrumento(Long usuarioId, Long instrumentoId) {
        criarMusicoInstrumento(usuarioId, instrumentoId, false);
    }

    private void criarMusicoInstrumento(Long usuarioId, Long instrumentoId, boolean principal) {
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Instrumento instrumento = instrumentoRepository.getReferenceById(instrumentoId);
        MusicoInstrumento musicoInstrumento = new MusicoInstrumento();
        musicoInstrumento.setId(new MusicoInstrumentoId(usuarioId, instrumentoId));
        musicoInstrumento.setUsuario(usuario);
        musicoInstrumento.setInstrumento(instrumento);
        musicoInstrumento.setPrincipal(principal);
        musicoInstrumentoRepository.save(musicoInstrumento);
    }

    private Long criarCulto(String tokenLider, LocalDateTime dataHora, CultoTipo tipo) throws Exception {
        String resposta = mockMvc.perform(post("/api/cultos")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarCultoRequest(dataHora, tipo, null, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }

    private void escalar(String tokenLider, Long cultoId, Long usuarioId) throws Exception {
        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                .header("Authorization", "Bearer " + tokenLider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new EscalarMusicoRequest(usuarioId))));
    }

    private Long escalarERetornarId(String tokenLider, Long cultoId, Long usuarioId) throws Exception {
        String resposta = mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + tokenLider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }

    private void definirStatusEscala(Long escalaId, EscalaStatus status) {
        Escala escala = escalaRepository.findById(escalaId).orElseThrow();
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
