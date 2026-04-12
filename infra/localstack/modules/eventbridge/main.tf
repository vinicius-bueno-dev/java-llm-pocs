# =============================================================================
# Modulo EventBridge — LocalStack
# Cobre: custom event bus, rules com event patterns, targets SQS,
#         scheduled rules e dead-letter queues.
# =============================================================================

# --- Custom Event Bus ---
resource "aws_cloudwatch_event_bus" "custom_bus" {
  name = "${var.project_name}-${var.environment}-poc-custom-bus"

  tags = {
    Name        = "${var.project_name}-poc-custom-bus"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "event-driven"
  }
}

# --- SQS Queue como target ---
resource "aws_sqs_queue" "event_target" {
  name = "${var.project_name}-${var.environment}-poc-event-target"

  tags = {
    Name        = "${var.project_name}-poc-event-target"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "event-driven"
    Type        = "target"
  }
}

# --- SQS Queue Policy (permitir EventBridge publicar) ---
resource "aws_sqs_queue_policy" "event_target_policy" {
  queue_url = aws_sqs_queue.event_target.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowEventBridgePublish"
        Effect    = "Allow"
        Principal = "*"
        Action    = "sqs:SendMessage"
        Resource  = aws_sqs_queue.event_target.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_cloudwatch_event_rule.order_events.arn
          }
        }
      }
    ]
  })
}

# --- DLQ para eventos falhados ---
resource "aws_sqs_queue" "event_dlq" {
  name                      = "${var.project_name}-${var.environment}-poc-event-dlq"
  message_retention_seconds = 1209600

  tags = {
    Name        = "${var.project_name}-poc-event-dlq"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "event-driven"
    Type        = "dead-letter"
  }
}

# --- Rule: Order Events ---
resource "aws_cloudwatch_event_rule" "order_events" {
  name           = "${var.project_name}-${var.environment}-poc-order-events"
  event_bus_name = aws_cloudwatch_event_bus.custom_bus.name
  description    = "Captura eventos de pedidos"

  event_pattern = jsonencode({
    source      = ["com.nameless.orders"]
    detail-type = ["OrderCreated", "OrderUpdated", "OrderCancelled"]
  })

  tags = {
    Name        = "${var.project_name}-poc-order-events"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "event-driven"
  }
}

# --- Target: SQS para Order Events ---
resource "aws_cloudwatch_event_target" "order_events_sqs" {
  rule           = aws_cloudwatch_event_rule.order_events.name
  event_bus_name = aws_cloudwatch_event_bus.custom_bus.name
  target_id      = "order-events-sqs-target"
  arn            = aws_sqs_queue.event_target.arn

  dead_letter_config {
    arn = aws_sqs_queue.event_dlq.arn
  }
}

# --- Rule: All Events (catch-all para observabilidade) ---
resource "aws_cloudwatch_event_rule" "all_events" {
  name           = "${var.project_name}-${var.environment}-poc-all-events"
  event_bus_name = aws_cloudwatch_event_bus.custom_bus.name
  description    = "Captura todos os eventos do bus customizado"

  event_pattern = jsonencode({
    source = [{ "prefix" : "com.nameless" }]
  })

  tags = {
    Name        = "${var.project_name}-poc-all-events"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "event-driven"
  }
}
