package br.com.maranatamusic.presentation.auth;

import java.util.Set;

public record AuthResponse(
        Long id,
        String token,
        String nome,
        String email,
        Set<String> papeis
) {}
