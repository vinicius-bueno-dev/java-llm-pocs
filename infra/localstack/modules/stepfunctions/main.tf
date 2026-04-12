# =============================================================================
# Modulo Step Functions — LocalStack
# Cobre: IAM role para execucao e state machine com Parallel + Choice + Wait.
# =============================================================================

# --- IAM Role para Step Functions ---
resource "aws_iam_role" "sfn_role" {
  name = "${var.project_name}-${var.environment}-poc-sfn-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "states.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-poc-sfn-role"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "step-functions"
  }
}

# --- State Machine: Order Processing Workflow ---
resource "aws_sfn_state_machine" "order_workflow" {
  name     = "${var.project_name}-${var.environment}-poc-order-workflow"
  role_arn = aws_iam_role.sfn_role.arn

  definition = jsonencode({
    Comment = "POC order processing workflow with Parallel, Choice and Wait"
    StartAt = "ValidateOrder"
    States = {
      ValidateOrder = {
        Type   = "Pass"
        Result = { "validated" = true }
        Next   = "CheckInventory"
      }
      CheckInventory = {
        Type = "Choice"
        Choices = [
          {
            Variable      = "$.validated"
            BooleanEquals = true
            Next          = "ProcessParallel"
          }
        ]
        Default = "OrderFailed"
      }
      ProcessParallel = {
        Type = "Parallel"
        Branches = [
          {
            StartAt = "ChargePayment"
            States = {
              ChargePayment = {
                Type   = "Pass"
                Result = { "payment" = "charged" }
                End    = true
              }
            }
          },
          {
            StartAt = "ReserveInventory"
            States = {
              ReserveInventory = {
                Type   = "Pass"
                Result = { "inventory" = "reserved" }
                End    = true
              }
            }
          }
        ]
        Next = "WaitForConfirmation"
      }
      WaitForConfirmation = {
        Type    = "Wait"
        Seconds = 1
        Next    = "OrderComplete"
      }
      OrderComplete = {
        Type = "Pass"
        Result = {
          "status" = "completed"
        }
        End = true
      }
      OrderFailed = {
        Type  = "Fail"
        Error = "OrderValidationFailed"
        Cause = "Order did not pass validation"
      }
    }
  })

  tags = {
    Name        = "${var.project_name}-poc-order-workflow"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "step-functions"
  }
}
