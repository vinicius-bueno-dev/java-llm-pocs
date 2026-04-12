output "custom_bus_name" {
  description = "Nome do event bus customizado"
  value       = aws_cloudwatch_event_bus.custom_bus.name
}

output "custom_bus_arn" {
  description = "ARN do event bus customizado"
  value       = aws_cloudwatch_event_bus.custom_bus.arn
}

output "event_target_queue_url" {
  description = "URL da fila SQS target"
  value       = aws_sqs_queue.event_target.url
}

output "event_target_queue_arn" {
  description = "ARN da fila SQS target"
  value       = aws_sqs_queue.event_target.arn
}

output "event_dlq_url" {
  description = "URL da dead-letter queue"
  value       = aws_sqs_queue.event_dlq.url
}

output "order_events_rule_arn" {
  description = "ARN da rule de order events"
  value       = aws_cloudwatch_event_rule.order_events.arn
}
