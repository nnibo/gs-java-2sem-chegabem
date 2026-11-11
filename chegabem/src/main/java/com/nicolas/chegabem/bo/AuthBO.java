package com.nicolas.chegabem.bo;

import com.nicolas.chegabem.dao.UsuariaRepository;
import com.nicolas.chegabem.dto.LoginRequest;
import com.nicolas.chegabem.dto.LoginResponse;
import com.nicolas.chegabem.model.Usuaria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthBO {

    private static final long SEGUNDOS_POR_DIA = 86_400L;

    private final UsuariaRepository usuariaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long expiracaoSegundos;

    public AuthBO(UsuariaRepository usuariaRepository,
                  PasswordEncoder passwordEncoder,
                  JwtEncoder jwtEncoder,
                  @Value("${jwt.expiration-days}") long expiracaoDias) {
        this.usuariaRepository = usuariaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expiracaoSegundos = expiracaoDias * SEGUNDOS_POR_DIA;
    }

    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest req) {
        Usuaria usuaria = usuariaRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais invalidas."));

        if (!passwordEncoder.matches(req.senha(), usuaria.getSenhaHash())) {
            throw new BadCredentialsException("Credenciais invalidas.");
        }

        return LoginResponse.de(gerarToken(usuaria), expiracaoSegundos,
                usuaria.getId(), usuaria.getNome(), usuaria.getPerfil());
    }

    private String gerarToken(Usuaria usuaria) {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("chega-bem-api")
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoSegundos, ChronoUnit.SECONDS))
                .subject(String.valueOf(usuaria.getId()))
                .claim("nome", usuaria.getNome())
                .claim("perfil", usuaria.getPerfil().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
