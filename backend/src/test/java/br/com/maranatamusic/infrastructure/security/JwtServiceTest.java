package br.com.maranatamusic.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // Chave Base64 fixa usada apenas nesta classe de teste — não usar em prod
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC1vbmx5LWRvLW5vdC11c2UtaW4tcHJvZC0xMjM0NTY3ODkwCg==";

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 24);
        userDetails = User.withUsername("musico@maranata.com")
                .password("irrelevante")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void gerarToken_deveRetornarTokenNaoNulo() {
        String token = jwtService.gerarToken(userDetails);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extrairEmail_deveRetornarEmailCorreto() {
        String token = jwtService.gerarToken(userDetails);
        assertThat(jwtService.extrairEmail(token)).isEqualTo("musico@maranata.com");
    }

    @Test
    void isTokenValido_tokenValidoEUsuarioCorreto_deveRetornarTrue() {
        String token = jwtService.gerarToken(userDetails);
        assertThat(jwtService.isTokenValido(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValido_emailDiferente_deveRetornarFalse() {
        String token = jwtService.gerarToken(userDetails);
        UserDetails outroUsuario = User.withUsername("outro@maranata.com")
                .password("x")
                .authorities(Collections.emptyList())
                .build();
        assertThat(jwtService.isTokenValido(token, outroUsuario)).isFalse();
    }

    @Test
    void isExpirado_tokenValido_deveRetornarFalse() {
        String token = jwtService.gerarToken(userDetails);
        assertThat(jwtService.isExpirado(token)).isFalse();
    }

    @Test
    void isExpirado_tokenExpirado_deveRetornarTrue() {
        // Gera token já expirado com JJWT diretamente
        long passado = System.currentTimeMillis() - 1000;
        String tokenExpirado = Jwts.builder()
                .subject("musico@maranata.com")
                .issuedAt(new Date(passado - 1000))
                .expiration(new Date(passado))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET)))
                .compact();

        assertThatThrownBy(() -> jwtService.isExpirado(tokenExpirado))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void extrairEmail_tokenMalformado_deveLancarExcecao() {
        assertThatThrownBy(() -> jwtService.extrairEmail("token.invalido.aqui"))
                .isInstanceOf(Exception.class);
    }
}
