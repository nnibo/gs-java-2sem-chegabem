package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.TipoRelato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoRelatoRepository extends JpaRepository<TipoRelato, Long> {

    Optional<TipoRelato> findByDescricaoIgnoreCase(String descricao);
}
