output "app_secret_arn" {
  description = "ARN do segredo da aplicacao"
  value       = aws_secretsmanager_secret.app_secret.arn
}

output "app_secret_name" {
  description = "Nome do segredo da aplicacao"
  value       = aws_secretsmanager_secret.app_secret.name
}

output "db_credentials_arn" {
  description = "ARN do segredo de credenciais do DB"
  value       = aws_secretsmanager_secret.db_credentials.arn
}

output "db_credentials_name" {
  description = "Nome do segredo de credenciais do DB"
  value       = aws_secretsmanager_secret.db_credentials.name
}

output "api_key_arn" {
  description = "ARN do segredo da API key"
  value       = aws_secretsmanager_secret.api_key.arn
}

output "api_key_name" {
  description = "Nome do segredo da API key"
  value       = aws_secretsmanager_secret.api_key.name
}
