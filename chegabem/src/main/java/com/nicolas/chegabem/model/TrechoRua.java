package com.nicolas.chegabem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "T_CB_TRECHO_RUA")
@Getter
@Setter
@NoArgsConstructor
public class TrechoRua {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_TRECHO_RUA")
    @SequenceGenerator(name = "SQ_CB_TRECHO_RUA", sequenceName = "SQ_CB_TRECHO_RUA", allocationSize = 1)
    @Column(name = "ID_TRECHO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_BAIRRO", nullable = false)
    private Bairro bairro;

    @Column(name = "NOME_RUA", nullable = false, length = 150)
    private String nomeRua;

    @Column(name = "NIVEL_ILUMINACAO", nullable = false, precision = 3, scale = 2)
    private BigDecimal nivelIluminacao;

    @Column(name = "NIVEL_RISCO", nullable = false, precision = 3, scale = 2)
    private BigDecimal nivelRisco;
}
