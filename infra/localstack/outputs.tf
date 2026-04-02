output "s3_bucket_name" {
  description = "Nome do bucket S3 principal"
  value       = module.s3.bucket_name
}

output "s3_bucket_arn" {
  description = "ARN do bucket S3 (formato LocalStack)"
  value       = module.s3.bucket_arn
}

output "s3_logs_bucket_name" {
  description = "Nome do bucket de logs"
  value       = module.s3.logs_bucket_name
}

output "s3_website_bucket_name" {
  description = "Nome do bucket de website"
  value       = module.s3.website_bucket_name
}

output "s3_website_endpoint" {
  description = "Endpoint do website estatico"
  value       = module.s3.website_endpoint
}

output "s3_sqs_queue_url" {
  description = "URL da fila SQS para eventos S3"
  value       = module.s3.sqs_queue_url
}

output "s3_sns_topic_arn" {
  description = "ARN do topico SNS para eventos S3"
  value       = module.s3.sns_topic_arn
}
