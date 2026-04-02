# Terraform + LocalStack

## Como o Terraform se conecta ao LocalStack?

O provider `hashicorp/aws` do Terraform permite configurar endpoints customizados.
Apontamos todos os serviços para `http://localhost:4566` e usamos credenciais fake.

## Configuração do Provider

```hcl
provider "aws" {
  region                      = "us-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    s3       = "http://localhost:4566"
    sqs      = "http://localhost:4566"
    dynamodb = "http://localhost:4566"
    # ... outros serviços
  }
}
```

## Fluxo de trabalho

```bash
# 1. Subir o LocalStack
cd infra && docker-compose up -d

# 2. Aguardar o LocalStack ficar saudável
curl http://localhost:4566/_localstack/health

# 3. Entrar no diretório Terraform
cd infra/localstack

# 4. Inicializar (baixa providers e módulos)
terraform init

# 5. Ver o que será criado
terraform plan

# 6. Aplicar
terraform apply -auto-approve

# 7. Ver outputs
terraform output

# 8. Para destruir tudo (LocalStack já reseta ao reiniciar, mas útil)
terraform destroy -auto-approve
```

## Boas práticas com LocalStack + Terraform

1. **State local** — não use backend remoto para estudos, o `terraform.tfstate` local é suficiente
2. **`-auto-approve`** — use em desenvolvimento local para agilizar
3. **Módulos** — organize por serviço (`modules/s3`, `modules/sqs`, etc.)
4. **Variáveis** — sempre use `variables.tf` para facilitar reutilização
5. **Outputs** — exporte os nomes/ARNs dos recursos para usar nas POCs Java

## Diferenças importantes: LocalStack vs AWS real

| Aspecto | LocalStack | AWS Real |
|---|---|---|
| Credenciais | Qualquer valor ("test") | Reais (IAM) |
| ARNs | Formato igual, mas com account_id `000000000000` | Seu account_id real |
| S3 URLs | Path-style por padrão | Virtual-hosted por padrão |
| Custo | Gratuito | Cobrado por uso |
| Persistência | Perde ao reiniciar (sem volume) | Permanente |

## Diagrama do fluxo

```
Developer → Terraform CLI
                ↓
        Terraform Provider AWS
                ↓ (HTTP para localhost:4566)
        LocalStack Container (Docker)
                ↓
        Recursos simulados (S3, SQS, etc.)
                ↓
        Aplicação Java (Spring Boot)
```
