package br.com.maranatamusic.presentation.culto;

import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.MusicoInstrumentoId;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import br.com.maranatamusic.domain.enums.CultoTipo;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.auth.RegistroRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                        .content(toJson(new EscalarMusicoRequest(usuarioId, instrumentoId))))
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
                        .content(toJson(new EscalarMusicoRequest(usuarioId, instrumentoId))))
                .andExpect(status().isForbidden());
    }

    // ---- cenário 12 ----

    @Test
    void escalarMusico_semMusicoInstrumentoCadastrado_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider12@maranata.com");
        Long cultoId = criarCulto(token, LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long instrumentoId = criarInstrumento("Violão M12", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Doze", "musico12@maranata.com");

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId, instrumentoId))))
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

        escalar(token, cultoId, usuario1Id, instrumentoId);

        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuario2Id, instrumentoId))))
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
                        .content(toJson(new EscalarMusicoRequest(usuarioId, instrumentoId))))
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
        Long instrumentoBId = criarInstrumento("Teclado M15B", CategoriaInstrumento.TECLA);
        Long usuarioId = criarMusicoAtivo("Musico Quinze", "musico15@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoAId);
        criarMusicoInstrumento(usuarioId, instrumentoBId);

        escalar(token, cultoAId, usuarioId, instrumentoAId);

        mockMvc.perform(post("/api/cultos/" + cultoBId + "/escalas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EscalarMusicoRequest(usuarioId, instrumentoBId))))
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

        escalar(token, cultoId, usuarioId, instrumentoId);

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

        escalar(token, cultoId, usuario1Id, instrumentoAId);
        escalar(token, cultoId, usuario2Id, instrumentoBId);

        mockMvc.perform(get("/api/cultos?mes=2026-08")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalEscalados").value(2));
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
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Instrumento instrumento = instrumentoRepository.getReferenceById(instrumentoId);
        MusicoInstrumento musicoInstrumento = new MusicoInstrumento();
        musicoInstrumento.setId(new MusicoInstrumentoId(usuarioId, instrumentoId));
        musicoInstrumento.setUsuario(usuario);
        musicoInstrumento.setInstrumento(instrumento);
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

    private void escalar(String tokenLider, Long cultoId, Long usuarioId, Long instrumentoId) throws Exception {
        mockMvc.perform(post("/api/cultos/" + cultoId + "/escalas")
                .header("Authorization", "Bearer " + tokenLider)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new EscalarMusicoRequest(usuarioId, instrumentoId))));
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
