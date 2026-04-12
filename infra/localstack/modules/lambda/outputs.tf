output "lambda_role_arn" {
  description = "ARN da IAM role de execucao Lambda"
  value       = aws_iam_role.lambda_exec.arn
}

output "lambda_role_name" {
  description = "Nome da IAM role de execucao Lambda"
  value       = aws_iam_role.lambda_exec.name
}

output "artifacts_bucket_name" {
  description = "Nome do bucket S3 para artefatos Lambda"
  value       = aws_s3_bucket.lambda_artifacts.bucket
}

output "artifacts_bucket_arn" {
  description = "ARN do bucket S3 para artefatos Lambda"
  value       = aws_s3_bucket.lambda_artifacts.arn
}

output "trigger_queue_url" {
  description = "URL da fila SQS trigger"
  value       = aws_sqs_queue.lambda_trigger.url
}

output "trigger_queue_arn" {
  description = "ARN da fila SQS trigger"
  value       = aws_sqs_queue.lambda_trigger.arn
}

output "dlq_url" {
  description = "URL da dead-letter queue Lambda"
  value       = aws_sqs_queue.lambda_dlq.url
}

output "dlq_arn" {
  description = "ARN da dead-letter queue Lambda"
  value       = aws_sqs_queue.lambda_dlq.arn
}
