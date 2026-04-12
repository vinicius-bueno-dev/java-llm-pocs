# =============================================================================
# Modulo SES — LocalStack
# Cobre: identidades verificadas e template de email.
# LocalStack auto-verifica identidades, entao nao ha espera.
# =============================================================================

resource "aws_ses_email_identity" "sender" {
  email = "noreply@nameless.dev"
}

resource "aws_ses_email_identity" "recipient" {
  email = "test@nameless.dev"
}

resource "aws_ses_template" "welcome" {
  name    = "${var.project_name}-${var.environment}-poc-welcome"
  subject = "Bem-vindo, {{name}}!"
  html    = "<h1>Ola {{name}}</h1><p>Bem-vindo ao sistema {{system}}.</p>"
  text    = "Ola {{name}}, bem-vindo ao sistema {{system}}."
}
