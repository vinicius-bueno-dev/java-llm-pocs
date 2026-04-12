# =============================================================================
# Modulo IAM — LocalStack
# Cobre: roles, policies e policy attachments para demonstrar IAM operations.
# =============================================================================

# --- Role de aplicacao ---
resource "aws_iam_role" "app_role" {
  name = "${var.project_name}-${var.environment}-poc-app-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          AWS = "*"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-poc-app-role"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "iam-policies"
  }
}

# --- Policy de leitura S3 ---
resource "aws_iam_policy" "s3_read" {
  name        = "${var.project_name}-${var.environment}-poc-s3-read"
  description = "Permite leitura em buckets S3"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "S3ReadAccess"
        Effect   = "Allow"
        Action   = ["s3:GetObject", "s3:ListBucket"]
        Resource = "*"
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-poc-s3-read"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "iam-policies"
  }
}

# --- Attachment ---
resource "aws_iam_role_policy_attachment" "app_s3_read" {
  role       = aws_iam_role.app_role.name
  policy_arn = aws_iam_policy.s3_read.arn
}
