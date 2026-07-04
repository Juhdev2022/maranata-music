package br.com.maranatamusic.presentation.escala;

import br.com.maranatamusic.application.escala.EscalaService;
import br.com.maranatamusic.infrastructure.security.CustomUserDetails;
import br.com.maranatamusic.presentation.escala.dto.EscalaMinhaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/escalas")
public class EscalaController {

    private final EscalaService escalaService;

    public EscalaController(EscalaService escalaService) {
        this.escalaService = escalaService;
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<EscalaMinhaResponse>> minhasEscalas(
            @RequestParam("mes") YearMonth mes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(escalaService.minhasEscalas(userDetails.getUsuario().getId(), mes));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<EscalaMinhaResponse> confirmar(@PathVariable Long id,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(escalaService.confirmar(id, userDetails.getUsuario().getId()));
    }

    @PostMapping("/{id}/recusar")
    public ResponseEntity<EscalaMinhaResponse> recusar(@PathVariable Long id,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(escalaService.recusar(id, userDetails.getUsuario().getId()));
    }
}
