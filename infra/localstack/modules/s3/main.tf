# =============================================================================
# Modulo S3 — LocalStack
# Cobre: bucket principal, logs, website, versioning, public access block,
#         CORS, encryption, lifecycle, logging, policy, notifications (SQS/SNS)
# =============================================================================

# --- Bucket Principal (POC Storage) ---
resource "aws_s3_bucket" "poc_bucket" {
  bucket = "${var.project_name}-${var.environment}-poc-storage"

  tags = {
    Name        = "${var.project_name}-poc-storage"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "s3-storage"
  }
}

# --- Versionamento ---
resource "aws_s3_bucket_versioning" "poc_bucket_versioning" {
  bucket = aws_s3_bucket.poc_bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}

# --- Bloquear acesso publico ---
resource "aws_s3_bucket_public_access_block" "poc_bucket_access" {
  bucket = aws_s3_bucket.poc_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# --- CORS ---
resource "aws_s3_bucket_cors_configuration" "poc_cors" {
  bucket = aws_s3_bucket.poc_bucket.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = ["http://localhost:3000", "http://localhost:8080"]
    expose_headers  = ["ETag", "x-amz-version-id"]
    max_age_seconds = 3600
  }
}

# --- Server-Side Encryption (SSE-S3) ---
resource "aws_s3_bucket_server_side_encryption_configuration" "poc_encryption" {
  bucket = aws_s3_bucket.poc_bucket.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

# --- Lifecycle Rules ---
resource "aws_s3_bucket_lifecycle_configuration" "poc_lifecycle" {
  bucket = aws_s3_bucket.poc_bucket.id

  rule {
    id     = "expire-temp-files"
    status = "Enabled"

    filter {
      prefix = "temp/"
    }

    expiration {
      days = 7
    }
  }

  rule {
    id     = "transition-archive"
    status = "Enabled"

    filter {
      prefix = "archive/"
    }

    transition {
      days          = 30
      storage_class = "GLACIER"
    }

    expiration {
      days = 365
    }
  }

  rule {
    id     = "cleanup-incomplete-multipart"
    status = "Enabled"

    filter {
      prefix = ""
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}

# --- Bucket Policy (leitura publica em public/*) ---
resource "aws_s3_bucket_policy" "poc_policy" {
  bucket = aws_s3_bucket.poc_bucket.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowPublicRead"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.poc_bucket.arn}/public/*"
      }
    ]
  })

  depends_on = [aws_s3_bucket_public_access_block.poc_bucket_access]
}

# =============================================================================
# Bucket de Logs (target para access logging)
# =============================================================================

resource "aws_s3_bucket" "logs_bucket" {
  bucket = "${var.project_name}-${var.environment}-poc-logs"

  tags = {
    Name        = "${var.project_name}-poc-logs"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "s3-storage"
    Purpose     = "access-logging"
  }
}

# --- Access Logging (poc_bucket -> logs_bucket) ---
resource "aws_s3_bucket_logging" "poc_logging" {
  bucket = aws_s3_bucket.poc_bucket.id

  target_bucket = aws_s3_bucket.logs_bucket.id
  target_prefix = "access-logs/"
}

# =============================================================================
# Bucket Website (static website hosting)
# =============================================================================

resource "aws_s3_bucket" "website_bucket" {
  bucket = "${var.project_name}-${var.environment}-poc-website"

  tags = {
    Name        = "${var.project_name}-poc-website"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "s3-storage"
    Purpose     = "static-website"
  }
}

resource "aws_s3_bucket_website_configuration" "website_config" {
  bucket = aws_s3_bucket.website_bucket.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "error.html"
  }
}

# =============================================================================
# Event Notifications — SQS Queue
# =============================================================================

resource "aws_sqs_queue" "s3_event_queue" {
  name = "${var.project_name}-${var.environment}-s3-events"

  tags = {
    Name        = "${var.project_name}-s3-events"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "s3-storage"
  }
}

resource "aws_sqs_queue_policy" "s3_event_queue_policy" {
  queue_url = aws_sqs_queue.s3_event_queue.url

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowS3SendMessage"
        Effect    = "Allow"
        Principal = { Service = "s3.amazonaws.com" }
        Action    = "sqs:SendMessage"
        Resource  = aws_sqs_queue.s3_event_queue.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_s3_bucket.poc_bucket.arn
          }
        }
      }
    ]
  })
}

# =============================================================================
# Event Notifications — SNS Topic
# =============================================================================

resource "aws_sns_topic" "s3_event_topic" {
  name = "${var.project_name}-${var.environment}-s3-events"

  tags = {
    Name        = "${var.project_name}-s3-events"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "s3-storage"
  }
}

resource "aws_sns_topic_policy" "s3_event_topic_policy" {
  arn = aws_sns_topic.s3_event_topic.arn

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowS3Publish"
        Effect    = "Allow"
        Principal = { Service = "s3.amazonaws.com" }
        Action    = "sns:Publish"
        Resource  = aws_sns_topic.s3_event_topic.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_s3_bucket.poc_bucket.arn
          }
        }
      }
    ]
  })
}

# =============================================================================
# S3 Bucket Notification (ObjectCreated -> SQS, ObjectRemoved -> SNS)
# =============================================================================

resource "aws_s3_bucket_notification" "poc_notification" {
  bucket = aws_s3_bucket.poc_bucket.id

  queue {
    queue_arn     = aws_sqs_queue.s3_event_queue.arn
    events        = ["s3:ObjectCreated:*"]
    filter_prefix = "uploads/"
  }

  topic {
    topic_arn     = aws_sns_topic.s3_event_topic.arn
    events        = ["s3:ObjectRemoved:*"]
    filter_prefix = "uploads/"
  }

  depends_on = [
    aws_sqs_queue_policy.s3_event_queue_policy,
    aws_sns_topic_policy.s3_event_topic_policy,
  ]
}
