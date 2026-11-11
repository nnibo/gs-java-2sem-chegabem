package com.nicolas.chegabem.dao;

import com.nicolas.chegabem.model.Trajeto;
import com.nicolas.chegabem.model.enums.StatusTrajeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TrajetoRepository extends JpaRepository<Trajeto, Long> {

    List<Trajeto> findByUsuariaIdOrderByDtInicioDesc(Long idUsuaria);

    boolean existsByUsuariaIdAndStatus(Long idUsuaria, StatusTrajeto status);

    List<Trajeto> findByStatusAndDtPrevistaChegadaBefore(StatusTrajeto status,
                                                         LocalDateTime limite);
}
