output "standard_queue_url" {
  description = "URL da fila standard"
  value       = aws_sqs_queue.standard_queue.url
}

output "standard_queue_arn" {
  description = "ARN da fila standard"
  value       = aws_sqs_queue.standard_queue.arn
}

output "fifo_queue_url" {
  description = "URL da fila FIFO"
  value       = aws_sqs_queue.fifo_queue.url
}

output "fifo_queue_arn" {
  description = "ARN da fila FIFO"
  value       = aws_sqs_queue.fifo_queue.arn
}

output "dlq_url" {
  description = "URL da dead-letter queue (standard)"
  value       = aws_sqs_queue.dlq.url
}

output "dlq_arn" {
  description = "ARN da dead-letter queue (standard)"
  value       = aws_sqs_queue.dlq.arn
}

output "fifo_dlq_url" {
  description = "URL da dead-letter queue (FIFO)"
  value       = aws_sqs_queue.fifo_dlq.url
}

output "fifo_dlq_arn" {
  description = "ARN da dead-letter queue (FIFO)"
  value       = aws_sqs_queue.fifo_dlq.arn
}

output "delay_queue_url" {
  description = "URL da delay queue"
  value       = aws_sqs_queue.delay_queue.url
}

output "delay_queue_arn" {
  description = "ARN da delay queue"
  value       = aws_sqs_queue.delay_queue.arn
}
