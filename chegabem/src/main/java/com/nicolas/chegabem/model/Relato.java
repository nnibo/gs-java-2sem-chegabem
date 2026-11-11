package com.nicolas.chegabem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_CB_RELATO")
@Getter
@Setter
@NoArgsConstructor
public class Relato {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_RELATO")
    @SequenceGenerator(name = "SQ_CB_RELATO", sequenceName = "SQ_CB_RELATO", allocationSize = 1)
    @Column(name = "ID_RELATO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TRECHO", nullable = false)
    private TrechoRua trecho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TIPO", nullable = false)
    private TipoRelato tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIA")
    private Usuaria usuaria;

    @Column(name = "DESCRICAO", nullable = false, length = 500)
    private String descricao;

    @Column(name = "DT_RELATO", nullable = false)
    private LocalDateTime dtRelato;

    @Column(name = "ANONIMO", nullable = false)
    private boolean anonimo = true;

    @PrePersist
    private void aoCriar() {
        if (dtRelato == null) {
            dtRelato = LocalDateTime.now();
        }
        if (anonimo) {
            usuaria = null;
        }
    }
}
