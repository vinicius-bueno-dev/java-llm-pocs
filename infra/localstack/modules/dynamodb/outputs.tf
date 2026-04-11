output "users_table_name" {
  description = "Nome da tabela de usuarios"
  value       = aws_dynamodb_table.users.name
}

output "users_table_arn" {
  description = "ARN da tabela de usuarios"
  value       = aws_dynamodb_table.users.arn
}

output "users_stream_arn" {
  description = "ARN do stream da tabela de usuarios"
  value       = aws_dynamodb_table.users.stream_arn
}

output "events_table_name" {
  description = "Nome da tabela de eventos"
  value       = aws_dynamodb_table.events.name
}

output "events_table_arn" {
  description = "ARN da tabela de eventos"
  value       = aws_dynamodb_table.events.arn
}
