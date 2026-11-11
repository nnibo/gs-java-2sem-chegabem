package com.nicolas.chegabem.config;

import com.nicolas.chegabem.dao.*;
import com.nicolas.chegabem.model.*;
import com.nicolas.chegabem.model.enums.PerfilUsuaria;
import com.nicolas.chegabem.model.enums.TipoPontoSeguro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "chegabem.seed.habilitado", havingValue = "true")
public class CargaInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);
    private static final String CIDADE = "Sao Paulo";

    private final BairroRepository bairroRepository;
    private final TrechoRuaRepository trechoRepository;
    private final TipoRelatoRepository tipoRelatoRepository;
    private final PontoSeguroRepository pontoRepository;
    private final UsuariaRepository usuariaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminSenha;

    public CargaInicial(BairroRepository bairroRepository,
                        TrechoRuaRepository trechoRepository,
                        TipoRelatoRepository tipoRelatoRepository,
                        PontoSeguroRepository pontoRepository,
                        UsuariaRepository usuariaRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${chegabem.seed.admin-email}") String adminEmail,
                        @Value("${chegabem.seed.admin-senha}") String adminSenha) {
        this.bairroRepository = bairroRepository;
        this.trechoRepository = trechoRepository;
        this.tipoRelatoRepository = tipoRelatoRepository;
        this.pontoRepository = pontoRepository;
        this.usuariaRepository = usuariaRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        criarTiposDeRelato();
        criarAdministrador();

        if (bairroRepository.count() > 0) {
            log.info("Carga inicial: bairros ja existem, nada a fazer.");
            return;
        }

        Bairro capaoRedondo = salvarBairro("Capao Redondo");
        Bairro brasilandia = salvarBairro("Brasilandia");
        Bairro grajau = salvarBairro("Grajau");
        salvarBairro("Cidade Tiradentes");

        salvarTrecho(capaoRedondo, "Rua das Acacias", "0.30");
        salvarTrecho(capaoRedondo, "Travessa do Mirante", "0.10");
        salvarTrecho(capaoRedondo, "Avenida Central", "0.85");
        salvarTrecho(brasilandia, "Rua do Horto", "0.25");
        salvarTrecho(brasilandia, "Ladeira Sao Jorge", "0.45");
        salvarTrecho(grajau, "Rua das Palmeiras", "0.70");
        salvarTrecho(grajau, "Viela do Corrego", "0.05");

        salvarPonto(capaoRedondo, "Padaria Pao Quente", TipoPontoSeguro.PADARIA,
                "06:00", "00:00", "-23.661400", "-46.776900");
        salvarPonto(capaoRedondo, "Drogaria Bem Estar", TipoPontoSeguro.FARMACIA,
                "08:00", "22:00", "-23.659800", "-46.774200");
        salvarPonto(brasilandia, "Posto Avenida 24h", TipoPontoSeguro.POSTO_COMBUSTIVEL,
                "00:00", "00:00", "-23.459100", "-46.682300");
        salvarPonto(grajau, "Mercado do Bairro", TipoPontoSeguro.MERCADO,
                "07:00", "23:00", "-23.776500", "-46.690100");

        log.info("Carga inicial concluida: {} bairro(s), {} trecho(s), {} ponto(s) seguro(s).",
                bairroRepository.count(), trechoRepository.count(), pontoRepository.count());
    }

    private void criarTiposDeRelato() {
        if (tipoRelatoRepository.count() > 0) {
            return;
        }
        List.of("Assedio", "Iluminacao apagada", "Rua deserta",
                "Terreno baldio", "Calcada intransitavel")
                .forEach(descricao -> {
                    TipoRelato tipo = new TipoRelato();
                    tipo.setDescricao(descricao);
                    tipoRelatoRepository.save(tipo);
                });
    }

    private void criarAdministrador() {
        if (usuariaRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
        Usuaria admin = new Usuaria();
        admin.setNome("Administracao Chega Bem");
        admin.setEmail(adminEmail.toLowerCase());
        admin.setSenhaHash(passwordEncoder.encode(adminSenha));
        admin.setTelefone("1100000000");
        admin.setPerfil(PerfilUsuaria.ADMIN);
        usuariaRepository.save(admin);

        log.warn("Conta ADMIN criada para {}. Troque a senha padrao antes de expor a API.", adminEmail);
    }

    private Bairro salvarBairro(String nome) {
        Bairro bairro = new Bairro();
        bairro.setNome(nome);
        bairro.setCidade(CIDADE);
        return bairroRepository.save(bairro);
    }

    private void salvarTrecho(Bairro bairro, String nomeRua, String iluminacao) {
        BigDecimal nivelIluminacao = new BigDecimal(iluminacao);

        TrechoRua trecho = new TrechoRua();
        trecho.setBairro(bairro);
        trecho.setNomeRua(nomeRua);
        trecho.setNivelIluminacao(nivelIluminacao);
        trecho.setNivelRisco(BigDecimal.ONE.subtract(nivelIluminacao)
                .multiply(new BigDecimal("0.50"))
                .setScale(2, java.math.RoundingMode.HALF_UP));
        trechoRepository.save(trecho);
    }

    private void salvarPonto(Bairro bairro, String nome, TipoPontoSeguro tipo,
                             String abertura, String fechamento,
                             String latitude, String longitude) {
        PontoSeguro ponto = new PontoSeguro();
        ponto.setBairro(bairro);
        ponto.setNome(nome);
        ponto.setTipo(tipo);
        ponto.setHrAbertura(LocalTime.parse(abertura));
        ponto.setHrFechamento(LocalTime.parse(fechamento));
        ponto.setLatitude(new BigDecimal(latitude));
        ponto.setLongitude(new BigDecimal(longitude));
        pontoRepository.save(ponto);
    }
}
