package com.nicolas.chegabem.model;

import com.nicolas.chegabem.model.enums.PerfilUsuaria;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_CB_USUARIA")
@Getter
@Setter
@NoArgsConstructor
public class Usuaria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_CB_USUARIA")
    @SequenceGenerator(name = "SQ_CB_USUARIA", sequenceName = "SQ_CB_USUARIA", allocationSize = 1)
    @Column(name = "ID_USUARIA")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "SENHA_HASH", nullable = false, length = 100)
    private String senhaHash;

    @Column(name = "TELEFONE", nullable = false, length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "PERFIL", nullable = false, length = 20)
    private PerfilUsuaria perfil = PerfilUsuaria.USUARIA;

    @Column(name = "DT_CADASTRO", nullable = false)
    private LocalDateTime dtCadastro;

    @PrePersist
    private void aoCriar() {
        if (dtCadastro == null) {
            dtCadastro = LocalDateTime.now();
        }
        if (perfil == null) {
            perfil = PerfilUsuaria.USUARIA;
        }
    }
}
