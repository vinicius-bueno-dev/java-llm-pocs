import { CodeBlock } from '../components/CodeBlock'
import { Table } from '../components/Table'
import { Callout } from '../components/Callout'
import { PageNav } from '../components/PageNav'
import { TOC } from '../components/TOC'
import { Breadcrumbs } from '../components/Breadcrumbs'
import { Footer } from '../components/Footer'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'visao-geral', title: 'Visao Geral' },
  { id: 'arquitetura', title: 'Arquitetura' },
  { id: 'terraform', title: 'Infraestrutura Terraform' },
  { id: 'identities', title: 'Identidades' },
  { id: 'send-email', title: 'Envio de Email Simples' },
  { id: 'html-email', title: 'Email HTML' },
  { id: 'raw-email', title: 'Email Raw com Anexo' },
  { id: 'templates', title: 'Templates' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocSesEmail() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>SES Email</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora o Amazon Simple Email Service (SES) em uma aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para gerenciar identidades verificadas,
          enviar emails em diferentes formatos e trabalhar com templates reutilizaveis.
        </p>
        <p>
          As funcionalidades cobertas incluem verificacao de identidades (emails),
          envio de email em texto puro, email HTML com fallback em texto, email raw
          com anexos (MIME manual), templates SES com variaveis de substituicao
          e envio de emails em massa (bulk templated).
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O SES no LocalStack
            auto-verifica identidades imediatamente, sem necessidade de clicar
            em links de confirmacao. Os emails nao sao realmente enviados — o
            LocalStack apenas simula a API.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ARQUITETURA */}
        {/* ================================================================ */}
        <h2 id="arquitetura" className="heading-anchor">
          <a href="#arquitetura" className="heading-anchor__link" aria-hidden="true">#</a>
          Arquitetura
        </h2>
        <p>
          A aplicacao segue a estrutura padrao das POCs: camadas de{' '}
          <strong>Controller</strong> e <strong>Service</strong> com injecao por construtor.
          Sao 3 controllers e 3 services, cada um responsavel por uma area funcional:
          identidades, envio de emails e templates.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.sesemail/
  config/         SesConfig (SesClient bean)
  controller/     IdentityController, EmailController,
                  TemplateController
  service/        IdentityService, EmailSenderService,
                  TemplateService
  dto/            Records: SendEmailDto, SendTemplatedEmailDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8093</code> para nao conflitar com as
            demais POCs (S3 na 8080, SQS na 8081, DynamoDB na 8082).
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* TERRAFORM */}
        {/* ================================================================ */}
        <h2 id="terraform" className="heading-anchor">
          <a href="#terraform" className="heading-anchor__link" aria-hidden="true">#</a>
          Infraestrutura Terraform
        </h2>
        <p>
          O modulo Terraform em <code>infra/localstack/modules/ses</code> provisiona
          identidades de email verificadas e um template de exemplo.
        </p>
        <Table
          headers={['Recurso', 'Nome / Email', 'Proposito']}
          rows={[
            [<code>aws_ses_email_identity</code>, 'noreply@nameless.dev', 'Identidade do remetente (from)'],
            [<code>aws_ses_email_identity</code>, 'test@nameless.dev', 'Identidade do destinatario para testes'],
            [<code>aws_ses_template</code>, 'poc-welcome', 'Template de boas-vindas com variaveis name e system'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/ses/main.tf" code={`resource "aws_ses_email_identity" "sender" {
  email = "noreply@nameless.dev"
}

resource "aws_ses_email_identity" "recipient" {
  email = "test@nameless.dev"
}

resource "aws_ses_template" "welcome" {
  name    = "\${var.project_name}-\${var.environment}-poc-welcome"
  subject = "Bem-vindo, {{name}}!"
  html    = "<h1>Ola {{name}}</h1><p>Bem-vindo ao sistema {{system}}.</p>"
  text    = "Ola {{name}}, bem-vindo ao sistema {{system}}."
}`} />

        <Callout type="info">
          <p>
            No LocalStack, identidades sao verificadas automaticamente ao serem criadas.
            Na AWS real, o SES envia um email de confirmacao que o usuario precisa clicar.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* IDENTITIES */}
        {/* ================================================================ */}
        <h2 id="identities" className="heading-anchor">
          <a href="#identities" className="heading-anchor__link" aria-hidden="true">#</a>
          Identidades
        </h2>
        <p>
          Antes de enviar emails pelo SES, e necessario verificar as identidades
          (enderecos de email ou dominios). O <code>IdentityService</code> encapsula
          as operacoes de verificacao, listagem, consulta de status e remocao.
        </p>
        <CodeBlock language="java" title="IdentityService.java" code={`// Verificar um endereco de email
sesClient.verifyEmailIdentity(VerifyEmailIdentityRequest.builder()
    .emailAddress(email)
    .build());

// Listar todas as identidades registradas
List<String> identities = sesClient.listIdentities(
    ListIdentitiesRequest.builder().build()).identities();

// Consultar status de verificacao
Map<String, IdentityVerificationAttributes> attrs =
    sesClient.getIdentityVerificationAttributes(
        GetIdentityVerificationAttributesRequest.builder()
            .identities(identities)
            .build()).verificationAttributes();

// Remover uma identidade
sesClient.deleteIdentity(DeleteIdentityRequest.builder()
    .identity(identity)
    .build());`} />

        <Callout type="tip">
          <p>
            No LocalStack, toda identidade fica com status <code>Success</code> imediatamente
            apos a chamada <code>verifyEmailIdentity</code>. Ideal para testes automatizados.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* SEND EMAIL */}
        {/* ================================================================ */}
        <h2 id="send-email" className="heading-anchor">
          <a href="#send-email" className="heading-anchor__link" aria-hidden="true">#</a>
          Envio de Email Simples
        </h2>
        <p>
          O envio mais basico usa <code>sendEmail</code> com corpo em texto puro.
          O remetente e configurado via <code>application.yml</code> na propriedade{' '}
          <code>aws.ses.from-email</code>. O DTO <code>SendEmailDto</code> encapsula
          os campos: <code>to</code>, <code>subject</code>, <code>bodyText</code> e{' '}
          <code>bodyHtml</code>.
        </p>
        <CodeBlock language="java" title="EmailSenderService.java — sendEmail" code={`public String sendEmail(SendEmailDto dto) {
    SendEmailResponse response = sesClient.sendEmail(
        SendEmailRequest.builder()
            .source(fromEmail)
            .destination(Destination.builder()
                .toAddresses(dto.to())
                .build())
            .message(Message.builder()
                .subject(content(dto.subject()))
                .body(Body.builder()
                    .text(content(dto.bodyText()))
                    .build())
                .build())
            .build());
    return response.messageId();
}

private Content content(String data) {
    return Content.builder()
        .data(data).charset("UTF-8").build();
}`} />

        {/* ================================================================ */}
        {/* HTML EMAIL */}
        {/* ================================================================ */}
        <h2 id="html-email" className="heading-anchor">
          <a href="#html-email" className="heading-anchor__link" aria-hidden="true">#</a>
          Email HTML
        </h2>
        <p>
          O metodo <code>sendHtmlEmail</code> envia emails com corpo HTML e um
          fallback opcional em texto puro. Clientes de email que nao suportam HTML
          exibem a versao em texto. Ambos os campos (<code>bodyHtml</code> e{' '}
          <code>bodyText</code>) sao opcionais no DTO, mas pelo menos um deve
          ser fornecido.
        </p>
        <CodeBlock language="java" title="EmailSenderService.java — sendHtmlEmail" code={`public String sendHtmlEmail(SendEmailDto dto) {
    Body.Builder bodyBuilder = Body.builder();
    if (dto.bodyText() != null) {
        bodyBuilder.text(content(dto.bodyText()));
    }
    if (dto.bodyHtml() != null) {
        bodyBuilder.html(content(dto.bodyHtml()));
    }

    SendEmailResponse response = sesClient.sendEmail(
        SendEmailRequest.builder()
            .source(fromEmail)
            .destination(Destination.builder()
                .toAddresses(dto.to()).build())
            .message(Message.builder()
                .subject(content(dto.subject()))
                .body(bodyBuilder.build()).build())
            .build());
    return response.messageId();
}`} />

        <Callout type="tip">
          <p>
            Sempre inclua uma versao em texto puro como fallback. Alguns clientes
            de email corporativos bloqueiam HTML por padrao.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* RAW EMAIL */}
        {/* ================================================================ */}
        <h2 id="raw-email" className="heading-anchor">
          <a href="#raw-email" className="heading-anchor__link" aria-hidden="true">#</a>
          Email Raw com Anexo
        </h2>
        <p>
          Para enviar emails com anexos, o SES requer o uso de <code>sendRawEmail</code>.
          A mensagem MIME e montada manualmente com boundary, corpo em texto e o
          anexo codificado em Base64. Nao ha dependencia de <code>javax.mail</code>.
        </p>
        <p>
          O endpoint aceita <code>multipart/form-data</code> com os campos{' '}
          <code>to</code>, <code>subject</code>, <code>bodyText</code> e{' '}
          <code>attachment</code> (arquivo).
        </p>
        <CodeBlock language="java" title="EmailSenderService.java — sendRawEmail (trecho)" code={`String boundary = "----=_Part_" + UUID.randomUUID()
    .toString().replace("-", "");

StringBuilder raw = new StringBuilder();
raw.append("From: ").append(fromEmail).append("\\r\\n");
raw.append("To: ").append(to).append("\\r\\n");
raw.append("Subject: ").append(subject).append("\\r\\n");
raw.append("MIME-Version: 1.0\\r\\n");
raw.append("Content-Type: multipart/mixed; boundary=\\"")
    .append(boundary).append("\\"\\r\\n\\r\\n");

// Parte texto
raw.append("--").append(boundary).append("\\r\\n");
raw.append("Content-Type: text/plain; charset=UTF-8\\r\\n\\r\\n");
raw.append(bodyText).append("\\r\\n");

// Parte anexo (Base64)
raw.append("--").append(boundary).append("\\r\\n");
raw.append("Content-Type: ").append(contentType)
    .append("; name=\\"").append(attachmentName).append("\\"\\r\\n");
raw.append("Content-Disposition: attachment; filename=\\"")
    .append(attachmentName).append("\\"\\r\\n");
raw.append("Content-Transfer-Encoding: base64\\r\\n\\r\\n");
raw.append(Base64.getMimeEncoder(76, "\\r\\n".getBytes())
    .encodeToString(attachmentData)).append("\\r\\n");
raw.append("--").append(boundary).append("--\\r\\n");

// Enviar via SES
sesClient.sendRawEmail(SendRawEmailRequest.builder()
    .rawMessage(RawMessage.builder()
        .data(SdkBytes.fromByteArray(
            raw.toString().getBytes(StandardCharsets.UTF_8)))
        .build())
    .build());`} />

        <Callout type="warning">
          <p>
            O tamanho maximo de uma mensagem raw no SES e de 10 MB (incluindo
            headers, corpo e anexos codificados em Base64). Lembre-se que Base64
            aumenta o tamanho do arquivo em ~33%.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* TEMPLATES */}
        {/* ================================================================ */}
        <h2 id="templates" className="heading-anchor">
          <a href="#templates" className="heading-anchor__link" aria-hidden="true">#</a>
          Templates
        </h2>
        <p>
          Templates SES permitem reutilizar layouts de email com variaveis de
          substituicao (ex.: <code>{'{{name}}'}</code>, <code>{'{{orderId}}'}</code>).
          O <code>TemplateService</code> gerencia o ciclo de vida completo: criacao,
          consulta, listagem, remocao e envio de emails usando templates.
        </p>
        <CodeBlock language="java" title="TemplateService.java — criar e enviar" code={`// Criar um template
sesClient.createTemplate(CreateTemplateRequest.builder()
    .template(Template.builder()
        .templateName("welcome-email")
        .subjectPart("Bem-vindo, {{name}}!")
        .htmlPart("<h1>Ola {{name}}</h1><p>Sistema: {{system}}</p>")
        .textPart("Ola {{name}}, sistema {{system}}.")
        .build())
    .build());

// Listar templates
List<TemplateMetadata> templates = sesClient.listTemplates(
    ListTemplatesRequest.builder().build()).templatesMetadata();

// Obter detalhes de um template
Template tpl = sesClient.getTemplate(GetTemplateRequest.builder()
    .templateName("welcome-email").build()).template();

// Enviar email com template
sesClient.sendTemplatedEmail(SendTemplatedEmailRequest.builder()
    .source(fromEmail)
    .destination(Destination.builder()
        .toAddresses("user@example.com").build())
    .template("welcome-email")
    .templateData("{\\"name\\":\\"Joao\\",\\"system\\":\\"Nameless\\"}")
    .build());`} />

        <p>
          O DTO <code>SendTemplatedEmailDto</code> recebe <code>to</code>,{' '}
          <code>templateName</code> e <code>templateData</code> (um{' '}
          <code>{'Map<String, String>'}</code> convertido para JSON internamente).
        </p>
        <p>
          O service tambem suporta envio em massa via <code>sendBulkEmail</code>,
          que usa <code>SendBulkTemplatedEmail</code> para enviar o mesmo template
          a multiplos destinatarios com dados de substituicao individuais.
        </p>

        {/* ================================================================ */}
        {/* API REFERENCE */}
        {/* ================================================================ */}
        <h2 id="api-reference" className="heading-anchor">
          <a href="#api-reference" className="heading-anchor__link" aria-hidden="true">#</a>
          Referencia da API
        </h2>
        <p>
          A tabela abaixo lista todos os endpoints expostos pela POC.
          Todos usam o prefixo base <code>/api/ses</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            [<code>POST</code>, <code>/api/ses/identities/verify</code>, 'Verificar um endereco de email'],
            [<code>GET</code>, <code>/api/ses/identities</code>, 'Listar todas as identidades'],
            [<code>GET</code>, <code>/api/ses/identities/verification?identities=...</code>, 'Consultar status de verificacao'],
            [<code>DELETE</code>, <code>/api/ses/identities/{'{identity}'}</code>, 'Remover uma identidade'],
            [<code>POST</code>, <code>/api/ses/emails/send</code>, 'Enviar email simples (texto puro)'],
            [<code>POST</code>, <code>/api/ses/emails/send-html</code>, 'Enviar email HTML com fallback em texto'],
            [<code>POST</code>, <code>/api/ses/emails/send-raw</code>, 'Enviar email raw com anexo (multipart form)'],
            [<code>POST</code>, <code>/api/ses/templates</code>, 'Criar um template SES'],
            [<code>GET</code>, <code>/api/ses/templates/{'{templateName}'}</code>, 'Obter detalhes de um template'],
            [<code>GET</code>, <code>/api/ses/templates</code>, 'Listar todos os templates'],
            [<code>DELETE</code>, <code>/api/ses/templates/{'{templateName}'}</code>, 'Remover um template'],
            [<code>POST</code>, <code>/api/ses/templates/send</code>, 'Enviar email usando um template'],
          ]}
        />

        {/* ================================================================ */}
        {/* COMO RODAR */}
        {/* ================================================================ */}
        <h2 id="rodando" className="heading-anchor">
          <a href="#rodando" className="heading-anchor__link" aria-hidden="true">#</a>
          Como Rodar
        </h2>
        <p>
          Siga os 4 passos abaixo para executar a POC localmente.
        </p>

        <h3>1. Subir o LocalStack</h3>
        <CodeBlock language="bash" code={`cd infra && docker-compose up -d

# Verificar se esta pronto
curl http://localhost:4566/_localstack/health`} />

        <h3>2. Provisionar infraestrutura com Terraform</h3>
        <CodeBlock language="bash" code={`cd infra/localstack
terraform init
terraform apply -auto-approve`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-ses-email
./mvnw spring-boot:run

# Roda na porta 8093`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Verificar uma identidade de email
curl -X POST "http://localhost:8093/api/ses/identities/verify" \\
  -H "Content-Type: application/json" \\
  -d '{"email": "user@example.com"}'

# Listar identidades
curl "http://localhost:8093/api/ses/identities"

# Enviar email simples (texto puro)
curl -X POST "http://localhost:8093/api/ses/emails/send" \\
  -H "Content-Type: application/json" \\
  -d '{
    "to": "test@nameless.dev",
    "subject": "Teste SES",
    "bodyText": "Ola, este e um email de teste!"
  }'

# Enviar email HTML
curl -X POST "http://localhost:8093/api/ses/emails/send-html" \\
  -H "Content-Type: application/json" \\
  -d '{
    "to": "test@nameless.dev",
    "subject": "Teste HTML",
    "bodyText": "Fallback texto",
    "bodyHtml": "<h1>Ola</h1><p>Email em HTML</p>"
  }'

# Enviar email raw com anexo
curl -X POST "http://localhost:8093/api/ses/emails/send-raw" \\
  -F "to=test@nameless.dev" \\
  -F "subject=Teste com anexo" \\
  -F "bodyText=Segue o arquivo em anexo." \\
  -F "attachment=@/caminho/para/arquivo.pdf"

# Criar template
curl -X POST "http://localhost:8093/api/ses/templates" \\
  -H "Content-Type: application/json" \\
  -d '{
    "templateName": "meu-template",
    "subject": "Ola {{name}}",
    "htmlBody": "<h1>Bem-vindo {{name}}</h1>",
    "textBody": "Bem-vindo {{name}}"
  }'

# Enviar email com template
curl -X POST "http://localhost:8093/api/ses/templates/send" \\
  -H "Content-Type: application/json" \\
  -d '{
    "to": "test@nameless.dev",
    "templateName": "meu-template",
    "templateData": {"name": "Joao"}
  }'

# Listar templates
curl "http://localhost:8093/api/ses/templates"`} />

        <Callout type="warning">
          <p>
            No LocalStack, os emails nao sao realmente enviados. A API retorna
            um <code>messageId</code> simulado. Para verificar o conteudo dos
            emails enviados, use o endpoint interno do LocalStack:{' '}
            <code>GET http://localhost:4566/_aws/ses</code>.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/ses-email" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
