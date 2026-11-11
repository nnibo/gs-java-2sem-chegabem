package com.nicolas.chegabem.controller;

import com.nicolas.chegabem.bo.AuthBO;
import com.nicolas.chegabem.bo.UsuariaBO;
import com.nicolas.chegabem.dto.LoginRequest;
import com.nicolas.chegabem.dto.LoginResponse;
import com.nicolas.chegabem.dto.UsuariaRequest;
import com.nicolas.chegabem.dto.UsuariaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@Tag(name = "1. Autenticacao", description = "Cadastro, login e emissao do token JWT")
public class AuthController {

    private final AuthBO authBO;
    private final UsuariaBO usuariaBO;

    public AuthController(AuthBO authBO, UsuariaBO usuariaBO) {
        this.authBO = authBO;
        this.usuariaBO = usuariaBO;
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Cadastra uma usuaria",
               description = "Endpoint aberto: e por aqui que se cria a primeira conta. "
                           + "O perfil atribuido e sempre USUARIA — nao e possivel se "
                           + "cadastrar como ADMIN pela API.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado")
    })
    public ResponseEntity<UsuariaResponse> registrar(@Valid @RequestBody UsuariaRequest req) {
        UsuariaResponse criada = usuariaBO.cadastrar(req);
        return ResponseEntity.created(URI.create("/usuarias/" + criada.id())).body(criada);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica e devolve o token JWT",
               description = "Use o token devolvido no botao Authorize do Swagger, "
                           + "ou no header Authorization: Bearer <token>.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticada com sucesso"),
            @ApiResponse(responseCode = "400", description = "E-mail ou senha ausentes/invalidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authBO.autenticar(req));
    }
}
