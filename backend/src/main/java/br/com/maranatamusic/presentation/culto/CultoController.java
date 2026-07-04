package br.com.maranatamusic.presentation.culto;

import br.com.maranatamusic.application.culto.CultoService;
import br.com.maranatamusic.infrastructure.security.CustomUserDetails;
import br.com.maranatamusic.presentation.culto.dto.CriarCultoRequest;
import br.com.maranatamusic.presentation.culto.dto.CultoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
