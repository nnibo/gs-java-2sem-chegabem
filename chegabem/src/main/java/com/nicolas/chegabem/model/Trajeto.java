package com.nicolas.chegabem.model;

import com.nicolas.chegabem.model.enums.StatusTrajeto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_CB_TRAJETO")
@Getter
@Setter
@NoArgsConstructor
public class Trajeto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_TRAJETO")
    @SequenceGenerator(name = "SQ_CB_TRAJETO", sequenceName = "SQ_CB_TRAJETO", allocationSize = 1)
    @Column(name = "ID_TRAJETO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIA", nullable = false)
    private Usuaria usuaria;

    @Column(name = "ORIGEM", nullable = false, length = 200)
    private String origem;

    @Column(name = "DESTINO", nullable = false, length = 200)
    private String destino;

    @Column(name = "DT_INICIO", nullable = false)
    private LocalDateTime dtInicio;

    @Column(name = "DT_PREVISTA_CHEGADA", nullable = false)
    private LocalDateTime dtPrevistaChegada;

    @Column(name = "DT_CHEGADA_CONFIRMADA")
    private LocalDateTime dtChegadaConfirmada;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusTrajeto status = StatusTrajeto.EM_ANDAMENTO;

    @PrePersist
    private void aoCriar() {
        if (dtInicio == null) {
            dtInicio = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusTrajeto.EM_ANDAMENTO;
        }
    }
}
