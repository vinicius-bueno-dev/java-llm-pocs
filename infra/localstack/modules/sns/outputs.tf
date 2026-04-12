output "standard_topic_arn" {
  description = "ARN do topic standard"
  value       = aws_sns_topic.standard_topic.arn
}

output "standard_topic_name" {
  description = "Nome do topic standard"
  value       = aws_sns_topic.standard_topic.name
}

output "fifo_topic_arn" {
  description = "ARN do topic FIFO"
  value       = aws_sns_topic.fifo_topic.arn
}

output "fifo_topic_name" {
  description = "Nome do topic FIFO"
  value       = aws_sns_topic.fifo_topic.name
}

output "subscriber_orders_queue_url" {
  description = "URL da fila subscriber de orders"
  value       = aws_sqs_queue.subscriber_orders.url
}

output "subscriber_orders_queue_arn" {
  description = "ARN da fila subscriber de orders"
  value       = aws_sqs_queue.subscriber_orders.arn
}

output "subscriber_analytics_queue_url" {
  description = "URL da fila subscriber de analytics"
  value       = aws_sqs_queue.subscriber_analytics.url
}

output "subscriber_analytics_queue_arn" {
  description = "ARN da fila subscriber de analytics"
  value       = aws_sqs_queue.subscriber_analytics.arn
}

output "dlq_url" {
  description = "URL da dead-letter queue do SNS"
  value       = aws_sqs_queue.sns_dlq.url
}

output "dlq_arn" {
  description = "ARN da dead-letter queue do SNS"
  value       = aws_sqs_queue.sns_dlq.arn
}
