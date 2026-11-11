package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.Alerta;
import com.nicolas.chegabem.model.enums.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByTrajetoIdOrderByDtEnvio(Long idTrajeto);

    boolean existsByTrajetoIdAndTipoAlerta(Long idTrajeto, TipoAlerta tipoAlerta);
}
