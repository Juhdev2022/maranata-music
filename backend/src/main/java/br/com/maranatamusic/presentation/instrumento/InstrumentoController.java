package br.com.maranatamusic.presentation.instrumento;

import br.com.maranatamusic.application.instrumento.InstrumentoService;
import br.com.maranatamusic.presentation.instrumento.dto.CriarInstrumentoRequest;
import br.com.maranatamusic.presentation.instrumento.dto.InstrumentoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoController {

    private final InstrumentoService instrumentoService;

    public InstrumentoController(InstrumentoService instrumentoService) {
        this.instrumentoService = instrumentoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<InstrumentoResponse> criar(@Valid @RequestBody CriarInstrumentoRequest request) {
        InstrumentoResponse response = instrumentoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InstrumentoResponse>> listar() {
        return ResponseEntity.ok(instrumentoService.listar());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        instrumentoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
