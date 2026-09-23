package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Usuario;
import com.facturadorsri.backend.dto.auth.LoginRequest;
import com.facturadorsri.backend.dto.auth.LoginResponse;
import com.facturadorsri.backend.exception.RateLimitExceededException;
import com.facturadorsri.backend.repository.UsuarioRepository;
import com.facturadorsri.backend.security.JwtService;
import com.facturadorsri.backend.security.LoginRateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final LoginRateLimiterService rateLimiterService;

    public LoginResponse login(LoginRequest request) {
        String claveIntentos = request.username().toLowerCase();
        if (rateLimiterService.estaBloqueado(claveIntentos)) {
            throw new RateLimitExceededException(
                    "Demasiados intentos fallidos. Intente de nuevo en unos minutos.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (BadCredentialsException ex) {
            rateLimiterService.registrarFallo(claveIntentos);
            throw ex;
        }

        rateLimiterService.registrarExito(claveIntentos);
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contrasena incorrectos"));

        String token = jwtService.generarToken(usuario.getUsername(), usuario.getRol().name());
        return new LoginResponse(token, usuario.getUsername(), usuario.getRol().name(),
                jwtService.expirationMinutes());
    }
}
