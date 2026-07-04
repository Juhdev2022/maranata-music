package br.com.maranatamusic.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationHours;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-hours}") long expirationHours) {
        this.secret = secret;
        this.expirationHours = expirationHours;
    }

    public String gerarToken(UserDetails userDetails) {
        long agora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(agora))
                .expiration(new Date(agora + expirationHours * 3_600_000))
                .signWith(getSigningKey())
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean isTokenValido(String token, UserDetails userDetails) {
        return extrairEmail(token).equals(userDetails.getUsername()) && !isExpirado(token);
    }

    public boolean isExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
