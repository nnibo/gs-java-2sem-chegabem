package com.nicolas.chegabem.controller;

import com.nicolas.chegabem.bo.ContatoConfiancaBO;
import com.nicolas.chegabem.config.Solicitante;
import com.nicolas.chegabem.dto.ContatoRequest;
import com.nicolas.chegabem.dto.ContatoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuarias/{idUsuaria}/contatos")
@Tag(name = "3. Contatos de confianca",
     description = "Quem e avisado quando a usuaria sai e quando nao chega")
public class ContatoConfiancaController {

    private final ContatoConfiancaBO contatoBO;

    public ContatoConfiancaController(ContatoConfiancaBO contatoBO) {
        this.contatoBO = contatoBO;
    }

    @PostMapping
    @Operation(summary = "Cadastra um contato de confianca",
               description = "Maximo de 5 contatos por usuaria.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou limite atingido"),
            @ApiResponse(responseCode = "403", description = "Contatos de outra usuaria")
    })
    public ResponseEntity<ContatoResponse> cadastrar(@PathVariable Long idUsuaria,
                                                     @Valid @RequestBody ContatoRequest req,
                                                     @AuthenticationPrincipal Jwt jwt) {
        ContatoResponse criado = contatoBO.cadastrar(idUsuaria, req, Solicitante.de(jwt));
        return ResponseEntity
                .created(URI.create("/usuarias/" + idUsuaria + "/contatos/" + criado.id()))
                .body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista os contatos de confianca da usuaria")
    public ResponseEntity<List<ContatoResponse>> listar(@PathVariable Long idUsuaria,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(contatoBO.listarPorUsuaria(idUsuaria, Solicitante.de(jwt)));
    }

    @DeleteMapping("/{idContato}")
    @Operation(summary = "Remove um contato de confianca")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido"),
            @ApiResponse(responseCode = "403", description = "Contatos de outra usuaria"),
            @ApiResponse(responseCode = "404", description = "Contato inexistente")
    })
    public ResponseEntity<Void> remover(@PathVariable Long idUsuaria,
                                        @PathVariable Long idContato,
                                        @AuthenticationPrincipal Jwt jwt) {
        contatoBO.remover(idUsuaria, idContato, Solicitante.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
