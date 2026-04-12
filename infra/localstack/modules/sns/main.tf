# =============================================================================
# Modulo SNS — LocalStack
# Cobre: topic standard, topic FIFO, subscriptions SQS com filter policies,
#         dead-letter queue para subscriptions falhadas.
# =============================================================================

# --- Topic Standard ---
resource "aws_sns_topic" "standard_topic" {
  name = "${var.project_name}-${var.environment}-poc-notifications"

  tags = {
    Name        = "${var.project_name}-poc-notifications"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sns-notifications"
    Type        = "standard"
  }
}

# --- Topic FIFO ---
resource "aws_sns_topic" "fifo_topic" {
  name                        = "${var.project_name}-${var.environment}-poc-notifications.fifo"
  fifo_topic                  = true
  content_based_deduplication = true

  tags = {
    Name        = "${var.project_name}-poc-notifications-fifo"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sns-notifications"
    Type        = "fifo"
  }
}

# --- SQS Queues para fan-out (subscribers) ---
resource "aws_sqs_queue" "subscriber_orders" {
  name = "${var.project_name}-${var.environment}-poc-sns-orders"

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.sns_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name        = "${var.project_name}-poc-sns-orders"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sns-notifications"
    Type        = "subscriber"
  }
}

resource "aws_sqs_queue" "subscriber_analytics" {
  name = "${var.project_name}-${var.environment}-poc-sns-analytics"

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.sns_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name        = "${var.project_name}-poc-sns-analytics"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sns-notifications"
    Type        = "subscriber"
  }
}

# --- Dead-Letter Queue para subscriptions ---
resource "aws_sqs_queue" "sns_dlq" {
  name                      = "${var.project_name}-${var.environment}-poc-sns-dlq"
  message_retention_seconds = 1209600 # 14 dias

  tags = {
    Name        = "${var.project_name}-poc-sns-dlq"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sns-notifications"
    Type        = "dead-letter"
  }
}

# --- Queue Policies (permitir SNS publicar nas filas) ---
resource "aws_sqs_queue_policy" "orders_policy" {
  queue_url = aws_sqs_queue.subscriber_orders.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowSNSPublish"
        Effect    = "Allow"
        Principal = "*"
        Action    = "sqs:SendMessage"
        Resource  = aws_sqs_queue.subscriber_orders.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.standard_topic.arn
          }
        }
      }
    ]
  })
}

resource "aws_sqs_queue_policy" "analytics_policy" {
  queue_url = aws_sqs_queue.subscriber_analytics.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowSNSPublish"
        Effect    = "Allow"
        Principal = "*"
        Action    = "sqs:SendMessage"
        Resource  = aws_sqs_queue.subscriber_analytics.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.standard_topic.arn
          }
        }
      }
    ]
  })
}

# --- Subscriptions SNS → SQS com filter policies ---
resource "aws_sns_topic_subscription" "orders_sub" {
  topic_arn = aws_sns_topic.standard_topic.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.subscriber_orders.arn

  raw_message_delivery = true

  filter_policy = jsonencode({
    eventType = ["order_created", "order_updated", "order_cancelled"]
  })

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.sns_dlq.arn
  })
}

resource "aws_sns_topic_subscription" "analytics_sub" {
  topic_arn = aws_sns_topic.standard_topic.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.subscriber_analytics.arn

  raw_message_delivery = true

  filter_policy = jsonencode({
    eventType = ["order_created", "payment_processed", "user_registered"]
  })

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.sns_dlq.arn
  })
}
