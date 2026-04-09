terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Provider AWS apontado para o LocalStack
provider "aws" {
  region                      = "us-east-1"
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true
  # S3 path-style é obrigatório no LocalStack (virtual-hosted não funciona localmente)
  s3_use_path_style = true

  # Redireciona todos os serviços para o endpoint do LocalStack
  endpoints {
    s3             = "http://localhost:4566"
    sqs            = "http://localhost:4566"
    sns            = "http://localhost:4566"
    dynamodb       = "http://localhost:4566"
    lambda         = "http://localhost:4566"
    apigateway     = "http://localhost:4566"
    iam            = "http://localhost:4566"
    ssm            = "http://localhost:4566"
    secretsmanager = "http://localhost:4566"
    cloudwatch     = "http://localhost:4566"
    cloudwatchlogs = "http://localhost:4566"
    events         = "http://localhost:4566"
    kinesis        = "http://localhost:4566"
    stepfunctions  = "http://localhost:4566"
  }
}
