package com.nicolas.chegabem.bo;

import com.nicolas.chegabem.config.Solicitante;
import com.nicolas.chegabem.dao.RelatoRepository;
import com.nicolas.chegabem.dao.TipoRelatoRepository;
import com.nicolas.chegabem.dao.TrechoRuaRepository;
import com.nicolas.chegabem.dto.RelatoRequest;
import com.nicolas.chegabem.dto.RelatoResponse;
import com.nicolas.chegabem.exception.RecursoNaoEncontradoException;
import com.nicolas.chegabem.model.Relato;
import com.nicolas.chegabem.model.TipoRelato;
import com.nicolas.chegabem.model.TrechoRua;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RelatoBO {

    private static final int RELATOS_PARA_RISCO_MAXIMO = 10;

    private static final BigDecimal PESO_ILUMINACAO = new BigDecimal("0.50");
    private static final BigDecimal PESO_RELATOS = new BigDecimal("0.50");

    private final RelatoRepository relatoRepository;
    private final TrechoRuaRepository trechoRepository;
    private final TipoRelatoRepository tipoRepository;
    private final UsuariaBO usuariaBO;

    public RelatoBO(RelatoRepository relatoRepository,
                    TrechoRuaRepository trechoRepository,
                    TipoRelatoRepository tipoRepository,
                    UsuariaBO usuariaBO) {
        this.relatoRepository = relatoRepository;
        this.trechoRepository = trechoRepository;
        this.tipoRepository = tipoRepository;
        this.usuariaBO = usuariaBO;
    }

    @Transactional
    public RelatoResponse registrar(RelatoRequest req, Solicitante solicitante) {
        TrechoRua trecho = trechoRepository.findById(req.idTrecho())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trecho de rua", req.idTrecho()));

        TipoRelato tipo = tipoRepository.findById(req.idTipo())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de relato", req.idTipo()));

        boolean anonimo = req.anonimoOuPadrao() || solicitante == null;

        Relato relato = new Relato();
        relato.setTrecho(trecho);
        relato.setTipo(tipo);
        relato.setDescricao(req.descricao().trim());
        relato.setAnonimo(anonimo);
        relato.setUsuaria(anonimo ? null : usuariaBO.buscarEntidade(solicitante.id()));

        Relato salvo = relatoRepository.save(relato);
        recalcularRisco(trecho);

        return RelatoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<RelatoResponse> listar(Long idTrecho) {
        List<Relato> relatos = (idTrecho == null)
                ? relatoRepository.findAllByOrderByDtRelatoDesc()
                : relatoRepository.findByTrechoIdOrderByDtRelatoDesc(idTrecho);

        return relatos.stream().map(RelatoResponse::de).toList();
    }

    @Transactional
    public void remover(Long idRelato) {
        Relato relato = relatoRepository.findById(idRelato)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Relato", idRelato));

        TrechoRua trecho = relato.getTrecho();
        relatoRepository.delete(relato);
        relatoRepository.flush();
        recalcularRisco(trecho);
    }

    private void recalcularRisco(TrechoRua trecho) {
        long qtdRelatos = relatoRepository.countByTrechoId(trecho.getId());

        BigDecimal parcelaIluminacao = BigDecimal.ONE
                .subtract(trecho.getNivelIluminacao())
                .multiply(PESO_ILUMINACAO);

        BigDecimal parcelaRelatos = BigDecimal
                .valueOf(Math.min(qtdRelatos, RELATOS_PARA_RISCO_MAXIMO))
                .divide(BigDecimal.valueOf(RELATOS_PARA_RISCO_MAXIMO), 4, RoundingMode.HALF_UP)
                .multiply(PESO_RELATOS);

        BigDecimal risco = parcelaIluminacao.add(parcelaRelatos)
                .min(BigDecimal.ONE)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        trecho.setNivelRisco(risco);
        trechoRepository.save(trecho);
    }
}
