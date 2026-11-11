package com.nicolas.chegabem.dto;

import com.nicolas.chegabem.model.Relato;

import java.time.LocalDateTime;

public record RelatoResponse(
        Long id,
        Long idTrecho,
        String nomeRua,
        String bairro,
        String tipo,
        String descricao,
        LocalDateTime dtRelato,
        boolean anonimo,
        String autora
) {
    public static RelatoResponse de(Relato r) {
        return new RelatoResponse(
                r.getId(),
                r.getTrecho().getId(),
                r.getTrecho().getNomeRua(),
                r.getTrecho().getBairro().getNome(),
                r.getTipo().getDescricao(),
                r.getDescricao(),
                r.getDtRelato(),
                r.isAnonimo(),
                r.getUsuaria() == null ? null : r.getUsuaria().getNome());
    }
}
