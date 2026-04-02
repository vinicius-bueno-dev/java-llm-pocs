import { CodeBlock } from '../components/CodeBlock'
import { Table } from '../components/Table'
import { PageNav } from '../components/PageNav'
import { TOC } from '../components/TOC'
import { LocalStackArchitecture } from '../components/diagrams/LocalStackArchitecture'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'what', title: 'O que é?' },
  { id: 'why', title: 'Por que usar?' },
  { id: 'how', title: 'Como funciona' },
  { id: 'services', title: 'Serviços Disponíveis' },
  { id: 'health', title: 'Verificando Status' },
  { id: 'awscli', title: 'AWS CLI + LocalStack' },
]

export function LocalStackConcepts() {
  return (
    <>
      <main className="app-content">
        <div className="section-header">
          <div className="section-header__overline">LocalStack & Terraform</div>
          <h1>Conceitos Fundamentais</h1>
        </div>

        <h2 id="what">O que é?</h2>
        <p>
          Plataforma que simula serviços AWS localmente via Docker.
          Crie buckets S3, filas SQS, tabelas DynamoDB — sem custo e sem conta AWS.
        </p>

        <h2 id="why">Por que usar?</h2>
        <ul>
          <li><strong>Gratuito</strong> — Community Edition cobre os serviços mais usados</li>
          <li><strong>Offline</strong> — funciona sem internet</li>
          <li><strong>Rápido</strong> — sem latência de rede real</li>
          <li><strong>Seguro</strong> — zero risco de cobranças acidentais</li>
          <li><strong>Ideal</strong> — para desenvolvimento e testes locais</li>
        </ul>

        <h2 id="how">Como funciona</h2>
        <p>
          Todos os serviços são acessíveis por um único endpoint: <code>http://localhost:4566</code>.
          O SDK identifica qual serviço usar pelo path e headers da requisição.
        </p>
        <LocalStackArchitecture />

        <h2 id="services">Serviços Disponíveis (Community)</h2>
        <Table
          headers={['Serviço', 'Uso']}
          rows={[
            [<strong>S3</strong>, 'Armazenamento de objetos'],
            [<strong>SQS</strong>, 'Filas de mensagens'],
            [<strong>SNS</strong>, 'Notificações pub/sub'],
            [<strong>DynamoDB</strong>, 'Banco NoSQL'],
            [<strong>Lambda</strong>, 'Funções serverless'],
            [<strong>API Gateway</strong>, 'Gateway de APIs REST'],
            [<strong>IAM</strong>, 'Identidade e acesso'],
            [<strong>SSM</strong>, 'Parameter Store'],
            [<strong>Secrets Manager</strong>, 'Gerenciamento de segredos'],
            [<strong>CloudWatch</strong>, 'Métricas e logs'],
            [<strong>EventBridge</strong>, 'Barramento de eventos'],
            [<strong>Kinesis</strong>, 'Streaming de dados'],
            [<strong>Step Functions</strong>, 'Orquestração de workflows'],
          ]}
        />

        <h2 id="health">Verificando Status</h2>
        <CodeBlock language="bash" code={`# Health check
curl http://localhost:4566/_localstack/health | jq

# Listar buckets S3
aws --endpoint-url=http://localhost:4566 s3 ls

# Listar filas SQS
aws --endpoint-url=http://localhost:4566 sqs list-queues`} />

        <h2 id="awscli">AWS CLI + LocalStack</h2>
        <p>Configure um profile dedicado:</p>
        <CodeBlock language="bash" title="~/.aws/credentials" code={`[localstack]
aws_access_key_id = test       # fake — LocalStack aceita qualquer valor
aws_secret_access_key = test   # fake — NÃO use credenciais reais aqui`} />

        <CodeBlock language="bash" title="~/.aws/config" code={`[profile localstack]
region = us-east-1
output = json`} />

        <p>Uso direto:</p>
        <CodeBlock language="bash" code={`aws --profile localstack --endpoint-url=http://localhost:4566 s3 ls`} />

        <p>Ou instale o wrapper <code>awslocal</code>:</p>
        <CodeBlock language="bash" code={`pip install awscli-local
awslocal s3 ls`} />

        <PageNav currentPath="/localstack/concepts" />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
