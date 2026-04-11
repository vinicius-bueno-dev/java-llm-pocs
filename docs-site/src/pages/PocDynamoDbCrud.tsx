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
  { id: 'single-table', title: 'Single-Table Design' },
  { id: 'crud', title: 'CRUD de Usuarios' },
  { id: 'optimistic-locking', title: 'Optimistic Locking' },
  { id: 'pagination', title: 'Paginacao com LastEvaluatedKey' },
  { id: 'gsi-lsi', title: 'GSI vs LSI' },
  { id: 'batch-scan', title: 'Batch Write e Scan' },
  { id: 'ttl', title: 'TTL — Time to Live' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'running', title: 'Como Rodar' },
]

export function PocDynamoDbCrud() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>DynamoDB CRUD</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="overview" className="heading-anchor">
          <a href="#overview" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora os principais conceitos do Amazon DynamoDB em uma aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para projetar tabelas, realizar operacoes
          de CRUD, paginar resultados, usar indices secundarios e aplicar tecnicas
          avancadas como optimistic locking e TTL.
        </p>
        <p>
          Os conceitos cobertos incluem: single-table design com chaves compostas (PK + SK),
          Global Secondary Index (GSI), Local Secondary Index (LSI), paginacao opaca via{' '}
          <code>LastEvaluatedKey</code>, optimistic locking com <code>ConditionExpression</code>,
          BatchWriteItem com chunking de 25 itens, Scan com paginacao e TTL automático.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O DynamoDB tem cobertura completa
            no tier gratuito, incluindo GSI, LSI, streams e TTL.
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
          A aplicacao segue o padrao <strong>Repository + Builder</strong>: uma camada
          de repositorio encapsula todas as chamadas ao SDK e os controllers delegam para
          services que orquestram as operacoes. Cada funcionalidade tem seu proprio controller.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.dynamodb/
  config/         DynamoDbConfig (DynamoDbClient bean)
  controller/     UserController, EventController, TtlController
  service/        UserService, EventService, TtlService
  repository/     UserRepository (toda interacao com AWS SDK)
  dto/            Records: CreateUserDto, UpdateUserDto
  codec/          PageTokenCodec (base64 <-> LastEvaluatedKey)`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8082</code> para nao conflitar com as POCs
            S3 (8080) e SQS (8081).
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
          O modulo Terraform em <code>infra/localstack/modules/dynamodb</code> provisiona
          duas tabelas: <code>users</code> (com GSI e LSI) e <code>events</code> (para
          demonstrar batch write e scan).
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_dynamodb_table</code>, 'users', 'Tabela principal com PK+SK, GSI by-email, LSI by-created-at, TTL e streams'],
            [<code>aws_dynamodb_table</code>, 'events', 'Tabela secundaria para demo de BatchWriteItem e Scan'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/dynamodb/main.tf (trecho)" code={`resource "aws_dynamodb_table" "users" {
  name         = "\${var.project_name}-\${var.environment}-users"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"
  range_key    = "sk"

  attribute { name = "pk";        type = "S" }
  attribute { name = "sk";        type = "S" }
  attribute { name = "email";     type = "S" }
  attribute { name = "createdAt"; type = "S" }

  global_secondary_index {
    name            = "by-email"
    hash_key        = "email"
    projection_type = "ALL"
  }

  local_secondary_index {
    name            = "by-created-at"
    range_key       = "createdAt"
    projection_type = "ALL"
  }

  ttl {
    attribute_name = "expiresAt"
    enabled        = true
  }

  stream_enabled   = true
  stream_view_type = "NEW_AND_OLD_IMAGES"
}`} />

        {/* ================================================================ */}
        {/* SINGLE TABLE DESIGN */}
        {/* ================================================================ */}
        <h2 id="single-table" className="heading-anchor">
          <a href="#single-table" className="heading-anchor__link" aria-hidden="true">#</a>
          Single-Table Design
        </h2>
        <p>
          O DynamoDB incentiva armazenar entidades relacionadas na mesma tabela usando
          chaves compostas semanticas. Esta POC usa o padrao <strong>PK = TENANT#id / SK = USER#id</strong>{' '}
          para isolar dados por tenant e permitir queries eficientes.
        </p>
        <Table
          headers={['Atributo', 'Valor Exemplo', 'Proposito']}
          rows={[
            [<code>pk</code>, 'TENANT#acme', 'Partition key — agrupa todos os usuarios do tenant'],
            [<code>sk</code>, 'USER#u-123', 'Sort key — identifica o usuario dentro do tenant'],
            [<code>email</code>, 'alice@acme.com', 'Atributo indexado no GSI by-email'],
            [<code>createdAt</code>, '2025-01-15T10:00:00Z', 'Atributo indexado no LSI by-created-at'],
            [<code>version</code>, '1', 'Contador para optimistic locking'],
            [<code>expiresAt</code>, '1750000000', 'Epoch Unix para TTL automatico'],
          ]}
        />

        <Callout type="tip">
          <p>
            Com single-table design, uma unica query na chave composta{' '}
            <code>pk = TENANT#acme</code> retorna todos os usuarios do tenant,
            sem necessidade de scan ou joins.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* CRUD */}
        {/* ================================================================ */}
        <h2 id="crud" className="heading-anchor">
          <a href="#crud" className="heading-anchor__link" aria-hidden="true">#</a>
          CRUD de Usuarios
        </h2>
        <p>
          As operacoes basicas usam <code>PutItem</code>, <code>GetItem</code>,{' '}
          <code>UpdateItem</code> e <code>DeleteItem</code> do SDK v2. O builder
          pattern do SDK torna as operacoes explicitas e type-safe.
        </p>
        <CodeBlock language="java" title="UserRepository.java" code={`// Criar usuario (PutItem)
dynamoDbClient.putItem(PutItemRequest.builder()
    .tableName(tableName)
    .item(Map.of(
        "pk",    AttributeValue.fromS("TENANT#" + dto.tenantId()),
        "sk",    AttributeValue.fromS("USER#"   + dto.userId()),
        "email", AttributeValue.fromS(dto.email()),
        "name",  AttributeValue.fromS(dto.name()),
        "version", AttributeValue.fromN("1")
    ))
    .build());

// Buscar usuario (GetItem)
GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
    .tableName(tableName)
    .key(Map.of(
        "pk", AttributeValue.fromS("TENANT#" + tenantId),
        "sk", AttributeValue.fromS("USER#"   + userId)
    ))
    .build());

// Deletar usuario (DeleteItem)
dynamoDbClient.deleteItem(DeleteItemRequest.builder()
    .tableName(tableName)
    .key(Map.of(
        "pk", AttributeValue.fromS("TENANT#" + tenantId),
        "sk", AttributeValue.fromS("USER#"   + userId)
    ))
    .build());`} />

        {/* ================================================================ */}
        {/* OPTIMISTIC LOCKING */}
        {/* ================================================================ */}
        <h2 id="optimistic-locking" className="heading-anchor">
          <a href="#optimistic-locking" className="heading-anchor__link" aria-hidden="true">#</a>
          Optimistic Locking
        </h2>
        <p>
          O DynamoDB nao tem transacoes de update com lock pessimista. O padrao recomendado
          e <strong>optimistic locking</strong>: ao atualizar, verificar se a versao atual
          bate com a esperada usando <code>ConditionExpression</code>. Se outra escrita
          ocorreu entre o read e o write, a operacao falha com{' '}
          <code>ConditionalCheckFailedException</code>.
        </p>
        <CodeBlock language="java" title="UserRepository.java — update com versao" code={`dynamoDbClient.updateItem(UpdateItemRequest.builder()
    .tableName(tableName)
    .key(Map.of(
        "pk", AttributeValue.fromS("TENANT#" + tenantId),
        "sk", AttributeValue.fromS("USER#"   + userId)
    ))
    .updateExpression("SET #name = :name, version = :newVersion")
    .conditionExpression("version = :expectedVersion")
    .expressionAttributeNames(Map.of("#name", "name"))
    .expressionAttributeValues(Map.of(
        ":name",            AttributeValue.fromS(dto.name()),
        ":newVersion",      AttributeValue.fromN(String.valueOf(dto.expectedVersion() + 1)),
        ":expectedVersion", AttributeValue.fromN(String.valueOf(dto.expectedVersion()))
    ))
    .build());`} />

        <Callout type="warning">
          <p>
            <strong>name</strong> e uma palavra reservada do DynamoDB. Sempre use
            expression attribute names (ex: <code>#name</code>) para campos com nomes
            reservados, ou a requisicao retornara erro de validacao.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* PAGINACAO */}
        {/* ================================================================ */}
        <h2 id="pagination" className="heading-anchor">
          <a href="#pagination" className="heading-anchor__link" aria-hidden="true">#</a>
          Paginacao com LastEvaluatedKey
        </h2>
        <p>
          O DynamoDB nao tem paginacao por numero de pagina. Em vez disso, cada resposta
          de Query/Scan inclui um <code>LastEvaluatedKey</code> — um mapa de atributos
          que representa o ultimo item retornado. Para buscar a proxima pagina, passe
          esse mapa como <code>ExclusiveStartKey</code>.
        </p>
        <p>
          A POC encapsula esse mapa em um token opaco usando <code>PageTokenCodec</code>:
          serializa o mapa para JSON e codifica em base64-URL-safe, tornando o token
          seguro para uso em query strings.
        </p>
        <CodeBlock language="java" title="PageTokenCodec.java" code={`// Encode: Map<String, AttributeValue> -> String base64
public String encode(Map<String, AttributeValue> lastEvaluatedKey) {
    String json = toJson(lastEvaluatedKey);  // serializacao customizada
    return Base64.getUrlEncoder().withoutPadding()
                 .encodeToString(json.getBytes(StandardCharsets.UTF_8));
}

// Decode: String base64 -> Map<String, AttributeValue>
public Map<String, AttributeValue> decode(String token) {
    byte[] bytes = Base64.getUrlDecoder().decode(token);
    return fromJson(new String(bytes, StandardCharsets.UTF_8));
}

// Uso na query
QueryRequest request = QueryRequest.builder()
    .tableName(tableName)
    .keyConditionExpression("pk = :pk")
    .expressionAttributeValues(Map.of(":pk", AttributeValue.fromS("TENANT#" + tenantId)))
    .limit(pageSize)
    .exclusiveStartKey(pageToken != null ? codec.decode(pageToken) : null)
    .build();

QueryResponse response = dynamoDbClient.query(request);

// Retornar proximo token se houver mais resultados
String nextToken = response.hasLastEvaluatedKey()
    ? codec.encode(response.lastEvaluatedKey())
    : null;`} />

        <Callout type="info">
          <p>
            O token e opaco para o cliente — ele nao deve interpretar nem construir tokens
            manualmente. Basta repassar o valor recebido na resposta anterior.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* GSI vs LSI */}
        {/* ================================================================ */}
        <h2 id="gsi-lsi" className="heading-anchor">
          <a href="#gsi-lsi" className="heading-anchor__link" aria-hidden="true">#</a>
          GSI vs LSI
        </h2>
        <p>
          O DynamoDB oferece dois tipos de indices secundarios com tradeoffs distintos:
        </p>
        <Table
          headers={['Caracteristica', 'GSI (Global Secondary Index)', 'LSI (Local Secondary Index)']}
          rows={[
            ['Partition key', 'Qualquer atributo (diferente da tabela)', 'Mesma PK da tabela'],
            ['Sort key', 'Qualquer atributo (opcional)', 'Atributo diferente da tabela'],
            ['Escopo da query', 'Toda a tabela', 'Dentro de uma partition key'],
            ['Consistencia', 'Eventually consistent apenas', 'Strongly consistent disponivel'],
            ['Quando criar', 'Qualquer momento', 'Somente na criacao da tabela'],
            ['Caso de uso', 'Query por email (by-email GSI)', 'Ordenar por data dentro do tenant (by-created-at LSI)'],
          ]}
        />

        <CodeBlock language="java" title="Queries por GSI e LSI" code={`// Query por GSI (by-email) — busca em toda a tabela pelo email
QueryRequest gsiQuery = QueryRequest.builder()
    .tableName(tableName)
    .indexName("by-email")
    .keyConditionExpression("email = :email")
    .expressionAttributeValues(Map.of(":email", AttributeValue.fromS(email)))
    .limit(pageSize)
    .build();

// Query por LSI (by-created-at) — ordena dentro de um tenant
QueryRequest lsiQuery = QueryRequest.builder()
    .tableName(tableName)
    .indexName("by-created-at")
    .keyConditionExpression("pk = :pk")
    .expressionAttributeValues(Map.of(":pk", AttributeValue.fromS("TENANT#" + tenantId)))
    .scanIndexForward(ascending)  // true = crescente, false = decrescente
    .limit(pageSize)
    .build();`} />

        {/* ================================================================ */}
        {/* BATCH WRITE E SCAN */}
        {/* ================================================================ */}
        <h2 id="batch-scan" className="heading-anchor">
          <a href="#batch-scan" className="heading-anchor__link" aria-hidden="true">#</a>
          Batch Write e Scan
        </h2>
        <p>
          <code>BatchWriteItem</code> permite escrever ate <strong>25 itens</strong> em
          uma unica chamada. Para listas maiores, a POC faz o chunking automaticamente.
          O <code>Scan</code> percorre todos os itens da tabela — deve ser evitado em
          producao em tabelas grandes, mas e util para operacoes de manutencao.
        </p>
        <CodeBlock language="java" title="EventService.java — BatchWriteItem com chunking" code={`public void batchWrite(List<EventDto> events) {
    // Dividir em chunks de 25 (limite do DynamoDB)
    List<List<EventDto>> chunks = partition(events, 25);

    for (List<EventDto> chunk : chunks) {
        List<WriteRequest> writes = chunk.stream()
            .map(e -> WriteRequest.builder()
                .putRequest(PutRequest.builder()
                    .item(Map.of(
                        "pk",      AttributeValue.fromS("EVENT#" + e.eventId()),
                        "sk",      AttributeValue.fromS("META"),
                        "type",    AttributeValue.fromS(e.type()),
                        "payload", AttributeValue.fromS(e.payload())
                    ))
                    .build())
                .build())
            .toList();

        dynamoDbClient.batchWriteItem(BatchWriteItemRequest.builder()
            .requestItems(Map.of(eventsTable, writes))
            .build());
    }
}

// Scan com paginacao
ScanResponse response = dynamoDbClient.scan(ScanRequest.builder()
    .tableName(eventsTable)
    .limit(pageSize)
    .exclusiveStartKey(startKey)
    .build());`} />

        <Callout type="warning">
          <p>
            <strong>BatchWriteItem nao garante ordem</strong> e pode retornar itens
            nao processados em <code>UnprocessedItems</code>. Sempre verifique e
            reenvie os itens pendentes com backoff exponencial.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* TTL */}
        {/* ================================================================ */}
        <h2 id="ttl" className="heading-anchor">
          <a href="#ttl" className="heading-anchor__link" aria-hidden="true">#</a>
          TTL — Time to Live
        </h2>
        <p>
          O TTL permite definir uma data de expiracao por item. O DynamoDB deleta
          automaticamente os itens expirados sem custo adicional de write. O atributo TTL
          deve conter um timestamp Unix em <strong>segundos</strong> (epoch).
        </p>
        <CodeBlock language="java" title="TtlService.java" code={`// Definir expiracao: adicionar N segundos a partir de agora
long expiresAt = Instant.now().plusSeconds(seconds).getEpochSecond();

dynamoDbClient.updateItem(UpdateItemRequest.builder()
    .tableName(usersTable)
    .key(Map.of(
        "pk", AttributeValue.fromS("TENANT#" + tenantId),
        "sk", AttributeValue.fromS("USER#"   + userId)
    ))
    .updateExpression("SET expiresAt = :exp")
    .expressionAttributeValues(Map.of(
        ":exp", AttributeValue.fromN(String.valueOf(expiresAt))
    ))
    .build());

// Verificar se TTL esta ativo na tabela
DescribeTimeToLiveResponse ttlInfo = dynamoDbClient.describeTimeToLive(
    DescribeTimeToLiveRequest.builder().tableName(usersTable).build());
// ttlInfo.timeToLiveDescription().timeToLiveStatus() -> ENABLED`} />

        <Callout type="info">
          <p>
            No LocalStack, a delecao por TTL pode nao ocorrer automaticamente como em
            producao. O item expirado ainda e visivel via GetItem ate ser deletado pelo
            worker interno. Em producao, a janela de delecao e de ate 48 horas apos a expiracao.
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
          A tabela abaixo lista todos os endpoints expostos pela POC na porta{' '}
          <code>8082</code>.
        </p>
        <Table
          headers={['Metodo', 'Path', 'Descricao']}
          rows={[
            ['POST', <code>/api/dynamodb/users</code>, 'Criar usuario (CreateUserDto)'],
            ['GET', <code>/api/dynamodb/users/{'{tenantId}/{userId}'}</code>, 'Buscar usuario por PK+SK'],
            ['PUT', <code>/api/dynamodb/users/{'{tenantId}/{userId}'}</code>, 'Atualizar nome com optimistic locking'],
            ['DELETE', <code>/api/dynamodb/users/{'{tenantId}/{userId}'}</code>, 'Deletar usuario'],
            ['GET', <code>/api/dynamodb/users/query/by-tenant</code>, 'Query na tabela principal por tenantId'],
            ['GET', <code>/api/dynamodb/users/query/by-email</code>, 'Query no GSI by-email'],
            ['GET', <code>/api/dynamodb/users/query/by-created-at</code>, 'Query no LSI by-created-at'],
            ['POST', <code>/api/dynamodb/events/batch</code>, 'BatchWriteItem de eventos'],
            ['GET', <code>/api/dynamodb/events/scan</code>, 'Scan da tabela events com paginacao'],
            ['GET', <code>/api/dynamodb/ttl/describe</code>, 'Consultar status do TTL'],
            ['POST', <code>/api/dynamodb/ttl/{'{tenantId}/{userId}'}</code>, 'Definir expiracao (query param: seconds)'],
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

# Ver nomes das tabelas criadas
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-dynamodb-crud
./mvnw spring-boot:run

# Roda na porta 8082`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Criar usuario
curl -X POST "http://localhost:8082/api/dynamodb/users" \\
  -H "Content-Type: application/json" \\
  -d '{"tenantId":"acme","userId":"u-1","email":"alice@acme.com","name":"Alice"}'

# Buscar usuario
curl "http://localhost:8082/api/dynamodb/users/acme/u-1"

# Atualizar com optimistic locking (expectedVersion = 1)
curl -X PUT "http://localhost:8082/api/dynamodb/users/acme/u-1" \\
  -H "Content-Type: application/json" \\
  -d '{"name":"Alice Updated","expectedVersion":1}'

# Query por tenant (com paginacao)
curl "http://localhost:8082/api/dynamodb/users/query/by-tenant?tenantId=acme&limit=10"

# Query por email via GSI
curl "http://localhost:8082/api/dynamodb/users/query/by-email?email=alice@acme.com"

# Batch write de eventos
curl -X POST "http://localhost:8082/api/dynamodb/events/batch" \\
  -H "Content-Type: application/json" \\
  -d '[{"eventId":"e-1","type":"LOGIN","payload":"{}"},{"eventId":"e-2","type":"LOGOUT","payload":"{}"}]'

# Definir TTL de 3600 segundos (1 hora)
curl -X POST "http://localhost:8082/api/dynamodb/ttl/acme/u-1?seconds=3600"

# Consultar status do TTL
curl "http://localhost:8082/api/dynamodb/ttl/describe"`} />

        <Callout type="tip">
          <p>
            Para testar a paginacao, inclua o parametro <code>pageToken</code> com o valor
            retornado no campo <code>nextPageToken</code> da resposta anterior.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/dynamodb-crud" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
