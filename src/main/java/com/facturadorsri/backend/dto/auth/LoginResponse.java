package com.facturadorsri.backend.dto.auth;

public record LoginResponse(String token, String username, String rol, long expiraEnMinutos) {
}
