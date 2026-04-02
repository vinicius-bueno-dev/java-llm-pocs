output "s3_bucket_name" {
  description = "Nome do bucket S3 criado para a POC"
  value       = module.s3.bucket_name
}

output "s3_bucket_arn" {
  description = "ARN do bucket S3 (formato LocalStack)"
  value       = module.s3.bucket_arn
}
