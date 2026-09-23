package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByTipoIdentificacionAndIdentificacion(String tipoIdentificacion, String identificacion);
}
