package com.nicolas.chegabem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuariaRequest(

        @NotBlank(message = "O nome e obrigatorio.")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio.")
        @Email(message = "E-mail em formato invalido.")
        @Size(max = 150, message = "O e-mail deve ter no maximo 150 caracteres.")
        String email,

        @NotBlank(message = "A senha e obrigatoria.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String senha,

        @NotBlank(message = "O telefone e obrigatorio.")
        @Pattern(regexp = "\\d{10,13}",
                 message = "Informe apenas digitos, com DDD. Ex.: 11999998888")
        String telefone
) {
}
