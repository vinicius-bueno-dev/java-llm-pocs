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
  { id: 'crud', title: 'CRUD de Objetos' },
  { id: 'versioning', title: 'Versionamento' },
  { id: 'presigned', title: 'Presigned URLs' },
  { id: 'multipart', title: 'Multipart Upload' },
  { id: 'encryption', title: 'Encryption (SSE-S3)' },
  { id: 'policies', title: 'Policies e ACLs' },
  { id: 'tags', title: 'Tags' },
  { id: 'lifecycle', title: 'Lifecycle Rules' },
  { id: 'cors', title: 'CORS' },
  { id: 'notifications', title: 'Event Notifications' },
  { id: 'logging', title: 'Access Logging' },
  { id: 'website', title: 'Website Hosting' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'running', title: 'Como Rodar' },
]

export function PocS3Storage() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>S3 Storage</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="overview" className="heading-anchor">
          <a href="#overview" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora 13 areas de funcionalidade do Amazon S3 em uma unica aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar, de forma pratica,
          como usar o <strong>AWS SDK for Java v2</strong> para interagir com buckets e objetos.
        </p>
        <p>
          As funcionalidades cobertas incluem operacoes CRUD basicas, versionamento, presigned URLs,
          multipart upload, encryption, policies, tags, lifecycle rules, CORS, event notifications,
          access logging e website hosting. Toda a infraestrutura e provisionada com Terraform
          apontando para o LocalStack.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. Nao precisa de conta AWS
            e nao gera custos.
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
          A aplicacao segue a estrutura padrao de uma API Spring Boot com camadas
          de <strong>Controller</strong> e <strong>Service</strong>. Cada area funcional
          tem seu proprio par controller/service, mantendo o codigo organizado e focado.
        </p>
        <p>
          O <code>S3Client</code> e o <code>S3Presigner</code> sao configurados em uma
          classe <code>S3Config</code> que aponta para o endpoint do LocalStack.
          Os controllers expoe endpoints REST para cada operacao, facilitando testes via curl ou Postman.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.s3/
  config/         S3Config (S3Client, S3Presigner, SqsClient)
  controller/     13 controllers (um por area funcional)
  service/        13 services (logica de negocio com AWS SDK)
  dto/            Records para CORS rules, lifecycle rules, etc.`} />

        <Callout type="tip">
          <p>
            A injecao de dependencia e feita por construtor (sem <code>@Autowired</code> em campos),
            seguindo as boas praticas do Spring Boot.
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
          O modulo Terraform em <code>infra/localstack/modules/s3</code> provisiona todos os
          recursos necessarios: 3 buckets (principal, logs, website), alem de configuracoes
          de versionamento, CORS, encryption, lifecycle, logging, policy e event notifications.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_s3_bucket</code>, 'poc-storage', 'Bucket principal para todas as operacoes'],
            [<code>aws_s3_bucket</code>, 'poc-logs', 'Target para access logging'],
            [<code>aws_s3_bucket</code>, 'poc-website', 'Static website hosting'],
            [<code>aws_s3_bucket_versioning</code>, '--', 'Versionamento habilitado no bucket principal'],
            [<code>aws_s3_bucket_cors_configuration</code>, '--', 'CORS para localhost:3000 e :8080'],
            [<code>aws_s3_bucket_server_side_encryption</code>, '--', 'SSE-S3 (AES256) por padrao'],
            [<code>aws_s3_bucket_lifecycle_configuration</code>, '--', '3 rules: temp expire, archive transition, multipart cleanup'],
            [<code>aws_s3_bucket_policy</code>, '--', 'Leitura publica em public/*'],
            [<code>aws_s3_bucket_logging</code>, '--', 'Logs do bucket principal para poc-logs'],
            [<code>aws_s3_bucket_website_configuration</code>, '--', 'index.html e error.html'],
            [<code>aws_s3_bucket_public_access_block</code>, '--', 'Bloqueia acesso publico por padrao'],
            [<code>aws_sqs_queue</code>, 's3-events', 'Fila para eventos ObjectCreated'],
            [<code>aws_sns_topic</code>, 's3-events', 'Topico para eventos ObjectRemoved'],
            [<code>aws_s3_bucket_notification</code>, '--', 'Liga eventos S3 ao SQS e SNS'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/s3/main.tf (trecho)" code={`resource "aws_s3_bucket" "poc_bucket" {
  bucket = "\${var.project_name}-\${var.environment}-poc-storage"
  tags = {
    Name        = "\${var.project_name}-poc-storage"
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

resource "aws_s3_bucket_versioning" "poc_bucket_versioning" {
  bucket = aws_s3_bucket.poc_bucket.id
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "poc_encryption" {
  bucket = aws_s3_bucket.poc_bucket.id
  rule {
    apply_server_side_encryption_by_default { sse_algorithm = "AES256" }
    bucket_key_enabled = true
  }
}`} />

        {/* ================================================================ */}
        {/* CRUD */}
        {/* ================================================================ */}
        <h2 id="crud" className="heading-anchor">
          <a href="#crud" className="heading-anchor__link" aria-hidden="true">#</a>
          CRUD de Objetos
        </h2>
        <p>
          As operacoes basicas de CRUD usam o <code>S3Client</code> do AWS SDK v2.
          O service <code>ObjectCrudService</code> encapsula PUT, GET, HEAD, DELETE,
          batch delete, COPY e LIST. Cada operacao corresponde a um endpoint REST.
        </p>
        <p>
          O upload envia bytes com <code>RequestBody.fromBytes()</code> e retorna o ETag.
          O download usa <code>getObjectAsBytes()</code> para simplificar o consumo.
          O copy permite duplicar objetos entre buckets ou dentro do mesmo bucket.
        </p>
        <CodeBlock language="java" title="ObjectCrudService.java" code={`// Upload
s3Client.putObject(
    PutObjectRequest.builder()
        .bucket(bucket).key(key)
        .contentType(contentType).build(),
    RequestBody.fromBytes(content));

// Download
byte[] data = s3Client.getObjectAsBytes(
    GetObjectRequest.builder()
        .bucket(bucket).key(key).build())
    .asByteArray();

// Delete
s3Client.deleteObject(DeleteObjectRequest.builder()
    .bucket(bucket).key(key).build());

// Copy
s3Client.copyObject(CopyObjectRequest.builder()
    .sourceBucket(srcBucket).sourceKey(srcKey)
    .destinationBucket(destBucket).destinationKey(destKey)
    .build());

// List
s3Client.listObjectsV2(ListObjectsV2Request.builder()
    .bucket(bucket).prefix(prefix).maxKeys(100).build())
    .contents().stream().map(S3Object::key).toList();`} />

        {/* ================================================================ */}
        {/* VERSIONING */}
        {/* ================================================================ */}
        <h2 id="versioning" className="heading-anchor">
          <a href="#versioning" className="heading-anchor__link" aria-hidden="true">#</a>
          Versionamento
        </h2>
        <p>
          Com versionamento habilitado, cada PUT cria uma nova versao do objeto.
          E possivel listar todas as versoes, baixar uma versao especifica pelo
          <code>versionId</code>, e deletar versoes individualmente.
        </p>
        <p>
          O Terraform ja habilita o versionamento no bucket principal. A API permite
          tambem habilitar/suspender via SDK e consultar o status atual.
        </p>
        <CodeBlock language="java" title="VersioningService.java" code={`// Habilitar versionamento
s3Client.putBucketVersioning(PutBucketVersioningRequest.builder()
    .bucket(bucket)
    .versioningConfiguration(VersioningConfiguration.builder()
        .status(BucketVersioningStatus.ENABLED).build())
    .build());

// Upload retorna versionId
PutObjectResponse response = s3Client.putObject(req, body);
String versionId = response.versionId();

// Baixar versao especifica
s3Client.getObjectAsBytes(GetObjectRequest.builder()
    .bucket(bucket).key(key)
    .versionId(versionId).build());

// Listar versoes
s3Client.listObjectVersions(ListObjectVersionsRequest.builder()
    .bucket(bucket).prefix(prefix).build());`} />

        {/* ================================================================ */}
        {/* PRESIGNED URLs */}
        {/* ================================================================ */}
        <h2 id="presigned" className="heading-anchor">
          <a href="#presigned" className="heading-anchor__link" aria-hidden="true">#</a>
          Presigned URLs
        </h2>
        <p>
          Presigned URLs permitem gerar links temporarios para download (GET) ou upload (PUT)
          de objetos sem expor credenciais. O link inclui uma assinatura com tempo de expiracao
          configuravel.
        </p>
        <p>
          Isso e util para permitir que frontends ou clientes externos facam upload direto
          ao S3, sem que o trafego passe pelo backend. O <code>S3Presigner</code> e
          um componente separado do <code>S3Client</code>.
        </p>
        <CodeBlock language="java" title="PresignedUrlService.java" code={`// Presigned GET (download temporario)
String getUrl = s3Presigner.presignGetObject(
    GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(15))
        .getObjectRequest(GetObjectRequest.builder()
            .bucket(bucket).key(key).build())
        .build())
    .url().toString();

// Presigned PUT (upload temporario)
String putUrl = s3Presigner.presignPutObject(
    PutObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(15))
        .putObjectRequest(PutObjectRequest.builder()
            .bucket(bucket).key(key)
            .contentType("application/octet-stream").build())
        .build())
    .url().toString();`} />

        <Callout type="warning">
          <p>
            No LocalStack, as presigned URLs usam <code>localhost:4566</code>.
            Em producao, elas apontam para o endpoint real do S3.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* MULTIPART */}
        {/* ================================================================ */}
        <h2 id="multipart" className="heading-anchor">
          <a href="#multipart" className="heading-anchor__link" aria-hidden="true">#</a>
          Multipart Upload
        </h2>
        <p>
          Para arquivos grandes (acima de 5 MB recomendado), o multipart upload divide
          o arquivo em partes que sao enviadas independentemente. Ao final, um
          <code>completeMultipartUpload</code> junta todas as partes no S3.
        </p>
        <p>
          O fluxo tem 3 etapas: iniciar o upload (recebe um <code>uploadId</code>),
          enviar cada parte com seu <code>partNumber</code>, e completar
          passando a lista de ETags. Uploads incompletos podem ser abortados.
        </p>
        <CodeBlock language="java" title="MultipartUploadService.java" code={`// 1. Iniciar
String uploadId = s3Client.createMultipartUpload(
    CreateMultipartUploadRequest.builder()
        .bucket(bucket).key(key)
        .contentType(contentType).build())
    .uploadId();

// 2. Enviar parte
String eTag = s3Client.uploadPart(
    UploadPartRequest.builder()
        .bucket(bucket).key(key)
        .uploadId(uploadId).partNumber(1).build(),
    RequestBody.fromBytes(data))
    .eTag();

// 3. Completar
s3Client.completeMultipartUpload(
    CompleteMultipartUploadRequest.builder()
        .bucket(bucket).key(key).uploadId(uploadId)
        .multipartUpload(CompletedMultipartUpload.builder()
            .parts(CompletedPart.builder()
                .partNumber(1).eTag(eTag).build())
            .build())
        .build());`} />

        {/* ================================================================ */}
        {/* ENCRYPTION */}
        {/* ================================================================ */}
        <h2 id="encryption" className="heading-anchor">
          <a href="#encryption" className="heading-anchor__link" aria-hidden="true">#</a>
          Encryption (SSE-S3)
        </h2>
        <p>
          Server-Side Encryption com chaves gerenciadas pelo S3 (SSE-S3, algoritmo AES256)
          e a forma mais simples de criptografar objetos em repouso. Pode ser configurada
          como padrao no bucket ou por objeto individual.
        </p>
        <p>
          O Terraform ja configura encryption por padrao com <code>bucket_key_enabled = true</code>.
          Via SDK, e possivel consultar a configuracao atual, alterar o algoritmo e verificar
          se um objeto especifico esta criptografado.
        </p>
        <CodeBlock language="java" title="EncryptionService.java" code={`// Configurar encryption padrao no bucket
s3Client.putBucketEncryption(PutBucketEncryptionRequest.builder()
    .bucket(bucket)
    .serverSideEncryptionConfiguration(
        ServerSideEncryptionConfiguration.builder()
            .rules(ServerSideEncryptionRule.builder()
                .applyServerSideEncryptionByDefault(
                    ServerSideEncryptionByDefault.builder()
                        .sseAlgorithm(ServerSideEncryption.AES256).build())
                .bucketKeyEnabled(true).build())
            .build())
    .build());

// Upload com encryption explicita
s3Client.putObject(PutObjectRequest.builder()
    .bucket(bucket).key(key)
    .serverSideEncryption(ServerSideEncryption.AES256).build(),
    RequestBody.fromBytes(content));`} />

        {/* ================================================================ */}
        {/* POLICIES */}
        {/* ================================================================ */}
        <h2 id="policies" className="heading-anchor">
          <a href="#policies" className="heading-anchor__link" aria-hidden="true">#</a>
          Policies e ACLs
        </h2>
        <p>
          Bucket policies sao documentos JSON que definem quem pode fazer o que no bucket.
          A POC demonstra como aplicar, consultar e deletar policies via SDK.
          O Terraform ja cria uma policy permitindo leitura publica em <code>public/*</code>.
        </p>
        <p>
          O Public Access Block controla se o bucket aceita ACLs ou policies publicas.
          Canned ACLs como <code>public-read</code> oferecem controle rapido
          de permissoes. Object ACLs permitem controle por objeto individual.
        </p>
        <CodeBlock language="java" title="PolicyAndAclService.java" code={`// Aplicar bucket policy (JSON)
s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
    .bucket(bucket).policy(policyJson).build());

// Configurar Public Access Block
s3Client.putPublicAccessBlock(PutPublicAccessBlockRequest.builder()
    .bucket(bucket)
    .publicAccessBlockConfiguration(PublicAccessBlockConfiguration.builder()
        .blockPublicAcls(true).blockPublicPolicy(true)
        .ignorePublicAcls(true).restrictPublicBuckets(true)
        .build())
    .build());

// Aplicar canned ACL em objeto
s3Client.putObjectAcl(PutObjectAclRequest.builder()
    .bucket(bucket).key(key)
    .acl(ObjectCannedACL.PUBLIC_READ).build());`} />

        {/* ================================================================ */}
        {/* TAGS */}
        {/* ================================================================ */}
        <h2 id="tags" className="heading-anchor">
          <a href="#tags" className="heading-anchor__link" aria-hidden="true">#</a>
          Tags
        </h2>
        <p>
          Tags sao pares chave-valor que podem ser aplicados a buckets e objetos.
          Sao uteis para organizacao, controle de custos e automacao
          (ex: lifecycle rules podem filtrar por tag).
        </p>
        <p>
          A API suporta operacoes de get, put e delete para tagging tanto no nivel
          de bucket quanto de objeto. Tags de bucket sao independentes das tags de objeto.
        </p>
        <CodeBlock language="java" title="TaggingService.java" code={`// Tags no bucket
s3Client.putBucketTagging(PutBucketTaggingRequest.builder()
    .bucket(bucket)
    .tagging(Tagging.builder()
        .tagSet(Tag.builder().key("env").value("dev").build(),
                Tag.builder().key("team").value("backend").build())
        .build())
    .build());

// Tags em objeto
s3Client.putObjectTagging(PutObjectTaggingRequest.builder()
    .bucket(bucket).key(key)
    .tagging(Tagging.builder()
        .tagSet(Tag.builder().key("category").value("report").build())
        .build())
    .build());`} />

        {/* ================================================================ */}
        {/* LIFECYCLE */}
        {/* ================================================================ */}
        <h2 id="lifecycle" className="heading-anchor">
          <a href="#lifecycle" className="heading-anchor__link" aria-hidden="true">#</a>
          Lifecycle Rules
        </h2>
        <p>
          Lifecycle rules automatizam a transicao e expiracao de objetos.
          Por exemplo: mover arquivos para Glacier apos 30 dias, ou deletar arquivos
          temporarios apos 7 dias. O Terraform ja configura 3 regras no bucket principal.
        </p>
        <p>
          Via SDK, e possivel consultar, configurar e deletar regras. Cada regra pode
          filtrar por prefixo ou tag, e combinar transicao com expiracao.
        </p>
        <CodeBlock language="java" title="LifecycleService.java" code={`// Configurar lifecycle rule
s3Client.putBucketLifecycleConfiguration(
    PutBucketLifecycleConfigurationRequest.builder()
        .bucket(bucket)
        .lifecycleConfiguration(BucketLifecycleConfiguration.builder()
            .rules(LifecycleRule.builder()
                .id("expire-temp")
                .status(ExpirationStatus.ENABLED)
                .filter(LifecycleRuleFilter.builder()
                    .prefix("temp/").build())
                .expiration(LifecycleExpiration.builder()
                    .days(7).build())
                .build())
            .build())
        .build());`} />

        <Callout type="tip">
          <p>
            O Terraform tambem configura uma regra para abortar multipart uploads
            incompletos apos 7 dias. Isso evita custos com uploads abandonados.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* CORS */}
        {/* ================================================================ */}
        <h2 id="cors" className="heading-anchor">
          <a href="#cors" className="heading-anchor__link" aria-hidden="true">#</a>
          CORS
        </h2>
        <p>
          Cross-Origin Resource Sharing (CORS) permite que browsers acessem o bucket
          a partir de origens diferentes. Isso e essencial quando o frontend
          faz upload direto via presigned URL ou acessa objetos publicos.
        </p>
        <p>
          A configuracao define quais origens, metodos e headers sao permitidos.
          O Terraform ja configura CORS para <code>localhost:3000</code> e <code>localhost:8080</code>.
        </p>
        <CodeBlock language="java" title="CorsService.java" code={`// Configurar CORS rules
s3Client.putBucketCors(PutBucketCorsRequest.builder()
    .bucket(bucket)
    .corsConfiguration(CORSConfiguration.builder()
        .corsRules(CORSRule.builder()
            .allowedHeaders("*")
            .allowedMethods("GET", "PUT", "POST", "DELETE")
            .allowedOrigins("http://localhost:3000")
            .exposeHeaders("ETag", "x-amz-version-id")
            .maxAgeSeconds(3600)
            .build())
        .build())
    .build());`} />

        {/* ================================================================ */}
        {/* NOTIFICATIONS */}
        {/* ================================================================ */}
        <h2 id="notifications" className="heading-anchor">
          <a href="#notifications" className="heading-anchor__link" aria-hidden="true">#</a>
          Event Notifications
        </h2>
        <p>
          O S3 pode notificar outros servicos quando eventos acontecem (criacao, exclusao,
          etc.). Esta POC configura notificacoes para SQS (ObjectCreated) e SNS
          (ObjectRemoved), ambos filtrados pelo prefixo <code>uploads/</code>.
        </p>
        <p>
          Via SDK, e possivel configurar, consultar e remover notificacoes.
          A POC tambem inclui um endpoint para consumir mensagens da fila SQS
          e verificar se os eventos estao chegando.
        </p>
        <CodeBlock language="java" title="NotificationService.java" code={`// Configurar notificacao para SQS
QueueConfiguration queueConfig = QueueConfiguration.builder()
    .queueArn(queueArn)
    .eventsWithStrings("s3:ObjectCreated:*")
    .filter(NotificationConfigurationFilter.builder()
        .key(S3KeyFilter.builder()
            .filterRules(FilterRule.builder()
                .name(FilterRuleName.PREFIX)
                .value("uploads/").build())
            .build())
        .build())
    .build();

s3Client.putBucketNotificationConfiguration(
    PutBucketNotificationConfigurationRequest.builder()
        .bucket(bucket)
        .notificationConfiguration(NotificationConfiguration.builder()
            .queueConfigurations(queueConfig).build())
        .build());`} />

        {/* ================================================================ */}
        {/* LOGGING */}
        {/* ================================================================ */}
        <h2 id="logging" className="heading-anchor">
          <a href="#logging" className="heading-anchor__link" aria-hidden="true">#</a>
          Access Logging
        </h2>
        <p>
          Access logging registra todas as requisicoes feitas ao bucket em arquivos de log
          armazenados em outro bucket. O Terraform configura o bucket principal para enviar
          logs ao bucket <code>poc-logs</code> com prefixo <code>access-logs/</code>.
        </p>
        <p>
          Via SDK, e possivel habilitar, consultar e desabilitar o logging.
          Os logs incluem informacoes como IP de origem, operacao, status HTTP e tempo de resposta.
        </p>
        <CodeBlock language="java" title="LoggingService.java" code={`// Habilitar access logging
s3Client.putBucketLogging(PutBucketLoggingRequest.builder()
    .bucket(bucket)
    .bucketLoggingStatus(BucketLoggingStatus.builder()
        .loggingEnabled(LoggingEnabled.builder()
            .targetBucket(logsBucket)
            .targetPrefix("access-logs/")
            .build())
        .build())
    .build());

// Consultar configuracao
s3Client.getBucketLogging(GetBucketLoggingRequest.builder()
    .bucket(bucket).build());`} />

        {/* ================================================================ */}
        {/* WEBSITE */}
        {/* ================================================================ */}
        <h2 id="website" className="heading-anchor">
          <a href="#website" className="heading-anchor__link" aria-hidden="true">#</a>
          Website Hosting
        </h2>
        <p>
          O S3 pode servir sites estaticos diretamente de um bucket. A configuracao
          define qual arquivo e o <code>index</code> e qual e a pagina de erro.
          O Terraform cria um bucket dedicado (<code>poc-website</code>) para isso.
        </p>
        <p>
          A POC demonstra como configurar, consultar e remover a configuracao de website,
          alem de fazer upload de arquivos HTML para testar o hosting.
        </p>
        <CodeBlock language="java" title="WebsiteHostingService.java" code={`// Configurar website hosting
s3Client.putBucketWebsiteConfiguration(
    PutBucketWebsiteConfigurationRequest.builder()
        .bucket(bucket)
        .websiteConfiguration(WebsiteConfiguration.builder()
            .indexDocument(IndexDocument.builder()
                .suffix("index.html").build())
            .errorDocument(ErrorDocument.builder()
                .key("error.html").build())
            .build())
        .build());`} />

        <Callout type="info">
          <p>
            No LocalStack, o site fica acessivel em{' '}
            <code>http://localhost:4566/{'<bucket>'}/?website</code>.
            Em producao, o S3 gera uma URL como{' '}
            <code>{'<bucket>'}.s3-website-us-east-1.amazonaws.com</code>.
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
          A tabela abaixo lista todos os 13 grupos de endpoints expostos pela POC.
          Todos usam o prefixo base <code>/api/s3</code>.
        </p>
        <Table
          headers={['Grupo', 'Base Path', 'Operacoes Principais']}
          rows={[
            ['CRUD', <code>/api/s3/objects</code>, 'PUT, GET, HEAD, DELETE, COPY, LIST'],
            ['Versioning', <code>/api/s3/versioning</code>, 'Enable, Suspend, Status, PUT, GET by version, List versions'],
            ['Presigned URLs', <code>/api/s3/presigned</code>, 'Generate GET URL, Generate PUT URL'],
            ['Multipart Upload', <code>/api/s3/multipart</code>, 'Initiate, Upload part, Complete, Abort, List'],
            ['Encryption', <code>/api/s3/encryption</code>, 'Configure, Get config, Delete, PUT encrypted, HEAD info'],
            ['Policies & ACLs', <code>/api/s3/policies</code>, 'Put policy, Get/Delete policy, Public access block, ACLs'],
            ['Tags', <code>/api/s3/tags</code>, 'Bucket tags (get/put/delete), Object tags (get/put/delete)'],
            ['Lifecycle', <code>/api/s3/lifecycle</code>, 'Get rules, Put rules, Delete rules'],
            ['CORS', <code>/api/s3/cors</code>, 'Get config, Put config, Delete config'],
            ['Notifications', <code>/api/s3/notifications</code>, 'Configure SQS/SNS, Get config, Delete, Poll SQS'],
            ['Logging', <code>/api/s3/logging</code>, 'Enable, Get config, Disable, List logs'],
            ['Website', <code>/api/s3/website</code>, 'Configure, Get config, Delete, Upload files'],
            ['Bucket Ops', <code>/api/s3/buckets</code>, 'Create, List, Delete, Head bucket'],
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
          Siga os 4 passos abaixo para executar a POC localmente. Certifique-se de que
          o Docker esta rodando antes de comecar.
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
        <CodeBlock language="bash" code={`cd pocs/poc-s3-storage
./mvnw spring-boot:run`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Upload de arquivo
curl -X POST "http://localhost:8080/api/s3/objects/my-bucket/hello.txt" \\
  -H "Content-Type: text/plain" \\
  -d "Hello, S3!"

# Download
curl "http://localhost:8080/api/s3/objects/my-bucket/hello.txt"

# Listar objetos
curl "http://localhost:8080/api/s3/objects/my-bucket?maxKeys=10"

# Gerar presigned URL para download
curl "http://localhost:8080/api/s3/presigned/my-bucket/hello.txt?expiration=15"

# Verificar versionamento
curl "http://localhost:8080/api/s3/versioning/my-bucket/status"

# Consultar encryption
curl "http://localhost:8080/api/s3/encryption/my-bucket"`} />

        <Callout type="warning">
          <p>
            Substitua <code>my-bucket</code> pelo nome real do bucket criado pelo Terraform.
            Use <code>terraform output</code> para ver os nomes dos buckets provisionados.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/s3-storage" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
