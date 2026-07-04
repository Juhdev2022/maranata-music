package br.com.maranatamusic.application.auth;

import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.exception.EmailJaCadastradoException;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.infrastructure.security.CustomUserDetails;
import br.com.maranatamusic.infrastructure.security.CustomUserDetailsService;
import br.com.maranatamusic.infrastructure.security.JwtService;
import br.com.maranatamusic.presentation.auth.AuthResponse;
import br.com.maranatamusic.presentation.auth.LoginRequest;
import br.com.maranatamusic.presentation.auth.RegistroRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setTelefone(request.telefone());
        usuario.getPapeis().add(Papel.MUSICO);

        usuarioRepository.save(usuario);

        // Carrega com JOIN FETCH para garantir papeis na sessão corrente
        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(usuario.getEmail());

        String token = jwtService.gerarToken(userDetails);
        return toAuthResponse(token, userDetails);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtService.gerarToken(userDetails);
        return toAuthResponse(token, userDetails);
    }

    private AuthResponse toAuthResponse(String token, CustomUserDetails userDetails) {
        Set<String> papeis = userDetails.getUsuario().getPapeis().stream()
                .map(Papel::name)
                .collect(Collectors.toSet());
        return new AuthResponse(token, userDetails.getUsuario().getNome(),
                userDetails.getUsername(), papeis);
    }
}
