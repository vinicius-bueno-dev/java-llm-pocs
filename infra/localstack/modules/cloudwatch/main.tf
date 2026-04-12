# =============================================================================
# Modulo CloudWatch — LocalStack
# Cobre: log group + stream, custom metric namespace e alarm.
# =============================================================================

# --- Log Group ---
resource "aws_cloudwatch_log_group" "app_logs" {
  name              = "/nameless/${var.environment}/poc-app"
  retention_in_days = 7

  tags = {
    Name        = "${var.project_name}-poc-app-logs"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "cloudwatch-logs"
  }
}

# --- Log Stream ---
resource "aws_cloudwatch_log_stream" "app_stream" {
  name           = "application"
  log_group_name = aws_cloudwatch_log_group.app_logs.name
}

# --- Metric Alarm ---
resource "aws_cloudwatch_metric_alarm" "high_error_rate" {
  alarm_name          = "${var.project_name}-${var.environment}-poc-high-error-rate"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ErrorCount"
  namespace           = "Nameless/POC"
  period              = 60
  statistic           = "Sum"
  threshold           = 10
  alarm_description   = "Alarme quando taxa de erros excede 10 em 1 minuto"

  tags = {
    Name        = "${var.project_name}-poc-high-error-rate"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "cloudwatch-logs"
  }
}
