output "main_key_id" {
  description = "ID da CMK principal"
  value       = aws_kms_key.main_key.key_id
}

output "main_key_arn" {
  description = "ARN da CMK principal"
  value       = aws_kms_key.main_key.arn
}

output "main_key_alias" {
  description = "Alias da CMK principal"
  value       = aws_kms_alias.main_key_alias.name
}

output "envelope_key_id" {
  description = "ID da CMK de envelope encryption"
  value       = aws_kms_key.envelope_key.key_id
}

output "envelope_key_arn" {
  description = "ARN da CMK de envelope encryption"
  value       = aws_kms_key.envelope_key.arn
}

output "envelope_key_alias" {
  description = "Alias da CMK de envelope encryption"
  value       = aws_kms_alias.envelope_key_alias.name
}
