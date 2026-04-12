output "sfn_role_arn" {
  description = "ARN da IAM role para Step Functions"
  value       = aws_iam_role.sfn_role.arn
}

output "order_workflow_arn" {
  description = "ARN da state machine de order workflow"
  value       = aws_sfn_state_machine.order_workflow.arn
}

output "order_workflow_name" {
  description = "Nome da state machine de order workflow"
  value       = aws_sfn_state_machine.order_workflow.name
}
