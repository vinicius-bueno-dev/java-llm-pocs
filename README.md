# java-llm-pocs

Repositório de POCs para estudo de **Claude Code**, **LocalStack** e **Terraform** com **Java 21 + Spring Boot**.

## Pré-requisitos

| Ferramenta | Versão mínima | Verificar |
|---|---|---|
| Java | 21 | `java --version` |
| Docker | 20+ | `docker --version` |
| Terraform | 1.0+ | `terraform --version` |
| Git | qualquer | `git --version` |

> Maven não precisa ser instalado globalmente — cada POC usa o Maven Wrapper (`./mvnw`).

## Setup do Ambiente

### 1. Subir o LocalStack

```bash
cd infra
docker-compose up -d

# Verificar se está saudável (aguardar ~30s)
curl http://localhost:4566/_localstack/health
```

### 2. Provisionar infraestrutura com Terraform

```bash
cd infra/localstack
terraform init
terraform apply -auto-approve
terraform output
```

### 3. Rodar uma POC

```bash
cd pocs/poc-s3-storage
./mvnw spring-boot:run
```

## Estrutura do Projeto

```
nameless/
├── CLAUDE.md              # Instruções para o Claude Code
├── pom.xml                # Parent POM (monorepo Maven)
├── infra/
│   ├── docker-compose.yml # LocalStack Community Edition
│   └── localstack/        # Terraform
│       ├── providers.tf   # Provider AWS → localhost:4566
│       ├── variables.tf
│       ├── main.tf        # Orquestração dos módulos
│       ├── outputs.tf
│       └── modules/
│           ├── s3/
│           ├── sqs/
│           ├── dynamodb/
│           └── lambda/
├── docs/
│   ├── claude-code/       # Guias de estudo Claude Code
│   └── localstack/        # Guias de estudo LocalStack + Terraform
└── pocs/
    ├── poc-s3-storage/
    ├── poc-sqs-messaging/
    ├── poc-dynamodb-crud/
    ├── poc-lambda-java/
    └── poc-event-driven/
```

## POCs Planejadas

| POC | Serviços AWS | Status |
|---|---|---|
| `poc-s3-storage` | S3 | 🔜 Em breve |
| `poc-sqs-messaging` | SQS | 🔜 Em breve |
| `poc-dynamodb-crud` | DynamoDB | 🔜 Em breve |
| `poc-lambda-java` | Lambda + S3 | 🔜 Em breve |
| `poc-event-driven` | EventBridge + SQS + Lambda | 🔜 Em breve |

## Documentação de Estudo

- [Claude Code — Básico](docs/claude-code/01-basics.md)
- [LocalStack — Conceitos](docs/localstack/01-concepts.md)
- [Terraform + LocalStack](docs/localstack/02-terraform-localstack.md)
