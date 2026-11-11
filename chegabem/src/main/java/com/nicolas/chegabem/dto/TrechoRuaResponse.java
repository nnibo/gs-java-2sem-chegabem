package com.nicolas.chegabem.dto;

import com.nicolas.chegabem.model.TrechoRua;

import java.math.BigDecimal;

public record TrechoRuaResponse(
        Long id,
        String nomeRua,
        String bairro,
        BigDecimal nivelIluminacao,
        BigDecimal nivelRisco,
        long qtdRelatos
) {
    public static TrechoRuaResponse de(TrechoRua t, long qtdRelatos) {
        return new TrechoRuaResponse(t.getId(), t.getNomeRua(),
                t.getBairro().getNome(), t.getNivelIluminacao(),
                t.getNivelRisco(), qtdRelatos);
    }
}
