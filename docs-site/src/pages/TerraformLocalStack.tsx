import { CodeBlock } from '../components/CodeBlock'
import { Table } from '../components/Table'
import { PageNav } from '../components/PageNav'
import { TOC } from '../components/TOC'
import { TerraformFlow } from '../components/diagrams/TerraformFlow'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'connection', title: 'Conexão' },
  { id: 'provider', title: 'Configuração do Provider' },
  { id: 'workflow', title: 'Fluxo de Trabalho' },
  { id: 'flow', title: 'Diagrama do Fluxo' },
  { id: 'best-practices', title: 'Boas Práticas' },
  { id: 'differences', title: 'LocalStack vs AWS' },
]

export function TerraformLocalStack() {
  return (
    <>
      <main className="app-content">
        <div className="section-header">
          <div className="section-header__overline">LocalStack & Terraform</div>
          <h1>Terraform + LocalStack</h1>
        </div>

        <h2 id="connection">Como o Terraform se conecta?</h2>
        <p>
          O provider <code>hashicorp/aws</code> aceita endpoints customizados.
          Apontamos tudo para <code>http://localhost:4566</code> com credenciais fake.
        </p>

        <h2 id="provider">Configuração do Provider</h2>
        <CodeBlock language="hcl" title="providers.tf" code={`provider "aws" {
  region                      = "us-east-1"
  access_key                  = "test"   # fake — LocalStack aceita qualquer valor
  secret_key                  = "test"   # fake — NÃO use credenciais reais aqui
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    s3       = "http://localhost:4566"
    sqs      = "http://localhost:4566"
    dynamodb = "http://localhost:4566"
    # ... outros serviços
  }
}`} />

        <h2 id="workflow">Fluxo de Trabalho</h2>
        <CodeBlock language="bash" code={`# 1. Subir o LocalStack
cd infra && docker-compose up -d

# 2. Aguardar o health check
curl http://localhost:4566/_localstack/health

# 3. Inicializar (baixa providers e módulos)
cd infra/localstack && terraform init

# 4. Ver o que será criado
terraform plan

# 5. Aplicar
terraform apply -auto-approve

# 6. Ver outputs
terraform output

# 7. Destruir (opcional — LocalStack reseta ao reiniciar)
terraform destroy -auto-approve`} />

        <h2 id="flow">Diagrama do Fluxo</h2>
        <TerraformFlow />

        <h2 id="best-practices">Boas Práticas</h2>
        <ol>
          <li><strong>State local</strong> — não use backend remoto para estudos</li>
          <li><strong><code>-auto-approve</code></strong> — agiliza em dev local</li>
          <li><strong>Módulos por serviço</strong> — <code>modules/s3</code>, <code>modules/sqs</code>, etc.</li>
          <li><strong>Variáveis</strong> — sempre use <code>variables.tf</code></li>
          <li><strong>Outputs</strong> — exporte nomes/ARNs para usar nas POCs Java</li>
        </ol>

        <h2 id="differences">LocalStack vs AWS Real</h2>
        <Table
          headers={['Aspecto', 'LocalStack', 'AWS Real']}
          rows={[
            ['Credenciais', 'Qualquer valor ("test")', 'Reais (IAM)'],
            ['ARNs', <><code>000000000000</code></>, 'Seu account_id real'],
            ['S3 URLs', 'Path-style por padrão', 'Virtual-hosted por padrão'],
            ['Custo', 'Gratuito', 'Cobrado por uso'],
            ['Persistência', 'Perde ao reiniciar (sem volume)', 'Permanente'],
          ]}
        />

        <PageNav currentPath="/localstack/terraform" />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
