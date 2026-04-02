variable "project_name" {
  description = "Prefixo usado em todos os recursos criados"
  type        = string
  default     = "nameless"
}

variable "environment" {
  description = "Ambiente (sempre 'local' neste projeto)"
  type        = string
  default     = "local"
}

variable "region" {
  description = "Região AWS simulada pelo LocalStack"
  type        = string
  default     = "us-east-1"
}
