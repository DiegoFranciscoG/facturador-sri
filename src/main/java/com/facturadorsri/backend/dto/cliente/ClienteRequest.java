package com.facturadorsri.backend.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Pattern(regexp = "04|05|06|07|08", message = "tipo de identificacion no valido")
        String tipoIdentificacion,

        @NotBlank @Size(min = 5, max = 20)
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "la identificacion solo admite letras y numeros")
        String identificacion,

        @NotBlank @Size(max = 300)
        String razonSocial,

        @Size(max = 300)
        String direccion,

        @Email @Size(max = 150)
        String email,

        @Pattern(regexp = "^$|^[0-9+()\\-\\s]{7,20}$", message = "telefono no valido")
        String telefono
) {
}
