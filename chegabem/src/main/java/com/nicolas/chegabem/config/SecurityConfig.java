package com.nicolas.chegabem.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationConverter conversor) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/trechos/criticos").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/pontos-seguros",
                                "/pontos-seguros/**",
                                "/bairros",
                                "/bairros/**",
                                "/trechos",
                                "/trechos/**",
                                "/relatos",
                                "/relatos/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversor)));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(@Value("${jwt.key}") String segredo) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave(segredo)));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.key}") String segredo) {
        return NimbusJwtDecoder.withSecretKey(chave(segredo))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("perfil");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(authorities);
        return conversor;
    }

    private SecretKey chave(String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "A variavel de ambiente JWT_KEY precisa ter no minimo 32 caracteres "
                    + "para HS256. Tamanho atual: " + bytes.length);
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
