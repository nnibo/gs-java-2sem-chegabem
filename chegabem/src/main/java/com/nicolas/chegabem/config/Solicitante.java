package com.nicolas.chegabem.config;

import com.nicolas.chegabem.model.enums.PerfilUsuaria;
import org.springframework.security.oauth2.jwt.Jwt;

public record Solicitante(Long id, PerfilUsuaria perfil) {

    public static Solicitante de(Jwt jwt) {
        return new Solicitante(
                Long.valueOf(jwt.getSubject()),
                PerfilUsuaria.valueOf(jwt.getClaimAsString("perfil")));
    }

    public boolean isAdmin() {
        return perfil == PerfilUsuaria.ADMIN;
    }

    public boolean podeAcessar(Long idDono) {
        return isAdmin() || id.equals(idDono);
    }
}
