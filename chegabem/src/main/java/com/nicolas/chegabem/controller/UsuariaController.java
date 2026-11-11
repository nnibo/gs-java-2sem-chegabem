package com.nicolas.chegabem.controller;

import com.nicolas.chegabem.bo.UsuariaBO;
import com.nicolas.chegabem.config.Solicitante;
import com.nicolas.chegabem.dto.UsuariaResponse;
import com.nicolas.chegabem.dto.UsuariaUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarias")
@Tag(name = "2. Usuarias", description = "Gestao da conta ja cadastrada")
public class UsuariaController {

    private final UsuariaBO usuariaBO;

    public UsuariaController(UsuariaBO usuariaBO) {
        this.usuariaBO = usuariaBO;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma usuaria pelo id",
               description = "Cada usuaria acessa apenas os proprios dados; ADMIN acessa todas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invalido"),
            @ApiResponse(responseCode = "403", description = "Dados de outra usuaria"),
            @ApiResponse(responseCode = "404", description = "Usuaria inexistente")
    })
    public ResponseEntity<UsuariaResponse> buscar(@PathVariable Long id,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuariaBO.buscarPorId(id, Solicitante.de(jwt)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todas as usuarias (somente ADMIN)")
    public ResponseEntity<List<UsuariaResponse>> listar() {
        return ResponseEntity.ok(usuariaBO.listarTodas());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome e telefone",
               description = "E-mail e senha nao sao alterados por aqui.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizada"),
            @ApiResponse(responseCode = "403", description = "Dados de outra usuaria"),
            @ApiResponse(responseCode = "404", description = "Usuaria inexistente")
    })
    public ResponseEntity<UsuariaResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody UsuariaUpdateRequest req,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuariaBO.atualizar(id, req, Solicitante.de(jwt)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a conta",
               description = "Recusado com 409 enquanto houver trajetos ou contatos vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removida"),
            @ApiResponse(responseCode = "403", description = "Conta de outra usuaria"),
            @ApiResponse(responseCode = "404", description = "Usuaria inexistente"),
            @ApiResponse(responseCode = "409", description = "Possui historico vinculado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id,
                                        @AuthenticationPrincipal Jwt jwt) {
        usuariaBO.remover(id, Solicitante.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
