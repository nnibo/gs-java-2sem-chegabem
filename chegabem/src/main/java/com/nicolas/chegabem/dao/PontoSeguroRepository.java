package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.PontoSeguro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PontoSeguroRepository extends JpaRepository<PontoSeguro, Long> {

    List<PontoSeguro> findByBairroNomeIgnoreCaseOrderByNome(String nomeBairro);

    List<PontoSeguro> findAllByOrderByNome();
}
