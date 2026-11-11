package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.TrechoRua;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrechoRuaRepository extends JpaRepository<TrechoRua, Long> {

    List<TrechoRua> findByBairroIdOrderByNomeRua(Long idBairro);

    List<TrechoRua> findTop10ByOrderByNivelRiscoDesc();
}
