package br.com.maranatamusic.presentation.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DefinirSenhaRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String senha,
        @NotBlank String confirmarSenha
) {}
