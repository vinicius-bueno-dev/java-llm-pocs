# =============================================================================
# Modulo SQS — LocalStack
# Cobre: standard queue, FIFO queue, dead-letter queues, delay queue,
#         redrive policies, queue policies e tags
# =============================================================================

# --- Standard Queue ---
resource "aws_sqs_queue" "standard_queue" {
  name                       = "${var.project_name}-${var.environment}-poc-standard-queue"
  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600 # 4 dias
  receive_wait_time_seconds  = 0

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name        = "${var.project_name}-poc-standard-queue"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sqs-messaging"
    Type        = "standard"
  }
}

resource "aws_sqs_queue_policy" "standard_queue_policy" {
  queue_url = aws_sqs_queue.standard_queue.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowSendMessage"
        Effect    = "Allow"
        Principal = "*"
        Action    = "sqs:SendMessage"
        Resource  = aws_sqs_queue.standard_queue.arn
      }
    ]
  })
}

# --- Dead-Letter Queue (Standard) ---
resource "aws_sqs_queue" "dlq" {
  name                      = "${var.project_name}-${var.environment}-poc-dlq"
  message_retention_seconds = 1209600 # 14 dias

  tags = {
    Name        = "${var.project_name}-poc-dlq"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sqs-messaging"
    Type        = "dead-letter"
  }
}

# --- FIFO Queue ---
resource "aws_sqs_queue" "fifo_queue" {
  name                        = "${var.project_name}-${var.environment}-poc-fifo-queue.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  visibility_timeout_seconds  = 30
  message_retention_seconds   = 345600

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.fifo_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name        = "${var.project_name}-poc-fifo-queue"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sqs-messaging"
    Type        = "fifo"
  }
}

# --- Dead-Letter Queue (FIFO) ---
resource "aws_sqs_queue" "fifo_dlq" {
  name                      = "${var.project_name}-${var.environment}-poc-fifo-dlq.fifo"
  fifo_queue                = true
  message_retention_seconds = 1209600

  tags = {
    Name        = "${var.project_name}-poc-fifo-dlq"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sqs-messaging"
    Type        = "dead-letter-fifo"
  }
}

# --- Delay Queue ---
resource "aws_sqs_queue" "delay_queue" {
  name          = "${var.project_name}-${var.environment}-poc-delay-queue"
  delay_seconds = 10

  tags = {
    Name        = "${var.project_name}-poc-delay-queue"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sqs-messaging"
    Type        = "delay"
  }
}
