package com.nicolas.chegabem.bo;

import com.nicolas.chegabem.dao.BairroRepository;
import com.nicolas.chegabem.dao.PontoSeguroRepository;
import com.nicolas.chegabem.dto.PontoSeguroRequest;
import com.nicolas.chegabem.dto.PontoSeguroResponse;
import com.nicolas.chegabem.exception.RecursoNaoEncontradoException;
import com.nicolas.chegabem.model.Bairro;
import com.nicolas.chegabem.model.PontoSeguro;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class PontoSeguroBO {

    private final PontoSeguroRepository pontoRepository;
    private final BairroRepository bairroRepository;

    public PontoSeguroBO(PontoSeguroRepository pontoRepository, BairroRepository bairroRepository) {
        this.pontoRepository = pontoRepository;
        this.bairroRepository = bairroRepository;
    }

    @Transactional(readOnly = true)
    public List<PontoSeguroResponse> listar(String nomeBairro) {
        List<PontoSeguro> pontos = (nomeBairro == null || nomeBairro.isBlank())
                ? pontoRepository.findAllByOrderByNome()
                : pontoRepository.findByBairroNomeIgnoreCaseOrderByNome(nomeBairro.trim());

        LocalTime agora = LocalTime.now();
        return pontos.stream()
                .map(p -> PontoSeguroResponse.de(p, estaAberto(p, agora)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PontoSeguroResponse buscarPorId(Long id) {
        PontoSeguro ponto = buscarEntidade(id);
        return PontoSeguroResponse.de(ponto, estaAberto(ponto, LocalTime.now()));
    }

    @Transactional
    public PontoSeguroResponse cadastrar(PontoSeguroRequest req) {
        PontoSeguro ponto = new PontoSeguro();
        aplicar(ponto, req);
        PontoSeguro salvo = pontoRepository.save(ponto);
        return PontoSeguroResponse.de(salvo, estaAberto(salvo, LocalTime.now()));
    }

    @Transactional
    public PontoSeguroResponse atualizar(Long id, PontoSeguroRequest req) {
        PontoSeguro ponto = buscarEntidade(id);
        aplicar(ponto, req);
        PontoSeguro salvo = pontoRepository.save(ponto);
        return PontoSeguroResponse.de(salvo, estaAberto(salvo, LocalTime.now()));
    }

    @Transactional
    public void remover(Long id) {
        pontoRepository.delete(buscarEntidade(id));
    }

    boolean estaAberto(PontoSeguro ponto, LocalTime hora) {
        LocalTime abertura = ponto.getHrAbertura();
        LocalTime fechamento = ponto.getHrFechamento();

        if (abertura.equals(fechamento)) {
            return true;
        }
        if (fechamento.isAfter(abertura)) {
            return !hora.isBefore(abertura) && hora.isBefore(fechamento);
        }
        return !hora.isBefore(abertura) || hora.isBefore(fechamento);
    }

    private void aplicar(PontoSeguro ponto, PontoSeguroRequest req) {
        Bairro bairro = bairroRepository.findById(req.idBairro())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bairro", req.idBairro()));

        ponto.setBairro(bairro);
        ponto.setNome(req.nome().trim());
        ponto.setTipo(req.tipo());
        ponto.setHrAbertura(req.hrAbertura());
        ponto.setHrFechamento(req.hrFechamento());
        ponto.setLatitude(req.latitude());
        ponto.setLongitude(req.longitude());
    }

    private PontoSeguro buscarEntidade(Long id) {
        return pontoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto seguro", id));
    }
}
