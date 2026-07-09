package br.com.maranatamusic.presentation.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CriarUsuarioRequest(
        @NotBlank String nome,
        @Email @NotBlank String email,
        String telefone
) {}
