import { Link } from 'react-router-dom'
import { Table } from '../components/Table'
import { Callout } from '../components/Callout'
import { CodeBlock } from '../components/CodeBlock'
import { TOC } from '../components/TOC'
import { Footer } from '../components/Footer'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'sections', title: 'Secoes' },
  { id: 'stack', title: 'Stack' },
  { id: 'setup', title: 'Setup Rapido' },
  { id: 'pocs', title: 'POCs Planejadas' },
  { id: 'prereqs', title: 'Pre-requisitos' },
]

interface HomeProps {
  onOpenSearch: () => void
}

export function Home({ onOpenSearch }: HomeProps) {
  return (
    <>
      <main className="app-content">
        <div className="hero">
          <h1>java-llm-pocs</h1>
          <p className="hero__subtitle">
            POCs para estudo de <strong>Claude Code</strong>, <strong>LocalStack</strong> e <strong>Terraform</strong> com Java 21 + Spring Boot.
          </p>
          <div className="stack-pills">
            <span className="stack-pill">Java 21</span>
            <span className="stack-pill">Spring Boot 3.4</span>
            <span className="stack-pill">AWS SDK v2</span>
            <span className="stack-pill">LocalStack</span>
            <span className="stack-pill">Terraform</span>
            <span className="stack-pill">Docker</span>
          </div>
          <div className="hero__actions">
            <Link to="/claude-code/basics" className="hero__btn hero__btn--primary">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="4 17 10 11 4 5" />
                <line x1="12" y1="19" x2="20" y2="19" />
              </svg>
              Comecar
            </Link>
            <button className="hero__btn hero__btn--secondary" onClick={onOpenSearch}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              Buscar <kbd>Ctrl K</kbd>
            </button>
          </div>
        </div>

        <h2 id="sections" className="heading-anchor">
          <a href="#sections" className="heading-anchor__link" aria-hidden="true">#</a>
          Secoes
        </h2>
        <div className="card-grid">
          <Link to="/claude-code/basics" className="card">
            <svg className="card__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <polyline points="4 17 10 11 4 5" />
              <line x1="12" y1="19" x2="20" y2="19" />
            </svg>
            <div className="card__title">Claude Code</div>
            <p className="card__description">
              CLI da Anthropic — comandos, hooks, slash commands e boas praticas.
            </p>
          </Link>
          <Link to="/localstack/concepts" className="card">
            <svg className="card__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <rect x="2" y="3" width="20" height="14" rx="2" />
              <line x1="8" y1="21" x2="16" y2="21" />
              <line x1="12" y1="17" x2="12" y2="21" />
            </svg>
            <div className="card__title">LocalStack & Terraform</div>
            <p className="card__description">
              Simule AWS local e provisione infra com Terraform — zero custo.
            </p>
          </Link>
        </div>

        <h2 id="stack" className="heading-anchor">
          <a href="#stack" className="heading-anchor__link" aria-hidden="true">#</a>
          Stack
        </h2>
        <ul>
          <li><strong>Java 21</strong> + <strong>Spring Boot 3.4</strong> + Maven Wrapper</li>
          <li><strong>AWS SDK for Java v2</strong> (<code>software.amazon.awssdk</code>)</li>
          <li><strong>LocalStack Community</strong> via Docker Compose</li>
          <li><strong>Terraform 1.x</strong> → <code>http://localhost:4566</code></li>
          <li><strong>Spring Cloud AWS 3.x</strong> quando aplicavel</li>
        </ul>

        <h2 id="setup" className="heading-anchor">
          <a href="#setup" className="heading-anchor__link" aria-hidden="true">#</a>
          Setup Rapido
        </h2>

        <h3>1. Subir o LocalStack</h3>
        <CodeBlock language="bash" code={`cd infra
docker-compose up -d

# Verificar saude (~30s)
curl http://localhost:4566/_localstack/health`} />

        <h3>2. Provisionar com Terraform</h3>
        <CodeBlock language="bash" code={`cd infra/localstack
terraform init
terraform apply -auto-approve
terraform output`} />

        <h3>3. Rodar uma POC</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-s3-storage
./mvnw spring-boot:run`} />

        <h2 id="pocs" className="heading-anchor">
          <a href="#pocs" className="heading-anchor__link" aria-hidden="true">#</a>
          POCs Planejadas
        </h2>
        <Table
          headers={['POC', 'Servicos AWS', 'Status']}
          rows={[
            [<code>poc-s3-storage</code>, 'S3', <span className="status-badge status-badge--done">Completo</span>],
            [<code>poc-sqs-messaging</code>, 'SQS', <span className="status-badge status-badge--soon">Em breve</span>],
            [<code>poc-dynamodb-crud</code>, 'DynamoDB', <span className="status-badge status-badge--soon">Em breve</span>],
            [<code>poc-lambda-java</code>, 'Lambda + S3', <span className="status-badge status-badge--soon">Em breve</span>],
            [<code>poc-event-driven</code>, 'EventBridge + SQS + Lambda', <span className="status-badge status-badge--soon">Em breve</span>],
          ]}
        />

        <h2 id="prereqs" className="heading-anchor">
          <a href="#prereqs" className="heading-anchor__link" aria-hidden="true">#</a>
          Pre-requisitos
        </h2>
        <Table
          headers={['Ferramenta', 'Versao minima', 'Verificar']}
          rows={[
            ['Java', '21', <code>java --version</code>],
            ['Docker', '20+', <code>docker --version</code>],
            ['Terraform', '1.0+', <code>terraform --version</code>],
            ['Git', 'qualquer', <code>git --version</code>],
          ]}
        />
        <Callout type="tip" title="Maven">
          <p>Nao precisa instalar globalmente — cada POC usa o Maven Wrapper (<code>./mvnw</code>).</p>
        </Callout>

        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
