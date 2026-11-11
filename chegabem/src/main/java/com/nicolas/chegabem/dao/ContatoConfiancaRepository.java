package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.ContatoConfianca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContatoConfiancaRepository extends JpaRepository<ContatoConfianca, Long> {

    List<ContatoConfianca> findByUsuariaIdOrderByNome(Long idUsuaria);

    long countByUsuariaId(Long idUsuaria);
}
