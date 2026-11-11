package com.nicolas.chegabem.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TrajetoRequest(

        @NotBlank(message = "A origem e obrigatoria.")
        @Size(max = 200, message = "A origem deve ter no maximo 200 caracteres.")
        String origem,

        @NotBlank(message = "O destino e obrigatorio.")
        @Size(max = 200, message = "O destino deve ter no maximo 200 caracteres.")
        String destino,

        @NotNull(message = "O horario previsto de chegada e obrigatorio.")
        @Future(message = "O horario previsto de chegada precisa estar no futuro.")
        LocalDateTime dtPrevistaChegada
) {
}
