package com.nicolas.chegabem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChegaBemIntegracaoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Test
    @DisplayName("Fluxo Chega Bem: cadastro, login, contato, trajeto e confirmacao de chegada")
    void fluxoCompletoDoMvp() throws Exception {
        String email = "camila" + System.nanoTime() + "@teste.com";
        long idUsuaria = cadastrar(email, "Camila Souza");
        String token = logar(email);

        mvc.perform(post("/usuarias/{id}/contatos", idUsuaria)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria (mae)","telefone":"11988887777","parentesco":"mae"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Maria (mae)"));

        String previsto = LocalDateTime.now().plusMinutes(20)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        MvcResult inicio = mvc.perform(post("/trajetos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"origem":"Estacao Capao Redondo","destino":"Rua das Acacias, 100",
                                 "dtPrevistaChegada":"%s"}""".formatted(previsto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.contatosAcompanhando").value(1))
                .andReturn();

        long idTrajeto = json(inicio).get("id").asLong();

        mvc.perform(put("/trajetos/{id}/chegada", idTrajeto)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.dtChegadaConfirmada").isNotEmpty());

        mvc.perform(put("/trajetos/{id}/chegada", idTrajeto)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A chegada deste trajeto ja foi confirmada."));
    }

    @Test
    @DisplayName("Regra 1: trajeto sem contato de confianca e recusado")
    void trajetoSemContatoDeConfiancaERecusado() throws Exception {
        String email = "rosa" + System.nanoTime() + "@teste.com";
        cadastrar(email, "Rosa Lima");
        String token = logar(email);

        String previsto = LocalDateTime.now().plusMinutes(15)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mvc.perform(post("/trajetos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"origem":"Terminal","destino":"Casa","dtPrevistaChegada":"%s"}"""
                                .formatted(previsto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem")
                        .value("Cadastre pelo menos um contato de confianca antes de iniciar um trajeto."));
    }

    @Test
    @DisplayName("Regra 2: horario previsto de chegada no passado e recusado")
    void horarioPrevistoNoPassadoERecusado() throws Exception {
        String email = "ana" + System.nanoTime() + "@teste.com";
        cadastrar(email, "Ana Paula");
        String token = logar(email);

        String passado = LocalDateTime.now().minusHours(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mvc.perform(post("/trajetos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"origem":"Terminal","destino":"Casa","dtPrevistaChegada":"%s"}"""
                                .formatted(passado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.dtPrevistaChegada").isNotEmpty());
    }

    @Test
    @DisplayName("Relato anonimo nao grava autoria e eleva o risco do trecho")
    void relatoAnonimoNaoGravaAutoriaEElevaRisco() throws Exception {
        String email = "bia" + System.nanoTime() + "@teste.com";
        cadastrar(email, "Beatriz Alves");
        String token = logar(email);

        long idTrecho = primeiroId("/trechos");
        long idTipo = 1L;

        double riscoAntes = riscoDoTrecho(idTrecho);

        mvc.perform(post("/relatos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idTrecho":%d,"idTipo":%d,"descricao":"Poste apagado na esquina","anonimo":true}"""
                                .formatted(idTrecho, idTipo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.anonimo").value(true))
                .andExpect(jsonPath("$.autora").doesNotExist());

        assertThat(riscoDoTrecho(idTrecho))
                .as("o relato precisa elevar o nivel de risco do trecho")
                .isGreaterThan(riscoAntes);
    }

    @Test
    @DisplayName("Relato identificado preserva a autoria")
    void relatoIdentificadoPreservaAutoria() throws Exception {
        String email = "lu" + System.nanoTime() + "@teste.com";
        cadastrar(email, "Luciana Dias");
        String token = logar(email);

        long idTrecho = primeiroId("/trechos");

        mvc.perform(post("/relatos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idTrecho":%d,"idTipo":1,"descricao":"Calcada quebrada","anonimo":false}"""
                                .formatted(idTrecho)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.anonimo").value(false))
                .andExpect(jsonPath("$.autora").value("Luciana Dias"));
    }

    @Test
    @DisplayName("Endpoints publicos respondem sem token")
    void endpointsPublicosRespondemSemToken() throws Exception {
        mvc.perform(get("/pontos-seguros")).andExpect(status().isOk());
        mvc.perform(get("/bairros")).andExpect(status().isOk());
        mvc.perform(get("/trechos")).andExpect(status().isOk());
        mvc.perform(get("/relatos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoint protegido sem token devolve 401")
    void endpointProtegidoSemTokenDevolve401() throws Exception {
        mvc.perform(get("/trajetos")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Painel de trechos criticos nao e publico")
    void painelDeTrechosCriticosNaoEPublico() throws Exception {
        mvc.perform(get("/trechos/criticos")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Uma usuaria nao acessa os dados de outra")
    void usuariaNaoAcessaDadosDeOutra() throws Exception {
        String emailA = "a" + System.nanoTime() + "@teste.com";
        String emailB = "b" + System.nanoTime() + "@teste.com";
        cadastrar(emailA, "Usuaria A");
        long idB = cadastrar(emailB, "Usuaria B");

        String tokenA = logar(emailA);

        mvc.perform(get("/usuarias/{id}", idB).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Moderacao de relato exige perfil ADMIN")
    void moderacaoDeRelatoExigeAdmin() throws Exception {
        String email = "comum" + System.nanoTime() + "@teste.com";
        cadastrar(email, "Usuaria Comum");
        String tokenComum = logar(email);

        long idTrecho = primeiroId("/trechos");
        MvcResult criado = mvc.perform(post("/relatos")
                        .header("Authorization", "Bearer " + tokenComum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idTrecho":%d,"idTipo":1,"descricao":"Teste de moderacao"}"""
                                .formatted(idTrecho)))
                .andExpect(status().isCreated())
                .andReturn();

        long idRelato = json(criado).get("id").asLong();

        mvc.perform(delete("/relatos/{id}", idRelato)
                        .header("Authorization", "Bearer " + tokenComum))
                .andExpect(status().isForbidden());

        String tokenAdmin = logar("admin@teste.local", "AdminTeste@2026");
        mvc.perform(delete("/relatos/{id}", idRelato)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Cadastro ignora campos extras e nao permite escalar para ADMIN")
    void cadastroIgnoraCamposExtras() throws Exception {
        String email = "extra" + System.nanoTime() + "@teste.com";

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Tentativa de Escalada","email":"%s",
                                 "senha":"SenhaForte123","telefone":"11999998888",
                                 "id":999,"perfil":"ADMIN","dtCadastro":"2020-01-01T00:00:00"}"""
                                .formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("USUARIA"))
                .andExpect(jsonPath("$.id").value(not(999)));

        String token = logar(email);
        mvc.perform(get("/usuarias").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Login com senha errada devolve 401 sem revelar se o e-mail existe")
    void loginComSenhaErradaDevolve401() throws Exception {
        String email = "x" + System.nanoTime() + "@teste.com";
        cadastrar(email, "Usuaria X");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"SenhaErrada123"}""".formatted(email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais invalidas."));

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"naoexiste@teste.com","senha":"SenhaErrada123"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais invalidas."));
    }

    @Test
    @DisplayName("E-mail duplicado devolve 409")
    void emailDuplicadoDevolve409() throws Exception {
        String email = "dup" + System.nanoTime() + "@teste.com";
        cadastrar(email, "Primeira");

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Segunda","email":"%s","senha":"SenhaForte123",
                                 "telefone":"11999998888"}""".formatted(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Ja existe uma conta com o e-mail informado."));
    }

    @Test
    @DisplayName("Dados invalidos devolvem 400 detalhando cada campo")
    void dadosInvalidosDevolvem400ComCampos() throws Exception {
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"nao-e-email","senha":"123","telefone":"abc"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").isNotEmpty())
                .andExpect(jsonPath("$.campos.email").isNotEmpty())
                .andExpect(jsonPath("$.campos.senha").isNotEmpty())
                .andExpect(jsonPath("$.campos.telefone").isNotEmpty());
    }

    @Test
    @DisplayName("Recurso inexistente devolve 404, nao 500")
    void recursoInexistenteDevolve404() throws Exception {
        mvc.perform(get("/pontos-seguros/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.caminho").value("/pontos-seguros/999999"))
                .andExpect(jsonPath("$.campos").doesNotExist());
    }

    @Test
    @DisplayName("Id com tipo invalido devolve 400")
    void idComTipoInvalidoDevolve400() throws Exception {
        mvc.perform(get("/pontos-seguros/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ponto seguro 24h aparece sempre como aberto")
    void pontoSeguro24hApareceComoAberto() throws Exception {
        mvc.perform(get("/pontos-seguros").param("bairro", "Brasilandia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Posto Avenida 24h"))
                .andExpect(jsonPath("$[0].abertoAgora").value(true))
                .andExpect(jsonPath("$[0].hrAbertura").value("00:00"))
                .andExpect(jsonPath("$[0].hrFechamento").value("00:00"));
    }

    @Test
    @DisplayName("Filtro por bairro sem ponto seguro devolve lista vazia")
    void filtroPorBairroSemPontoSeguroDevolveListaVazia() throws Exception {
        mvc.perform(get("/pontos-seguros").param("bairro", "Cidade Tiradentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private long cadastrar(String email, String nome) throws Exception {
        MvcResult res = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","email":"%s","senha":"SenhaForte123",
                                 "telefone":"11999998888"}""".formatted(nome, email)))
                .andExpect(status().isCreated())
                .andReturn();
        return json(res).get("id").asLong();
    }

    private String logar(String email) throws Exception {
        return logar(email, "SenhaForte123");
    }

    private String logar(String email, String senha) throws Exception {
        MvcResult res = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}""".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn();
        return json(res).get("token").asText();
    }

    private long primeiroId(String caminho) throws Exception {
        MvcResult res = mvc.perform(get(caminho)).andExpect(status().isOk()).andReturn();
        return json(res).get(0).get("id").asLong();
    }

    private double riscoDoTrecho(long idTrecho) throws Exception {
        MvcResult res = mvc.perform(get("/trechos")).andExpect(status().isOk()).andReturn();
        for (JsonNode trecho : json(res)) {
            if (trecho.get("id").asLong() == idTrecho) {
                return trecho.get("nivelRisco").asDouble();
            }
        }
        throw new AssertionError("Trecho " + idTrecho + " nao encontrado em GET /trechos");
    }

    private JsonNode json(MvcResult res) throws Exception {
        return om.readTree(res.getResponse().getContentAsString());
    }
}
