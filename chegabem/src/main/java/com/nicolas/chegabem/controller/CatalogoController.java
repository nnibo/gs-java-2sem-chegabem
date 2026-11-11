package com.nicolas.chegabem.controller;

import com.nicolas.chegabem.bo.CatalogoBO;
import com.nicolas.chegabem.dto.BairroResponse;
import com.nicolas.chegabem.dto.TrechoRuaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@Tag(name = "7. Bairros e trechos",
     description = "Dados de referencia e painel de trechos criticos")
public class CatalogoController {

    private final CatalogoBO catalogoBO;

    public CatalogoController(CatalogoBO catalogoBO) {
        this.catalogoBO = catalogoBO;
    }

    @GetMapping("/bairros")
    @SecurityRequirements
    @Operation(summary = "Lista os bairros atendidos")
    public ResponseEntity<List<BairroResponse>> listarBairros(
            @Parameter(description = "Filtra por cidade. Ex.: Sao Paulo")
            @RequestParam(required = false) String cidade) {
        return ResponseEntity.ok(catalogoBO.listarBairros(cidade));
    }

    @GetMapping("/trechos")
    @SecurityRequirements
    @Operation(summary = "Lista os trechos de rua",
               description = "Traz nivelIluminacao e nivelRisco na escala 0.00 a 1.00, que e "
                           + "a entrada do calculo de rota segura.")
    public ResponseEntity<List<TrechoRuaResponse>> listarTrechos(
            @Parameter(description = "Id do bairro. Consulte em GET /bairros")
            @RequestParam(name = "bairro", required = false) Long idBairro) {
        return ResponseEntity.ok(catalogoBO.listarTrechos(idBairro));
    }

    @GetMapping("/trechos/criticos")
    @Operation(summary = "Painel dos 10 trechos de maior risco",
               description = "Caso de uso do agente publico: priorizar troca de iluminacao. "
                           + "Exige autenticacao para nao expor um ranking de 'ruas perigosas' "
                           + "publicamente, o que estigmatizaria bairros.")
    public ResponseEntity<List<TrechoRuaResponse>> listarTrechosCriticos() {
        return ResponseEntity.ok(catalogoBO.listarTrechosCriticos());
    }
}
