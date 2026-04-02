# LocalStack — Conceitos Fundamentais

## O que é o LocalStack?

LocalStack é uma plataforma que simula serviços da AWS localmente no seu computador,
usando Docker. Você pode criar buckets S3, filas SQS, tabelas DynamoDB, etc. sem
custo e sem precisar de conta AWS.

## Por que usar?

- **Gratuito** — Community Edition cobre os serviços mais importantes
- **Offline** — funciona sem internet
- **Rápido** — sem latência de rede real
- **Seguro** — não há risco de cobranças acidentais na AWS
- **Ideal para desenvolvimento e testes**

## Como funciona?

O LocalStack expõe um único endpoint HTTP: `http://localhost:4566`

Todos os serviços AWS são acessíveis neste endpoint. O SDK da AWS identifica qual
serviço usar pelo path e headers da requisição.

```
http://localhost:4566   ← Tudo passa por aqui
    ├── S3 requests
    ├── SQS requests
    ├── DynamoDB requests
    └── Lambda requests
```

## Serviços disponíveis na Community Edition (gratuitos)

| Serviço | Uso |
|---|---|
| **S3** | Armazenamento de objetos |
| **SQS** | Filas de mensagens |
| **SNS** | Notificações pub/sub |
| **DynamoDB** | Banco NoSQL |
| **Lambda** | Funções serverless |
| **API Gateway** | Gateway de APIs REST |
| **IAM** | Identidade e acesso |
| **SSM** | Parameter Store |
| **Secrets Manager** | Gerenciamento de segredos |
| **CloudWatch** | Métricas e logs |
| **EventBridge** | Barramento de eventos |
| **Kinesis** | Streaming de dados |
| **Step Functions** | Orquestração de workflows |

## Verificando o status

```bash
# Health check completo
curl http://localhost:4566/_localstack/health | jq

# Listar buckets S3 (usando AWS CLI configurado para LocalStack)
aws --endpoint-url=http://localhost:4566 s3 ls

# Listar filas SQS
aws --endpoint-url=http://localhost:4566 sqs list-queues
```

## AWS CLI com LocalStack

Configure um profile específico para o LocalStack:

```bash
# ~/.aws/credentials
[localstack]
aws_access_key_id = test
aws_secret_access_key = test

# ~/.aws/config
[profile localstack]
region = us-east-1
output = json
```

Uso:
```bash
aws --profile localstack --endpoint-url=http://localhost:4566 s3 ls
```

Ou instale o `awslocal` (wrapper que já aponta para localhost:4566):
```bash
pip install awscli-local
awslocal s3 ls
```

## Próximo: [02 — Serviços em detalhe](./02-services.md)
