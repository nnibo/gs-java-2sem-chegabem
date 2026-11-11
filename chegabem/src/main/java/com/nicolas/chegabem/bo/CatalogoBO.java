package com.nicolas.chegabem.bo;

import com.nicolas.chegabem.dao.BairroRepository;
import com.nicolas.chegabem.dao.RelatoRepository;
import com.nicolas.chegabem.dao.TrechoRuaRepository;
import com.nicolas.chegabem.dto.BairroResponse;
import com.nicolas.chegabem.dto.TrechoRuaResponse;
import com.nicolas.chegabem.model.TrechoRua;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoBO {

    private final BairroRepository bairroRepository;
    private final TrechoRuaRepository trechoRepository;
    private final RelatoRepository relatoRepository;

    public CatalogoBO(BairroRepository bairroRepository,
                      TrechoRuaRepository trechoRepository,
                      RelatoRepository relatoRepository) {
        this.bairroRepository = bairroRepository;
        this.trechoRepository = trechoRepository;
        this.relatoRepository = relatoRepository;
    }

    @Transactional(readOnly = true)
    public List<BairroResponse> listarBairros(String cidade) {
        var bairros = (cidade == null || cidade.isBlank())
                ? bairroRepository.findAll()
                : bairroRepository.findByCidadeIgnoreCaseOrderByNome(cidade.trim());

        return bairros.stream().map(BairroResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<TrechoRuaResponse> listarTrechos(Long idBairro) {
        List<TrechoRua> trechos = (idBairro == null)
                ? trechoRepository.findAll()
                : trechoRepository.findByBairroIdOrderByNomeRua(idBairro);

        return trechos.stream().map(this::comContagem).toList();
    }

    @Transactional(readOnly = true)
    public List<TrechoRuaResponse> listarTrechosCriticos() {
        return trechoRepository.findTop10ByOrderByNivelRiscoDesc()
                .stream().map(this::comContagem).toList();
    }

    private TrechoRuaResponse comContagem(TrechoRua trecho) {
        return TrechoRuaResponse.de(trecho, relatoRepository.countByTrechoId(trecho.getId()));
    }
}
