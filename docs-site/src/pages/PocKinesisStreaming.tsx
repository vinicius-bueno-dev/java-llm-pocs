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
  { id: 'streams', title: 'Gerenciamento de Streams' },
  { id: 'shards', title: 'Shards' },
  { id: 'producer', title: 'Producer' },
  { id: 'consumer', title: 'Consumer' },
  { id: 'partition-keys', title: 'Partition Keys' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocKinesisStreaming() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>Kinesis Streaming</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora o <strong>Amazon Kinesis Data Streams</strong> em uma aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para criar e gerenciar streams, publicar
          registros (producer) e consumi-los (consumer) usando shard iterators.
        </p>
        <p>
          As funcionalidades cobertas incluem criacao e exclusao de streams, listagem
          e descricao de streams, gerenciamento de shards, envio de registros individuais
          e em batch, consumo via shard iterators com diferentes tipos (TRIM_HORIZON,
          LATEST, etc.) e o conceito de partition keys para distribuicao de dados entre shards.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O Kinesis Data Streams
            tem boa cobertura no tier gratuito, incluindo criacao de streams com
            multiplos shards e operacoes de put/get records.
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
          Cada area funcional (streams, producer, consumer) tem seu proprio par controller/service.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.kinesisstreaming/
  config/         KinesisConfig (KinesisClient bean)
  controller/     StreamController, ProducerController, ConsumerController
  service/        StreamService, ProducerService, ConsumerService
  dto/            Records: PutRecordDto, BatchPutDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8092</code> para nao conflitar com as
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
          O modulo Terraform em <code>infra/localstack/modules/kinesis</code> provisiona
          um data stream com 2 shards para demonstrar partitioning de dados.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_kinesis_stream</code>, 'poc-data-stream', 'Data stream com 2 shards e retencao de 24 horas'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/kinesis/main.tf" code={`resource "aws_kinesis_stream" "data_stream" {
  name        = "\${var.project_name}-\${var.environment}-poc-data-stream"
  shard_count = 2

  retention_period = 24

  tags = {
    Name        = "\${var.project_name}-poc-data-stream"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "kinesis-streaming"
  }
}`} />

        <Callout type="info">
          <p>
            O <code>retention_period</code> define por quantas horas os registros ficam
            disponiveis no stream. O padrao da AWS e 24 horas (minimo), podendo chegar
            a 8760 horas (365 dias) no tier pago.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* STREAMS */}
        {/* ================================================================ */}
        <h2 id="streams" className="heading-anchor">
          <a href="#streams" className="heading-anchor__link" aria-hidden="true">#</a>
          Gerenciamento de Streams
        </h2>
        <p>
          O <code>StreamService</code> encapsula as operacoes de ciclo de vida de um
          Kinesis Data Stream: criar, listar, descrever e deletar. Ao criar um stream,
          voce especifica o numero de shards, que determina a capacidade de throughput.
        </p>
        <CodeBlock language="java" title="StreamService.java" code={`// Criar stream com N shards
kinesisClient.createStream(CreateStreamRequest.builder()
    .streamName("my-stream")
    .shardCount(2)
    .build());

// Listar todos os streams
List<String> streams = kinesisClient.listStreams(
    ListStreamsRequest.builder().build()).streamNames();

// Descrever um stream (nome, ARN, status, shards, retencao)
DescribeStreamResponse response = kinesisClient.describeStream(
    DescribeStreamRequest.builder()
        .streamName("my-stream").build());
StreamDescription desc = response.streamDescription();
// desc.streamName(), desc.streamARN(), desc.streamStatusAsString()
// desc.shards().size(), desc.retentionPeriodHours()

// Deletar stream (com consumer deletion forcada)
kinesisClient.deleteStream(DeleteStreamRequest.builder()
    .streamName("my-stream")
    .enforceConsumerDeletion(true)
    .build());`} />

        {/* ================================================================ */}
        {/* SHARDS */}
        {/* ================================================================ */}
        <h2 id="shards" className="heading-anchor">
          <a href="#shards" className="heading-anchor__link" aria-hidden="true">#</a>
          Shards
        </h2>
        <p>
          Shards sao a unidade de capacidade de um Kinesis Data Stream. Cada shard
          suporta ate 1 MB/s de escrita e 2 MB/s de leitura. Os registros sao
          distribuidos entre shards com base na <strong>partition key</strong>,
          que e mapeada para um hash key range especifico de cada shard.
        </p>
        <p>
          A POC permite listar os shards de um stream, exibindo o{' '}
          <code>shardId</code>, o range de hash keys (<code>startingHashKey</code> e{' '}
          <code>endingHashKey</code>) e o <code>startingSequenceNumber</code>.
        </p>
        <CodeBlock language="java" title="StreamService.java — listShards" code={`// Listar shards de um stream
ListShardsResponse response = kinesisClient.listShards(
    ListShardsRequest.builder()
        .streamName("my-stream")
        .build());

// Cada shard tem: shardId, hashKeyRange, sequenceNumberRange
response.shards().stream().map(shard -> Map.of(
    "shardId", shard.shardId(),
    "hashKeyRangeStart", shard.hashKeyRange().startingHashKey(),
    "hashKeyRangeEnd", shard.hashKeyRange().endingHashKey(),
    "sequenceNumberRangeStart",
        shard.sequenceNumberRange().startingSequenceNumber()
)).toList();`} />

        <Callout type="tip">
          <p>
            Com 2 shards, o espaco de hash keys (0 a 2^128 - 1) e dividido ao meio.
            Registros com partition keys cujo hash cai na primeira metade vao para o
            shard-000000000000, e os demais para o shard-000000000001.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* PRODUCER */}
        {/* ================================================================ */}
        <h2 id="producer" className="heading-anchor">
          <a href="#producer" className="heading-anchor__link" aria-hidden="true">#</a>
          Producer
        </h2>
        <p>
          O producer envia registros para um Kinesis Data Stream. Cada registro
          consiste em: <code>streamName</code>, <code>partitionKey</code> e{' '}
          <code>data</code> (payload em bytes). A POC suporta envio individual
          (<code>putRecord</code>) e em batch (<code>putRecords</code> — ate 500
          registros por chamada).
        </p>
        <CodeBlock language="java" title="ProducerService.java — putRecord" code={`// Enviar um registro individual
PutRecordResponse response = kinesisClient.putRecord(
    PutRecordRequest.builder()
        .streamName("my-stream")
        .partitionKey("user-123")
        .data(SdkBytes.fromString("payload data", StandardCharsets.UTF_8))
        .build());

// Resposta contem o shard de destino e o sequence number
String shardId = response.shardId();
String sequenceNumber = response.sequenceNumber();`} />

        <CodeBlock language="java" title="ProducerService.java — putRecords (batch)" code={`// Enviar multiplos registros em batch
List<PutRecordsRequestEntry> entries = records.stream()
    .map(record -> PutRecordsRequestEntry.builder()
        .partitionKey(record.partitionKey())
        .data(SdkBytes.fromString(record.data(), StandardCharsets.UTF_8))
        .build())
    .toList();

PutRecordsResponse response = kinesisClient.putRecords(
    PutRecordsRequest.builder()
        .streamName("my-stream")
        .records(entries)
        .build());

// Verificar falhas parciais
int failedCount = response.failedRecordCount();`} />

        <Callout type="warning">
          <p>
            No <code>putRecords</code>, mesmo que a chamada retorne sucesso (HTTP 200),
            registros individuais podem falhar. Sempre verifique o{' '}
            <code>failedRecordCount</code> e o <code>errorCode</code> de cada entrada
            na resposta para implementar retries seletivos.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* CONSUMER */}
        {/* ================================================================ */}
        <h2 id="consumer" className="heading-anchor">
          <a href="#consumer" className="heading-anchor__link" aria-hidden="true">#</a>
          Consumer
        </h2>
        <p>
          O consumer le registros de um shard usando <strong>shard iterators</strong>.
          O fluxo e: (1) obter um shard iterator para um shard especifico,
          (2) chamar <code>getRecords</code> passando o iterator, (3) usar o{' '}
          <code>nextShardIterator</code> da resposta para continuar lendo.
        </p>
        <p>
          A POC suporta tres modos de consumo via endpoint: obter um shard iterator
          manualmente, ler registros com um iterator existente, e um endpoint
          conveniente que consome desde o inicio de um shard (<code>TRIM_HORIZON</code>).
        </p>
        <CodeBlock language="java" title="ConsumerService.java — getShardIterator" code={`// Obter shard iterator (tipos: TRIM_HORIZON, LATEST,
// AT_SEQUENCE_NUMBER, AFTER_SEQUENCE_NUMBER, AT_TIMESTAMP)
ShardIteratorType type = ShardIteratorType.fromValue("TRIM_HORIZON");

GetShardIteratorResponse response = kinesisClient.getShardIterator(
    GetShardIteratorRequest.builder()
        .streamName("my-stream")
        .shardId("shardId-000000000000")
        .shardIteratorType(type)
        .build());

String iterator = response.shardIterator();`} />

        <CodeBlock language="java" title="ConsumerService.java — getRecords" code={`// Ler registros usando o shard iterator
GetRecordsResponse response = kinesisClient.getRecords(
    GetRecordsRequest.builder()
        .shardIterator(iterator)
        .limit(100)
        .build());

// Processar registros
response.records().forEach(record -> {
    String data = record.data().asString(StandardCharsets.UTF_8);
    String partitionKey = record.partitionKey();
    String sequenceNumber = record.sequenceNumber();
});

// Usar nextShardIterator para continuar lendo
String nextIterator = response.nextShardIterator();
long millisBehind = response.millisBehindLatest();`} />

        <Callout type="tip">
          <p>
            <strong>TRIM_HORIZON</strong> le desde o registro mais antigo disponivel no shard.{' '}
            <strong>LATEST</strong> le apenas registros novos a partir deste momento.
            Use <code>millisBehindLatest</code> para monitorar o atraso do consumer
            em relacao ao final do stream.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* PARTITION KEYS */}
        {/* ================================================================ */}
        <h2 id="partition-keys" className="heading-anchor">
          <a href="#partition-keys" className="heading-anchor__link" aria-hidden="true">#</a>
          Partition Keys
        </h2>
        <p>
          A <strong>partition key</strong> e uma string definida pelo producer que
          determina em qual shard o registro sera armazenado. O Kinesis aplica um
          hash MD5 na partition key e mapeia o resultado para o range de hash keys
          de um dos shards do stream.
        </p>
        <p>
          Escolher boas partition keys e fundamental para distribuir a carga
          uniformemente entre shards. Exemplos de boas chaves: user ID, device ID,
          session ID. Exemplos ruins: valores fixos (todos os registros no mesmo shard)
          ou timestamps (distribuicao desigual).
        </p>
        <CodeBlock language="json" title="Exemplo de PutRecordDto" code={`{
  "streamName": "my-stream",
  "partitionKey": "user-456",
  "data": "{\\"event\\":\\"click\\",\\"page\\":\\"/home\\"}"
}`} />

        <CodeBlock language="json" title="Exemplo de BatchPutDto" code={`{
  "streamName": "my-stream",
  "records": [
    { "partitionKey": "user-123", "data": "evento A" },
    { "partitionKey": "user-456", "data": "evento B" },
    { "partitionKey": "user-789", "data": "evento C" }
  ]
}`} />

        <Callout type="warning">
          <p>
            Se todos os registros usarem a mesma partition key, eles serao direcionados
            para o mesmo shard, criando um <strong>hot shard</strong>. Isso desperdicaria
            a capacidade dos demais shards e poderia causar throttling.
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
          Todos usam o prefixo base <code>/api/kinesis</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            [<code>POST</code>, <code>/api/kinesis/streams/{'{streamName}'}?shardCount=1</code>, 'Criar um stream com N shards'],
            [<code>GET</code>, <code>/api/kinesis/streams</code>, 'Listar todos os streams'],
            [<code>GET</code>, <code>/api/kinesis/streams/{'{streamName}'}</code>, 'Descrever um stream (ARN, status, shards, retencao)'],
            [<code>DELETE</code>, <code>/api/kinesis/streams/{'{streamName}'}</code>, 'Deletar um stream'],
            [<code>GET</code>, <code>/api/kinesis/streams/{'{streamName}'}/shards</code>, 'Listar shards de um stream'],
            [<code>POST</code>, <code>/api/kinesis/producer/record</code>, 'Enviar um registro (body: PutRecordDto)'],
            [<code>POST</code>, <code>/api/kinesis/producer/records</code>, 'Enviar registros em batch (body: BatchPutDto)'],
            [<code>GET</code>, <code>/api/kinesis/consumer/iterator/{'{streamName}'}/{'{shardId}'}?iteratorType=TRIM_HORIZON</code>, 'Obter shard iterator'],
            [<code>GET</code>, <code>/api/kinesis/consumer/records?shardIterator=...&limit=100</code>, 'Ler registros via shard iterator'],
            [<code>GET</code>, <code>/api/kinesis/consumer/consume/{'{streamName}'}/{'{shardId}'}</code>, 'Consumir todos os registros desde o inicio do shard'],
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
        <CodeBlock language="bash" code={`cd pocs/poc-kinesis-streaming
./mvnw spring-boot:run

# Roda na porta 8092`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Listar streams
curl "http://localhost:8092/api/kinesis/streams"

# Criar um stream com 2 shards
curl -X POST "http://localhost:8092/api/kinesis/streams/my-stream?shardCount=2"

# Descrever um stream
curl "http://localhost:8092/api/kinesis/streams/my-stream"

# Listar shards
curl "http://localhost:8092/api/kinesis/streams/my-stream/shards"

# Enviar um registro
curl -X POST "http://localhost:8092/api/kinesis/producer/record" \\
  -H "Content-Type: application/json" \\
  -d '{"streamName":"my-stream","partitionKey":"user-123","data":"Hello Kinesis!"}'

# Enviar registros em batch
curl -X POST "http://localhost:8092/api/kinesis/producer/records" \\
  -H "Content-Type: application/json" \\
  -d '{"streamName":"my-stream","records":[{"partitionKey":"k1","data":"msg1"},{"partitionKey":"k2","data":"msg2"}]}'

# Consumir registros desde o inicio de um shard
curl "http://localhost:8092/api/kinesis/consumer/consume/my-stream/shardId-000000000000"

# Obter shard iterator manualmente
curl "http://localhost:8092/api/kinesis/consumer/iterator/my-stream/shardId-000000000000?iteratorType=TRIM_HORIZON"

# Ler registros com shard iterator
curl "http://localhost:8092/api/kinesis/consumer/records?shardIterator=<ITERATOR>&limit=10"`} />

        <Callout type="warning">
          <p>
            Substitua <code>{'<ITERATOR>'}</code> pelo valor retornado pelo endpoint
            de shard iterator. Os iterators expiram apos 5 minutos de inatividade.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/kinesis-streaming" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
