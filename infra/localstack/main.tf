# =============================================================================
# Infraestrutura LocalStack — nameless POCs
# =============================================================================
# Este arquivo orquestra os módulos de infraestrutura.
# Cada POC tem seu módulo correspondente aqui.
# =============================================================================

# --- POC 1: S3 ---
module "s3" {
  source       = "./modules/s3"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 2: SQS ---
module "sqs" {
  source       = "./modules/sqs"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 3: DynamoDB ---
module "dynamodb" {
  source       = "./modules/dynamodb"
  project_name = var.project_name
  environment  = var.environment
}
