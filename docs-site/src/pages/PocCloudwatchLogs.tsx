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
  { id: 'log-groups', title: 'Log Groups' },
  { id: 'log-streams', title: 'Log Streams' },
  { id: 'log-events', title: 'Log Events' },
  { id: 'metrics', title: 'Metrics' },
  { id: 'alarms', title: 'Alarms' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocCloudwatchLogs() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>CloudWatch Logs</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora as funcionalidades do Amazon CloudWatch em uma aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para gerenciar log groups, log streams,
          log events, metricas customizadas e alarmes.
        </p>
        <p>
          As funcionalidades cobertas incluem criacao e gerenciamento de log groups
          com politicas de retencao, log streams, envio e consulta de log events,
          publicacao de metricas customizadas (individual e batch), consulta de
          estatisticas e gerenciamento de alarmes baseados em metricas.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O CloudWatch Logs e
            CloudWatch Metrics possuem boa cobertura no tier gratuito, incluindo
            log groups, streams, metricas customizadas e alarmes.
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
          A aplicacao segue a estrutura padrao do projeto: camadas de{' '}
          <strong>Controller</strong> e <strong>Service</strong> com injecao por construtor.
          Dois pares controller/service cobrem as areas de logs e metricas respectivamente.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.cloudwatchlogs/
  config/         CloudWatchConfig (CloudWatchLogsClient + CloudWatchClient beans)
  controller/     LogController, MetricController
  service/        LogGroupService, MetricService
  dto/            Records: PutLogEventDto, PutMetricDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8088</code> para nao conflitar com
            outras POCs do projeto.
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
          O modulo Terraform em <code>infra/localstack/modules/cloudwatch</code> provisiona
          um log group com stream e um alarme de metricas para a POC.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_cloudwatch_log_group</code>, '/nameless/dev/poc-app', 'Log group principal com retencao de 7 dias'],
            [<code>aws_cloudwatch_log_stream</code>, 'application', 'Log stream dentro do log group principal'],
            [<code>aws_cloudwatch_metric_alarm</code>, 'high-error-rate', 'Alarme quando ErrorCount excede 10 em 1 minuto'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/cloudwatch/main.tf (trecho)" code={`resource "aws_cloudwatch_log_group" "app_logs" {
  name              = "/nameless/\${var.environment}/poc-app"
  retention_in_days = 7

  tags = {
    Name        = "\${var.project_name}-poc-app-logs"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "cloudwatch-logs"
  }
}

resource "aws_cloudwatch_log_stream" "app_stream" {
  name           = "application"
  log_group_name = aws_cloudwatch_log_group.app_logs.name
}

resource "aws_cloudwatch_metric_alarm" "high_error_rate" {
  alarm_name          = "\${var.project_name}-\${var.environment}-poc-high-error-rate"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ErrorCount"
  namespace           = "Nameless/POC"
  period              = 60
  statistic           = "Sum"
  threshold           = 10
  alarm_description   = "Alarme quando taxa de erros excede 10 em 1 minuto"
}`} />

        {/* ================================================================ */}
        {/* LOG GROUPS */}
        {/* ================================================================ */}
        <h2 id="log-groups" className="heading-anchor">
          <a href="#log-groups" className="heading-anchor__link" aria-hidden="true">#</a>
          Log Groups
        </h2>
        <p>
          Log groups sao containers logicos que agrupam log streams relacionados.
          A POC demonstra como criar log groups com tags opcionais, listar log groups
          (com filtro por prefixo), configurar politicas de retencao e deletar log groups.
        </p>
        <CodeBlock language="java" title="LogGroupService.java" code={`// Criar log group
logsClient.createLogGroup(CreateLogGroupRequest.builder()
    .logGroupName(groupName)
    .build());

// Adicionar tags ao log group
logsClient.tagLogGroup(TagLogGroupRequest.builder()
    .logGroupName(groupName)
    .tags(tags)
    .build());

// Listar log groups com filtro por prefixo
logsClient.describeLogGroups(DescribeLogGroupsRequest.builder()
    .logGroupNamePrefix(prefix)
    .build()).logGroups();

// Configurar politica de retencao (ex: 7 dias)
logsClient.putRetentionPolicy(PutRetentionPolicyRequest.builder()
    .logGroupName(groupName)
    .retentionInDays(7)
    .build());

// Deletar log group
logsClient.deleteLogGroup(DeleteLogGroupRequest.builder()
    .logGroupName(groupName)
    .build());`} />

        <Callout type="info">
          <p>
            Valores validos para <code>retentionInDays</code> incluem: 1, 3, 5, 7, 14,
            30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1096, 1827, 2192, 2557,
            2922, 3288 e 3653. Sem politica de retencao, os logs sao mantidos indefinidamente.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* LOG STREAMS */}
        {/* ================================================================ */}
        <h2 id="log-streams" className="heading-anchor">
          <a href="#log-streams" className="heading-anchor__link" aria-hidden="true">#</a>
          Log Streams
        </h2>
        <p>
          Log streams sao sequencias de log events que compartilham a mesma fonte.
          Cada log stream pertence a exatamente um log group. A POC permite criar
          log streams dentro de um log group e listar todos os streams existentes.
        </p>
        <CodeBlock language="java" title="LogGroupService.java" code={`// Criar log stream dentro de um log group
logsClient.createLogStream(CreateLogStreamRequest.builder()
    .logGroupName(groupName)
    .logStreamName(streamName)
    .build());

// Listar log streams de um log group
logsClient.describeLogStreams(DescribeLogStreamsRequest.builder()
    .logGroupName(groupName)
    .build()).logStreams();`} />

        <Callout type="tip">
          <p>
            Uma pratica comum e usar um log stream por instancia de aplicacao ou por
            data (ex: <code>application/2026-04-11</code>), facilitando a organizacao
            e consulta dos logs.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* LOG EVENTS */}
        {/* ================================================================ */}
        <h2 id="log-events" className="heading-anchor">
          <a href="#log-events" className="heading-anchor__link" aria-hidden="true">#</a>
          Log Events
        </h2>
        <p>
          Log events sao os registros individuais de log dentro de um stream. Cada evento
          possui uma mensagem e um timestamp. A POC demonstra como enviar eventos em batch
          e consultar eventos com limite configuravel.
        </p>
        <CodeBlock language="java" title="LogGroupService.java" code={`// Enviar log events para um stream
List<InputLogEvent> inputEvents = events.stream()
    .map(e -> InputLogEvent.builder()
        .message(e.message())
        .timestamp(e.effectiveTimestamp())
        .build())
    .toList();

PutLogEventsResponse response = logsClient.putLogEvents(
    PutLogEventsRequest.builder()
        .logGroupName(groupName)
        .logStreamName(streamName)
        .logEvents(inputEvents)
        .build());

// Consultar log events de um stream (com limite)
GetLogEventsResponse response = logsClient.getLogEvents(
    GetLogEventsRequest.builder()
        .logGroupName(groupName)
        .logStreamName(streamName)
        .limit(50)
        .startFromHead(true)
        .build());`} />

        <Callout type="warning">
          <p>
            Os log events devem ser enviados em ordem cronologica dentro de um mesmo
            stream. O <code>nextSequenceToken</code> retornado pelo <code>putLogEvents</code>{' '}
            deve ser usado em chamadas subsequentes na AWS real, mas no LocalStack
            esse controle e mais flexivel.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* METRICS */}
        {/* ================================================================ */}
        <h2 id="metrics" className="heading-anchor">
          <a href="#metrics" className="heading-anchor__link" aria-hidden="true">#</a>
          Metrics
        </h2>
        <p>
          CloudWatch Metrics permite publicar metricas customizadas organizadas por namespace.
          A POC demonstra envio individual e em batch, listagem de metricas e consulta de
          estatisticas agregadas (Average, Sum, Maximum, Minimum, SampleCount).
        </p>
        <CodeBlock language="java" title="MetricService.java" code={`// Publicar metrica individual
MetricDatum datum = MetricDatum.builder()
    .metricName(metricName)
    .value(value)
    .unit(StandardUnit.fromValue(unit))
    .timestamp(Instant.now())
    .build();

cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
    .namespace(namespace)
    .metricData(datum)
    .build());

// Publicar metricas em batch
List<MetricDatum> data = metrics.stream()
    .map(m -> MetricDatum.builder()
        .metricName(m.metricName())
        .value(m.value())
        .unit(StandardUnit.fromValue(m.unit()))
        .timestamp(Instant.now())
        .build())
    .toList();

cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
    .namespace(namespace)
    .metricData(data)
    .build());

// Consultar estatisticas de uma metrica
GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(
    GetMetricStatisticsRequest.builder()
        .namespace(namespace)
        .metricName(metricName)
        .statistics(Statistic.fromValue("Average"))
        .startTime(start)
        .endTime(end)
        .period(periodSeconds)
        .build());`} />

        <Callout type="info">
          <p>
            Unidades validas para <code>StandardUnit</code> incluem: <code>Seconds</code>,{' '}
            <code>Microseconds</code>, <code>Milliseconds</code>, <code>Bytes</code>,{' '}
            <code>Kilobytes</code>, <code>Megabytes</code>, <code>Count</code>,{' '}
            <code>Percent</code>, <code>None</code>, entre outras.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ALARMS */}
        {/* ================================================================ */}
        <h2 id="alarms" className="heading-anchor">
          <a href="#alarms" className="heading-anchor__link" aria-hidden="true">#</a>
          Alarms
        </h2>
        <p>
          CloudWatch Alarms monitoram metricas e disparam acoes quando um threshold
          e atingido. A POC demonstra como criar alarmes baseados em metricas,
          listar alarmes existentes e deletar alarmes.
        </p>
        <CodeBlock language="java" title="MetricService.java" code={`// Criar alarme baseado em metrica
cloudWatchClient.putMetricAlarm(PutMetricAlarmRequest.builder()
    .alarmName(alarmName)
    .namespace(namespace)
    .metricName(metricName)
    .threshold(threshold)
    .comparisonOperator(ComparisonOperator.fromValue("GreaterThanThreshold"))
    .evaluationPeriods(1)
    .period(60)
    .statistic(Statistic.AVERAGE)
    .actionsEnabled(false)
    .build());

// Listar alarmes
cloudWatchClient.describeAlarms(DescribeAlarmsRequest.builder()
    .build()).metricAlarms();

// Deletar alarme
cloudWatchClient.deleteAlarms(DeleteAlarmsRequest.builder()
    .alarmNames(alarmName)
    .build());`} />

        <Callout type="tip">
          <p>
            Operadores de comparacao disponiveis: <code>GreaterThanThreshold</code>,{' '}
            <code>GreaterThanOrEqualToThreshold</code>, <code>LessThanThreshold</code>{' '}
            e <code>LessThanOrEqualToThreshold</code>. No LocalStack, as acoes
            de alarme (SNS, Auto Scaling) nao sao executadas, mas o estado do alarme
            e avaliado corretamente.
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
          A tabela abaixo lista todos os endpoints expostos pela POC, organizados
          por controller. A aplicacao roda na porta <code>8088</code>.
        </p>

        <h3>LogController — <code>/api/cloudwatch/logs</code></h3>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            ['POST', <code>/api/cloudwatch/logs/groups?groupName=X</code>, 'Criar log group (body opcional com tags)'],
            ['GET', <code>/api/cloudwatch/logs/groups?prefix=X</code>, 'Listar log groups (filtro por prefixo opcional)'],
            ['DELETE', <code>{'/api/cloudwatch/logs/groups/{groupName}'}</code>, 'Deletar log group'],
            ['PUT', <code>{'/api/cloudwatch/logs/groups/{groupName}/retention?retentionDays=N'}</code>, 'Configurar politica de retencao'],
            ['POST', <code>{'/api/cloudwatch/logs/groups/{groupName}/streams?streamName=X'}</code>, 'Criar log stream'],
            ['GET', <code>{'/api/cloudwatch/logs/groups/{groupName}/streams'}</code>, 'Listar log streams de um grupo'],
            ['POST', <code>{'/api/cloudwatch/logs/groups/{groupName}/streams/{streamName}/events'}</code>, 'Enviar log events (body: lista de eventos)'],
            ['GET', <code>{'/api/cloudwatch/logs/groups/{groupName}/streams/{streamName}/events?limit=N'}</code>, 'Consultar log events (limite padrao: 50)'],
          ]}
        />

        <h3>MetricController — <code>/api/cloudwatch/metrics</code></h3>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            ['POST', <code>/api/cloudwatch/metrics?namespace=X&metricName=Y&value=N&unit=Z</code>, 'Publicar metrica individual'],
            ['POST', <code>/api/cloudwatch/metrics/batch?namespace=X</code>, 'Publicar metricas em batch (body: lista de metricas)'],
            ['GET', <code>/api/cloudwatch/metrics?namespace=X</code>, 'Listar metricas (filtro por namespace opcional)'],
            ['GET', <code>/api/cloudwatch/metrics/statistics?namespace=X&metricName=Y&stat=Z&periodMinutes=N</code>, 'Consultar estatisticas agregadas'],
            ['POST', <code>/api/cloudwatch/metrics/alarms?alarmName=X&namespace=Y&metricName=Z&threshold=N&comparisonOperator=OP</code>, 'Criar alarme de metrica'],
            ['GET', <code>/api/cloudwatch/metrics/alarms</code>, 'Listar todos os alarmes'],
            ['DELETE', <code>{'/api/cloudwatch/metrics/alarms/{alarmName}'}</code>, 'Deletar alarme'],
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
        <CodeBlock language="bash" code={`cd pocs/poc-cloudwatch-logs
./mvnw spring-boot:run

# Roda na porta 8088`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Criar log group
curl -X POST "http://localhost:8088/api/cloudwatch/logs/groups?groupName=/app/test" \\
  -H "Content-Type: application/json" \\
  -d '{"env": "dev", "team": "backend"}'

# Listar log groups
curl "http://localhost:8088/api/cloudwatch/logs/groups"

# Criar log stream
curl -X POST "http://localhost:8088/api/cloudwatch/logs/groups/%2Fapp%2Ftest/streams?streamName=my-stream"

# Enviar log events
curl -X POST "http://localhost:8088/api/cloudwatch/logs/groups/%2Fapp%2Ftest/streams/my-stream/events" \\
  -H "Content-Type: application/json" \\
  -d '[{"message": "Aplicacao iniciada com sucesso"}, {"message": "Processando requisicao"}]'

# Consultar log events
curl "http://localhost:8088/api/cloudwatch/logs/groups/%2Fapp%2Ftest/streams/my-stream/events?limit=10"

# Publicar metrica customizada
curl -X POST "http://localhost:8088/api/cloudwatch/metrics?namespace=Nameless/POC&metricName=RequestCount&value=1&unit=Count"

# Listar metricas
curl "http://localhost:8088/api/cloudwatch/metrics?namespace=Nameless/POC"

# Criar alarme
curl -X POST "http://localhost:8088/api/cloudwatch/metrics/alarms?alarmName=high-errors&namespace=Nameless/POC&metricName=ErrorCount&threshold=10&comparisonOperator=GreaterThanThreshold"

# Listar alarmes
curl "http://localhost:8088/api/cloudwatch/metrics/alarms"

# Consultar estatisticas
curl "http://localhost:8088/api/cloudwatch/metrics/statistics?namespace=Nameless/POC&metricName=RequestCount&stat=Average&periodMinutes=60"`} />

        <Callout type="warning">
          <p>
            Nomes de log groups que contem barras (ex: <code>/app/test</code>) devem
            ser codificados como <code>%2F</code> quando usados como path variable
            nas URLs. No query parameter (<code>?groupName=</code>) podem ser usados
            diretamente.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/cloudwatch-logs" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
