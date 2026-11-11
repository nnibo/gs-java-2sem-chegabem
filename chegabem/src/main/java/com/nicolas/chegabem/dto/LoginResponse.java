package com.nicolas.chegabem.dto;

import com.nicolas.chegabem.model.enums.PerfilUsuaria;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        Long idUsuaria,
        String nome,
        PerfilUsuaria perfil
) {
    public static LoginResponse de(String token, long expiraEmSegundos,
                                   Long idUsuaria, String nome, PerfilUsuaria perfil) {
        return new LoginResponse(token, "Bearer", expiraEmSegundos, idUsuaria, nome, perfil);
    }
}
