package com.nicolas.chegabem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_CB_CONTATO_CONFIANCA")
@Getter
@Setter
@NoArgsConstructor
public class ContatoConfianca {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_CONTATO_CONFIANCA")
    @SequenceGenerator(name = "SQ_CB_CONTATO_CONFIANCA", sequenceName = "SQ_CB_CONTATO_CONFIANCA", allocationSize = 1)
    @Column(name = "ID_CONTATO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIA", nullable = false)
    private Usuaria usuaria;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "TELEFONE", nullable = false, length = 20)
    private String telefone;

    @Column(name = "PARENTESCO", length = 50)
    private String parentesco;
}
