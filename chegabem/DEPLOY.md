# Deploy — Render.com

A API sobe no Render como **Web Service Docker**, a partir do `Dockerfile` deste diretório.
O blueprint `render.yaml` na raiz do repositório já descreve o serviço inteiro.

---

## Por que Docker e não "Java nativo"

O Render não tem runtime nativo de Java. O caminho suportado é imagem Docker — que o
projeto já tem: build multi-stage (Maven + JDK 21 para compilar, JRE 21 para rodar),
usuário sem privilégio e `server.port=${PORT:8080}`, que é exatamente o contrato do Render.

---

## Passo a passo

### 1. Suba o repositório atualizado

```bash
git add render.yaml chegabem/Dockerfile chegabem/DEPLOY.md
git commit -m "Configura deploy no Render"
git push
```

### 2. Crie o serviço pelo blueprint

No painel do Render: **New → Blueprint** → conecte o repositório
`nnibo/gs-java-2sem-chegabem` → o Render lê o `render.yaml` e propõe o serviço
`chegabem-api`.

> Alternativa manual (**New → Web Service**): escolha *Docker*, e em **Root Directory**
> informe `chegabem`. Sem isso o Render procura o `Dockerfile` na raiz e falha.

### 3. Preencha as seis variáveis de ambiente

O `render.yaml` marca todas as credenciais como `sync: false`, então o Render pede cada
valor na criação e guarda só no painel — nada de segredo no Git.

| Variável | Valor |
|---|---|
| `DATABASE_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DATABASE_USERNAME` | usuário do Oracle FIAP (RM) |
| `DATABASE_PASSWORD` | senha do Oracle FIAP |
| `JWT_KEY` | segredo do token, **mínimo 32 caracteres** |
| `ADMIN_EMAIL` | e-mail da conta de administração |
| `ADMIN_PASSWORD` | senha da conta de administração |

São as mesmas do Run Configuration do IntelliJ (ver `COMO_TESTAR.md`).

### 4. Aguarde o build

O primeiro build leva alguns minutos: o Render baixa as dependências Maven e empacota o
JAR dentro da imagem. O deploy fica *live* quando o health check em `/v3/api-docs`
responde 200.

### 5. Valide

```
https://chegabem-api.onrender.com/swagger-ui.html
```

A carga inicial é idempotente: se as tabelas do Oracle já tiverem bairros, trechos, tipos
de relato e a conta ADMIN, a subida no Render não duplica nada.

---

## O que muda em relação ao ambiente local

- **Fuso horário** — o container do Render roda em UTC, então o `Dockerfile` fixa
  `TZ=America/Sao_Paulo`. Sem isso, `estaAberto` do ponto seguro e os horários de trajeto
  ficariam 3 horas deslocados.
- **Memória** — o plano free dá 512 MB, e o `-XX:MaxRAMPercentage` está em 65% para o heap
  caber junto com metaspace e threads sem o container ser derrubado por OOM.
- **Log de SQL** — `SPRING_JPA_SHOW_SQL=false` no `render.yaml` desliga o `show-sql` do
  Hibernate, que no plano free consome a cota de log muito rápido.
- **Hibernação** — no plano free o serviço dorme após ~15 min sem requisição, e a primeira
  chamada seguinte demora ~1 min para responder. Enquanto dorme, a verificação agendada de
  não chegada (`@Scheduled`, a cada 60 s) **não roda** — ela retoma no próximo acesso.
- **Banco** — continua o Oracle da FIAP, acessado pela internet; o Render não hospeda nada
  de dados aqui.
