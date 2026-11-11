package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.Usuaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuariaRepository extends JpaRepository<Usuaria, Long> {

    Optional<Usuaria> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
