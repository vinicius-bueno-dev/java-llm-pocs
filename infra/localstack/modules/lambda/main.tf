# =============================================================================
# Modulo Lambda — LocalStack
# Cobre: IAM role de execucao, bucket para deploy de artefatos,
#         fila SQS como event source, funcoes placeholder (deploy via POC).
# =============================================================================

# --- IAM Role para execucao Lambda ---
resource "aws_iam_role" "lambda_exec" {
  name = "${var.project_name}-${var.environment}-poc-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-poc-lambda-role"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "lambda-java"
  }
}

# --- Policy basica (logs + SQS + S3 read) ---
resource "aws_iam_role_policy" "lambda_policy" {
  name = "${var.project_name}-${var.environment}-poc-lambda-policy"
  role = aws_iam_role.lambda_exec.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "CloudWatchLogs"
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "arn:aws:logs:*:*:*"
      },
      {
        Sid    = "SQSAccess"
        Effect = "Allow"
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = aws_sqs_queue.lambda_trigger.arn
      },
      {
        Sid    = "S3ReadAccess"
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.lambda_artifacts.arn,
          "${aws_s3_bucket.lambda_artifacts.arn}/*"
        ]
      }
    ]
  })
}

# --- S3 Bucket para artefatos Lambda (JARs) ---
resource "aws_s3_bucket" "lambda_artifacts" {
  bucket = "${var.project_name}-${var.environment}-poc-lambda-artifacts"

  tags = {
    Name        = "${var.project_name}-poc-lambda-artifacts"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "lambda-java"
    Type        = "artifacts"
  }
}

# --- SQS Queue como event source para Lambda ---
resource "aws_sqs_queue" "lambda_trigger" {
  name                       = "${var.project_name}-${var.environment}-poc-lambda-trigger"
  visibility_timeout_seconds = 60

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.lambda_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name        = "${var.project_name}-poc-lambda-trigger"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "lambda-java"
    Type        = "trigger"
  }
}

# --- Dead-Letter Queue para Lambda ---
resource "aws_sqs_queue" "lambda_dlq" {
  name                      = "${var.project_name}-${var.environment}-poc-lambda-dlq"
  message_retention_seconds = 1209600 # 14 dias

  tags = {
    Name        = "${var.project_name}-poc-lambda-dlq"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "lambda-java"
    Type        = "dead-letter"
  }
}
