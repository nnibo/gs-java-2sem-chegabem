package com.nicolas.chegabem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nicolas.chegabem.model.enums.TipoPontoSeguro;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record PontoSeguroRequest(

        @NotNull(message = "O bairro e obrigatorio.")
        Long idBairro,

        @NotBlank(message = "O nome do estabelecimento e obrigatorio.")
        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres.")
        String nome,

        @NotNull(message = "O tipo e obrigatorio.")
        TipoPontoSeguro tipo,

        @NotNull(message = "O horario de abertura e obrigatorio.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime hrAbertura,

        @NotNull(message = "O horario de fechamento e obrigatorio.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime hrFechamento,

        @NotNull(message = "A latitude e obrigatoria.")
        @DecimalMin(value = "-90.0", message = "Latitude invalida.")
        @DecimalMax(value = "90.0", message = "Latitude invalida.")
        BigDecimal latitude,

        @NotNull(message = "A longitude e obrigatoria.")
        @DecimalMin(value = "-180.0", message = "Longitude invalida.")
        @DecimalMax(value = "180.0", message = "Longitude invalida.")
        BigDecimal longitude
) {
}
