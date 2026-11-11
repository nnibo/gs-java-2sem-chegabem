package com.nicolas.chegabem.bo;

import com.nicolas.chegabem.config.Solicitante;
import com.nicolas.chegabem.dao.AlertaRepository;
import com.nicolas.chegabem.dao.ContatoConfiancaRepository;
import com.nicolas.chegabem.dao.TrajetoRepository;
import com.nicolas.chegabem.dto.TrajetoRequest;
import com.nicolas.chegabem.dto.TrajetoResponse;
import com.nicolas.chegabem.exception.RecursoNaoEncontradoException;
import com.nicolas.chegabem.exception.RegraDeNegocioException;
import com.nicolas.chegabem.model.Alerta;
import com.nicolas.chegabem.model.ContatoConfianca;
import com.nicolas.chegabem.model.Trajeto;
import com.nicolas.chegabem.model.enums.StatusTrajeto;
import com.nicolas.chegabem.model.enums.TipoAlerta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrajetoBO {

    private static final Logger log = LoggerFactory.getLogger(TrajetoBO.class);

    private final TrajetoRepository trajetoRepository;
    private final ContatoConfiancaRepository contatoRepository;
    private final AlertaRepository alertaRepository;
    private final UsuariaBO usuariaBO;
    private final long toleranciaMinutos;

    public TrajetoBO(TrajetoRepository trajetoRepository,
                     ContatoConfiancaRepository contatoRepository,
                     AlertaRepository alertaRepository,
                     UsuariaBO usuariaBO,
                     @Value("${chegabem.trajeto.tolerancia-minutos}") long toleranciaMinutos) {
        this.trajetoRepository = trajetoRepository;
        this.contatoRepository = contatoRepository;
        this.alertaRepository = alertaRepository;
        this.usuariaBO = usuariaBO;
        this.toleranciaMinutos = toleranciaMinutos;
    }

    @Transactional
    public TrajetoResponse iniciar(TrajetoRequest req, Solicitante solicitante) {
        Long idUsuaria = solicitante.id();

        List<ContatoConfianca> contatos = contatoRepository.findByUsuariaIdOrderByNome(idUsuaria);
        if (contatos.isEmpty()) {
            throw new RegraDeNegocioException(
                    "Cadastre pelo menos um contato de confianca antes de iniciar um trajeto.");
        }

        if (trajetoRepository.existsByUsuariaIdAndStatus(idUsuaria, StatusTrajeto.EM_ANDAMENTO)) {
            throw new RegraDeNegocioException(
                    "Voce ja tem um trajeto em andamento. Confirme a chegada ou cancele antes de iniciar outro.");
        }

        LocalDateTime inicio = LocalDateTime.now();

        if (!req.dtPrevistaChegada().isAfter(inicio)) {
            throw new RegraDeNegocioException(
                    "O horario previsto de chegada precisa ser posterior ao inicio do trajeto.");
        }

        Trajeto trajeto = new Trajeto();
        trajeto.setUsuaria(usuariaBO.buscarEntidade(idUsuaria));
        trajeto.setOrigem(req.origem().trim());
        trajeto.setDestino(req.destino().trim());
        trajeto.setDtInicio(inicio);
        trajeto.setDtPrevistaChegada(req.dtPrevistaChegada());
        trajeto.setStatus(StatusTrajeto.EM_ANDAMENTO);

        Trajeto salvo = trajetoRepository.save(trajeto);
        notificar(salvo, contatos, TipoAlerta.INICIO_TRAJETO);

        return TrajetoResponse.de(salvo, contatos.size());
    }

    @Transactional
    public TrajetoResponse confirmarChegada(Long idTrajeto, Solicitante solicitante) {
        Trajeto trajeto = buscarComAcesso(idTrajeto, solicitante);

        if (trajeto.getStatus() == StatusTrajeto.CONCLUIDO) {
            throw new RegraDeNegocioException("A chegada deste trajeto ja foi confirmada.");
        }
        if (trajeto.getStatus() == StatusTrajeto.CANCELADO) {
            throw new RegraDeNegocioException("Este trajeto foi cancelado e nao pode ser confirmado.");
        }

        trajeto.setDtChegadaConfirmada(LocalDateTime.now());
        trajeto.setStatus(StatusTrajeto.CONCLUIDO);
        Trajeto salvo = trajetoRepository.save(trajeto);

        List<ContatoConfianca> contatos =
                contatoRepository.findByUsuariaIdOrderByNome(trajeto.getUsuaria().getId());
        notificar(salvo, contatos, TipoAlerta.CHEGADA_CONFIRMADA);

        return TrajetoResponse.de(salvo, contatos.size());
    }

    @Transactional
    public TrajetoResponse cancelar(Long idTrajeto, Solicitante solicitante) {
        Trajeto trajeto = buscarComAcesso(idTrajeto, solicitante);

        if (trajeto.getStatus() != StatusTrajeto.EM_ANDAMENTO) {
            throw new RegraDeNegocioException("Apenas um trajeto em andamento pode ser cancelado.");
        }

        trajeto.setStatus(StatusTrajeto.CANCELADO);
        Trajeto salvo = trajetoRepository.save(trajeto);
        return TrajetoResponse.de(salvo, 0);
    }

    @Transactional(readOnly = true)
    public TrajetoResponse consultar(Long idTrajeto, Solicitante solicitante) {
        Trajeto trajeto = buscarComAcesso(idTrajeto, solicitante);
        int contatos = (int) contatoRepository.countByUsuariaId(trajeto.getUsuaria().getId());
        return TrajetoResponse.de(trajeto, contatos);
    }

    @Transactional(readOnly = true)
    public List<TrajetoResponse> listarDaUsuaria(Solicitante solicitante) {
        int contatos = (int) contatoRepository.countByUsuariaId(solicitante.id());
        return trajetoRepository.findByUsuariaIdOrderByDtInicioDesc(solicitante.id())
                .stream().map(t -> TrajetoResponse.de(t, contatos)).toList();
    }

    @Transactional
    public int alertarNaoChegadas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(toleranciaMinutos);

        List<Trajeto> atrasados = trajetoRepository
                .findByStatusAndDtPrevistaChegadaBefore(StatusTrajeto.EM_ANDAMENTO, limite);

        for (Trajeto trajeto : atrasados) {
            if (alertaRepository.existsByTrajetoIdAndTipoAlerta(trajeto.getId(), TipoAlerta.NAO_CHEGADA)) {
                continue;
            }

            trajeto.setStatus(StatusTrajeto.ALERTA_DISPARADO);
            trajetoRepository.save(trajeto);

            List<ContatoConfianca> contatos =
                    contatoRepository.findByUsuariaIdOrderByNome(trajeto.getUsuaria().getId());
            notificar(trajeto, contatos, TipoAlerta.NAO_CHEGADA);

            log.warn("Alerta de nao chegada: trajeto {} da usuaria {} previsto para {}, {} contato(s) avisado(s).",
                    trajeto.getId(), trajeto.getUsuaria().getId(),
                    trajeto.getDtPrevistaChegada(), contatos.size());
        }

        return atrasados.size();
    }

    private void notificar(Trajeto trajeto, List<ContatoConfianca> contatos, TipoAlerta tipo) {
        for (ContatoConfianca contato : contatos) {
            Alerta alerta = new Alerta();
            alerta.setTrajeto(trajeto);
            alerta.setContato(contato);
            alerta.setTipoAlerta(tipo);
            alertaRepository.save(alerta);
        }
    }

    private Trajeto buscarComAcesso(Long idTrajeto, Solicitante solicitante) {
        Trajeto trajeto = trajetoRepository.findById(idTrajeto)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Trajeto", idTrajeto));

        if (!solicitante.podeAcessar(trajeto.getUsuaria().getId())) {
            throw new AccessDeniedException("Este trajeto pertence a outra usuaria.");
        }
        return trajeto;
    }
}
