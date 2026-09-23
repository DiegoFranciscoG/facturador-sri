package com.facturadorsri.backend.security;

import com.facturadorsri.backend.config.SeguridadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting en memoria para el login (Regla maestra seccion 3: "rate
 * limiting en login"). Suficiente para una sola instancia (Render free =
 * un solo dyno); si se escalara a varias instancias habria que mover esto
 * a un almacen compartido (Redis).
 */
@Component
@RequiredArgsConstructor
public class LoginRateLimiterService {

    private record Intentos(int cantidad, Instant primerIntento) {
    }

    private final Map<String, Intentos> intentosPorClave = new ConcurrentHashMap<>();
    private final SeguridadProperties seguridadProperties;

    public boolean estaBloqueado(String clave) {
        Intentos intentos = intentosPorClave.get(clave);
        if (intentos == null) {
            return false;
        }
        if (ventanaExpirada(intentos)) {
            intentosPorClave.remove(clave);
            return false;
        }
        return intentos.cantidad() >= seguridadProperties.loginMaxIntentos();
    }

    public void registrarFallo(String clave) {
        intentosPorClave.compute(clave, (k, actual) -> {
            if (actual == null || ventanaExpirada(actual)) {
                return new Intentos(1, Instant.now());
            }
            return new Intentos(actual.cantidad() + 1, actual.primerIntento());
        });
    }

    public void registrarExito(String clave) {
        intentosPorClave.remove(clave);
    }

    private boolean ventanaExpirada(Intentos intentos) {
        Duration ventana = Duration.ofMinutes(seguridadProperties.loginBloqueoMinutos());
        return Instant.now().isAfter(intentos.primerIntento().plus(ventana));
    }
}
