package com.nicolas.chegabem.bo;

import com.nicolas.chegabem.config.Solicitante;
import com.nicolas.chegabem.dao.UsuariaRepository;
import com.nicolas.chegabem.dto.UsuariaRequest;
import com.nicolas.chegabem.dto.UsuariaResponse;
import com.nicolas.chegabem.dto.UsuariaUpdateRequest;
import com.nicolas.chegabem.exception.ConflitoException;
import com.nicolas.chegabem.exception.RecursoNaoEncontradoException;
import com.nicolas.chegabem.model.Usuaria;
import com.nicolas.chegabem.model.enums.PerfilUsuaria;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuariaBO {

    private final UsuariaRepository usuariaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuariaBO(UsuariaRepository usuariaRepository, PasswordEncoder passwordEncoder) {
        this.usuariaRepository = usuariaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuariaResponse cadastrar(UsuariaRequest req) {
        if (usuariaRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflitoException("Ja existe uma conta com o e-mail informado.");
        }

        Usuaria usuaria = new Usuaria();
        usuaria.setNome(req.nome().trim());
        usuaria.setEmail(req.email().trim().toLowerCase());
        usuaria.setSenhaHash(passwordEncoder.encode(req.senha()));
        usuaria.setTelefone(req.telefone());
        usuaria.setPerfil(PerfilUsuaria.USUARIA);

        return UsuariaResponse.de(usuariaRepository.save(usuaria));
    }

    @Transactional(readOnly = true)
    public UsuariaResponse buscarPorId(Long id, Solicitante solicitante) {
        garantirAcesso(id, solicitante);
        return UsuariaResponse.de(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<UsuariaResponse> listarTodas() {
        return usuariaRepository.findAll().stream().map(UsuariaResponse::de).toList();
    }

    @Transactional
    public UsuariaResponse atualizar(Long id, UsuariaUpdateRequest req, Solicitante solicitante) {
        garantirAcesso(id, solicitante);

        Usuaria usuaria = buscarEntidade(id);
        usuaria.setNome(req.nome().trim());
        usuaria.setTelefone(req.telefone());

        return UsuariaResponse.de(usuariaRepository.save(usuaria));
    }

    @Transactional
    public void remover(Long id, Solicitante solicitante) {
        garantirAcesso(id, solicitante);
        usuariaRepository.delete(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Usuaria buscarEntidade(Long id) {
        return usuariaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuaria", id));
    }

    private void garantirAcesso(Long idAlvo, Solicitante solicitante) {
        if (!solicitante.podeAcessar(idAlvo)) {
            throw new AccessDeniedException("Voce so pode acessar os seus proprios dados.");
        }
    }
}
