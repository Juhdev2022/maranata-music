package br.com.maranatamusic.presentation.musico;

import br.com.maranatamusic.domain.Culto;
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
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.auth.RegistroRequest;
import br.com.maranatamusic.presentation.musico.dto.VincularMusicoInstrumentoRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MusicoInstrumentoControllerIT {

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
    void vincular_comoLider_deveRetornar201() throws Exception {
        String token = obterTokenLider("lider1@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Um", "musico1@maranata.com");
        Long instrumentoId = criarInstrumento("Violão M1", CategoriaInstrumento.CORDA);

        mockMvc.perform(post("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VincularMusicoInstrumentoRequest(false))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId))
                .andExpect(jsonPath("$.instrumentoId").value(instrumentoId))
                .andExpect(jsonPath("$.principal").value(false));
    }

    // ---- cenário 2 ----

    @Test
    void vincular_comoMusico_deveRetornar403() throws Exception {
        String token = obterTokenMusico("musico2@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Alvo Dois", "alvo2@maranata.com");
        Long instrumentoId = criarInstrumento("Violão M2", CategoriaInstrumento.CORDA);

        mockMvc.perform(post("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VincularMusicoInstrumentoRequest(false))))
                .andExpect(status().isForbidden());
    }

    // ---- cenário 3 ----

    @Test
    void vincular_semToken_deveRetornar401() throws Exception {
        Long usuarioId = criarMusicoAtivo("Musico Tres", "musico3@maranata.com");
        Long instrumentoId = criarInstrumento("Violão M3", CategoriaInstrumento.CORDA);

        mockMvc.perform(post("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VincularMusicoInstrumentoRequest(false))))
                .andExpect(status().isUnauthorized());
    }

    // ---- cenário 4 ----

    @Test
    void vincular_principalTrue_devePromoverEDespromoverOAntigo() throws Exception {
        String token = obterTokenLider("lider4@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Quatro", "musico4@maranata.com");
        Long instrumentoAId = criarInstrumento("Violão M4", CategoriaInstrumento.CORDA);
        Long instrumentoBId = criarInstrumento("Guitarra M4", CategoriaInstrumento.CORDA);
        criarMusicoInstrumento(usuarioId, instrumentoAId, true);

        mockMvc.perform(post("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoBId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VincularMusicoInstrumentoRequest(true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.principal").value(true));

        MusicoInstrumento antigo = musicoInstrumentoRepository
                .findById(new MusicoInstrumentoId(usuarioId, instrumentoAId)).orElseThrow();
        assertThat(antigo.isPrincipal()).isFalse();
    }

    // ---- cenário 5a ----

    @Test
    void vincular_jaExistenteComMesmoPrincipal_deveRetornar200SemAlteracao() throws Exception {
        String token = obterTokenLider("lider5a@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Cinco A", "musico5a@maranata.com");
        Long instrumentoId = criarInstrumento("Violão M5A", CategoriaInstrumento.CORDA);
        criarMusicoInstrumento(usuarioId, instrumentoId, false);

        mockMvc.perform(post("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VincularMusicoInstrumentoRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(false));
    }

    // ---- cenário 5b ----

    @Test
    void vincular_jaExistenteComPrincipalDiferente_deveRetornar200ComAlteracaoAplicada() throws Exception {
        String token = obterTokenLider("lider5b@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Cinco B", "musico5b@maranata.com");
        Long instrumentoAId = criarInstrumento("Violão M5B", CategoriaInstrumento.CORDA);
        Long instrumentoBId = criarInstrumento("Guitarra M5B", CategoriaInstrumento.CORDA);
        criarMusicoInstrumento(usuarioId, instrumentoAId, true);
        criarMusicoInstrumento(usuarioId, instrumentoBId, false);

        mockMvc.perform(post("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoBId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VincularMusicoInstrumentoRequest(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal").value(true));

        MusicoInstrumento antigo = musicoInstrumentoRepository
                .findById(new MusicoInstrumentoId(usuarioId, instrumentoAId)).orElseThrow();
        assertThat(antigo.isPrincipal()).isFalse();
    }

    // ---- cenário 6 ----

    @Test
    void vincular_usuarioInexistente_deveRetornar404() throws Exception {
        String token = obterTokenLider("lider6@maranata.com");
        Long instrumentoId = criarInstrumento("Violão M6", CategoriaInstrumento.CORDA);

        mockMvc.perform(post("/api/musicos/9999/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VincularMusicoInstrumentoRequest(false))))
                .andExpect(status().isNotFound());
    }

    // ---- cenário 7 ----

    @Test
    void vincular_instrumentoInexistente_deveRetornar404() throws Exception {
        String token = obterTokenLider("lider7@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Sete", "musico7@maranata.com");

        mockMvc.perform(post("/api/musicos/" + usuarioId + "/instrumentos/9999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new VincularMusicoInstrumentoRequest(false))))
                .andExpect(status().isNotFound());
    }

    // ---- cenário 8 ----

    @Test
    void desvincular_comoLider_deveRetornar204() throws Exception {
        String token = obterTokenLider("lider8@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Oito", "musico8@maranata.com");
        Long instrumentoId = criarInstrumento("Violão M8", CategoriaInstrumento.CORDA);
        criarMusicoInstrumento(usuarioId, instrumentoId, false);

        mockMvc.perform(delete("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ---- cenário 9 ----

    @Test
    void desvincular_comEscalaFuturaConfirmada_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider9@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Nove", "musico9@maranata.com");
        Long instrumentoId = criarInstrumento("Violão M9", CategoriaInstrumento.CORDA);
        criarMusicoInstrumento(usuarioId, instrumentoId, false);
        Long cultoId = criarCulto(LocalDateTime.now().plusDays(5), CultoTipo.DOMINGO_NOITE);
        criarEscala(cultoId, usuarioId, instrumentoId, EscalaStatus.CONFIRMADA);

        mockMvc.perform(delete("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    // ---- cenário 10 ----

    @Test
    void desvincular_vinculoInexistente_deveRetornar404() throws Exception {
        String token = obterTokenLider("lider10@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Dez", "musico10@maranata.com");
        Long instrumentoId = criarInstrumento("Violão M10", CategoriaInstrumento.CORDA);

        mockMvc.perform(delete("/api/musicos/" + usuarioId + "/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ---- cenário 11 ----

    @Test
    void listarInstrumentosDoUsuario_deveRetornarOrdenadoPorPrincipalENome() throws Exception {
        String token = obterTokenMusico("musico11@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Onze", "alvo11@maranata.com");
        Long instrumentoZId = criarInstrumento("Zabumba M11", CategoriaInstrumento.PERCUSSAO);
        Long instrumentoAId = criarInstrumento("Acordeon M11", CategoriaInstrumento.SOPRO);
        criarMusicoInstrumento(usuarioId, instrumentoZId, false);
        criarMusicoInstrumento(usuarioId, instrumentoAId, true);

        mockMvc.perform(get("/api/musicos/" + usuarioId + "/instrumentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].instrumentoId").value(instrumentoAId))
                .andExpect(jsonPath("$[0].principal").value(true))
                .andExpect(jsonPath("$[1].instrumentoId").value(instrumentoZId))
                .andExpect(jsonPath("$[1].principal").value(false));
    }

    // ---- cenário 12 ----

    @Test
    void listarInstrumentosDoUsuario_semVinculos_deveRetornarListaVazia() throws Exception {
        String token = obterTokenMusico("musico12@maranata.com");
        Long usuarioId = criarMusicoAtivo("Musico Doze", "alvo12@maranata.com");

        mockMvc.perform(get("/api/musicos/" + usuarioId + "/instrumentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---- cenário 13 ----

    @Test
    void listarInstrumentosDoUsuario_usuarioInexistente_deveRetornar404() throws Exception {
        String token = obterTokenMusico("musico13@maranata.com");

        mockMvc.perform(get("/api/musicos/9999/instrumentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ---- helpers ----

    private String obterTokenLider(String email) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Líder Teste");
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        usuario.setAtivo(true);
        usuario.getPapeis().add(Papel.LIDER);
        usuarioRepository.save(usuario);
        return login(email);
    }

    private String obterTokenMusico(String email) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RegistroRequest("Músico Teste", email, SENHA_PADRAO, null))));
        return login(email);
    }

    private Long criarMusicoAtivo(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_PADRAO));
        usuario.setAtivo(true);
        usuario.getPapeis().add(Papel.MUSICO);
        return usuarioRepository.save(usuario).getId();
    }

    private Long criarInstrumento(String nome, CategoriaInstrumento categoria) {
        Instrumento instrumento = new Instrumento();
        instrumento.setNome(nome);
        instrumento.setCategoria(categoria);
        return instrumentoRepository.save(instrumento).getId();
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
