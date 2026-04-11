# =============================================================================
# Modulo DynamoDB — LocalStack
# Cobre: tabela principal (PK+SK) com GSI, LSI, TTL e Streams;
#         tabela secundaria de eventos para demonstrar BatchWrite e pagination.
# =============================================================================

# --- Tabela Principal: users ---
# PK: pk (String)  — ex.: "TENANT#acme"
# SK: sk (String)  — ex.: "USER#alice@example.com"
# GSI: by-email (hash = email)
# LSI: by-created-at (range = createdAt)
# TTL: campo "expiresAt"
# Streams: NEW_AND_OLD_IMAGES
resource "aws_dynamodb_table" "users" {
  name         = "${var.project_name}-${var.environment}-poc-users"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"
  range_key    = "sk"

  attribute {
    name = "pk"
    type = "S"
  }

  attribute {
    name = "sk"
    type = "S"
  }

  attribute {
    name = "email"
    type = "S"
  }

  attribute {
    name = "createdAt"
    type = "S"
  }

  # Global Secondary Index — buscar usuario por email em qualquer tenant
  global_secondary_index {
    name            = "by-email"
    hash_key        = "email"
    projection_type = "ALL"
  }

  # Local Secondary Index — listar users de um tenant ordenados por data de criacao
  local_secondary_index {
    name            = "by-created-at"
    range_key       = "createdAt"
    projection_type = "ALL"
  }

  ttl {
    attribute_name = "expiresAt"
    enabled        = true
  }

  stream_enabled   = true
  stream_view_type = "NEW_AND_OLD_IMAGES"

  tags = {
    Name        = "${var.project_name}-poc-users"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "dynamodb-crud"
    Type        = "primary"
  }
}

# --- Tabela Secundaria: events ---
# Usada para demonstrar BatchWriteItem, Scan com pagination e projecoes reduzidas.
resource "aws_dynamodb_table" "events" {
  name         = "${var.project_name}-${var.environment}-poc-events"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "eventId"

  attribute {
    name = "eventId"
    type = "S"
  }

  tags = {
    Name        = "${var.project_name}-poc-events"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "dynamodb-crud"
    Type        = "secondary"
  }
}
