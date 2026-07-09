package br.com.maranatamusic.presentation.escala;

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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EscalaControllerIT {

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
    void minhasEscalas_comEscalasNoMes_deveRetornar200ComLista() throws Exception {
        Usuario musico = criarUsuario("Musico Um", "musico1@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão E1", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(get("/api/escalas/minhas?mes=2026-08")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));
    }

    // ---- cenário 2 ----

    @Test
    void minhasEscalas_semEscalas_deveRetornar200ComListaVazia() throws Exception {
        Usuario musico = criarUsuario("Musico Dois", "musico2@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());

        mockMvc.perform(get("/api/escalas/minhas?mes=2026-08")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---- cenário 3 ----

    @Test
    void confirmar_escalaPropriaPendente_deveRetornar200EConfirmar() throws Exception {
        Usuario musico = criarUsuario("Musico Tres", "musico3@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão E3", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/escalas/" + escalaId + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.confirmadaEm").exists());
    }

    // ---- cenário 4 ----

    @Test
    void confirmar_escalaJaConfirmada_deveRetornar200Idempotente() throws Exception {
        Usuario musico = criarUsuario("Musico Quatro", "musico4@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão E4", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.CONFIRMADA);

        mockMvc.perform(post("/api/escalas/" + escalaId + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    // ---- cenário 5 ----

    @Test
    void confirmar_escalaSubstituida_deveRetornar409() throws Exception {
        Usuario musico = criarUsuario("Musico Cinco", "musico5@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão E5", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.SUBSTITUIDA);

        mockMvc.perform(post("/api/escalas/" + escalaId + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Escala em estado que não permite confirmação"));
    }

    // ---- cenário 6 ----

    @Test
    void confirmar_escalaDeOutroMusico_deveRetornar403() throws Exception {
        Usuario dono = criarUsuario("Musico Dono Seis", "musico6dono@maranata.com", Papel.MUSICO);
        Usuario outro = criarUsuario("Musico Outro Seis", "musico6outro@maranata.com", Papel.MUSICO);
        String tokenOutro = login(outro.getEmail());
        Long instrumentoId = criarInstrumento("Violão E6", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, dono.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/escalas/" + escalaId + "/confirmar")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Você não pode confirmar escala de outro músico"));
    }

    // ---- cenário 7 ----

    @Test
    void confirmar_escalaInexistente_deveRetornar404() throws Exception {
        Usuario musico = criarUsuario("Musico Sete", "musico7@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());

        mockMvc.perform(post("/api/escalas/9999/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Escala não encontrada: 9999"));
    }

    // ---- cenário 8 ----

    @Test
    void recusar_escalaPendente_deveRetornar200ERecusar() throws Exception {
        Usuario musico = criarUsuario("Musico Oito", "musico8@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão E8", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/escalas/" + escalaId + "/recusar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECUSADA"));
    }

    // ---- cenário 9 ----

    @Test
    void recusar_escalaJaConfirmada_deveRetornar200ERecusar() throws Exception {
        Usuario musico = criarUsuario("Musico Nove", "musico9@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão E9", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.CONFIRMADA);

        mockMvc.perform(post("/api/escalas/" + escalaId + "/recusar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECUSADA"));
    }

    // ---- cenário 10 ----

    @Test
    void recusar_escalaSubstituida_deveRetornar409() throws Exception {
        Usuario musico = criarUsuario("Musico Dez", "musico10@maranata.com", Papel.MUSICO);
        String token = login(musico.getEmail());
        Long instrumentoId = criarInstrumento("Violão E10", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, musico.getId(), instrumentoId, EscalaStatus.SUBSTITUIDA);

        mockMvc.perform(post("/api/escalas/" + escalaId + "/recusar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Escala em estado que não permite confirmação"));
    }

    // ---- cenário 11 ----

    @Test
    void recusar_escalaDeOutroMusico_deveRetornar403() throws Exception {
        Usuario dono = criarUsuario("Musico Dono Onze", "musico11dono@maranata.com", Papel.MUSICO);
        Usuario outro = criarUsuario("Musico Outro Onze", "musico11outro@maranata.com", Papel.MUSICO);
        String tokenOutro = login(outro.getEmail());
        Long instrumentoId = criarInstrumento("Violão E11", CategoriaInstrumento.CORDA);
        Long cultoId = criarCulto(LocalDateTime.of(2026, 8, 2, 19, 0), CultoTipo.DOMINGO_NOITE);
        Long escalaId = criarEscala(cultoId, dono.getId(), instrumentoId, EscalaStatus.PENDENTE);

        mockMvc.perform(post("/api/escalas/" + escalaId + "/recusar")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isForbidden());
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
