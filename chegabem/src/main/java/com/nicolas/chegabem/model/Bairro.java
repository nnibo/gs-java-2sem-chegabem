package com.nicolas.chegabem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_CB_BAIRRO")
@Getter
@Setter
@NoArgsConstructor
public class Bairro {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_BAIRRO")
    @SequenceGenerator(name = "SQ_CB_BAIRRO", sequenceName = "SQ_CB_BAIRRO", allocationSize = 1)
    @Column(name = "ID_BAIRRO")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "CIDADE", nullable = false, length = 100)
    private String cidade;
}
