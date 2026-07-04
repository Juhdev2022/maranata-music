package br.com.maranatamusic.presentation.culto;

import br.com.maranatamusic.application.culto.CultoService;
import br.com.maranatamusic.infrastructure.security.CustomUserDetails;
import br.com.maranatamusic.presentation.culto.dto.CriarCultoRequest;
import br.com.maranatamusic.presentation.culto.dto.CultoDetalheResponse;
import br.com.maranatamusic.presentation.culto.dto.CultoResponse;
import br.com.maranatamusic.presentation.culto.dto.EscalaResumo;
import br.com.maranatamusic.presentation.culto.dto.EscalarMusicoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/cultos")
public class CultoController {

    private final CultoService cultoService;

    public CultoController(CultoService cultoService) {
        this.cultoService = cultoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<CultoResponse> criar(@Valid @RequestBody CriarCultoRequest request,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        CultoResponse response = cultoService.criar(request, userDetails.getUsuario().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CultoResponse>> listar(@RequestParam("mes") YearMonth mes) {
        return ResponseEntity.ok(cultoService.listarPorMes(mes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CultoDetalheResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(cultoService.buscarDetalhe(id));
    }

    @PostMapping("/{cultoId}/escalas")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<EscalaResumo> escalar(@PathVariable Long cultoId,
                                                 @Valid @RequestBody EscalarMusicoRequest request) {
        EscalaResumo response = cultoService.escalar(cultoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
