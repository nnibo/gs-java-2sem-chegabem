package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.Relato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelatoRepository extends JpaRepository<Relato, Long> {

    List<Relato> findByTrechoIdOrderByDtRelatoDesc(Long idTrecho);

    long countByTrechoId(Long idTrecho);

    List<Relato> findAllByOrderByDtRelatoDesc();
}
