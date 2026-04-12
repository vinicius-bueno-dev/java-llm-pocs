output "sender_email" {
  description = "Email do remetente verificado"
  value       = aws_ses_email_identity.sender.email
}

output "recipient_email" {
  description = "Email do destinatario verificado"
  value       = aws_ses_email_identity.recipient.email
}

output "welcome_template_name" {
  description = "Nome do template de boas-vindas"
  value       = aws_ses_template.welcome.name
}
