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
