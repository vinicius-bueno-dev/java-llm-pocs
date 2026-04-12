output "log_group_name" {
  description = "Nome do log group"
  value       = aws_cloudwatch_log_group.app_logs.name
}

output "log_group_arn" {
  description = "ARN do log group"
  value       = aws_cloudwatch_log_group.app_logs.arn
}

output "log_stream_name" {
  description = "Nome do log stream"
  value       = aws_cloudwatch_log_stream.app_stream.name
}

output "alarm_name" {
  description = "Nome do metric alarm"
  value       = aws_cloudwatch_metric_alarm.high_error_rate.alarm_name
}

output "alarm_arn" {
  description = "ARN do metric alarm"
  value       = aws_cloudwatch_metric_alarm.high_error_rate.arn
}
