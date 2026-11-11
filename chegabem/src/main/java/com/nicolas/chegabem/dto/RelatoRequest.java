package com.nicolas.chegabem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RelatoRequest(

        @NotNull(message = "O trecho e obrigatorio.")
        Long idTrecho,

        @NotNull(message = "O tipo de relato e obrigatorio.")
        Long idTipo,

        @NotBlank(message = "A descricao e obrigatoria.")
        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres.")
        String descricao,

        Boolean anonimo
) {
    public boolean anonimoOuPadrao() {
        return anonimo == null || anonimo;
    }
}
