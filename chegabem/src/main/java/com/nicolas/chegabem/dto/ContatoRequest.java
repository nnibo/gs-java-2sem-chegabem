package com.nicolas.chegabem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContatoRequest(

        @NotBlank(message = "O nome do contato e obrigatorio.")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres.")
        String nome,

        @NotBlank(message = "O telefone do contato e obrigatorio.")
        @Pattern(regexp = "\\d{10,13}",
                 message = "Informe apenas digitos, com DDD. Ex.: 11999998888")
        String telefone,

        @Size(max = 50, message = "O parentesco deve ter no maximo 50 caracteres.")
        String parentesco
) {
}
