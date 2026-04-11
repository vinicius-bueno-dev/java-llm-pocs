import { TOC } from '../components/TOC'
import { Breadcrumbs } from '../components/Breadcrumbs'
import { PageNav } from '../components/PageNav'
import { Footer } from '../components/Footer'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'visao-geral', title: 'Visao Geral' },
  { id: 'storage', title: 'Storage & Content' },
  { id: 'messaging', title: 'Messaging & Queues' },
  { id: 'compute', title: 'Compute' },
  { id: 'database', title: 'Database' },
  { id: 'integration', title: 'Integration & Events' },
  { id: 'security', title: 'Security & Identity' },
  { id: 'observability', title: 'Observability' },
  { id: 'networking', title: 'Networking & API' },
  { id: 'legenda', title: 'Legenda' },
]

type Phase = 'done' | 'current' | 'next' | 'planned' | 'exploring'

interface Service {
  name: string
  aws: string
  poc: string
  description: string
  phase: Phase
  features?: string[]
}

interface Category {
  id: string
  title: string
  icon: React.ReactNode
  services: Service[]
}

const phaseLabels: Record<Phase, string> = {
  done: 'Completo',
  current: 'Em andamento',
  next: 'Proximo',
  planned: 'Planejado',
  exploring: 'Explorando',
}

const phaseOrder: Phase[] = ['done', 'current', 'next', 'planned', 'exploring']

const categories: Category[] = [
  {
    id: 'storage',
    title: 'Storage & Content',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7" />
        <ellipse cx="12" cy="7" rx="8" ry="4" />
        <path d="M4 12c0 2.21 3.582 4 8 4s8-1.79 8-4" />
      </svg>
    ),
    services: [
      {
        name: 'S3',
        aws: 'Amazon S3',
        poc: 'poc-s3-storage',
        description: 'Object storage com CRUD, versionamento, presigned URLs, multipart upload, lifecycle, policies, CORS, notifications, website hosting, object lock, checksums e conditional requests.',
        phase: 'done',
        features: ['CRUD', 'Versioning', 'Presigned URLs', 'Multipart', 'Lifecycle', 'ACLs', 'CORS', 'Notifications', 'Website Hosting', 'Object Lock', 'Checksums', 'Conditional Requests'],
      },
      {
        name: 'CloudFront',
        aws: 'Amazon CloudFront',
        poc: 'poc-cloudfront-cdn',
        description: 'CDN para distribuicao de conteudo estatico com origin S3 e cache behaviors.',
        phase: 'exploring',
        features: ['Origin S3', 'Cache Policies', 'Invalidation'],
      },
    ],
  },
  {
    id: 'messaging',
    title: 'Messaging & Queues',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
      </svg>
    ),
    services: [
      {
        name: 'SQS',
        aws: 'Amazon SQS',
        poc: 'poc-sqs-messaging',
        description: 'Filas de mensagens com standard queues, FIFO, dead-letter queues, delay queues, batch operations, tags e policies.',
        phase: 'current',
        features: ['Standard Queue', 'FIFO', 'DLQ', 'Long Polling', 'Batch Operations', 'Delay Queue', 'Tags', 'Policies', 'Redrive'],
      },
      {
        name: 'SNS',
        aws: 'Amazon SNS',
        poc: 'poc-sns-notifications',
        description: 'Pub/sub messaging com topics, subscriptions, fan-out para SQS e filtros de mensagem.',
        phase: 'planned',
        features: ['Topics', 'Subscriptions', 'Fan-out', 'Message Filtering'],
      },
      {
        name: 'Kinesis',
        aws: 'Amazon Kinesis',
        poc: 'poc-kinesis-streaming',
        description: 'Streaming de dados em tempo real com data streams, consumers e processamento ordenado.',
        phase: 'exploring',
        features: ['Data Streams', 'Shards', 'Consumers', 'Ordered Processing'],
      },
    ],
  },
  {
    id: 'compute',
    title: 'Compute',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <rect x="4" y="4" width="16" height="16" rx="2" />
        <path d="M9 9h6v6H9z" />
        <path d="M9 1v3M15 1v3M9 20v3M15 20v3M20 9h3M20 14h3M1 9h3M1 14h3" />
      </svg>
    ),
    services: [
      {
        name: 'Lambda',
        aws: 'AWS Lambda',
        poc: 'poc-lambda-java',
        description: 'Functions serverless com Java 21, triggers S3/SQS, cold start optimization e layers.',
        phase: 'planned',
        features: ['Java Runtime', 'S3 Trigger', 'SQS Trigger', 'Layers', 'SnapStart'],
      },
      {
        name: 'Step Functions',
        aws: 'AWS Step Functions',
        poc: 'poc-step-functions',
        description: 'Orquestracao de workflows com state machines, parallel execution e error handling.',
        phase: 'exploring',
        features: ['State Machines', 'Parallel', 'Error Handling', 'Wait States'],
      },
    ],
  },
  {
    id: 'database',
    title: 'Database',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <ellipse cx="12" cy="5" rx="9" ry="3" />
        <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3" />
        <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5" />
      </svg>
    ),
    services: [
      {
        name: 'DynamoDB',
        aws: 'Amazon DynamoDB',
        poc: 'poc-dynamodb-crud',
        description: 'NoSQL key-value com CRUD, queries, GSI/LSI, streams, TTL, optimistic locking e batch operations.',
        phase: 'done',
        features: ['CRUD', 'Query & Scan', 'GSI/LSI', 'Pagination', 'TTL', 'Batch Ops', 'Optimistic Locking'],
      },
      {
        name: 'ElastiCache',
        aws: 'Amazon ElastiCache',
        poc: 'poc-elasticache-redis',
        description: 'Cache distribuido com Redis — sessoes, rate limiting e cache-aside pattern.',
        phase: 'exploring',
        features: ['Redis', 'Session Store', 'Rate Limiting', 'Cache-Aside'],
      },
    ],
  },
  {
    id: 'integration',
    title: 'Integration & Events',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <circle cx="12" cy="12" r="3" />
        <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83" />
      </svg>
    ),
    services: [
      {
        name: 'EventBridge',
        aws: 'Amazon EventBridge',
        poc: 'poc-event-driven',
        description: 'Event bus com rules, patterns, targets e integracao com SQS e Lambda.',
        phase: 'planned',
        features: ['Event Bus', 'Rules', 'Patterns', 'Multi-target', 'Scheduled Events'],
      },
      {
        name: 'SES',
        aws: 'Amazon SES',
        poc: 'poc-ses-email',
        description: 'Envio de emails transacionais com templates, attachments e bounce handling.',
        phase: 'exploring',
        features: ['Send Email', 'Templates', 'Attachments', 'Bounce Handling'],
      },
    ],
  },
  {
    id: 'security',
    title: 'Security & Identity',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
      </svg>
    ),
    services: [
      {
        name: 'Secrets Manager',
        aws: 'AWS Secrets Manager',
        poc: 'poc-secrets-manager',
        description: 'Gerenciamento de segredos com rotacao automatica, versionamento e integracao Spring.',
        phase: 'planned',
        features: ['Store/Retrieve', 'Rotation', 'Versioning', 'Spring Integration'],
      },
      {
        name: 'IAM',
        aws: 'AWS IAM',
        poc: 'poc-iam-policies',
        description: 'Roles, policies e permissoes — simulando cenarios de acesso no LocalStack.',
        phase: 'exploring',
        features: ['Roles', 'Policies', 'Assume Role', 'Policy Simulator'],
      },
      {
        name: 'KMS',
        aws: 'AWS KMS',
        poc: 'poc-kms-encryption',
        description: 'Gerenciamento de chaves de criptografia com encrypt/decrypt e envelope encryption.',
        phase: 'exploring',
        features: ['Key Management', 'Encrypt/Decrypt', 'Envelope Encryption'],
      },
    ],
  },
  {
    id: 'observability',
    title: 'Observability',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
      </svg>
    ),
    services: [
      {
        name: 'CloudWatch',
        aws: 'Amazon CloudWatch',
        poc: 'poc-cloudwatch-logs',
        description: 'Logs, metricas customizadas, alarms e dashboards para monitoramento das POCs.',
        phase: 'planned',
        features: ['Log Groups', 'Custom Metrics', 'Alarms', 'Dashboards'],
      },
    ],
  },
  {
    id: 'networking',
    title: 'Networking & API',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
        <rect x="2" y="3" width="20" height="14" rx="2" />
        <path d="M8 21h8M12 17v4" />
      </svg>
    ),
    services: [
      {
        name: 'API Gateway',
        aws: 'Amazon API Gateway',
        poc: 'poc-api-gateway',
        description: 'REST e HTTP APIs com Lambda integration, authorizers e throttling.',
        phase: 'exploring',
        features: ['REST API', 'HTTP API', 'Lambda Proxy', 'Authorizers', 'Throttling'],
      },
    ],
  },
]

function PhaseIndicator({ phase }: { phase: Phase }) {
  return (
    <span className={`roadmap-phase roadmap-phase--${phase}`}>
      <span className="roadmap-phase__dot" />
      {phaseLabels[phase]}
    </span>
  )
}

function ServiceCard({ service }: { service: Service }) {
  return (
    <div className={`roadmap-card roadmap-card--${service.phase}`}>
      <div className="roadmap-card__header">
        <div className="roadmap-card__titles">
          <h4 className="roadmap-card__name">{service.name}</h4>
          <span className="roadmap-card__aws">{service.aws}</span>
        </div>
        <PhaseIndicator phase={service.phase} />
      </div>
      <p className="roadmap-card__desc">{service.description}</p>
      <code className="roadmap-card__poc">{service.poc}</code>
      {service.features && (
        <div className="roadmap-card__features">
          {service.features.map((f) => (
            <span key={f} className="roadmap-card__feature">{f}</span>
          ))}
        </div>
      )}
    </div>
  )
}

function CategorySection({ category }: { category: Category }) {
  const sorted = [...category.services].sort(
    (a, b) => phaseOrder.indexOf(a.phase) - phaseOrder.indexOf(b.phase),
  )

  return (
    <section className="roadmap-category">
      <h2 id={category.id} className="heading-anchor">
        <a href={`#${category.id}`} className="heading-anchor__link" aria-hidden="true">#</a>
        <span className="roadmap-category__icon">{category.icon}</span>
        {category.title}
      </h2>
      <div className="roadmap-grid">
        {sorted.map((s) => (
          <ServiceCard key={s.poc} service={s} />
        ))}
      </div>
    </section>
  )
}

function ProgressBar() {
  const allServices = categories.flatMap((c) => c.services)
  const total = allServices.length
  const counts: Record<Phase, number> = { done: 0, current: 0, next: 0, planned: 0, exploring: 0 }
  allServices.forEach((s) => counts[s.phase]++)

  return (
    <div className="roadmap-progress">
      <div className="roadmap-progress__header">
        <span className="roadmap-progress__label">Progresso geral</span>
        <span className="roadmap-progress__count">
          {counts.done} de {total} servicos
        </span>
      </div>
      <div className="roadmap-progress__bar">
        {phaseOrder.map((phase) => {
          const pct = (counts[phase] / total) * 100
          if (pct === 0) return null
          return (
            <div
              key={phase}
              className={`roadmap-progress__segment roadmap-progress__segment--${phase}`}
              style={{ width: `${pct}%` }}
              title={`${phaseLabels[phase]}: ${counts[phase]}`}
            />
          )
        })}
      </div>
      <div className="roadmap-progress__legend">
        {phaseOrder.map((phase) => (
          <span key={phase} className="roadmap-progress__legend-item">
            <span className={`roadmap-progress__legend-dot roadmap-progress__legend-dot--${phase}`} />
            {phaseLabels[phase]} ({counts[phase]})
          </span>
        ))}
      </div>
    </div>
  )
}

export function Roadmap() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>Roadmap de Servicos AWS</h1>
        </div>

        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Este roadmap apresenta todos os servicos AWS que serao explorados neste repositorio.
          Cada servico sera implementado como uma POC independente usando <strong>Spring Boot + AWS SDK v2</strong> conectado
          ao <strong>LocalStack</strong>, com infraestrutura provisionada via <strong>Terraform</strong>.
        </p>

        <ProgressBar />

        {categories.map((cat) => (
          <CategorySection key={cat.id} category={cat} />
        ))}

        <h2 id="legenda" className="heading-anchor">
          <a href="#legenda" className="heading-anchor__link" aria-hidden="true">#</a>
          Legenda
        </h2>
        <div className="roadmap-legend">
          <div className="roadmap-legend__item">
            <PhaseIndicator phase="done" />
            <span>POC implementada e documentada. Codigo disponivel no repositorio.</span>
          </div>
          <div className="roadmap-legend__item">
            <PhaseIndicator phase="current" />
            <span>Desenvolvimento ativo. POC em construcao.</span>
          </div>
          <div className="roadmap-legend__item">
            <PhaseIndicator phase="next" />
            <span>Proximo na fila. Infraestrutura Terraform ja planejada.</span>
          </div>
          <div className="roadmap-legend__item">
            <PhaseIndicator phase="planned" />
            <span>Planejado para implementacao. Escopo definido.</span>
          </div>
          <div className="roadmap-legend__item">
            <PhaseIndicator phase="exploring" />
            <span>Em fase de exploracao. Viabilidade no LocalStack sendo avaliada.</span>
          </div>
        </div>

        <PageNav currentPath="/pocs/roadmap" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
