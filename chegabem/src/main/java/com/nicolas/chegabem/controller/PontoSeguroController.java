package com.nicolas.chegabem.controller;

import com.nicolas.chegabem.bo.PontoSeguroBO;
import com.nicolas.chegabem.dto.PontoSeguroRequest;
import com.nicolas.chegabem.dto.PontoSeguroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pontos-seguros")
@Tag(name = "4. Pontos seguros",
     description = "Comercios e locais de apoio no caminho, com horario de funcionamento")
public class PontoSeguroController {

    private final PontoSeguroBO pontoSeguroBO;

    public PontoSeguroController(PontoSeguroBO pontoSeguroBO) {
        this.pontoSeguroBO = pontoSeguroBO;
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Lista pontos seguros, opcionalmente filtrando por bairro",
               description = "Endpoint publico. Cada item traz abertoAgora ja calculado, "
                           + "levando em conta estabelecimentos que atravessam a meia-noite.")
    public ResponseEntity<List<PontoSeguroResponse>> listar(
            @Parameter(description = "Nome do bairro. Ex.: Capao Redondo")
            @RequestParam(required = false) String bairro) {
        return ResponseEntity.ok(pontoSeguroBO.listar(bairro));
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Busca um ponto seguro pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "Ponto seguro inexistente")
    })
    public ResponseEntity<PontoSeguroResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(pontoSeguroBO.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um ponto seguro",
               description = "Usado pelo comercio parceiro. Horarios no formato HH:mm.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "404", description = "Bairro inexistente")
    })
    public ResponseEntity<PontoSeguroResponse> cadastrar(@Valid @RequestBody PontoSeguroRequest req) {
        PontoSeguroResponse criado = pontoSeguroBO.cadastrar(req);
        return ResponseEntity.created(URI.create("/pontos-seguros/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados e horario de um ponto seguro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado"),
            @ApiResponse(responseCode = "404", description = "Ponto seguro ou bairro inexistente")
    })
    public ResponseEntity<PontoSeguroResponse> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody PontoSeguroRequest req) {
        return ResponseEntity.ok(pontoSeguroBO.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um ponto seguro")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido"),
            @ApiResponse(responseCode = "404", description = "Ponto seguro inexistente")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        pontoSeguroBO.remover(id);
        return ResponseEntity.noContent().build();
    }
}
