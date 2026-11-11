package com.nicolas.chegabem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuariaUpdateRequest(

        @NotBlank(message = "O nome e obrigatorio.")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres.")
        String nome,

        @NotBlank(message = "O telefone e obrigatorio.")
        @Pattern(regexp = "\\d{10,13}",
                 message = "Informe apenas digitos, com DDD. Ex.: 11999998888")
        String telefone
) {
}
