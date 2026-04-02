# java-llm-pocs

Repositório de POCs para estudo de **Claude Code**, **LocalStack** e **Terraform** com **Java 21 + Spring Boot**.

## O que você vai encontrar aqui

| Seção | Conteúdo |
|---|---|
| [Claude Code](claude-code/01-basics.md) | CLI da Anthropic — comandos, hooks, slash commands e boas práticas |
| [LocalStack & Terraform](localstack/01-concepts.md) | Simulação de AWS local e provisionamento com Terraform |

---

## Stack

- **Java 21** + **Spring Boot 3.x** + **Maven Wrapper**
- **AWS SDK for Java v2** (`software.amazon.awssdk`)
- **LocalStack Community** via Docker Compose
- **Terraform 1.x** apontado para `http://localhost:4566`
- **Spring Cloud AWS 3.x** quando aplicável

---

## Setup Rápido

### 1. Subir o LocalStack

```bash
cd infra
docker-compose up -d

# Verificar saúde (aguardar ~30s)
curl http://localhost:4566/_localstack/health
```

### 2. Provisionar com Terraform

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

---

## POCs Planejadas

| POC | Serviços AWS | Status |
|---|---|---|
| `poc-s3-storage` | S3 | Em breve |
| `poc-sqs-messaging` | SQS | Em breve |
| `poc-dynamodb-crud` | DynamoDB | Em breve |
| `poc-lambda-java` | Lambda + S3 | Em breve |
| `poc-event-driven` | EventBridge + SQS + Lambda | Em breve |

---

## Pré-requisitos

| Ferramenta | Versão mínima | Verificar |
|---|---|---|
| Java | 21 | `java --version` |
| Docker | 20+ | `docker --version` |
| Terraform | 1.0+ | `terraform --version` |
| Git | qualquer | `git --version` |

!!! tip "Maven"
    Não precisa instalar o Maven globalmente — cada POC usa o Maven Wrapper (`./mvnw`).
