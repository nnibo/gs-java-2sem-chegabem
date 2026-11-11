package com.nicolas.chegabem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_CB_TIPO_RELATO")
@Getter
@Setter
@NoArgsConstructor
public class TipoRelato {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_TIPO_RELATO")
    @SequenceGenerator(name = "SQ_CB_TIPO_RELATO", sequenceName = "SQ_CB_TIPO_RELATO", allocationSize = 1)
    @Column(name = "ID_TIPO")
    private Long id;

    @Column(name = "DESCRICAO", nullable = false, unique = true, length = 80)
    private String descricao;
}
