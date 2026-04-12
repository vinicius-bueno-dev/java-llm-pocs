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

# --- POC 4: SNS ---
module "sns" {
  source       = "./modules/sns"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 5: Lambda ---
module "lambda" {
  source       = "./modules/lambda"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 6: EventBridge ---
module "eventbridge" {
  source       = "./modules/eventbridge"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 7: Secrets Manager ---
module "secretsmanager" {
  source       = "./modules/secretsmanager"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 8: KMS ---
module "kms" {
  source       = "./modules/kms"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 9: CloudWatch ---
module "cloudwatch" {
  source       = "./modules/cloudwatch"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 10: Step Functions ---
module "stepfunctions" {
  source       = "./modules/stepfunctions"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 11: Kinesis ---
module "kinesis" {
  source       = "./modules/kinesis"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 12: SES ---
module "ses" {
  source       = "./modules/ses"
  project_name = var.project_name
  environment  = var.environment
}

# --- POC 13: IAM ---
module "iam" {
  source       = "./modules/iam"
  project_name = var.project_name
  environment  = var.environment
}
