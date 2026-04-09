import { CodeBlock } from '../components/CodeBlock'
import { Table } from '../components/Table'
import { Callout } from '../components/Callout'
import { PageNav } from '../components/PageNav'
import { TOC } from '../components/TOC'
import { Breadcrumbs } from '../components/Breadcrumbs'
import { Footer } from '../components/Footer'
import type { TOCItem } from '../components/TOC'

const tocItems: TOCItem[] = [
  { id: 'overview', title: 'Visao Geral' },
  { id: 'architecture', title: 'Arquitetura' },
  { id: 'terraform', title: 'Infraestrutura Terraform' },
  { id: 'queue-ops', title: 'Queue Operations' },
  { id: 'producer', title: 'Message Producer' },
  { id: 'consumer', title: 'Message Consumer' },
  { id: 'dlq', title: 'Dead-Letter Queue' },
  { id: 'fifo', title: 'FIFO Queue' },
  { id: 'delay', title: 'Delay Queue' },
  { id: 'tags', title: 'Queue Tags' },
  { id: 'policies', title: 'Policies & Attributes' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'running', title: 'Como Rodar' },
]

export function PocSqsMessaging() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>SQS Messaging</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="overview" className="heading-anchor">
          <a href="#overview" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora 8 areas de funcionalidade do Amazon SQS em uma aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para gerenciar filas, enviar e consumir
          mensagens com diferentes padroes.
        </p>
        <p>
          As funcionalidades cobertas incluem standard queues, FIFO queues, dead-letter queues
          com redrive, delay queues, batch operations, message attributes, visibility timeout,
          long polling, tags e queue policies.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O SQS tem cobertura
            muito ampla no tier gratuito, incluindo FIFO e DLQ com redrive.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ARQUITETURA */}
        {/* ================================================================ */}
        <h2 id="architecture" className="heading-anchor">
          <a href="#architecture" className="heading-anchor__link" aria-hidden="true">#</a>
          Arquitetura
        </h2>
        <p>
          A aplicacao segue a mesma estrutura da POC S3: camadas de{' '}
          <strong>Controller</strong> e <strong>Service</strong> com injecao por construtor.
          Cada area funcional tem seu proprio par controller/service.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.sqs/
  config/         SqsConfig (SqsClient bean)
  controller/     8 controllers (um por area funcional)
  service/        8 services (logica de negocio com AWS SDK)
  dto/            Records: SendMessageDto, BatchMessageDto,
                  RedriveConfigDto, QueueAttributesDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8081</code> para nao conflitar com a POC S3 (porta 8080).
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
          O modulo Terraform em <code>infra/localstack/modules/sqs</code> provisiona
          5 filas com diferentes configuracoes: standard, FIFO, dead-letter queues
          e delay queue.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_sqs_queue</code>, 'poc-standard-queue', 'Fila standard principal com redrive para DLQ'],
            [<code>aws_sqs_queue</code>, 'poc-fifo-queue.fifo', 'Fila FIFO com content-based deduplication'],
            [<code>aws_sqs_queue</code>, 'poc-dlq', 'Dead-letter queue (standard) — retencao 14 dias'],
            [<code>aws_sqs_queue</code>, 'poc-fifo-dlq.fifo', 'Dead-letter queue (FIFO)'],
            [<code>aws_sqs_queue</code>, 'poc-delay-queue', 'Delay queue — delay de 10 segundos'],
            [<code>aws_sqs_queue_policy</code>, '--', 'Permite envio de mensagens na fila standard'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/sqs/main.tf (trecho)" code={`resource "aws_sqs_queue" "standard_queue" {
  name                       = "\${var.project_name}-\${var.environment}-poc-standard-queue"
  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600  # 4 dias
  receive_wait_time_seconds  = 0

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = 3
  })
}

resource "aws_sqs_queue" "fifo_queue" {
  name                        = "\${var.project_name}-\${var.environment}-poc-fifo-queue.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
}`} />

        {/* ================================================================ */}
        {/* QUEUE OPERATIONS */}
        {/* ================================================================ */}
        <h2 id="queue-ops" className="heading-anchor">
          <a href="#queue-ops" className="heading-anchor__link" aria-hidden="true">#</a>
          Queue Operations
        </h2>
        <p>
          Operacoes basicas de gerenciamento de filas: criar standard e FIFO,
          listar, obter URL por nome, consultar/alterar atributos, deletar e purge.
        </p>
        <CodeBlock language="java" title="QueueOperationsService.java" code={`// Criar standard queue
String url = sqsClient.createQueue(CreateQueueRequest.builder()
    .queueName("my-queue").build()).queueUrl();

// Criar FIFO queue
sqsClient.createQueue(CreateQueueRequest.builder()
    .queueName("my-queue.fifo")
    .attributes(Map.of(
        QueueAttributeName.FIFO_QUEUE, "true",
        QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true"))
    .build());

// Listar queues
sqsClient.listQueues().queueUrls();

// Obter atributos
sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
    .queueUrl(url).attributeNamesWithStrings("All").build());

// Purge (limpar todas as mensagens)
sqsClient.purgeQueue(PurgeQueueRequest.builder()
    .queueUrl(url).build());`} />

        {/* ================================================================ */}
        {/* PRODUCER */}
        {/* ================================================================ */}
        <h2 id="producer" className="heading-anchor">
          <a href="#producer" className="heading-anchor__link" aria-hidden="true">#</a>
          Message Producer
        </h2>
        <p>
          O producer envia mensagens para filas standard e FIFO, com suporte a
          message attributes customizados, message timers (delay por mensagem)
          e batch operations (ate 10 mensagens por chamada).
        </p>
        <CodeBlock language="java" title="MessageProducerService.java" code={`// Enviar mensagem simples
sqsClient.sendMessage(SendMessageRequest.builder()
    .queueUrl(url).messageBody("Hello SQS").build());

// Enviar com atributos customizados
sqsClient.sendMessage(SendMessageRequest.builder()
    .queueUrl(url).messageBody(body)
    .messageAttributes(Map.of("priority",
        MessageAttributeValue.builder()
            .dataType("String").stringValue("high").build()))
    .build());

// Enviar FIFO (com group e deduplication)
sqsClient.sendMessage(SendMessageRequest.builder()
    .queueUrl(fifoUrl).messageBody(body)
    .messageGroupId("order-group")
    .messageDeduplicationId("unique-id").build());

// Batch send (ate 10 mensagens)
sqsClient.sendMessageBatch(SendMessageBatchRequest.builder()
    .queueUrl(url).entries(entries).build());`} />

        {/* ================================================================ */}
        {/* CONSUMER */}
        {/* ================================================================ */}
        <h2 id="consumer" className="heading-anchor">
          <a href="#consumer" className="heading-anchor__link" aria-hidden="true">#</a>
          Message Consumer
        </h2>
        <p>
          O consumer recebe mensagens com suporte a short polling (retorno imediato)
          e long polling (espera ate <code>waitTimeSeconds</code>). Apos processar,
          a mensagem deve ser deletada via receipt handle. Tambem suporta
          alteracao de visibility timeout e operacoes em batch.
        </p>
        <CodeBlock language="java" title="MessageConsumerService.java" code={`// Receber com long polling (espera ate 5s)
List<Message> messages = sqsClient.receiveMessage(
    ReceiveMessageRequest.builder()
        .queueUrl(url)
        .maxNumberOfMessages(10)
        .waitTimeSeconds(5)
        .messageSystemAttributeNamesWithStrings("All")
        .messageAttributeNames("All")
        .build()).messages();

// Deletar apos processar
sqsClient.deleteMessage(DeleteMessageRequest.builder()
    .queueUrl(url)
    .receiptHandle(msg.receiptHandle()).build());

// Alterar visibility timeout (mais tempo para processar)
sqsClient.changeMessageVisibility(
    ChangeMessageVisibilityRequest.builder()
        .queueUrl(url)
        .receiptHandle(handle)
        .visibilityTimeout(60).build());`} />

        <Callout type="tip">
          <p>
            <strong>Long polling</strong> reduz custos e latencia comparado com short polling.
            Use <code>waitTimeSeconds &gt; 0</code> para evitar chamadas vazias repetidas.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* DLQ */}
        {/* ================================================================ */}
        <h2 id="dlq" className="heading-anchor">
          <a href="#dlq" className="heading-anchor__link" aria-hidden="true">#</a>
          Dead-Letter Queue
        </h2>
        <p>
          Quando uma mensagem falha ao ser processada (recebida mais de <code>maxReceiveCount</code> vezes
          sem ser deletada), ela e automaticamente movida para a DLQ.
          A POC demonstra como configurar a redrive policy, listar mensagens na DLQ,
          e usar <code>StartMessageMoveTask</code> para reprocessar mensagens.
        </p>
        <CodeBlock language="java" title="DeadLetterQueueService.java" code={`// Configurar redrive policy
sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
    .queueUrl(queueUrl)
    .attributes(Map.of(
        QueueAttributeName.REDRIVE_POLICY,
        "{\\"deadLetterTargetArn\\":\\"" + dlqArn + "\\",\\"maxReceiveCount\\":3}"))
    .build());

// Mover mensagens de volta da DLQ para a fila original
sqsClient.startMessageMoveTask(StartMessageMoveTaskRequest.builder()
    .sourceArn(dlqArn)
    .destinationArn(originalQueueArn)
    .build());`} />

        <Callout type="warning">
          <p>
            No LocalStack, mensagens podem nao mover para a DLQ imediatamente ao exceder
            o maxReceiveCount — elas esperam a proxima chamada ReceiveMessage.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* FIFO */}
        {/* ================================================================ */}
        <h2 id="fifo" className="heading-anchor">
          <a href="#fifo" className="heading-anchor__link" aria-hidden="true">#</a>
          FIFO Queue
        </h2>
        <p>
          Filas FIFO garantem <strong>ordenacao exata</strong> (first-in-first-out) e{' '}
          <strong>exactly-once processing</strong> via deduplication.
          Mensagens com o mesmo <code>MessageGroupId</code> sao entregues em ordem.
          Grupos diferentes podem ser processados em paralelo.
        </p>
        <p>
          A POC demonstra: criacao de FIFO queue, envio ordenado por grupo,
          deduplicacao (enviar mesma mensagem 2x, verificar que so chega 1),
          e paralelismo entre message groups.
        </p>
        <CodeBlock language="java" title="FifoQueueService.java" code={`// Enviar mensagens ordenadas no mesmo grupo
for (String msg : messages) {
    sqsClient.sendMessage(SendMessageRequest.builder()
        .queueUrl(fifoUrl)
        .messageBody(msg)
        .messageGroupId("order-group")
        .messageDeduplicationId(uniqueId())
        .build());
}

// Demonstrar deduplicacao (mesma msg 2x = 1 entrega)
String dedupId = "dedup-123";
sqsClient.sendMessage(req.messageDeduplicationId(dedupId).build());
sqsClient.sendMessage(req.messageDeduplicationId(dedupId).build());
// Somente 1 mensagem na fila!

// Enviar para multiplos grupos (processamento paralelo)
sqsClient.sendMessage(req.messageGroupId("group-A").build());
sqsClient.sendMessage(req.messageGroupId("group-B").build());`} />

        {/* ================================================================ */}
        {/* DELAY */}
        {/* ================================================================ */}
        <h2 id="delay" className="heading-anchor">
          <a href="#delay" className="heading-anchor__link" aria-hidden="true">#</a>
          Delay Queue
        </h2>
        <p>
          Delay queues adiam a entrega de todas as mensagens por um periodo configuravel
          (ate 15 minutos). Alem disso, e possivel definir delay por mensagem individual
          usando <code>delaySeconds</code> no envio (message timer).
        </p>
        <CodeBlock language="java" title="DelayQueueService.java" code={`// Criar queue com delay padrao de 10 segundos
sqsClient.createQueue(CreateQueueRequest.builder()
    .queueName("delay-queue")
    .attributes(Map.of(
        QueueAttributeName.DELAY_SECONDS, "10"))
    .build());

// Enviar mensagem com delay individual
sqsClient.sendMessage(SendMessageRequest.builder()
    .queueUrl(url)
    .messageBody("Delayed message")
    .delaySeconds(30)  // Atrasa 30s, independente do queue delay
    .build());`} />

        {/* ================================================================ */}
        {/* TAGS */}
        {/* ================================================================ */}
        <h2 id="tags" className="heading-anchor">
          <a href="#tags" className="heading-anchor__link" aria-hidden="true">#</a>
          Queue Tags
        </h2>
        <p>
          Tags sao pares chave-valor aplicados a queues para organizacao e controle.
          A API suporta adicionar, listar e remover tags.
        </p>
        <CodeBlock language="java" title="QueueTagService.java" code={`// Adicionar tags
sqsClient.tagQueue(TagQueueRequest.builder()
    .queueUrl(url)
    .tags(Map.of("env", "dev", "team", "backend"))
    .build());

// Listar tags
Map<String, String> tags = sqsClient.listQueueTags(
    ListQueueTagsRequest.builder().queueUrl(url).build()).tags();

// Remover tags
sqsClient.untagQueue(UntagQueueRequest.builder()
    .queueUrl(url).tagKeys("env").build());`} />

        {/* ================================================================ */}
        {/* POLICIES */}
        {/* ================================================================ */}
        <h2 id="policies" className="heading-anchor">
          <a href="#policies" className="heading-anchor__link" aria-hidden="true">#</a>
          Policies e Attributes
        </h2>
        <p>
          Queue policies sao documentos JSON que controlam acesso a fila.
          Os atributos avancados permitem configurar visibility timeout,
          message retention, maximum message size e receive wait time.
        </p>
        <CodeBlock language="java" title="QueuePolicyService.java" code={`// Configurar policy
sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
    .queueUrl(url)
    .attributes(Map.of(QueueAttributeName.POLICY, policyJson))
    .build());

// Configurar atributos avancados
sqsClient.setQueueAttributes(SetQueueAttributesRequest.builder()
    .queueUrl(url)
    .attributes(Map.of(
        QueueAttributeName.VISIBILITY_TIMEOUT, "60",
        QueueAttributeName.MESSAGE_RETENTION_PERIOD, "604800",
        QueueAttributeName.MAXIMUM_MESSAGE_SIZE, "131072",
        QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "10"))
    .build());`} />

        <Callout type="info">
          <p>
            No LocalStack Community, policies sao aceitas via API mas o enforcement
            de IAM nao e aplicado. O comportamento e funcional para desenvolvimento.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* API REFERENCE */}
        {/* ================================================================ */}
        <h2 id="api-reference" className="heading-anchor">
          <a href="#api-reference" className="heading-anchor__link" aria-hidden="true">#</a>
          Referencia da API
        </h2>
        <p>
          A tabela abaixo lista todos os 8 grupos de endpoints expostos pela POC.
          Todos usam o prefixo base <code>/api/sqs</code>.
        </p>
        <Table
          headers={['Grupo', 'Base Path', 'Operacoes Principais']}
          rows={[
            ['Queue Ops', <code>/api/sqs/queues</code>, 'Create standard/FIFO, List, Get URL, Attributes, Delete, Purge'],
            ['Producer', <code>/api/sqs/messages</code>, 'Send, Send with attrs, Send with delay, Send FIFO, Batch, FIFO batch'],
            ['Consumer', <code>/api/sqs/consumer</code>, 'Receive (short/long polling), Delete, Batch delete, Visibility timeout'],
            ['DLQ', <code>/api/sqs/dlq</code>, 'Configure redrive, Get policy, List DLQ msgs, Redrive, Simulate failure'],
            ['FIFO', <code>/api/sqs/fifo</code>, 'Create FIFO, Send ordered, Deduplication demo, Multi-group'],
            ['Delay', <code>/api/sqs/delay</code>, 'Create delay queue, Send with delay, Demonstrate delay'],
            ['Tags', <code>/api/sqs/tags</code>, 'Add tags, List tags, Remove tags'],
            ['Policies', <code>/api/sqs/policies</code>, 'Set/Get/Remove policy, Set/Get advanced attributes'],
          ]}
        />

        {/* ================================================================ */}
        {/* COMO RODAR */}
        {/* ================================================================ */}
        <h2 id="running" className="heading-anchor">
          <a href="#running" className="heading-anchor__link" aria-hidden="true">#</a>
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
terraform apply -auto-approve

# Ver URLs das filas criadas
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-sqs-messaging
./mvnw spring-boot:run

# Roda na porta 8081`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Listar filas
curl "http://localhost:8081/api/sqs/queues"

# Enviar mensagem
curl -X POST "http://localhost:8081/api/sqs/messages/send?queueUrl=<URL>" \\
  -H "Content-Type: text/plain" \\
  -d "Hello SQS!"

# Receber mensagens (long polling 5s)
curl "http://localhost:8081/api/sqs/consumer/receive?queueUrl=<URL>&maxMessages=5&waitTimeSeconds=5"

# Enviar FIFO
curl -X POST "http://localhost:8081/api/sqs/messages/send-fifo?queueUrl=<FIFO_URL>&groupId=g1" \\
  -H "Content-Type: text/plain" \\
  -d "Ordered message"

# Listar tags
curl "http://localhost:8081/api/sqs/tags?queueUrl=<URL>"

# Ver atributos
curl "http://localhost:8081/api/sqs/queues/attributes?queueUrl=<URL>"`} />

        <Callout type="warning">
          <p>
            Substitua <code>{'<URL>'}</code> pela URL real da fila. Use{' '}
            <code>terraform output</code> ou o endpoint{' '}
            <code>/api/sqs/queues/url?name=nome-da-fila</code> para obter a URL.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/sqs-messaging" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
