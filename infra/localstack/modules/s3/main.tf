# =============================================================================
# Módulo S3 — LocalStack
# =============================================================================

resource "aws_s3_bucket" "poc_bucket" {
  bucket = "${var.project_name}-${var.environment}-poc-storage"

  tags = {
    Name        = "${var.project_name}-poc-storage"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "s3-storage"
  }
}

# Configuração de versionamento (boas práticas)
resource "aws_s3_bucket_versioning" "poc_bucket_versioning" {
  bucket = aws_s3_bucket.poc_bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Bloquear acesso público (simulando ambiente de produção)
resource "aws_s3_bucket_public_access_block" "poc_bucket_access" {
  bucket = aws_s3_bucket.poc_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
