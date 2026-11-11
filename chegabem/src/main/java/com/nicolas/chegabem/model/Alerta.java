package com.nicolas.chegabem.model;

import com.nicolas.chegabem.model.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_CB_ALERTA")
@Getter
@Setter
@NoArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_ALERTA")
    @SequenceGenerator(name = "SQ_CB_ALERTA", sequenceName = "SQ_CB_ALERTA", allocationSize = 1)
    @Column(name = "ID_ALERTA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TRAJETO", nullable = false)
    private Trajeto trajeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CONTATO", nullable = false)
    private ContatoConfianca contato;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_ALERTA", nullable = false, length = 25)
    private TipoAlerta tipoAlerta;

    @Column(name = "DT_ENVIO", nullable = false)
    private LocalDateTime dtEnvio;

    @PrePersist
    private void aoCriar() {
        if (dtEnvio == null) {
            dtEnvio = LocalDateTime.now();
        }
    }
}
