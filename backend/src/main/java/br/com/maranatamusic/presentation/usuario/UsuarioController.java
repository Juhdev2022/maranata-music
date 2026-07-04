package br.com.maranatamusic.presentation.usuario;

import br.com.maranatamusic.application.usuario.UsuarioService;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.presentation.usuario.dto.AlterarPapelRequest;
import br.com.maranatamusic.presentation.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/{id}/papeis")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<UsuarioResponse> adicionarPapel(@PathVariable Long id,
                                                           @Valid @RequestBody AlterarPapelRequest request) {
        UsuarioResponse response = usuarioService.adicionarPapel(id, request.papel());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/papeis/{papel}")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<Void> removerPapel(@PathVariable Long id, @PathVariable Papel papel) {
        usuarioService.removerPapel(id, papel);
        return ResponseEntity.noContent().build();
    }
}
