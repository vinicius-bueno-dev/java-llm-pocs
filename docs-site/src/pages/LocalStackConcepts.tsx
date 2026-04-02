import { CodeBlock } from '../components/CodeBlock'
import { Table } from '../components/Table'
import { PageNav } from '../components/PageNav'
import { TOC } from '../components/TOC'
import { Breadcrumbs } from '../components/Breadcrumbs'
import { Footer } from '../components/Footer'
import { LocalStackArchitecture } from '../components/diagrams/LocalStackArchitecture'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'what', title: 'O que e?' },
  { id: 'why', title: 'Por que usar?' },
  { id: 'how', title: 'Como funciona' },
  { id: 'services', title: 'Servicos Disponiveis' },
  { id: 'health', title: 'Verificando Status' },
  { id: 'awscli', title: 'AWS CLI + LocalStack' },
]

export function LocalStackConcepts() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">LocalStack & Terraform</div>
          <h1>Conceitos Fundamentais</h1>
        </div>

        <h2 id="what" className="heading-anchor">
          <a href="#what" className="heading-anchor__link" aria-hidden="true">#</a>
          O que e?
        </h2>
        <p>
          Plataforma que simula servicos AWS localmente via Docker.
          Crie buckets S3, filas SQS, tabelas DynamoDB — sem custo e sem conta AWS.
        </p>

        <h2 id="why" className="heading-anchor">
          <a href="#why" className="heading-anchor__link" aria-hidden="true">#</a>
          Por que usar?
        </h2>
        <ul>
          <li><strong>Gratuito</strong> — Community Edition cobre os servicos mais usados</li>
          <li><strong>Offline</strong> — funciona sem internet</li>
          <li><strong>Rapido</strong> — sem latencia de rede real</li>
          <li><strong>Seguro</strong> — zero risco de cobrancas acidentais</li>
          <li><strong>Ideal</strong> — para desenvolvimento e testes locais</li>
        </ul>

        <h2 id="how" className="heading-anchor">
          <a href="#how" className="heading-anchor__link" aria-hidden="true">#</a>
          Como funciona
        </h2>
        <p>
          Todos os servicos sao acessiveis por um unico endpoint: <code>http://localhost:4566</code>.
          O SDK identifica qual servico usar pelo path e headers da requisicao.
        </p>
        <LocalStackArchitecture />

        <h2 id="services" className="heading-anchor">
          <a href="#services" className="heading-anchor__link" aria-hidden="true">#</a>
          Servicos Disponiveis (Community)
        </h2>
        <Table
          headers={['Servico', 'Uso']}
          rows={[
            [<strong>S3</strong>, 'Armazenamento de objetos'],
            [<strong>SQS</strong>, 'Filas de mensagens'],
            [<strong>SNS</strong>, 'Notificacoes pub/sub'],
            [<strong>DynamoDB</strong>, 'Banco NoSQL'],
            [<strong>Lambda</strong>, 'Funcoes serverless'],
            [<strong>API Gateway</strong>, 'Gateway de APIs REST'],
            [<strong>IAM</strong>, 'Identidade e acesso'],
            [<strong>SSM</strong>, 'Parameter Store'],
            [<strong>Secrets Manager</strong>, 'Gerenciamento de segredos'],
            [<strong>CloudWatch</strong>, 'Metricas e logs'],
            [<strong>EventBridge</strong>, 'Barramento de eventos'],
            [<strong>Kinesis</strong>, 'Streaming de dados'],
            [<strong>Step Functions</strong>, 'Orquestracao de workflows'],
          ]}
        />

        <h2 id="health" className="heading-anchor">
          <a href="#health" className="heading-anchor__link" aria-hidden="true">#</a>
          Verificando Status
        </h2>
        <CodeBlock language="bash" code={`# Health check
curl http://localhost:4566/_localstack/health | jq

# Listar buckets S3
aws --endpoint-url=http://localhost:4566 s3 ls

# Listar filas SQS
aws --endpoint-url=http://localhost:4566 sqs list-queues`} />

        <h2 id="awscli" className="heading-anchor">
          <a href="#awscli" className="heading-anchor__link" aria-hidden="true">#</a>
          AWS CLI + LocalStack
        </h2>
        <p>Configure um profile dedicado:</p>
        <CodeBlock language="bash" title="~/.aws/credentials" code={`[localstack]
aws_access_key_id = test       # fake — LocalStack aceita qualquer valor
aws_secret_access_key = test   # fake — NAO use credenciais reais aqui`} />

        <CodeBlock language="bash" title="~/.aws/config" code={`[profile localstack]
region = us-east-1
output = json`} />

        <p>Uso direto:</p>
        <CodeBlock language="bash" code={`aws --profile localstack --endpoint-url=http://localhost:4566 s3 ls`} />

        <p>Ou instale o wrapper <code>awslocal</code>:</p>
        <CodeBlock language="bash" code={`pip install awscli-local
awslocal s3 ls`} />

        <PageNav currentPath="/localstack/concepts" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
