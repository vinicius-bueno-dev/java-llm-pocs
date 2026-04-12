# =============================================================================
# Modulo Secrets Manager — LocalStack
# Cobre: segredo com versao inicial, segredo JSON para credenciais de DB,
#         e segredo com tags para demonstrar filtering.
# =============================================================================

# --- Segredo simples (string) ---
resource "aws_secretsmanager_secret" "app_secret" {
  name = "${var.project_name}-${var.environment}-poc-app-secret"

  tags = {
    Name        = "${var.project_name}-poc-app-secret"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "secrets-manager"
    Type        = "application"
  }
}

resource "aws_secretsmanager_secret_version" "app_secret_value" {
  secret_id     = aws_secretsmanager_secret.app_secret.id
  secret_string = "my-super-secret-value-v1"
}

# --- Segredo JSON (credenciais de DB) ---
resource "aws_secretsmanager_secret" "db_credentials" {
  name = "${var.project_name}-${var.environment}-poc-db-credentials"

  tags = {
    Name        = "${var.project_name}-poc-db-credentials"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "secrets-manager"
    Type        = "database"
  }
}

resource "aws_secretsmanager_secret_version" "db_credentials_value" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = "admin"
    password = "s3cret-p@ssw0rd"
    host     = "localhost"
    port     = 5432
    dbname   = "pocdb"
  })
}

# --- Segredo para API key ---
resource "aws_secretsmanager_secret" "api_key" {
  name = "${var.project_name}-${var.environment}-poc-api-key"

  tags = {
    Name        = "${var.project_name}-poc-api-key"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "secrets-manager"
    Type        = "api-key"
  }
}

resource "aws_secretsmanager_secret_version" "api_key_value" {
  secret_id     = aws_secretsmanager_secret.api_key.id
  secret_string = "ak_live_1234567890abcdef"
}
