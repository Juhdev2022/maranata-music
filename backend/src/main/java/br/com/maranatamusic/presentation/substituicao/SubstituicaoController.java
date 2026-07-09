package br.com.maranatamusic.presentation.substituicao;

import br.com.maranatamusic.application.substituicao.SubstituicaoService;
import br.com.maranatamusic.infrastructure.security.CustomUserDetails;
import br.com.maranatamusic.presentation.substituicao.dto.RejeitarSubstituicaoRequest;
import br.com.maranatamusic.presentation.substituicao.dto.SolicitacaoResponse;
import br.com.maranatamusic.presentation.substituicao.dto.SolicitarSubstituicaoRequest;
import br.com.maranatamusic.presentation.substituicao.dto.SubstitutoElegivelResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/substituicoes")
public class SubstituicaoController {

    private final SubstituicaoService substituicaoService;

    public SubstituicaoController(SubstituicaoService substituicaoService) {
        this.substituicaoService = substituicaoService;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> solicitar(@Valid @RequestBody SolicitarSubstituicaoRequest request,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        SolicitacaoResponse response = substituicaoService.solicitar(request, userDetails.getUsuario().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<List<SolicitacaoResponse>> pendentes() {
        return ResponseEntity.ok(substituicaoService.listarPendentes());
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<SolicitacaoResponse> aprovar(@PathVariable Long id,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(substituicaoService.aprovar(id, userDetails.getUsuario().getId()));
    }

    @PostMapping("/{id}/rejeitar")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<SolicitacaoResponse> rejeitar(@PathVariable Long id,
                                                         @RequestBody RejeitarSubstituicaoRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(substituicaoService.rejeitar(id, request, userDetails.getUsuario().getId()));
    }

    @GetMapping("/substitutos-elegiveis")
    public ResponseEntity<List<SubstitutoElegivelResponse>> substitutosElegiveis(
            @RequestParam Long escalaId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(substituicaoService.listarSubstitutosElegiveis(escalaId, userDetails.getUsuario()));
    }
}
