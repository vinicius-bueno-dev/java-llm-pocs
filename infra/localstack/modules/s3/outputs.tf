output "bucket_name" {
  description = "Nome do bucket principal"
  value       = aws_s3_bucket.poc_bucket.id
}

output "bucket_arn" {
  description = "ARN do bucket principal"
  value       = aws_s3_bucket.poc_bucket.arn
}

output "bucket_domain_name" {
  description = "Domain name do bucket (path-style no LocalStack)"
  value       = aws_s3_bucket.poc_bucket.bucket_domain_name
}

output "logs_bucket_name" {
  description = "Nome do bucket de logs"
  value       = aws_s3_bucket.logs_bucket.id
}

output "website_bucket_name" {
  description = "Nome do bucket de website"
  value       = aws_s3_bucket.website_bucket.id
}

output "website_endpoint" {
  description = "Endpoint do website estatico"
  value       = aws_s3_bucket_website_configuration.website_config.website_endpoint
}

output "sqs_queue_url" {
  description = "URL da fila SQS para eventos S3"
  value       = aws_sqs_queue.s3_event_queue.url
}

output "sqs_queue_arn" {
  description = "ARN da fila SQS para eventos S3"
  value       = aws_sqs_queue.s3_event_queue.arn
}

output "sns_topic_arn" {
  description = "ARN do topico SNS para eventos S3"
  value       = aws_sns_topic.s3_event_topic.arn
}
