output "stream_name" {
  description = "Nome do data stream"
  value       = aws_kinesis_stream.data_stream.name
}

output "stream_arn" {
  description = "ARN do data stream"
  value       = aws_kinesis_stream.data_stream.arn
}
