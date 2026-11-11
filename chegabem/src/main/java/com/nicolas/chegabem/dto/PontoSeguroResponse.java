package com.nicolas.chegabem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nicolas.chegabem.model.PontoSeguro;
import com.nicolas.chegabem.model.enums.TipoPontoSeguro;

import java.math.BigDecimal;
import java.time.LocalTime;

public record PontoSeguroResponse(
        Long id,
        String nome,
        TipoPontoSeguro tipo,
        String bairro,
        String cidade,
        @JsonFormat(pattern = "HH:mm") LocalTime hrAbertura,
        @JsonFormat(pattern = "HH:mm") LocalTime hrFechamento,
        boolean abertoAgora,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static PontoSeguroResponse de(PontoSeguro p, boolean abertoAgora) {
        return new PontoSeguroResponse(p.getId(), p.getNome(), p.getTipo(),
                p.getBairro().getNome(), p.getBairro().getCidade(),
                p.getHrAbertura(), p.getHrFechamento(), abertoAgora,
                p.getLatitude(), p.getLongitude());
    }
}
