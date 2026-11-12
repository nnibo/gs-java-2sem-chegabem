# Chega Bem — API REST

API da CitizenTech **Chega Bem**: rota segura, acompanhamento de trajeto com alerta de não chegada, pontos seguros e relatos anônimos, para a segurança de mulheres no trajeto a pé pela cidade de **São Paulo**.

Entrega da disciplina **Domain Driven Design (Java)** — FIAP, Global Solution.

---

## API em produção

A API está no ar no Render, com Swagger UI aberto:

### https://gs-java-2sem-chegabem.onrender.com/swagger-ui.html

> **O primeiro acesso pode levar até 2 minutos.** O plano gratuito do Render hiberna o
> serviço após alguns minutos sem uso, e a primeira requisição precisa subir o container e
> inicializar a aplicação. Basta aguardar o carregamento — não é erro. Os acessos seguintes
> respondem normalmente.

Para autenticar, veja [Como autenticar](#como-autenticar): o fluxo é o mesmo em produção e
em ambiente local, só muda a URL base.

---

## Stack

| | |
|---|---|
| Java | 21 (bytecode 17) |
| Spring Boot | 4.1.2 |
| Persistência | Spring Data JPA + Hibernate 7 → Oracle |
| Segurança | Spring Security 7 + JWT (HS256) |
| Documentação | OpenAPI 3 / Swagger UI (springdoc 3.2.0) |
| Testes | JUnit 5 + MockMvc, sobre H2 em memória |

---

## Como executar localmente

### 1. Configure as variáveis de ambiente

O `application.properties` não guarda nenhum segredo: tudo que é credencial vem do ambiente, e **a aplicação não sobe sem essas seis variáveis**.

| Variável | Para quê |
|---|---|
| `DATABASE_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DATABASE_USERNAME` | usuário do Oracle FIAP (seu RM) |
| `DATABASE_PASSWORD` | senha do Oracle FIAP |
| `JWT_KEY` | segredo do token, **mínimo 32 caracteres** |
| `ADMIN_EMAIL` | e-mail da conta de administração |
| `ADMIN_PASSWORD` | senha da conta de administração |

No IntelliJ: *Run → Edit Configurations → Environment variables*. Isso vale só para a execução pela IDE — para rodar `./mvnw spring-boot:run` direto no terminal, exporte as mesmas variáveis na sessão do shell.

> Se `JWT_KEY` tiver menos de 32 caracteres, a aplicação recusa subir com uma mensagem explícita — HS256 exige esse tamanho.

### 2. Suba a aplicação

```bash
./mvnw spring-boot:run
```

Na primeira subida, com `ddl-auto=update`, o Hibernate cria as tabelas a partir do model, e a carga inicial insere bairros, trechos, tipos de relato e a conta de administração. A API já fica utilizável.

### 3. Abra o Swagger

```
http://localhost:8080/swagger-ui.html
```

### 4. Rode os testes

```bash
./mvnw test
```

Os testes usam **H2 em memória** (perfil `test`) e não precisam do Oracle.

---

## Como autenticar

1. **Cadastre-se** em `POST /auth/register` (endpoint aberto).
2. **Faça login** em `POST /auth/login` e copie o campo `token` da resposta.
3. No Swagger, clique em **Authorize**, cole o token e confirme. Fora do Swagger, envie o header:

```
Authorization: Bearer <token>
```

A conta de administração criada pela carga inicial usa o e-mail definido em `ADMIN_EMAIL` e a senha definida em `ADMIN_PASSWORD`. É a única com perfil `ADMIN`, necessário para moderar relatos — não é possível se cadastrar como `ADMIN` pela API.

---

## Endpoints

Legenda da coluna **Acesso**: **público** = sem token · **token** = exige token válido · **ADMIN** = exige perfil `ADMIN`

### Autenticação
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/auth/register` | público | Cadastra uma usuária |
| POST | `/auth/login` | público | Autentica e devolve o token JWT |

### Usuárias
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/usuarias/{id}` | token | Busca uma usuária |
| GET | `/usuarias` | ADMIN | Lista todas |
| PUT | `/usuarias/{id}` | token | Atualiza nome e telefone |
| DELETE | `/usuarias/{id}` | token | Remove a conta |

### Contatos de confiança
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/usuarias/{id}/contatos` | token | Cadastra um contato (máx. 5) |
| GET | `/usuarias/{id}/contatos` | token | Lista os contatos |
| DELETE | `/usuarias/{id}/contatos/{idContato}` | token | Remove um contato |

### Pontos seguros
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/pontos-seguros?bairro=` | público | Lista, com `abertoAgora` calculado |
| GET | `/pontos-seguros/{id}` | público | Busca um ponto seguro |
| POST | `/pontos-seguros` | token | Cadastra um ponto seguro |
| PUT | `/pontos-seguros/{id}` | token | Atualiza horário e dados |
| DELETE | `/pontos-seguros/{id}` | token | Remove um ponto seguro |

### Trajetos (Chega Bem)
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/trajetos` | token | Inicia um trajeto e avisa os contatos |
| PUT | `/trajetos/{id}/chegada` | token | Confirma a chegada ("Cheguei bem") |
| PUT | `/trajetos/{id}/cancelamento` | token | Cancela um trajeto em andamento |
| GET | `/trajetos/{id}` | token | Consulta o status |
| GET | `/trajetos` | token | Lista os trajetos da usuária |

### Relatos
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/relatos` | token | Registra um relato (anônimo por padrão) |
| GET | `/relatos?trecho=` | público | Lista relatos |
| DELETE | `/relatos/{id}` | ADMIN | Remove um relato (moderação) |

### Bairros e trechos
| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| GET | `/bairros?cidade=` | público | Lista os bairros atendidos |
| GET | `/trechos?bairro=` | público | Lista os trechos, com risco e iluminação |
| GET | `/trechos/criticos` | token | Painel dos 10 trechos de maior risco |

---

## Corpos JSON para copiar e colar

Todos os endpoints que recebem corpo, na ordem em que você vai usar. Em todos: `Content-Type: application/json`.

Os endpoints marcados como **token** exigem o header `Authorization: Bearer <token>`, obtido no passo 2.

### 1. `POST /auth/register` (público)

```json
{
  "nome": "Camila Souza",
  "email": "camila@teste.com",
  "senha": "SenhaForte123",
  "telefone": "11999998888"
}
```

| Campo | Regra |
|---|---|
| `nome` | obrigatório, até 100 caracteres |
| `email` | obrigatório, formato válido, **único** |
| `senha` | obrigatória, **entre 8 e 72** caracteres |
| `telefone` | obrigatório, **só dígitos** com DDD (10 a 13) — `11999998888`, não `(11) 99999-8888` |

### 2. `POST /auth/login` (público)

```json
{
  "email": "camila@teste.com",
  "senha": "SenhaForte123"
}
```

Resposta traz o `token` que você usa em tudo abaixo.

### 3. `POST /usuarias/{id}/contatos` (token)

```json
{
  "nome": "Maria (mae)",
  "telefone": "11988887777",
  "parentesco": "mae"
}
```

`parentesco` é opcional (até 50 caracteres). Máximo de 5 contatos por usuária.

### 4. `POST /trajetos` (token)

```json
{
  "origem": "Estacao Capao Redondo",
  "destino": "Rua das Acacias, 100",
  "dtPrevistaChegada": "2028-12-31T23:45:00"
}
```

A data acima é deliberadamente distante para o corpo funcionar copiado e colado, sem edição, por todo 2028. **Qualquer data no futuro serve** — formato `AAAA-MM-DDTHH:MM:SS`, sem fuso. Não existe campo de usuária: ela vem do token.

> `dtPrevistaChegada` é o único campo do projeto que caduca: tem `@Future`, e data no passado é recusada com **400**.

Pré-requisitos: ao menos 1 contato de confiança cadastrado e nenhum outro trajeto `EM_ANDAMENTO`.

### 5. `POST /relatos` (token)

```json
{
  "idTrecho": 1,
  "idTipo": 1,
  "descricao": "Poste apagado na esquina, rua escura a noite",
  "anonimo": true
}
```

`anonimo` é opcional e vale `true` por omissão. Com `true`, a autoria **não** é gravada. `descricao` até 500 caracteres.

### 6. `POST /pontos-seguros` (token)

```json
{
  "idBairro": 1,
  "nome": "Farmacia 24 Horas",
  "tipo": "FARMACIA",
  "hrAbertura": "08:00",
  "hrFechamento": "22:00",
  "latitude": -23.660000,
  "longitude": -46.775000
}
```

Horários **sempre** `HH:mm` com zero à esquerda (`08:00`, nunca `8:00`). Abertura igual ao fechamento significa 24 horas.

### 7. `PUT /pontos-seguros/{id}` (token)

Mesma estrutura do item 6 — manda o objeto inteiro, não só o campo alterado.

```json
{
  "idBairro": 1,
  "nome": "Farmacia 24 Horas",
  "tipo": "FARMACIA",
  "hrAbertura": "00:00",
  "hrFechamento": "00:00",
  "latitude": -23.660000,
  "longitude": -46.775000
}
```

### 8. `PUT /usuarias/{id}` (token)

```json
{
  "nome": "Camila Souza Lima",
  "telefone": "11977776666"
}
```

Só nome e telefone. E-mail e senha não se alteram por aqui.

### Endpoints que **não** levam corpo

Mande sem body, só com o header de autorização — Postman em `Body → none`:

```
PUT    /trajetos/{id}/chegada
PUT    /trajetos/{id}/cancelamento
DELETE /usuarias/{id}
DELETE /usuarias/{id}/contatos/{idContato}
DELETE /pontos-seguros/{id}
DELETE /relatos/{id}
```

---

### Valores aceitos e ids da carga inicial

**`tipo` em pontos seguros:**

```
PADARIA · FARMACIA · POSTO_COMBUSTIVEL · MERCADO · RESTAURANTE · UNIDADE_PUBLICA · OUTRO
```

**`idTipo` em relatos** — não há endpoint que liste os tipos (lacuna conhecida), então use estes ids da carga inicial:

| id | Descrição |
|---|---|
| 1 | Assedio |
| 2 | Iluminacao apagada |
| 3 | Rua deserta |
| 4 | Terreno baldio |
| 5 | Calcada intransitavel |

**`idBairro`** — confirme sempre em `GET /bairros`, porque depende da sequence:

| id | Bairro |
|---|---|
| 1 | Capao Redondo |
| 2 | Brasilandia |
| 3 | Grajau |
| 4 | Cidade Tiradentes *(sem ponto seguro, de propósito)* |

**`idTrecho`** — confirme em `GET /trechos`. São 7, distribuídos entre os bairros 1, 2 e 3.

### Valores enviados pelo sistema, não por você

Não mande estes campos: a API os define, e campos extras no corpo são silenciosamente ignorados.

Isso vale como garantia de segurança, não só como detalhe: mandar `"perfil": "ADMIN"` no `POST /auth/register` **não** cria uma conta de administração — o perfil é definido na camada BO, nunca pelo corpo da requisição. Há um teste de integração que trava esse comportamento (`cadastroIgnoraCamposExtras`).

| Campo | Quem define |
|---|---|
| `id` | sequence do Oracle |
| `perfil` | sempre `USUARIA` no cadastro |
| `dtCadastro`, `dtInicio`, `dtRelato` | momento da criação |
| `dtChegadaConfirmada` | `PUT /trajetos/{id}/chegada` |
| `status` | ciclo de vida do trajeto |
| `nivelRisco` | recalculado a cada relato |
| `idUsuaria` em trajetos e relatos | vem do token |

---

## Arquitetura

```
src/main/java/com/nicolas/chegabem/
├── model/        9 entidades JPA + enums + converter de horário
├── dao/          9 repositórios Spring Data (acesso ao Oracle)
├── bo/           regras de negócio + rotina de alerta
├── dto/          records de entrada/saída com Bean Validation
├── controller/   7 controllers REST
├── exception/    exceções de domínio + @RestControllerAdvice
└── config/       Security/JWT, OpenAPI, agendamento, carga inicial
```

**A camada BO devolve DTO, nunca entidade.** Isso mantém o mapeamento dentro da transação (evitando `LazyInitializationException`), impede que o hash da senha escape numa resposta e desacopla o JSON do modelo de banco.

### Regras de negócio

As três exigidas pelo briefing, em `TrajetoBO`:

1. **Trajeto exige contato de confiança** — sem ninguém para avisar, o acompanhamento não faria sentido.
2. **Horário previsto precisa ser futuro** — validado no DTO e novamente na BO, porque a regra pertence ao domínio, não ao formato de entrada.
3. **Alerta de não chegada** — `VerificadorDeTrajetos` roda a cada minuto, varre os trajetos vencidos (prazo + tolerância), muda o status para `ALERTA_DISPARADO` e grava um alerta por contato. É agendado, e não acionado por endpoint, porque se a usuária não chegou ela justamente *não* vai abrir o app para avisar.

Outras: no máximo 5 contatos por usuária; um trajeto em andamento por vez; e cada usuária só alcança os próprios dados (`ADMIN` alcança todos).

### Anonimato dos relatos

`anonimo = true` (o padrão) grava `ID_USUARIA` como **NULL**. A autoria fica irrecuperável, inclusive para a moderação. Anonimato aqui é *ausência do dado*, não um flag pedindo para o sistema não olhar. Com `anonimo = false`, a autoria é guardada para a usuária acompanhar os próprios relatos.

### Tratamento de exceções

Centralizado em `GlobalExceptionHandler`, sem nenhum `try/catch` nos controllers:

| Situação | HTTP |
|---|---|
| Recurso inexistente / rota inexistente | 404 |
| Regra de negócio violada, entrada inválida, tipo errado | 400 |
| Credenciais incorretas | 401 |
| Sem permissão para a operação | 403 |
| Método HTTP não suportado | 405 |
| E-mail duplicado, violação de integridade | 409 |
| Falha não prevista | 500 |

Erros de validação detalham campo a campo:

```json
{
  "timestamp": "2026-11-07T22:14:03",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Um ou mais campos estao invalidos.",
  "caminho": "/usuarias",
  "campos": { "email": "E-mail em formato invalido." }
}
```

O handler de 500 **não** devolve a mensagem original, para não expor stack trace ou SQL ao cliente.

---

## Deploy no Render

Está no ar em **https://gs-java-2sem-chegabem.onrender.com** como Web Service Docker, no
plano gratuito. O Render não tem runtime nativo de Java, então o build vem do `Dockerfile`:
multi-estágio, compila com Maven e publica só o JRE com o jar, rodando como usuário sem
privilégios.

### Como o serviço foi configurado

| Campo | Valor |
|---|---|
| Language | `Docker` |
| Branch | `main` |
| Root Directory | `chegabem` |
| Instance Type | `Free` |
| Health Check Path | `/v3/api-docs` |

O **Root Directory** é o campo que não pode faltar: o projeto Maven vive em `chegabem/`, e
sem ele o Render procura o `Dockerfile` na raiz do repositório e o build falha.

### Variáveis de ambiente

As mesmas seis da seção "Como executar localmente" — `DATABASE_URL`,
`DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_KEY`, `ADMIN_EMAIL` e `ADMIN_PASSWORD` —
mais `SPRING_JPA_SHOW_SQL=false`, que sobrescreve o `show-sql=true` do
`application.properties` (variável de ambiente tem precedência sobre o arquivo no Spring
Boot). Em produção o log de SQL do Hibernate consome a cota de log do plano free muito
rápido.

A porta não precisa de configuração: o Render injeta `PORT` e o `application.properties` já
lê `${PORT:8080}`.

### O que muda em relação ao ambiente local

- **Fuso horário** — o container roda em UTC, então o `Dockerfile` fixa
  `TZ=America/Sao_Paulo`. Sem isso o `abertoAgora` dos pontos seguros e os horários de
  trajeto ficariam 3 horas deslocados.
- **Memória** — o plano free dá 512 MB, e o `-XX:MaxRAMPercentage` está em 65% para o heap
  caber junto com metaspace e threads sem o container ser derrubado por OOM.
- **Hibernação** — o serviço dorme após alguns minutos sem requisição, e a primeira chamada
  seguinte leva até 2 minutos. Enquanto dorme, a verificação agendada de não chegada
  (`@Scheduled`, a cada 60 s) **não roda** — ela retoma no próximo acesso. Para demonstrar
  o alerta de não chegada disparando sozinho, aqueça a URL antes.
- **Banco** — continua o Oracle da FIAP, acessado pela internet; nada de dados é hospedado
  no Render.

A carga inicial é idempotente: se o schema já tiver bairros, trechos, tipos de relato e a
conta ADMIN, a subida no Render não duplica nada.
