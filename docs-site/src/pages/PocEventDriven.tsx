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
  { id: 'event-bus', title: 'Event Bus' },
  { id: 'rules', title: 'Rules' },
  { id: 'targets', title: 'Targets' },
  { id: 'publish', title: 'Publicar Eventos' },
  { id: 'patterns', title: 'Event Patterns' },
  { id: 'scheduled-rules', title: 'Scheduled Rules' },
  { id: 'dlq', title: 'Dead-Letter Queue' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocEventDriven() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>Event-Driven (EventBridge)</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora o <strong>Amazon EventBridge</strong> em uma aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para criar event buses customizados,
          definir rules com event patterns, configurar targets (SQS e Lambda) e
          publicar eventos programaticamente.
        </p>
        <p>
          As funcionalidades cobertas incluem custom event buses, rules com event patterns,
          scheduled rules, targets SQS e Lambda, publicacao de eventos unitarios e em batch,
          verificacao de entrega em filas target e dead-letter queues para eventos falhados.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O EventBridge tem boa cobertura
            no tier gratuito, incluindo custom buses, rules, targets SQS e scheduled expressions.
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
          Sao 3 controllers, cada um responsavel por uma area funcional: gerenciamento de
          event buses, rules/targets e publicacao de eventos.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.eventdriven/
  config/         EventBridgeConfig (EventBridgeClient, SqsClient, LambdaClient)
  controller/     EventBusController, RuleController, EventPublisherController
  service/        EventBusService, RuleService, EventPublisherService
  dto/            Records: CreateRuleDto, PutEventDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8085</code> para nao conflitar com as demais POCs.
            A configuracao esta em <code>application.yml</code>.
          </p>
        </Callout>

        <p>
          O <code>EventBridgeConfig</code> cria 3 beans do AWS SDK: o{' '}
          <code>EventBridgeClient</code> para operacoes de bus, rules e publicacao; o{' '}
          <code>SqsClient</code> para verificar entrega de eventos nas filas target; e o{' '}
          <code>LambdaClient</code> para integracao com funcoes Lambda como target.
        </p>
        <CodeBlock language="java" title="EventBridgeConfig.java" code={`@Configuration
public class EventBridgeConfig {

    @Bean
    public EventBridgeClient eventBridgeClient(
            @Value("\${aws.endpoint}") String endpoint,
            @Value("\${aws.region}") String region,
            @Value("\${aws.access-key}") String accessKey,
            @Value("\${aws.secret-key}") String secretKey) {
        return EventBridgeClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(staticCredentials(accessKey, secretKey))
                .build();
    }

    @Bean
    public SqsClient sqsClient(...) { /* mesma configuracao */ }

    @Bean
    public LambdaClient lambdaClient(...) { /* mesma configuracao */ }
}`} />

        {/* ================================================================ */}
        {/* TERRAFORM */}
        {/* ================================================================ */}
        <h2 id="terraform" className="heading-anchor">
          <a href="#terraform" className="heading-anchor__link" aria-hidden="true">#</a>
          Infraestrutura Terraform
        </h2>
        <p>
          O modulo Terraform em <code>infra/localstack/modules/eventbridge</code> provisiona
          um custom event bus, filas SQS como target e DLQ, rules com event patterns
          e targets associados.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_cloudwatch_event_bus</code>, 'poc-custom-bus', 'Event bus customizado para eventos da aplicacao'],
            [<code>aws_sqs_queue</code>, 'poc-event-target', 'Fila SQS que recebe eventos roteados pela rule'],
            [<code>aws_sqs_queue</code>, 'poc-event-dlq', 'Dead-letter queue para eventos falhados (retencao 14 dias)'],
            [<code>aws_cloudwatch_event_rule</code>, 'poc-order-events', 'Captura eventos de pedidos (OrderCreated, OrderUpdated, OrderCancelled)'],
            [<code>aws_cloudwatch_event_rule</code>, 'poc-all-events', 'Catch-all para observabilidade (prefix: com.nameless)'],
            [<code>aws_cloudwatch_event_target</code>, 'order-events-sqs-target', 'Envia eventos de pedidos para a fila SQS com DLQ configurada'],
            [<code>aws_sqs_queue_policy</code>, '--', 'Permite EventBridge publicar na fila SQS target'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/eventbridge/main.tf (trecho)" code={`# Custom Event Bus
resource "aws_cloudwatch_event_bus" "custom_bus" {
  name = "\${var.project_name}-\${var.environment}-poc-custom-bus"
}

# SQS Queue como target
resource "aws_sqs_queue" "event_target" {
  name = "\${var.project_name}-\${var.environment}-poc-event-target"
}

# DLQ para eventos falhados
resource "aws_sqs_queue" "event_dlq" {
  name                      = "\${var.project_name}-\${var.environment}-poc-event-dlq"
  message_retention_seconds = 1209600  # 14 dias
}

# Rule: Order Events
resource "aws_cloudwatch_event_rule" "order_events" {
  name           = "\${var.project_name}-\${var.environment}-poc-order-events"
  event_bus_name = aws_cloudwatch_event_bus.custom_bus.name
  description    = "Captura eventos de pedidos"

  event_pattern = jsonencode({
    source      = ["com.nameless.orders"]
    detail-type = ["OrderCreated", "OrderUpdated", "OrderCancelled"]
  })
}

# Target: SQS com DLQ
resource "aws_cloudwatch_event_target" "order_events_sqs" {
  rule           = aws_cloudwatch_event_rule.order_events.name
  event_bus_name = aws_cloudwatch_event_bus.custom_bus.name
  target_id      = "order-events-sqs-target"
  arn            = aws_sqs_queue.event_target.arn

  dead_letter_config {
    arn = aws_sqs_queue.event_dlq.arn
  }
}`} />

        {/* ================================================================ */}
        {/* EVENT BUS */}
        {/* ================================================================ */}
        <h2 id="event-bus" className="heading-anchor">
          <a href="#event-bus" className="heading-anchor__link" aria-hidden="true">#</a>
          Event Bus
        </h2>
        <p>
          O <strong>event bus</strong> e o canal central do EventBridge. Toda conta AWS
          possui um bus <code>default</code>, mas a POC demonstra como criar buses customizados
          para isolar dominios de eventos. Operacoes disponiveis: criar, listar, descrever e deletar.
        </p>
        <CodeBlock language="java" title="EventBusService.java" code={`// Criar custom event bus
CreateEventBusResponse response = eventBridgeClient.createEventBus(
        CreateEventBusRequest.builder().name(busName).build());
String arn = response.eventBusArn();

// Listar todos os buses
List<EventBus> buses = eventBridgeClient.listEventBuses(
        ListEventBusesRequest.builder().build()).eventBuses();

// Descrever um bus especifico
DescribeEventBusResponse bus = eventBridgeClient.describeEventBus(
        DescribeEventBusRequest.builder().name(busName).build());

// Deletar bus customizado
eventBridgeClient.deleteEventBus(
        DeleteEventBusRequest.builder().name(busName).build());`} />

        <Callout type="warning">
          <p>
            Nao e possivel deletar o bus <code>default</code>. Antes de deletar um bus
            customizado, remova todas as rules associadas a ele.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* RULES */}
        {/* ================================================================ */}
        <h2 id="rules" className="heading-anchor">
          <a href="#rules" className="heading-anchor__link" aria-hidden="true">#</a>
          Rules
        </h2>
        <p>
          <strong>Rules</strong> definem quais eventos serao capturados e para onde serao roteados.
          Cada rule pertence a um event bus e pode ter um <code>eventPattern</code> (filtragem
          por conteudo) ou uma <code>scheduleExpression</code> (execucao periodica). A POC
          permite criar rules, listar, descrever, habilitar/desabilitar e deletar.
        </p>
        <CodeBlock language="java" title="RuleService.java — criar rule" code={`// Criar rule com event pattern
PutRuleRequest.Builder builder = PutRuleRequest.builder()
        .name(dto.ruleName())
        .eventBusName(busName)
        .state(dto.enabled() ? RuleState.ENABLED : RuleState.DISABLED);

if (dto.description() != null) builder.description(dto.description());
if (dto.eventPattern() != null) builder.eventPattern(dto.eventPattern());
if (dto.scheduleExpression() != null) builder.scheduleExpression(dto.scheduleExpression());

PutRuleResponse response = eventBridgeClient.putRule(builder.build());
String ruleArn = response.ruleArn();`} />

        <p>
          O DTO <code>CreateRuleDto</code> e um record Java 21 que encapsula os parametros:
        </p>
        <CodeBlock language="java" title="CreateRuleDto.java" code={`public record CreateRuleDto(
        String ruleName,
        String description,
        String eventPattern,
        String scheduleExpression,
        boolean enabled
) {}`} />

        <CodeBlock language="java" title="RuleService.java — habilitar/desabilitar/deletar" code={`// Habilitar rule
eventBridgeClient.enableRule(EnableRuleRequest.builder()
        .eventBusName(busName).name(ruleName).build());

// Desabilitar rule
eventBridgeClient.disableRule(DisableRuleRequest.builder()
        .eventBusName(busName).name(ruleName).build());

// Deletar rule (remove targets antes)
eventBridgeClient.removeTargets(RemoveTargetsRequest.builder()
        .eventBusName(busName).rule(ruleName)
        .ids(targets.stream().map(t -> t.get("id")).toList())
        .build());
eventBridgeClient.deleteRule(DeleteRuleRequest.builder()
        .eventBusName(busName).name(ruleName).build());`} />

        <Callout type="tip">
          <p>
            Antes de deletar uma rule, e obrigatorio remover todos os seus targets.
            O <code>RuleService</code> faz isso automaticamente no metodo <code>deleteRule</code>.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* TARGETS */}
        {/* ================================================================ */}
        <h2 id="targets" className="heading-anchor">
          <a href="#targets" className="heading-anchor__link" aria-hidden="true">#</a>
          Targets
        </h2>
        <p>
          <strong>Targets</strong> sao os destinos para onde os eventos filtrados por uma rule
          sao enviados. A POC suporta dois tipos de target: <strong>SQS</strong> (fila)
          e <strong>Lambda</strong> (funcao). Cada target e identificado por um <code>targetId</code>{' '}
          unico dentro da rule.
        </p>
        <CodeBlock language="java" title="RuleService.java — adicionar targets" code={`// Adicionar target SQS
eventBridgeClient.putTargets(PutTargetsRequest.builder()
        .eventBusName(busName)
        .rule(ruleName)
        .targets(Target.builder()
                .id(targetId)
                .arn(sqsArn)
                .build())
        .build());

// Adicionar target Lambda
eventBridgeClient.putTargets(PutTargetsRequest.builder()
        .eventBusName(busName)
        .rule(ruleName)
        .targets(Target.builder()
                .id(targetId)
                .arn(lambdaArn)
                .build())
        .build());

// Listar targets de uma rule
ListTargetsByRuleResponse response = eventBridgeClient.listTargetsByRule(
        ListTargetsByRuleRequest.builder()
                .eventBusName(busName)
                .rule(ruleName)
                .build());`} />

        <Callout type="info">
          <p>
            Uma rule pode ter ate 5 targets na AWS. No LocalStack, o limite
            pode variar, mas o comportamento funcional e o mesmo.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* PUBLISH */}
        {/* ================================================================ */}
        <h2 id="publish" className="heading-anchor">
          <a href="#publish" className="heading-anchor__link" aria-hidden="true">#</a>
          Publicar Eventos
        </h2>
        <p>
          A publicacao de eventos e feita via <code>PutEvents</code>. Cada evento e composto
          por <code>source</code> (origem), <code>detailType</code> (tipo do evento)
          e <code>detail</code> (payload JSON). A POC oferece 3 modos: publicacao unitaria,
          batch (multiplos eventos) e publicacao com verificacao de entrega.
        </p>
        <CodeBlock language="java" title="PutEventDto.java" code={`public record PutEventDto(
        String source,
        String detailType,
        Map<String, Object> detail
) {}`} />

        <CodeBlock language="java" title="EventPublisherService.java — publicar evento" code={`// Publicar evento unitario
String detailJson = objectMapper.writeValueAsString(dto.detail());

PutEventsResponse response = eventBridgeClient.putEvents(
        PutEventsRequest.builder()
                .entries(PutEventsRequestEntry.builder()
                        .eventBusName(busName)
                        .source(dto.source())
                        .detailType(dto.detailType())
                        .detail(detailJson)
                        .build())
                .build());

int failedCount = response.failedEntryCount();`} />

        <CodeBlock language="java" title="EventPublisherService.java — batch e verificacao" code={`// Publicar batch (multiplos eventos em uma chamada)
List<PutEventsRequestEntry> entries = events.stream()
        .map(dto -> PutEventsRequestEntry.builder()
                .eventBusName(busName)
                .source(dto.source())
                .detailType(dto.detailType())
                .detail(objectMapper.writeValueAsString(dto.detail()))
                .build())
        .toList();

eventBridgeClient.putEvents(PutEventsRequest.builder()
        .entries(entries).build());

// Publicar e verificar entrega nas filas target
Map<String, Object> publishResult = putEvent(busName, dto);
for (String queueUrl : targetQueueUrls) {
    List<Message> received = sqsClient.receiveMessage(
            ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build()).messages();
}`} />

        <Callout type="tip">
          <p>
            O endpoint <code>/verify</code> publica o evento e imediatamente verifica se
            ele chegou nas filas target informadas (long polling de 5s). Ideal para testes
            end-to-end do fluxo EventBridge &rarr; SQS.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* EVENT PATTERNS */}
        {/* ================================================================ */}
        <h2 id="patterns" className="heading-anchor">
          <a href="#patterns" className="heading-anchor__link" aria-hidden="true">#</a>
          Event Patterns
        </h2>
        <p>
          Event patterns sao filtros JSON que determinam quais eventos uma rule captura.
          O EventBridge compara o evento publicado com o pattern da rule e so roteia
          se houver match. Os campos mais comuns sao <code>source</code> e{' '}
          <code>detail-type</code>.
        </p>
        <CodeBlock language="json" title="Pattern: capturar eventos de pedidos" code={`{
  "source": ["com.nameless.orders"],
  "detail-type": ["OrderCreated", "OrderUpdated", "OrderCancelled"]
}`} />

        <CodeBlock language="json" title="Pattern: catch-all com prefix" code={`{
  "source": [{ "prefix": "com.nameless" }]
}`} />

        <p>
          Ao criar uma rule via API, o pattern e enviado como string JSON no campo{' '}
          <code>eventPattern</code> do <code>CreateRuleDto</code>:
        </p>
        <CodeBlock language="bash" title="Exemplo de criacao de rule com pattern" code={`curl -X POST "http://localhost:8085/api/events/rules?busName=my-bus" \\
  -H "Content-Type: application/json" \\
  -d '{
    "ruleName": "order-rule",
    "description": "Captura eventos de pedido",
    "eventPattern": "{\\"source\\": [\\"com.nameless.orders\\"], \\"detail-type\\": [\\"OrderCreated\\"]}",
    "scheduleExpression": null,
    "enabled": true
  }'`} />

        <Callout type="info">
          <p>
            O EventBridge suporta operadores avancados nos patterns como{' '}
            <code>prefix</code>, <code>anything-but</code>, <code>numeric</code>{' '}
            e <code>exists</code>. Consulte a documentacao da AWS para detalhes.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* SCHEDULED RULES */}
        {/* ================================================================ */}
        <h2 id="scheduled-rules" className="heading-anchor">
          <a href="#scheduled-rules" className="heading-anchor__link" aria-hidden="true">#</a>
          Scheduled Rules
        </h2>
        <p>
          Alem de event patterns, rules podem usar <strong>schedule expressions</strong>{' '}
          para disparar em intervalos regulares. O EventBridge suporta dois formatos:
          expressoes <code>rate()</code> e expressoes <code>cron()</code>.
        </p>
        <CodeBlock language="bash" title="Criar rule com schedule expression" code={`# Rate: a cada 5 minutos
curl -X POST "http://localhost:8085/api/events/rules?busName=my-bus" \\
  -H "Content-Type: application/json" \\
  -d '{
    "ruleName": "heartbeat-rule",
    "description": "Dispara a cada 5 minutos",
    "eventPattern": null,
    "scheduleExpression": "rate(5 minutes)",
    "enabled": true
  }'

# Cron: todo dia as 9:00 UTC
curl -X POST "http://localhost:8085/api/events/rules?busName=my-bus" \\
  -H "Content-Type: application/json" \\
  -d '{
    "ruleName": "daily-report-rule",
    "description": "Dispara todo dia as 9h UTC",
    "eventPattern": null,
    "scheduleExpression": "cron(0 9 * * ? *)",
    "enabled": true
  }'`} />

        <Callout type="warning">
          <p>
            No LocalStack Community, scheduled rules sao aceitas via API mas
            a execucao automatica pode nao ocorrer. O comportamento e util
            para validar a criacao e configuracao da rule.
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
          Quando o EventBridge nao consegue entregar um evento ao target (ex: fila SQS
          indisponivel ou Lambda com erro), o evento pode ser enviado para uma{' '}
          <strong>dead-letter queue (DLQ)</strong>. A DLQ e configurada no nivel do target,
          nao da rule.
        </p>
        <CodeBlock language="hcl" title="DLQ configurada no target (Terraform)" code={`resource "aws_cloudwatch_event_target" "order_events_sqs" {
  rule           = aws_cloudwatch_event_rule.order_events.name
  event_bus_name = aws_cloudwatch_event_bus.custom_bus.name
  target_id      = "order-events-sqs-target"
  arn            = aws_sqs_queue.event_target.arn

  dead_letter_config {
    arn = aws_sqs_queue.event_dlq.arn
  }
}`} />

        <p>
          A fila DLQ criada pelo Terraform tem retencao de <strong>14 dias</strong>{' '}
          (1.209.600 segundos), tempo suficiente para investigar e reprocessar eventos
          que falharam na entrega.
        </p>
        <CodeBlock language="hcl" title="DLQ com retencao estendida" code={`resource "aws_sqs_queue" "event_dlq" {
  name                      = "\${var.project_name}-\${var.environment}-poc-event-dlq"
  message_retention_seconds = 1209600  # 14 dias
}`} />

        <Callout type="tip">
          <p>
            Use o endpoint <code>/api/events/publish/verify</code> passando a URL da DLQ
            como target para verificar se eventos falhados estao chegando corretamente
            na dead-letter queue.
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
          Todos usam o prefixo base <code>/api/events</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            ['POST', <code>/api/events/buses?busName=nome</code>, 'Criar custom event bus'],
            ['GET', <code>/api/events/buses</code>, 'Listar todos os event buses'],
            ['GET', <code>/api/events/buses/{'{busName}'}</code>, 'Descrever um event bus'],
            ['DELETE', <code>/api/events/buses/{'{busName}'}</code>, 'Deletar um event bus'],
            ['POST', <code>/api/events/rules?busName=nome</code>, 'Criar rule (body: CreateRuleDto)'],
            ['GET', <code>/api/events/rules?busName=nome</code>, 'Listar rules de um bus'],
            ['GET', <code>/api/events/rules/{'{ruleName}'}?busName=nome</code>, 'Descrever uma rule'],
            ['PUT', <code>/api/events/rules/{'{ruleName}'}/enable?busName=nome</code>, 'Habilitar rule'],
            ['PUT', <code>/api/events/rules/{'{ruleName}'}/disable?busName=nome</code>, 'Desabilitar rule'],
            ['DELETE', <code>/api/events/rules/{'{ruleName}'}?busName=nome</code>, 'Deletar rule (remove targets antes)'],
            ['POST', <code>/api/events/rules/{'{ruleName}'}/targets/sqs?busName=...&targetId=...&sqsArn=...</code>, 'Adicionar target SQS a uma rule'],
            ['POST', <code>/api/events/rules/{'{ruleName}'}/targets/lambda?busName=...&targetId=...&lambdaArn=...</code>, 'Adicionar target Lambda a uma rule'],
            ['GET', <code>/api/events/rules/{'{ruleName}'}/targets?busName=nome</code>, 'Listar targets de uma rule'],
            ['POST', <code>/api/events/publish?busName=nome</code>, 'Publicar evento unitario (body: PutEventDto)'],
            ['POST', <code>/api/events/publish/batch?busName=nome</code>, 'Publicar batch de eventos (body: List PutEventDto)'],
            ['POST', <code>/api/events/publish/verify?busName=nome&targetQueueUrls=url1,url2</code>, 'Publicar e verificar entrega nas filas target'],
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

# Ver outputs (bus name, queue URLs, ARNs)
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-event-driven
./mvnw spring-boot:run

# Roda na porta 8085`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Listar event buses
curl "http://localhost:8085/api/events/buses"

# Criar custom event bus
curl -X POST "http://localhost:8085/api/events/buses?busName=my-custom-bus"

# Criar rule com event pattern
curl -X POST "http://localhost:8085/api/events/rules?busName=nameless-local-poc-custom-bus" \\
  -H "Content-Type: application/json" \\
  -d '{
    "ruleName": "order-rule",
    "description": "Captura eventos de pedido",
    "eventPattern": "{\\"source\\": [\\"com.nameless.orders\\"], \\"detail-type\\": [\\"OrderCreated\\"]}",
    "scheduleExpression": null,
    "enabled": true
  }'

# Listar rules
curl "http://localhost:8085/api/events/rules?busName=nameless-local-poc-custom-bus"

# Adicionar target SQS
curl -X POST "http://localhost:8085/api/events/rules/order-rule/targets/sqs?busName=nameless-local-poc-custom-bus&targetId=sqs-1&sqsArn=<SQS_ARN>"

# Publicar evento
curl -X POST "http://localhost:8085/api/events/publish?busName=nameless-local-poc-custom-bus" \\
  -H "Content-Type: application/json" \\
  -d '{
    "source": "com.nameless.orders",
    "detailType": "OrderCreated",
    "detail": {"orderId": "123", "total": 99.90}
  }'

# Publicar batch
curl -X POST "http://localhost:8085/api/events/publish/batch?busName=nameless-local-poc-custom-bus" \\
  -H "Content-Type: application/json" \\
  -d '[
    {"source": "com.nameless.orders", "detailType": "OrderCreated", "detail": {"orderId": "1"}},
    {"source": "com.nameless.orders", "detailType": "OrderUpdated", "detail": {"orderId": "2"}}
  ]'

# Publicar e verificar entrega
curl -X POST "http://localhost:8085/api/events/publish/verify?busName=nameless-local-poc-custom-bus&targetQueueUrls=<QUEUE_URL>" \\
  -H "Content-Type: application/json" \\
  -d '{
    "source": "com.nameless.orders",
    "detailType": "OrderCreated",
    "detail": {"orderId": "456", "total": 50.00}
  }'`} />

        <Callout type="warning">
          <p>
            Substitua <code>{'<SQS_ARN>'}</code> e <code>{'<QUEUE_URL>'}</code> pelos
            valores reais. Use <code>terraform output</code> para obter o ARN e a URL
            da fila target provisionada.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/event-driven" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
