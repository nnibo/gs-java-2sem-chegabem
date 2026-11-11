package com.nicolas.chegabem.dto;

import com.nicolas.chegabem.model.ContatoConfianca;

public record ContatoResponse(
        Long id,
        Long idUsuaria,
        String nome,
        String telefone,
        String parentesco
) {
    public static ContatoResponse de(ContatoConfianca c) {
        return new ContatoResponse(c.getId(), c.getUsuaria().getId(),
                c.getNome(), c.getTelefone(), c.getParentesco());
    }
}
