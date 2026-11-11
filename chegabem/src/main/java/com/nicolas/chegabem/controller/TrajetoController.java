package com.nicolas.chegabem.controller;

import com.nicolas.chegabem.bo.TrajetoBO;
import com.nicolas.chegabem.config.Solicitante;
import com.nicolas.chegabem.dto.TrajetoRequest;
import com.nicolas.chegabem.dto.TrajetoResponse;
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
@RequestMapping("/trajetos")
@Tag(name = "5. Trajetos (Chega Bem)",
     description = "Acompanhamento da caminhada, da saida ate a confirmacao de chegada")
public class TrajetoController {

    private final TrajetoBO trajetoBO;

    public TrajetoController(TrajetoBO trajetoBO) {
        this.trajetoBO = trajetoBO;
    }

    @PostMapping
    @Operation(summary = "Inicia um trajeto e avisa os contatos de confianca",
               description = "Exige pelo menos 1 contato de confianca cadastrado e nenhum "
                           + "outro trajeto em andamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trajeto iniciado"),
            @ApiResponse(responseCode = "400",
                         description = "Sem contato de confianca, trajeto ja em andamento, "
                                     + "ou horario previsto no passado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invalido")
    })
    public ResponseEntity<TrajetoResponse> iniciar(@Valid @RequestBody TrajetoRequest req,
                                                   @AuthenticationPrincipal Jwt jwt) {
        TrajetoResponse criado = trajetoBO.iniciar(req, Solicitante.de(jwt));
        return ResponseEntity.created(URI.create("/trajetos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}/chegada")
    @Operation(summary = "Confirma a chegada",
               description = "E o 'Cheguei bem'. Encerra o acompanhamento e avisa os contatos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chegada confirmada"),
            @ApiResponse(responseCode = "400", description = "Trajeto ja concluido ou cancelado"),
            @ApiResponse(responseCode = "403", description = "Trajeto de outra usuaria"),
            @ApiResponse(responseCode = "404", description = "Trajeto inexistente")
    })
    public ResponseEntity<TrajetoResponse> confirmarChegada(@PathVariable Long id,
                                                            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(trajetoBO.confirmarChegada(id, Solicitante.de(jwt)));
    }

    @PutMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela um trajeto em andamento",
               description = "Para quando a usuaria desiste do caminho. Nao gera alerta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trajeto cancelado"),
            @ApiResponse(responseCode = "400", description = "Trajeto nao esta em andamento")
    })
    public ResponseEntity<TrajetoResponse> cancelar(@PathVariable Long id,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(trajetoBO.cancelar(id, Solicitante.de(jwt)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta o status de um trajeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "403", description = "Trajeto de outra usuaria"),
            @ApiResponse(responseCode = "404", description = "Trajeto inexistente")
    })
    public ResponseEntity<TrajetoResponse> consultar(@PathVariable Long id,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(trajetoBO.consultar(id, Solicitante.de(jwt)));
    }

    @GetMapping
    @Operation(summary = "Lista os trajetos da usuaria autenticada",
               description = "Do mais recente para o mais antigo.")
    public ResponseEntity<List<TrajetoResponse>> listarMeus(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(trajetoBO.listarDaUsuaria(Solicitante.de(jwt)));
    }
}
