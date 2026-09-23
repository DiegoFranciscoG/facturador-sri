package com.facturadorsri.backend.config;

import com.facturadorsri.backend.domain.Usuario;
import com.facturadorsri.backend.domain.enums.RolUsuario;
import com.facturadorsri.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el usuario administrador inicial desde ADMIN_USERNAME/ADMIN_PASSWORD
 * si la tabla usuario esta vacia. Nunca hay una contrasena de ejemplo
 * versionada: si las variables no estan definidas, la aplicacion no
 * arranca (Regla maestra seccion 3).
 */
@Component
@RequiredArgsConstructor
public class SeguridadBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeguridadBootstrapRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeguridadProperties seguridadProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        Usuario admin = Usuario.builder()
                .username(seguridadProperties.adminUsername())
                .passwordHash(passwordEncoder.encode(seguridadProperties.adminPassword()))
                .rol(RolUsuario.ADMIN)
                .activo(true)
                .build();
        usuarioRepository.save(admin);
        log.info("Usuario administrador inicial creado: {}", admin.getUsername());
    }
}
