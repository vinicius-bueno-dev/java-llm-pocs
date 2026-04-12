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
  { id: 'topics', title: 'Topics' },
  { id: 'subscriptions', title: 'Subscriptions' },
  { id: 'publish', title: 'Publicacao de Mensagens' },
  { id: 'fan-out', title: 'Fan-Out' },
  { id: 'filtering', title: 'Message Filtering' },
  { id: 'fifo', title: 'FIFO Topics' },
  { id: 'dlq', title: 'Dead-Letter Queue' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocSnsNotifications() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>SNS Notifications</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora as principais funcionalidades do Amazon SNS (Simple Notification
          Service) em uma aplicacao Spring Boot conectada ao LocalStack. O objetivo e
          demonstrar como usar o <strong>AWS SDK for Java v2</strong> para gerenciar topics,
          subscriptions, publicar mensagens e implementar padroes como fan-out e message filtering.
        </p>
        <p>
          As funcionalidades cobertas incluem topics standard e FIFO, subscriptions SNS para SQS
          com filter policies, publicacao simples e em batch, fan-out para multiplas filas,
          message attributes, raw message delivery e dead-letter queues para subscriptions falhadas.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O SNS tem boa cobertura
            no tier gratuito, incluindo topics FIFO, subscriptions com filter policy e DLQ.
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
          A aplicacao segue a mesma estrutura das demais POCs: camadas de{' '}
          <strong>Controller</strong> e <strong>Service</strong> com injecao por construtor.
          Cada area funcional tem seu proprio par controller/service. Alem do SnsClient,
          a aplicacao tambem configura um SqsClient para verificar fan-out e gerenciar
          subscriptions SQS.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.sns/
  config/         SnsConfig (SnsClient + SqsClient beans)
  controller/     4 controllers (Topic, Subscription, Notification, FanOut)
  service/        4 services (logica de negocio com AWS SDK)
  dto/            Records: SubscriptionDto, PublishMessageDto,
                  BatchPublishDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8083</code> para nao conflitar com as
            POCs S3 (8080), SQS (8081) e DynamoDB (8082).
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
          O modulo Terraform em <code>infra/localstack/modules/sns</code> provisiona
          topics, filas SQS subscriber, dead-letter queue, queue policies e subscriptions
          com filter policies.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_sns_topic</code>, 'poc-notifications', 'Topic standard principal'],
            [<code>aws_sns_topic</code>, 'poc-notifications.fifo', 'Topic FIFO com content-based deduplication'],
            [<code>aws_sqs_queue</code>, 'poc-sns-orders', 'Fila subscriber para eventos de pedidos'],
            [<code>aws_sqs_queue</code>, 'poc-sns-analytics', 'Fila subscriber para eventos de analytics'],
            [<code>aws_sqs_queue</code>, 'poc-sns-dlq', 'Dead-letter queue para subscriptions falhadas (retencao 14 dias)'],
            [<code>aws_sqs_queue_policy</code>, '--', 'Permite SNS publicar nas filas orders e analytics'],
            [<code>aws_sns_topic_subscription</code>, 'orders_sub', 'Subscription com filter: order_created, order_updated, order_cancelled'],
            [<code>aws_sns_topic_subscription</code>, 'analytics_sub', 'Subscription com filter: order_created, payment_processed, user_registered'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/sns/main.tf (trecho)" code={`resource "aws_sns_topic" "standard_topic" {
  name = "\${var.project_name}-\${var.environment}-poc-notifications"

  tags = {
    Name        = "\${var.project_name}-poc-notifications"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "sns-notifications"
    Type        = "standard"
  }
}

resource "aws_sns_topic" "fifo_topic" {
  name                        = "\${var.project_name}-\${var.environment}-poc-notifications.fifo"
  fifo_topic                  = true
  content_based_deduplication = true
}

resource "aws_sns_topic_subscription" "orders_sub" {
  topic_arn            = aws_sns_topic.standard_topic.arn
  protocol             = "sqs"
  endpoint             = aws_sqs_queue.subscriber_orders.arn
  raw_message_delivery = true

  filter_policy = jsonencode({
    eventType = ["order_created", "order_updated", "order_cancelled"]
  })

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.sns_dlq.arn
  })
}`} />

        {/* ================================================================ */}
        {/* TOPICS */}
        {/* ================================================================ */}
        <h2 id="topics" className="heading-anchor">
          <a href="#topics" className="heading-anchor__link" aria-hidden="true">#</a>
          Topics
        </h2>
        <p>
          Topics sao os canais de publicacao do SNS. A POC demonstra criacao de topics
          standard e FIFO, listagem, consulta e alteracao de atributos, exclusao e
          gerenciamento de tags.
        </p>
        <CodeBlock language="java" title="TopicService.java" code={`// Criar topic standard com tags
CreateTopicRequest.Builder builder = CreateTopicRequest.builder().name(name);
if (tags != null && !tags.isEmpty()) {
    List<Tag> snsTags = tags.entrySet().stream()
            .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
            .toList();
    builder.tags(snsTags);
}
CreateTopicResponse response = snsClient.createTopic(builder.build());

// Criar topic FIFO
String fifoName = name.endsWith(".fifo") ? name : name + ".fifo";
snsClient.createTopic(CreateTopicRequest.builder()
        .name(fifoName)
        .attributes(Map.of(
                "FifoTopic", "true",
                "ContentBasedDeduplication", String.valueOf(contentBasedDedup)))
        .build());

// Listar todos os topics
snsClient.listTopics().topics();

// Consultar atributos de um topic
snsClient.getTopicAttributes(GetTopicAttributesRequest.builder()
        .topicArn(topicArn).build());

// Adicionar tags a um topic existente
snsClient.tagResource(TagResourceRequest.builder()
        .resourceArn(topicArn).tags(snsTags).build());`} />

        <Callout type="tip">
          <p>
            Topics FIFO devem ter o sufixo <code>.fifo</code> no nome. O service
            adiciona automaticamente caso o nome nao termine com esse sufixo.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* SUBSCRIPTIONS */}
        {/* ================================================================ */}
        <h2 id="subscriptions" className="heading-anchor">
          <a href="#subscriptions" className="heading-anchor__link" aria-hidden="true">#</a>
          Subscriptions
        </h2>
        <p>
          Subscriptions conectam topics a endpoints consumidores. Nesta POC o protocolo
          usado e <strong>SQS</strong> — o SNS entrega mensagens diretamente nas filas.
          O service automaticamente configura a queue policy para permitir que o SNS
          publique na fila, obtem o ARN da fila a partir da URL e suporta raw message
          delivery e filter policies.
        </p>
        <CodeBlock language="java" title="SubscriptionService.java" code={`// Criar subscription SNS -> SQS
String queueArn = getQueueArn(queueUrl);
allowSnsToPublishToSqs(topicArn, queueArn, queueUrl);

SubscribeRequest.Builder builder = SubscribeRequest.builder()
        .topicArn(topicArn)
        .protocol("sqs")
        .endpoint(queueArn);

if (rawMessageDelivery) {
    builder.attributes(Map.of("RawMessageDelivery", "true"));
}

SubscribeResponse response = snsClient.subscribe(builder.build());

// Configurar filter policy na subscription
if (filterPolicy != null && !filterPolicy.isEmpty()) {
    setFilterPolicy(subscriptionArn, filterPolicy);
}

// Listar subscriptions de um topic
snsClient.listSubscriptionsByTopic(ListSubscriptionsByTopicRequest.builder()
        .topicArn(topicArn).build());

// Remover subscription
snsClient.unsubscribe(UnsubscribeRequest.builder()
        .subscriptionArn(subscriptionArn).build());`} />

        <Callout type="info">
          <p>
            Com <strong>Raw Message Delivery</strong> ativado, o subscriber recebe
            apenas o corpo da mensagem sem o envelope JSON do SNS. Isso simplifica
            o parsing no lado do consumidor.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* PUBLISH */}
        {/* ================================================================ */}
        <h2 id="publish" className="heading-anchor">
          <a href="#publish" className="heading-anchor__link" aria-hidden="true">#</a>
          Publicacao de Mensagens
        </h2>
        <p>
          O NotificationPublisherService oferece 4 modos de publicacao: simples,
          com message attributes, FIFO (com group e deduplication) e batch (ate 10
          mensagens por chamada).
        </p>
        <CodeBlock language="java" title="NotificationPublisherService.java" code={`// Publicar mensagem simples
PublishResponse response = snsClient.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .message(message)
        .subject(subject)  // opcional
        .build());

// Publicar com message attributes (usados para filtering)
Map<String, MessageAttributeValue> msgAttributes = new HashMap<>();
attributes.forEach((k, v) -> msgAttributes.put(k,
        MessageAttributeValue.builder()
                .dataType("String").stringValue(v).build()));

snsClient.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .message(message)
        .messageAttributes(msgAttributes)
        .build());

// Publicar em topic FIFO
snsClient.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .message(message)
        .messageGroupId(groupId)
        .messageDeduplicationId(deduplicationId)
        .build());

// Batch publish (ate 10 mensagens)
snsClient.publishBatch(PublishBatchRequest.builder()
        .topicArn(topicArn)
        .publishBatchRequestEntries(entries)
        .build());`} />

        {/* ================================================================ */}
        {/* FAN-OUT */}
        {/* ================================================================ */}
        <h2 id="fan-out" className="heading-anchor">
          <a href="#fan-out" className="heading-anchor__link" aria-hidden="true">#</a>
          Fan-Out
        </h2>
        <p>
          O padrao fan-out e um dos principais casos de uso do SNS: uma unica mensagem
          publicada em um topic e entregue a <strong>multiplos subscribers</strong>
          simultaneamente. O FanOutController demonstra esse padrao publicando uma mensagem
          e verificando a entrega em todas as filas SQS inscritas.
        </p>
        <CodeBlock language="java" title="FanOutService.java" code={`// Publicar e verificar fan-out em multiplas filas
PublishResponse publishResponse = snsClient.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .message(message)
        .messageAttributes(msgAttributes)
        .build());

// Verificar entrega em cada fila subscriber
for (String queueUrl : subscriberQueueUrls) {
    List<Message> received = sqsClient.receiveMessage(
            ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build()).messages();
    // Registra quantas mensagens cada fila recebeu
}`} />

        <Callout type="tip">
          <p>
            No exemplo desta POC, uma mensagem publicada no topic standard e entregue
            tanto para a fila <code>orders</code> quanto para a fila <code>analytics</code>,
            desde que os atributos da mensagem correspondam as filter policies das subscriptions.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* FILTERING */}
        {/* ================================================================ */}
        <h2 id="filtering" className="heading-anchor">
          <a href="#filtering" className="heading-anchor__link" aria-hidden="true">#</a>
          Message Filtering
        </h2>
        <p>
          Filter policies permitem que cada subscriber receba apenas as mensagens que
          lhe interessam, baseado nos <strong>message attributes</strong>. Isso evita
          que o subscriber tenha que filtrar mensagens no lado do consumidor.
        </p>
        <p>
          Na infraestrutura Terraform, as subscriptions ja vem configuradas com filter policies:
        </p>
        <Table
          headers={['Subscriber', 'Filter Policy (eventType)', 'Descricao']}
          rows={[
            ['orders', <code>order_created, order_updated, order_cancelled</code>, 'Recebe apenas eventos relacionados a pedidos'],
            ['analytics', <code>order_created, payment_processed, user_registered</code>, 'Recebe eventos para pipeline de analytics'],
          ]}
        />

        <CodeBlock language="java" title="Filtered Fan-Out" code={`// Publicar com eventType como message attribute
PublishResponse response = snsClient.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .message(message)
        .messageAttributes(Map.of(
                "eventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(eventType)  // ex: "order_created"
                        .build()))
        .build());

// Se eventType = "order_created":
//   - fila orders RECEBE (esta no filter policy)
//   - fila analytics RECEBE (tambem esta no filter policy)
// Se eventType = "payment_processed":
//   - fila orders NAO recebe (nao esta no filter)
//   - fila analytics RECEBE`} />

        <CodeBlock language="java" title="Atualizar filter policy via API" code={`// Alterar filter policy de uma subscription existente
String policyJson = objectMapper.writeValueAsString(filterPolicy);
snsClient.setSubscriptionAttributes(
        SetSubscriptionAttributesRequest.builder()
                .subscriptionArn(subscriptionArn)
                .attributeName("FilterPolicy")
                .attributeValue(policyJson)
                .build());`} />

        <Callout type="warning">
          <p>
            Se uma subscription nao possui filter policy, ela recebe <strong>todas</strong> as
            mensagens publicadas no topic. Adicione filter policies para controlar a entrega.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* FIFO */}
        {/* ================================================================ */}
        <h2 id="fifo" className="heading-anchor">
          <a href="#fifo" className="heading-anchor__link" aria-hidden="true">#</a>
          FIFO Topics
        </h2>
        <p>
          Topics FIFO garantem <strong>ordenacao exata</strong> (first-in-first-out) e{' '}
          <strong>exactly-once delivery</strong> via deduplication. Mensagens com o mesmo{' '}
          <code>MessageGroupId</code> sao entregues em ordem. A POC demonstra criacao
          de topics FIFO com content-based deduplication e publicacao com group ID.
        </p>
        <CodeBlock language="java" title="Criar topic FIFO e publicar" code={`// Criar topic FIFO
snsClient.createTopic(CreateTopicRequest.builder()
        .name("my-topic.fifo")
        .attributes(Map.of(
                "FifoTopic", "true",
                "ContentBasedDeduplication", "true"))
        .build());

// Publicar em topic FIFO (requer groupId)
snsClient.publish(PublishRequest.builder()
        .topicArn(fifoTopicArn)
        .message("Mensagem ordenada")
        .messageGroupId("order-group")
        .messageDeduplicationId("unique-id")  // opcional se content-based dedup
        .build());`} />

        <Callout type="info">
          <p>
            Com <code>ContentBasedDeduplication</code> habilitado, o SNS gera
            automaticamente o deduplication ID baseado no hash SHA-256 do corpo
            da mensagem. O <code>messageDeduplicationId</code> explicito e opcional nesse caso.
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
          Quando uma subscription falha ao entregar mensagens para o endpoint, o SNS
          pode redirecionar essas mensagens para uma DLQ (dead-letter queue). Na infraestrutura
          Terraform, ambas as subscriptions (orders e analytics) possuem redrive policy
          apontando para a fila <code>poc-sns-dlq</code> com retencao de 14 dias.
        </p>
        <p>
          Alem das subscriptions Terraform, a API permite configurar DLQ dinamicamente
          em qualquer subscription existente.
        </p>
        <CodeBlock language="java" title="Configurar DLQ em subscription" code={`// Configurar redrive policy na subscription
String redrivePolicy = objectMapper.writeValueAsString(
        Map.of("deadLetterTargetArn", dlqArn));

snsClient.setSubscriptionAttributes(
        SetSubscriptionAttributesRequest.builder()
                .subscriptionArn(subscriptionArn)
                .attributeName("RedrivePolicy")
                .attributeValue(redrivePolicy)
                .build());`} />

        <CodeBlock language="hcl" title="DLQ e redrive no Terraform" code={`resource "aws_sqs_queue" "sns_dlq" {
  name                      = "\${var.project_name}-\${var.environment}-poc-sns-dlq"
  message_retention_seconds = 1209600  # 14 dias
}

resource "aws_sqs_queue" "subscriber_orders" {
  name = "\${var.project_name}-\${var.environment}-poc-sns-orders"

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.sns_dlq.arn
    maxReceiveCount     = 3
  })
}`} />

        <Callout type="warning">
          <p>
            No LocalStack, o comportamento da DLQ para SNS subscriptions pode nao ser
            identico ao da AWS real. Mensagens rejeitadas pela fila subscriber sao
            redirecionadas, mas o timing pode variar.
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
          A tabela abaixo lista todos os endpoints expostos pela POC.
          Todos usam o prefixo base <code>/api/sns</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            ['POST', <code>/api/sns/topics/standard?name=X</code>, 'Criar topic standard (body opcional: tags)'],
            ['POST', <code>/api/sns/topics/fifo?name=X</code>, 'Criar topic FIFO com content-based dedup'],
            ['GET', <code>/api/sns/topics</code>, 'Listar todos os topics'],
            ['GET', <code>/api/sns/topics/attributes?topicArn=X</code>, 'Consultar atributos de um topic'],
            ['PUT', <code>/api/sns/topics/attributes?topicArn=X&...</code>, 'Alterar atributo de um topic'],
            ['DELETE', <code>/api/sns/topics?topicArn=X</code>, 'Deletar um topic'],
            ['POST', <code>/api/sns/topics/tags?topicArn=X</code>, 'Adicionar tags a um topic'],
            ['GET', <code>/api/sns/topics/tags?topicArn=X</code>, 'Listar tags de um topic'],
            ['POST', <code>/api/sns/subscriptions/sqs?topicArn=X&queueUrl=Y</code>, 'Criar subscription SNS para SQS'],
            ['GET', <code>/api/sns/subscriptions?topicArn=X</code>, 'Listar subscriptions de um topic'],
            ['GET', <code>/api/sns/subscriptions/attributes?subscriptionArn=X</code>, 'Consultar atributos de subscription'],
            ['PUT', <code>/api/sns/subscriptions/filter-policy?subscriptionArn=X</code>, 'Atualizar filter policy'],
            ['PUT', <code>/api/sns/subscriptions/attributes?subscriptionArn=X&...</code>, 'Alterar atributo de subscription'],
            ['DELETE', <code>/api/sns/subscriptions?subscriptionArn=X</code>, 'Remover subscription (unsubscribe)'],
            ['PUT', <code>/api/sns/subscriptions/dlq?subscriptionArn=X&dlqArn=Y</code>, 'Configurar DLQ em subscription'],
            ['POST', <code>/api/sns/notifications/publish?topicArn=X</code>, 'Publicar mensagem simples'],
            ['POST', <code>/api/sns/notifications/publish-with-attributes?topicArn=X&message=Y</code>, 'Publicar com message attributes'],
            ['POST', <code>/api/sns/notifications/publish-fifo?topicArn=X&groupId=G</code>, 'Publicar em topic FIFO'],
            ['POST', <code>/api/sns/notifications/publish-batch?topicArn=X</code>, 'Publicar batch (ate 10 mensagens)'],
            ['POST', <code>/api/sns/fanout/publish-and-verify?topicArn=X&subscriberQueueUrls=U1,U2</code>, 'Publicar e verificar fan-out nas filas'],
            ['POST', <code>/api/sns/fanout/filtered?topicArn=X&eventType=E&subscriberQueueUrls=U1,U2</code>, 'Demonstrar fan-out com filtering'],
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
terraform apply -auto-approve

# Ver recursos criados
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-sns-notifications
./mvnw spring-boot:run

# Roda na porta 8083`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Listar topics
curl "http://localhost:8083/api/sns/topics"

# Criar topic standard
curl -X POST "http://localhost:8083/api/sns/topics/standard?name=meu-topic" \\
  -H "Content-Type: application/json" \\
  -d '{"env": "dev", "team": "backend"}'

# Criar subscription SNS -> SQS
curl -X POST "http://localhost:8083/api/sns/subscriptions/sqs?topicArn=<TOPIC_ARN>&queueUrl=<QUEUE_URL>" \\
  -H "Content-Type: application/json" \\
  -d '{"rawMessageDelivery": true, "filterPolicy": {"eventType": ["order_created"]}}'

# Publicar mensagem simples
curl -X POST "http://localhost:8083/api/sns/notifications/publish?topicArn=<TOPIC_ARN>" \\
  -H "Content-Type: text/plain" \\
  -d "Hello SNS!"

# Publicar com attributes (para filtering)
curl -X POST "http://localhost:8083/api/sns/notifications/publish-with-attributes?topicArn=<TOPIC_ARN>&message=Pedido+criado" \\
  -H "Content-Type: application/json" \\
  -d '{"eventType": "order_created"}'

# Fan-out: publicar e verificar entrega nas filas
curl -X POST "http://localhost:8083/api/sns/fanout/publish-and-verify?topicArn=<TOPIC_ARN>&subscriberQueueUrls=<URL1>,<URL2>" \\
  -H "Content-Type: text/plain" \\
  -d "Fan-out message"

# Fan-out filtrado
curl -X POST "http://localhost:8083/api/sns/fanout/filtered?topicArn=<TOPIC_ARN>&eventType=order_created&subscriberQueueUrls=<URL1>,<URL2>" \\
  -H "Content-Type: text/plain" \\
  -d "Filtered fan-out message"

# Listar subscriptions
curl "http://localhost:8083/api/sns/subscriptions?topicArn=<TOPIC_ARN>"`} />

        <Callout type="warning">
          <p>
            Substitua <code>{'<TOPIC_ARN>'}</code>, <code>{'<QUEUE_URL>'}</code> e demais
            placeholders pelos valores reais. Use <code>terraform output</code> ou
            o endpoint <code>/api/sns/topics</code> para obter os ARNs dos topics.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/sns-notifications" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
