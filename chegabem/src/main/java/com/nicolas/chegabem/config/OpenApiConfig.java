package com.nicolas.chegabem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "bearer-jwt";

    @Bean
    public OpenAPI chegaBemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chega Bem API")
                        .version("1.0.0")
                        .description("""
                                API REST da CitizenTech Chega Bem: rota segura, acompanhamento
                                de trajeto com alerta de nao chegada, pontos seguros e relatos
                                anonimos, para a seguranca de mulheres no trajeto a pe pela
                                cidade de Sao Paulo.

                                ### Como autenticar
                                1. Cadastre-se em `POST /auth/register` (aberto, sem token).
                                2. Faca login em `POST /auth/login` e copie o campo `token`.
                                3. Clique em **Authorize**, cole o token e confirme.

                                Endpoints de leitura de pontos seguros, bairros e trechos sao
                                publicos e podem ser testados sem token.

                                FIAP - Global Solution - Domain Driven Design (Java).""")
                        .license(new License().name("Uso academico - FIAP")))
                .components(new Components().addSecuritySchemes(ESQUEMA,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token devolvido por POST /auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }
}
