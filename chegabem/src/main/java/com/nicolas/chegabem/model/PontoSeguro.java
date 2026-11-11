package com.nicolas.chegabem.model;

import com.nicolas.chegabem.model.converter.HoraVarcharConverter;
import com.nicolas.chegabem.model.enums.TipoPontoSeguro;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "T_CB_PONTO_SEGURO")
@Getter
@Setter
@NoArgsConstructor
public class PontoSeguro {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_PONTO_SEGURO")
    @SequenceGenerator(name = "SQ_CB_PONTO_SEGURO", sequenceName = "SQ_CB_PONTO_SEGURO", allocationSize = 1)
    @Column(name = "ID_PONTO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_BAIRRO", nullable = false)
    private Bairro bairro;

    @Column(name = "NOME", nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 30)
    private TipoPontoSeguro tipo;

    @Convert(converter = HoraVarcharConverter.class)
    @Column(name = "HR_ABERTURA", nullable = false, length = 5)
    private LocalTime hrAbertura;

    @Convert(converter = HoraVarcharConverter.class)
    @Column(name = "HR_FECHAMENTO", nullable = false, length = 5)
    private LocalTime hrFechamento;

    @Column(name = "LATITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;
}
