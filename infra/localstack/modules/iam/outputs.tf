output "app_role_arn" {
  description = "ARN da role de aplicacao"
  value       = aws_iam_role.app_role.arn
}

output "app_role_name" {
  description = "Nome da role de aplicacao"
  value       = aws_iam_role.app_role.name
}

output "s3_read_policy_arn" {
  description = "ARN da policy de leitura S3"
  value       = aws_iam_policy.s3_read.arn
}
