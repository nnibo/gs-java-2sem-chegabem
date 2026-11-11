package com.nicolas.chegabem.dto;

import com.nicolas.chegabem.model.Bairro;

public record BairroResponse(Long id, String nome, String cidade) {

    public static BairroResponse de(Bairro b) {
        return new BairroResponse(b.getId(), b.getNome(), b.getCidade());
    }
}
