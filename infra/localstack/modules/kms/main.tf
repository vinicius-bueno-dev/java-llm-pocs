# =============================================================================
# Modulo KMS — LocalStack
# Cobre: CMK simetrica com alias, data key grant e key rotation.
# =============================================================================

# --- Customer Managed Key (symmetric) ---
resource "aws_kms_key" "main_key" {
  description             = "CMK principal para POC de encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = {
    Name        = "${var.project_name}-poc-main-key"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "kms-encryption"
    Type        = "symmetric"
  }
}

# --- Alias para a CMK ---
resource "aws_kms_alias" "main_key_alias" {
  name          = "alias/${var.project_name}-${var.environment}-poc-main"
  target_key_id = aws_kms_key.main_key.key_id
}

# --- CMK secundaria para envelope encryption ---
resource "aws_kms_key" "envelope_key" {
  description             = "CMK para demonstrar envelope encryption"
  deletion_window_in_days = 7

  tags = {
    Name        = "${var.project_name}-poc-envelope-key"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "kms-encryption"
    Type        = "envelope"
  }
}

resource "aws_kms_alias" "envelope_key_alias" {
  name          = "alias/${var.project_name}-${var.environment}-poc-envelope"
  target_key_id = aws_kms_key.envelope_key.key_id
}
