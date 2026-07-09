package br.com.maranatamusic.presentation.instrumento;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Escala;
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
import br.com.maranatamusic.infrastructure.persistence.MusicaCultoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.auth.RegistroRequest;
import br.com.maranatamusic.presentation.instrumento.dto.CriarInstrumentoRequest;
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
class InstrumentoControllerIT {

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
    void criarInstrumento_comoLider_deveRetornar201() throws Exception {
        String token = obterTokenLider("lider1@maranata.com");

        mockMvc.perform(post("/api/instrumentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarInstrumentoRequest("Violão M1", CategoriaInstrumento.CORDA))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Violão M1"))
                .andExpect(jsonPath("$.categoria").value("CORDA"));
    }

    // ---- cenário 2 ----

    @Test
    void criarInstrumento_comoMusico_deveRetornar403() throws Exception {
        String token = obterTokenMusico("musico2@maranata.com");

        mockMvc.perform(post("/api/instrumentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarInstrumentoRequest("Violão M2", CategoriaInstrumento.CORDA))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    // ---- cenário 3 ----

    @Test
    void criarInstrumento_semToken_deveRetornar401() throws Exception {
        mockMvc.perform(post("/api/instrumentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarInstrumentoRequest("Violão M3", CategoriaInstrumento.CORDA))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    // ---- cenário 4 ----

    @Test
    void criarInstrumento_nomeDuplicadoCaseInsensitive_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider4@maranata.com");
        criarInstrumento("Violão M4", CategoriaInstrumento.CORDA);

        mockMvc.perform(post("/api/instrumentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarInstrumentoRequest("violão m4", CategoriaInstrumento.CORDA))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Instrumento já cadastrado: violão m4"));
    }

    // ---- cenário 4b ----

    @Test
    void criarInstrumento_comNomeAcentuado_deveRetornar201EPreservarAcentos() throws Exception {
        String token = obterTokenLider("lider4b@maranata.com");

        mockMvc.perform(post("/api/instrumentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarInstrumentoRequest("Cajón", CategoriaInstrumento.PERCUSSAO))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Cajón"));

        mockMvc.perform(post("/api/instrumentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarInstrumentoRequest("Percussão", CategoriaInstrumento.PERCUSSAO))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Percussão"));

        mockMvc.perform(post("/api/instrumentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarInstrumentoRequest("Órgão", CategoriaInstrumento.TECLA))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Órgão"));

        mockMvc.perform(get("/api/instrumentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Cajón')]").exists())
                .andExpect(jsonPath("$[?(@.nome == 'Percussão')]").exists())
                .andExpect(jsonPath("$[?(@.nome == 'Órgão')]").exists());
    }

    // ---- cenário 5 ----

    @Test
    void criarInstrumento_semNomeOuCategoria_deveRetornar400() throws Exception {
        String token = obterTokenLider("lider5@maranata.com");

        mockMvc.perform(post("/api/instrumentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new CriarInstrumentoRequest("", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.categoria").exists());
    }

    // ---- cenário 6 ----

    @Test
    void listarInstrumentos_comoQualquerAutenticado_deveRetornar200() throws Exception {
        String token = obterTokenMusico("musico6@maranata.com");
        criarInstrumento("Teclado M6", CategoriaInstrumento.TECLA);

        mockMvc.perform(get("/api/instrumentos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ---- cenário 7 ----

    @Test
    void deletarInstrumento_comoLider_instrumentoLivre_deveRetornar204() throws Exception {
        String token = obterTokenLider("lider7@maranata.com");
        Long instrumentoId = criarInstrumento("Cajón M7", CategoriaInstrumento.PERCUSSAO);

        mockMvc.perform(delete("/api/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ---- cenário 8 ----

    @Test
    void deletarInstrumento_comoMusico_deveRetornar403() throws Exception {
        String tokenLider = obterTokenLider("lider8-setup@maranata.com");
        Long instrumentoId = criarInstrumento("Sax M8", CategoriaInstrumento.SOPRO);
        String tokenMusico = obterTokenMusico("musico8@maranata.com");

        mockMvc.perform(delete("/api/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + tokenMusico))
                .andExpect(status().isForbidden());
    }

    // ---- cenário 9 ----

    @Test
    void deletarInstrumento_emUsoPorEscala_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider9@maranata.com");
        Long instrumentoId = criarInstrumento("Bateria M9", CategoriaInstrumento.PERCUSSAO);
        Long usuarioId = criarMusicoAtivo("Musico Nove", "musico9@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        criarEscala(cultoId, usuarioId, instrumentoId);

        mockMvc.perform(delete("/api/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Instrumento em uso em uma ou mais escalas — não pode ser removido"));
    }

    // ---- cenário 10 ----

    @Test
    void deletarInstrumento_vinculadoAMusicoInstrumento_deveRetornar409() throws Exception {
        String token = obterTokenLider("lider10@maranata.com");
        Long instrumentoId = criarInstrumento("Guitarra M10", CategoriaInstrumento.CORDA);
        Long usuarioId = criarMusicoAtivo("Musico Dez", "musico10@maranata.com");
        criarMusicoInstrumento(usuarioId, instrumentoId);

        mockMvc.perform(delete("/api/instrumentos/" + instrumentoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Instrumento em uso em uma ou mais escalas — não pode ser removido"));
    }

    // ---- cenário 11 ----

    @Test
    void deletarInstrumento_inexistente_deveRetornar404() throws Exception {
        String token = obterTokenLider("lider11@maranata.com");

        mockMvc.perform(delete("/api/instrumentos/9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Instrumento não encontrado"));
    }

    // ---- helpers ----

    private String obterTokenLider(String email) throws Exception {
        return criarUsuarioComPapelELogar("Líder Teste", email, Papel.LIDER, true);
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

    private void criarEscala(Long cultoId, Long usuarioId, Long instrumentoId) {
        Escala escala = new Escala();
        escala.setCulto(cultoRepository.getReferenceById(cultoId));
        escala.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        escala.setInstrumento(instrumentoRepository.getReferenceById(instrumentoId));
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
