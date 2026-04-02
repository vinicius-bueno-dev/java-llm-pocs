output "bucket_name" {
  description = "Nome do bucket criado"
  value       = aws_s3_bucket.poc_bucket.id
}

output "bucket_arn" {
  description = "ARN do bucket"
  value       = aws_s3_bucket.poc_bucket.arn
}

output "bucket_domain_name" {
  description = "Domain name do bucket (path-style no LocalStack)"
  value       = aws_s3_bucket.poc_bucket.bucket_domain_name
}
