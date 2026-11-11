package com.nicolas.chegabem.dto;

import com.nicolas.chegabem.model.Usuaria;
import com.nicolas.chegabem.model.enums.PerfilUsuaria;

import java.time.LocalDateTime;

public record UsuariaResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        PerfilUsuaria perfil,
        LocalDateTime dtCadastro
) {
    public static UsuariaResponse de(Usuaria u) {
        return new UsuariaResponse(u.getId(), u.getNome(), u.getEmail(),
                u.getTelefone(), u.getPerfil(), u.getDtCadastro());
    }
}
