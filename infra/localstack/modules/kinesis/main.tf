# =============================================================================
# Modulo Kinesis — LocalStack
# Cobre: data stream com 2 shards para demonstrar partitioning.
# =============================================================================

resource "aws_kinesis_stream" "data_stream" {
  name        = "${var.project_name}-${var.environment}-poc-data-stream"
  shard_count = 2

  retention_period = 24

  tags = {
    Name        = "${var.project_name}-poc-data-stream"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "kinesis-streaming"
  }
}
