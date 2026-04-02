# java-llm-pocs — Instruções para o Claude Code

## Contexto do Projeto

Repositório de POCs para estudo de:
1. **Claude Code** — recursos da plataforma do básico ao avançado
2. **LocalStack** — simulação de serviços AWS localmente (gratuito, via Docker)
3. **Terraform** — IaC para provisionar infraestrutura no LocalStack
4. **Spring Boot (Java 21)** — runtime das POCs, build com Maven

O foco é **aprendizado técnico**, não regras de negócio. Prefira código claro e didático.

---

## Stack

- **Java 21** + **Spring Boot 3.x** + **Maven** (monorepo com Maven Wrapper)
- **AWS SDK for Java v2** (`software.amazon.awssdk`)
- **LocalStack Community** via Docker Compose
- **Terraform 1.x** com provider `hashicorp/aws` apontado para `http://localhost:4566`
- **Spring Cloud AWS 3.x** quando aplicável

---

## Estrutura do Projeto

```
nameless/
├── CLAUDE.md              # Este arquivo
├── pom.xml                # Parent POM (monorepo)
├── infra/
│   ├── docker-compose.yml # LocalStack
│   └── localstack/        # Terraform modules
│       ├── main.tf
│       ├── providers.tf
│       └── modules/       # s3, sqs, dynamodb, lambda
├── docs/
│   ├── claude-code/       # Guias de estudo Claude Code
│   └── localstack/        # Guias de estudo LocalStack
└── pocs/
    ├── poc-s3-storage/
    ├── poc-sqs-messaging/
    ├── poc-dynamodb-crud/
    ├── poc-lambda-java/
    └── poc-event-driven/
```

---

## Convenções

### Java / Spring Boot
- Java 21 com records, sealed classes e features modernas quando útil
- Pacote base: `dev.nameless.poc.<nome-da-poc>`
- Configuração via `application.yml` (não `.properties`)
- Testes com JUnit 5 + Testcontainers (quando necessário)
- Injeção por construtor, sem `@Autowired` em campos

### Terraform
- Sempre usar `terraform fmt` antes de commitar
- Variáveis em `variables.tf`, outputs em `outputs.tf`
- Módulos reutilizáveis em `infra/localstack/modules/`
- Nunca hardcodar credenciais (LocalStack aceita qualquer valor fake)

### LocalStack
- Endpoint: `http://localhost:4566`
- Credenciais fake: `AWS_ACCESS_KEY_ID=test`, `AWS_SECRET_ACCESS_KEY=test`
- Region padrão: `us-east-1`
- Verificar status: `curl http://localhost:4566/_localstack/health`

### Commits
- Formato: `<tipo>(<escopo>): <mensagem>`
- Tipos: `feat`, `fix`, `docs`, `infra`, `poc`, `chore`
- Exemplos:
  - `infra(localstack): add SQS module`
  - `poc(s3): implement presigned URL upload`
  - `docs(claude-code): add hooks guide`

---

## Como Rodar

### 1. Subir LocalStack
```bash
cd infra && docker-compose up -d
```

### 2. Provisionar infra com Terraform
```bash
cd infra/localstack
terraform init
terraform apply -auto-approve
```

### 3. Rodar uma POC
```bash
cd pocs/poc-s3-storage
./mvnw spring-boot:run
```

---

## Notas para o Claude Code

- Ao criar novas POCs, seguir a estrutura de módulo Maven existente
- Sempre validar se o LocalStack está rodando antes de sugerir testes de integração
- Preferir `AWS SDK v2` direto em POCs simples, `Spring Cloud AWS` em POCs mais elaboradas
- Documentar cada POC com um `README.md` explicando o conceito estudado
