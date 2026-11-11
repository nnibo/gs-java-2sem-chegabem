package com.nicolas.chegabem.controller;

import com.nicolas.chegabem.bo.RelatoBO;
import com.nicolas.chegabem.config.Solicitante;
import com.nicolas.chegabem.dto.RelatoRequest;
import com.nicolas.chegabem.dto.RelatoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/relatos")
@Tag(name = "6. Relatos",
     description = "Registro de assedio, iluminacao apagada e rua deserta por trecho")
public class RelatoController {

    private final RelatoBO relatoBO;

    public RelatoController(RelatoBO relatoBO) {
        this.relatoBO = relatoBO;
    }

    @PostMapping
    @Operation(summary = "Registra um relato",
               description = "Com anonimo = true (o padrao), a autoria nao e gravada em "
                           + "lugar nenhum: a coluna ID_USUARIA fica NULL e nao ha como "
                           + "descobrir quem relatou, nem pela moderacao. O login e exigido "
                           + "apenas para conter abuso, nao para identificar a autora.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Relato registrado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Trecho ou tipo inexistente")
    })
    public ResponseEntity<RelatoResponse> registrar(@Valid @RequestBody RelatoRequest req,
                                                    @AuthenticationPrincipal Jwt jwt) {
        RelatoResponse criado = relatoBO.registrar(req, Solicitante.de(jwt));
        return ResponseEntity.created(URI.create("/relatos/" + criado.id())).body(criado);
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Lista relatos, opcionalmente filtrando por trecho",
               description = "Endpoint publico: os relatos existem para alertar outras pessoas.")
    public ResponseEntity<List<RelatoResponse>> listar(
            @Parameter(description = "Id do trecho de rua. Consulte em GET /trechos")
            @RequestParam(name = "trecho", required = false) Long idTrecho) {
        return ResponseEntity.ok(relatoBO.listar(idTrecho));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um relato (moderacao, somente ADMIN)",
               description = "Apos a remocao, o nivel de risco do trecho e recalculado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Relato removido"),
            @ApiResponse(responseCode = "403", description = "Exige perfil ADMIN"),
            @ApiResponse(responseCode = "404", description = "Relato inexistente")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        relatoBO.remover(id);
        return ResponseEntity.noContent().build();
    }
}
