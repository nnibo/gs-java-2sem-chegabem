package com.nicolas.chegabem.bo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VerificadorDeTrajetos {

    private static final Logger log = LoggerFactory.getLogger(VerificadorDeTrajetos.class);

    private final TrajetoBO trajetoBO;

    public VerificadorDeTrajetos(TrajetoBO trajetoBO) {
        this.trajetoBO = trajetoBO;
    }

    @Scheduled(fixedDelayString = "${chegabem.trajeto.intervalo-verificacao-ms}",
               initialDelayString = "${chegabem.trajeto.intervalo-verificacao-ms}")
    public void verificarNaoChegadas() {
        try {
            int alertados = trajetoBO.alertarNaoChegadas();
            if (alertados > 0) {
                log.info("Verificacao de trajetos: {} trajeto(s) com alerta de nao chegada.", alertados);
            }
        } catch (Exception e) {
            log.error("Falha na verificacao periodica de trajetos.", e);
        }
    }
}
