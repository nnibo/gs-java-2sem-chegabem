package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.Bairro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BairroRepository extends JpaRepository<Bairro, Long> {

    Optional<Bairro> findByNomeIgnoreCase(String nome);

    List<Bairro> findByCidadeIgnoreCaseOrderByNome(String cidade);
}
