package br.com.maranatamusic.presentation.escala.dto;

import br.com.maranatamusic.domain.enums.CultoTipo;

import java.time.LocalDateTime;

public record CultoResumo(Long id, LocalDateTime dataHora, CultoTipo tipo) {
}
