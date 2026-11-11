package com.nicolas.chegabem.dto;

import com.nicolas.chegabem.model.Trajeto;
import com.nicolas.chegabem.model.enums.StatusTrajeto;

import java.time.LocalDateTime;

public record TrajetoResponse(
        Long id,
        Long idUsuaria,
        String origem,
        String destino,
        LocalDateTime dtInicio,
        LocalDateTime dtPrevistaChegada,
        LocalDateTime dtChegadaConfirmada,
        StatusTrajeto status,
        int contatosAcompanhando
) {
    public static TrajetoResponse de(Trajeto t, int contatosAcompanhando) {
        return new TrajetoResponse(t.getId(), t.getUsuaria().getId(),
                t.getOrigem(), t.getDestino(), t.getDtInicio(),
                t.getDtPrevistaChegada(), t.getDtChegadaConfirmada(),
                t.getStatus(), contatosAcompanhando);
    }
}
