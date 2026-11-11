package com.nicolas.chegabem.bo;

import com.nicolas.chegabem.config.Solicitante;
import com.nicolas.chegabem.dao.ContatoConfiancaRepository;
import com.nicolas.chegabem.dto.ContatoRequest;
import com.nicolas.chegabem.dto.ContatoResponse;
import com.nicolas.chegabem.exception.RecursoNaoEncontradoException;
import com.nicolas.chegabem.exception.RegraDeNegocioException;
import com.nicolas.chegabem.model.ContatoConfianca;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContatoConfiancaBO {

    private static final int MAXIMO_POR_USUARIA = 5;

    private final ContatoConfiancaRepository contatoRepository;
    private final UsuariaBO usuariaBO;

    public ContatoConfiancaBO(ContatoConfiancaRepository contatoRepository, UsuariaBO usuariaBO) {
        this.contatoRepository = contatoRepository;
        this.usuariaBO = usuariaBO;
    }

    @Transactional
    public ContatoResponse cadastrar(Long idUsuaria, ContatoRequest req, Solicitante solicitante) {
        garantirAcesso(idUsuaria, solicitante);

        if (contatoRepository.countByUsuariaId(idUsuaria) >= MAXIMO_POR_USUARIA) {
            throw new RegraDeNegocioException(
                    "Limite de " + MAXIMO_POR_USUARIA + " contatos de confianca atingido.");
        }

        ContatoConfianca contato = new ContatoConfianca();
        contato.setUsuaria(usuariaBO.buscarEntidade(idUsuaria));
        contato.setNome(req.nome().trim());
        contato.setTelefone(req.telefone());
        contato.setParentesco(req.parentesco() == null ? null : req.parentesco().trim());

        return ContatoResponse.de(contatoRepository.save(contato));
    }

    @Transactional(readOnly = true)
    public List<ContatoResponse> listarPorUsuaria(Long idUsuaria, Solicitante solicitante) {
        garantirAcesso(idUsuaria, solicitante);
        return contatoRepository.findByUsuariaIdOrderByNome(idUsuaria)
                .stream().map(ContatoResponse::de).toList();
    }

    @Transactional
    public void remover(Long idUsuaria, Long idContato, Solicitante solicitante) {
        garantirAcesso(idUsuaria, solicitante);

        ContatoConfianca contato = contatoRepository.findById(idContato)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contato de confianca", idContato));

        if (!contato.getUsuaria().getId().equals(idUsuaria)) {
            throw new RecursoNaoEncontradoException(
                    "Contato de confianca de id " + idContato + " nao pertence a esta usuaria.");
        }

        contatoRepository.delete(contato);
    }

    private void garantirAcesso(Long idUsuaria, Solicitante solicitante) {
        if (!solicitante.podeAcessar(idUsuaria)) {
            throw new AccessDeniedException("Voce so pode gerenciar os seus proprios contatos.");
        }
    }
}
