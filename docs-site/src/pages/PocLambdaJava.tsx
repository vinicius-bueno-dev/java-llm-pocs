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
  { id: 'funcoes', title: 'Gerenciamento de Funcoes' },
  { id: 'invocacao', title: 'Invocacao de Funcoes' },
  { id: 'event-source-mapping', title: 'Event Source Mapping' },
  { id: 'deploy', title: 'Deploy e Artefatos' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocLambdaJava() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>Lambda Java</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora o gerenciamento e invocacao de funcoes AWS Lambda atraves de uma
          aplicacao Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para criar, configurar, invocar e conectar
          funcoes Lambda a event sources como SQS.
        </p>
        <p>
          As funcionalidades cobertas incluem criacao de funcoes com handlers embutidos
          (echo, S3 trigger, SQS trigger), upload de ZIPs customizados, invocacao sincrona
          e assincrona, dry-run para validacao, event source mappings com SQS, e deploy
          de artefatos via bucket S3.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. Os handlers de exemplo sao
            escritos em Python (empacotados como ZIP em memoria), pois o LocalStack
            Community suporta execucao de Lambdas Python nativamente. O runtime Java 21
            e configurado como padrao, mas os handlers inline usam Python para simplicidade.
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
          Sao 3 controllers e 4 services, cada um responsavel por uma area funcional.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.lambda/
  config/         LambdaConfig (LambdaClient, S3Client, SqsClient, IamClient, CloudWatchLogsClient)
  controller/     FunctionController, InvocationController, EventSourceController
  service/        FunctionManagementService, FunctionInvocationService,
                  EventSourceMappingService, LambdaDeployService
  dto/            Records: CreateFunctionDto, InvokeFunctionDto, EventSourceMappingDto`} />

        <p>
          A classe <code>LambdaConfig</code> configura 5 beans de clientes AWS SDK v2,
          todos apontando para o endpoint do LocalStack com credenciais fake:
        </p>
        <CodeBlock language="java" title="LambdaConfig.java (trecho)" code={`@Configuration
public class LambdaConfig {

    @Bean
    public LambdaClient lambdaClient(
            @Value("\${aws.endpoint}") String endpoint,
            @Value("\${aws.region}") String region,
            @Value("\${aws.access-key}") String accessKey,
            @Value("\${aws.secret-key}") String secretKey) {
        return LambdaClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(staticCredentials(accessKey, secretKey))
                .build();
    }

    // Tambem configura: S3Client, SqsClient, IamClient, CloudWatchLogsClient
}`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8084</code> para nao conflitar com as
            demais POCs (S3 em 8080, SQS em 8081, DynamoDB em 8083).
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
          O modulo Terraform em <code>infra/localstack/modules/lambda</code> provisiona
          a infraestrutura base necessaria para a POC: IAM role, bucket de artefatos,
          fila SQS como event source e dead-letter queue.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_iam_role</code>, 'poc-lambda-role', 'Role de execucao Lambda com AssumeRole para lambda.amazonaws.com'],
            [<code>aws_iam_role_policy</code>, 'poc-lambda-policy', 'Permissoes para CloudWatch Logs, SQS e S3 read'],
            [<code>aws_s3_bucket</code>, 'poc-lambda-artifacts', 'Bucket para armazenar artefatos (JARs/ZIPs) de deploy'],
            [<code>aws_sqs_queue</code>, 'poc-lambda-trigger', 'Fila SQS como event source para Lambda (visibility 60s)'],
            [<code>aws_sqs_queue</code>, 'poc-lambda-dlq', 'Dead-letter queue com retencao de 14 dias'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/lambda/main.tf (trecho)" code={`resource "aws_iam_role" "lambda_exec" {
  name = "\${var.project_name}-\${var.environment}-poc-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

resource "aws_s3_bucket" "lambda_artifacts" {
  bucket = "\${var.project_name}-\${var.environment}-poc-lambda-artifacts"
}

resource "aws_sqs_queue" "lambda_trigger" {
  name                       = "\${var.project_name}-\${var.environment}-poc-lambda-trigger"
  visibility_timeout_seconds = 60

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.lambda_dlq.arn
    maxReceiveCount     = 3
  })
}`} />

        <Callout type="info">
          <p>
            A fila de trigger usa <code>visibility_timeout_seconds = 60</code> (maior que
            o timeout padrao da Lambda de 30s) para evitar que mensagens sejam reprocessadas
            enquanto a funcao ainda esta executando.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* FUNCOES */}
        {/* ================================================================ */}
        <h2 id="funcoes" className="heading-anchor">
          <a href="#funcoes" className="heading-anchor__link" aria-hidden="true">#</a>
          Gerenciamento de Funcoes
        </h2>
        <p>
          O <code>FunctionController</code> expoe endpoints para criar funcoes com handlers
          embutidos, fazer upload de ZIPs customizados, listar funcoes, consultar configuracao,
          atualizar parametros e deletar funcoes.
        </p>

        <h3>Criar funcoes com handlers embutidos</h3>
        <p>
          A POC inclui 3 handlers Python pre-definidos que sao empacotados como ZIP em memoria
          pelo <code>LambdaDeployService</code>: um handler de echo que retorna o evento recebido,
          um handler para eventos S3 e um handler para eventos SQS.
        </p>
        <CodeBlock language="java" title="FunctionController.java - Criar Echo Function" code={`@PostMapping("/create-echo")
public ResponseEntity<Map<String, String>> createEchoFunction(
        @RequestParam String functionName) throws IOException {
    byte[] zip = deployService.createEchoHandlerZip();
    CreateFunctionDto dto = new CreateFunctionDto(
            functionName, "handler.handler", "Echo function for testing",
            null, null, null);
    return ResponseEntity.ok(managementService.createFunction(dto, zip));
}`} />

        <CodeBlock language="java" title="FunctionManagementService.java - createFunction" code={`public Map<String, String> createFunction(CreateFunctionDto dto, byte[] zipBytes) {
    FunctionCode code = FunctionCode.builder()
            .zipFile(SdkBytes.fromByteArray(zipBytes))
            .build();

    CreateFunctionRequest.Builder builder = CreateFunctionRequest.builder()
            .functionName(dto.functionName())
            .runtime(defaultRuntime)
            .role(roleArn)
            .handler(dto.handler())
            .code(code)
            .timeout(dto.timeout() != null ? dto.timeout() : defaultTimeout)
            .memorySize(dto.memorySize() != null ? dto.memorySize() : defaultMemorySize);

    if (dto.environmentVariables() != null && !dto.environmentVariables().isEmpty()) {
        builder.environment(Environment.builder()
                .variables(dto.environmentVariables()).build());
    }

    CreateFunctionResponse response = lambdaClient.createFunction(builder.build());
    return Map.of(
            "functionName", response.functionName(),
            "functionArn", response.functionArn(),
            "runtime", response.runtimeAsString(),
            "state", response.stateAsString() != null ? response.stateAsString() : "Active");
}`} />

        <h3>Listar funcoes e consultar configuracao</h3>
        <p>
          O endpoint <code>GET /api/lambda/functions</code> retorna todas as funcoes Lambda
          registradas, e <code>GET /api/lambda/functions/{'{functionName}'}</code> retorna
          detalhes de configuracao como runtime, handler, timeout, memorySize e variaveis de ambiente.
        </p>
        <CodeBlock language="java" title="FunctionManagementService.java - listFunctions / getConfig" code={`public List<Map<String, String>> listFunctions() {
    ListFunctionsResponse response = lambdaClient.listFunctions();
    return response.functions().stream()
            .map(f -> Map.of(
                    "functionName", f.functionName(),
                    "functionArn", f.functionArn(),
                    "runtime", f.runtimeAsString(),
                    "handler", f.handler(),
                    "lastModified", f.lastModified() != null ? f.lastModified() : "n/a"))
            .toList();
}

public Map<String, String> getFunctionConfiguration(String functionName) {
    GetFunctionConfigurationResponse response = lambdaClient.getFunctionConfiguration(
            GetFunctionConfigurationRequest.builder()
                    .functionName(functionName).build());
    // Retorna: functionName, functionArn, runtime, handler,
    //          timeout, memorySize, lastModified, envVarCount
}`} />

        <h3>Atualizar configuracao</h3>
        <p>
          O endpoint <code>PUT /api/lambda/functions/{'{functionName}'}/configuration</code> permite
          alterar timeout, memorySize e variaveis de ambiente de uma funcao existente.
        </p>
        <CodeBlock language="java" title="FunctionManagementService.java - updateConfiguration" code={`public Map<String, String> updateFunctionConfiguration(String functionName,
        Integer timeout, Integer memorySize, Map<String, String> envVars) {
    UpdateFunctionConfigurationRequest.Builder builder =
            UpdateFunctionConfigurationRequest.builder()
                    .functionName(functionName);

    if (timeout != null) builder.timeout(timeout);
    if (memorySize != null) builder.memorySize(memorySize);
    if (envVars != null) {
        builder.environment(Environment.builder().variables(envVars).build());
    }

    UpdateFunctionConfigurationResponse response =
            lambdaClient.updateFunctionConfiguration(builder.build());
    return Map.of(
            "functionName", response.functionName(),
            "timeout", String.valueOf(response.timeout()),
            "memorySize", String.valueOf(response.memorySize()));
}`} />

        {/* ================================================================ */}
        {/* INVOCACAO */}
        {/* ================================================================ */}
        <h2 id="invocacao" className="heading-anchor">
          <a href="#invocacao" className="heading-anchor__link" aria-hidden="true">#</a>
          Invocacao de Funcoes
        </h2>
        <p>
          O <code>InvocationController</code> expoe 3 modos de invocacao, cada um
          usando um <code>InvocationType</code> diferente do SDK. O payload e enviado
          como corpo da requisicao (JSON string).
        </p>

        <h3>Invocacao sincrona (RequestResponse)</h3>
        <p>
          A invocacao sincrona espera a funcao terminar e retorna o payload de resposta,
          o status code, a versao executada e eventuais erros. Usa{' '}
          <code>InvocationType.REQUEST_RESPONSE</code>.
        </p>
        <CodeBlock language="java" title="FunctionInvocationService.java - invokeSync" code={`public Map<String, Object> invokeSync(String functionName, String payload) {
    InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
            .functionName(functionName)
            .invocationType(InvocationType.REQUEST_RESPONSE)
            .payload(SdkBytes.fromUtf8String(payload != null ? payload : "{}"))
            .build());

    String responsePayload = response.payload().asString(StandardCharsets.UTF_8);
    return Map.of(
            "statusCode", response.statusCode(),
            "payload", responsePayload,
            "executedVersion", response.executedVersion() != null
                    ? response.executedVersion() : "$LATEST",
            "functionError", response.functionError() != null
                    ? response.functionError() : "none");
}`} />

        <h3>Invocacao assincrona (Event)</h3>
        <p>
          A invocacao assincrona envia o evento e retorna imediatamente com status 202
          (Accepted). A funcao executa em background. Usa{' '}
          <code>InvocationType.EVENT</code>.
        </p>
        <CodeBlock language="java" title="FunctionInvocationService.java - invokeAsync" code={`public Map<String, Object> invokeAsync(String functionName, String payload) {
    InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
            .functionName(functionName)
            .invocationType(InvocationType.EVENT)
            .payload(SdkBytes.fromUtf8String(payload != null ? payload : "{}"))
            .build());

    return Map.of(
            "statusCode", response.statusCode(),
            "accepted", response.statusCode() == 202);
}`} />

        <h3>Dry-run (validacao)</h3>
        <p>
          O dry-run valida os parametros da invocacao sem executar a funcao de fato.
          Retorna status 204 se a configuracao e valida. Usa{' '}
          <code>InvocationType.DRY_RUN</code>.
        </p>
        <CodeBlock language="java" title="FunctionInvocationService.java - invokeDryRun" code={`public Map<String, Object> invokeDryRun(String functionName, String payload) {
    InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
            .functionName(functionName)
            .invocationType(InvocationType.DRY_RUN)
            .payload(SdkBytes.fromUtf8String(payload != null ? payload : "{}"))
            .build());

    return Map.of(
            "statusCode", response.statusCode(),
            "validated", response.statusCode() == 204);
}`} />

        <Callout type="tip">
          <p>
            Use <strong>dry-run</strong> para validar permissoes e configuracao antes de
            invocar a funcao em producao. No LocalStack, o comportamento pode diferir
            levemente da AWS real.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* EVENT SOURCE MAPPING */}
        {/* ================================================================ */}
        <h2 id="event-source-mapping" className="heading-anchor">
          <a href="#event-source-mapping" className="heading-anchor__link" aria-hidden="true">#</a>
          Event Source Mapping
        </h2>
        <p>
          Event source mappings conectam uma fila SQS (ou outro servico) a uma funcao Lambda.
          Quando mensagens chegam na fila, o Lambda as consome automaticamente em batches.
          O <code>EventSourceController</code> expoe operacoes de CRUD para esses mapeamentos.
        </p>
        <CodeBlock language="java" title="EventSourceMappingService.java - criar mapping" code={`public Map<String, String> createEventSourceMapping(EventSourceMappingDto dto) {
    CreateEventSourceMappingResponse response = lambdaClient.createEventSourceMapping(
            CreateEventSourceMappingRequest.builder()
                    .functionName(dto.functionName())
                    .eventSourceArn(dto.eventSourceArn())
                    .batchSize(dto.batchSize() > 0 ? dto.batchSize() : 10)
                    .enabled(dto.enabled())
                    .build());

    return Map.of(
            "uuid", response.uuid(),
            "functionArn", response.functionArn(),
            "eventSourceArn", response.eventSourceArn(),
            "state", response.state() != null ? response.state() : "Enabled");
}`} />

        <p>
          O DTO <code>EventSourceMappingDto</code> e um record Java que encapsula os parametros:
        </p>
        <CodeBlock language="java" title="EventSourceMappingDto.java" code={`public record EventSourceMappingDto(
        String functionName,
        String eventSourceArn,
        Integer batchSize,
        boolean enabled
) {}`} />

        <p>
          Alem de criar, e possivel listar mapeamentos por funcao, atualizar o batch size
          e o estado (habilitado/desabilitado), e deletar mapeamentos por UUID.
        </p>
        <CodeBlock language="java" title="EventSourceMappingService.java - atualizar mapping" code={`public Map<String, String> updateEventSourceMapping(
        String uuid, Integer batchSize, boolean enabled) {
    UpdateEventSourceMappingRequest.Builder builder =
            UpdateEventSourceMappingRequest.builder()
                    .uuid(uuid)
                    .enabled(enabled);

    if (batchSize != null) {
        builder.batchSize(batchSize);
    }

    UpdateEventSourceMappingResponse response =
            lambdaClient.updateEventSourceMapping(builder.build());
    return Map.of(
            "uuid", response.uuid(),
            "state", response.state() != null ? response.state() : "n/a",
            "batchSize", String.valueOf(response.batchSize()));
}`} />

        <Callout type="warning">
          <p>
            No LocalStack Community, event source mappings sao registrados via API, mas
            o polling automatico de mensagens SQS pode nao funcionar perfeitamente.
            Para testar o fluxo completo, considere invocar a funcao diretamente com
            um payload SQS simulado.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* DEPLOY */}
        {/* ================================================================ */}
        <h2 id="deploy" className="heading-anchor">
          <a href="#deploy" className="heading-anchor__link" aria-hidden="true">#</a>
          Deploy e Artefatos
        </h2>
        <p>
          O <code>LambdaDeployService</code> e responsavel por empacotar handlers Python
          como ZIPs em memoria e fazer upload de artefatos para o bucket S3. A POC inclui
          3 handlers pre-definidos e suporte a upload de ZIPs customizados.
        </p>

        <h3>Handlers embutidos</h3>
        <p>
          Cada handler e uma funcao Python empacotada dinamicamente em um arquivo ZIP.
          O metodo <code>createSampleHandlerZip</code> recebe o codigo-fonte e gera o ZIP
          com um <code>ZipOutputStream</code>.
        </p>
        <CodeBlock language="java" title="LambdaDeployService.java - empacotamento ZIP" code={`public byte[] createSampleHandlerZip(String handlerCode) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
        zos.putNextEntry(new ZipEntry("handler.py"));
        zos.write(handlerCode.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
    return baos.toByteArray();
}`} />

        <Table
          headers={['Handler', 'Endpoint de Criacao', 'Descricao']}
          rows={[
            ['Echo', <code>POST /create-echo</code>, 'Retorna o evento recebido como JSON (teste basico)'],
            ['S3 Trigger', <code>POST /create-s3-trigger</code>, 'Processa eventos S3 (bucket + key de cada record)'],
            ['SQS Trigger', <code>POST /create-sqs-trigger</code>, 'Processa mensagens SQS (messageId + body de cada record)'],
          ]}
        />

        <h3>Upload de artefatos para S3</h3>
        <p>
          O metodo <code>uploadArtifact</code> envia o ZIP para o bucket de artefatos
          configurado em <code>aws.lambda.artifacts-bucket</code>. Isso permite manter
          versoes dos artefatos no S3.
        </p>
        <CodeBlock language="java" title="LambdaDeployService.java - upload para S3" code={`public Map<String, String> uploadArtifact(String key, byte[] zipBytes) {
    s3Client.putObject(
            PutObjectRequest.builder()
                    .bucket(artifactsBucket)
                    .key(key)
                    .contentType("application/zip")
                    .build(),
            RequestBody.fromBytes(zipBytes));

    return Map.of(
            "bucket", artifactsBucket,
            "key", key,
            "size", String.valueOf(zipBytes.length));
}`} />

        <h3>Upload de ZIP customizado</h3>
        <p>
          O endpoint <code>POST /api/lambda/functions/upload</code> aceita um arquivo ZIP
          via multipart e cria a funcao diretamente a partir dele:
        </p>
        <CodeBlock language="java" title="FunctionController.java - upload customizado" code={`@PostMapping("/upload")
public ResponseEntity<Map<String, String>> createFromZip(
        @RequestParam String functionName,
        @RequestParam String handler,
        @RequestParam(required = false) String description,
        @RequestPart("file") MultipartFile file) throws IOException {
    CreateFunctionDto dto = new CreateFunctionDto(
            functionName, handler, description, null, null, null);
    return ResponseEntity.ok(managementService.createFunction(dto, file.getBytes()));
}`} />

        {/* ================================================================ */}
        {/* API REFERENCE */}
        {/* ================================================================ */}
        <h2 id="api-reference" className="heading-anchor">
          <a href="#api-reference" className="heading-anchor__link" aria-hidden="true">#</a>
          Referencia da API
        </h2>
        <p>
          A tabela abaixo lista todos os endpoints expostos pela POC.
          A aplicacao roda na porta <code>8084</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            ['POST', <code>/api/lambda/functions/create-echo</code>, 'Cria funcao com handler echo (retorna evento)'],
            ['POST', <code>/api/lambda/functions/create-s3-trigger</code>, 'Cria funcao com handler de eventos S3'],
            ['POST', <code>/api/lambda/functions/create-sqs-trigger</code>, 'Cria funcao com handler de eventos SQS'],
            ['POST', <code>/api/lambda/functions/upload</code>, 'Cria funcao a partir de ZIP customizado (multipart)'],
            ['GET', <code>/api/lambda/functions</code>, 'Lista todas as funcoes Lambda'],
            ['GET', <code>/api/lambda/functions/{'{functionName}'}</code>, 'Retorna configuracao detalhada da funcao'],
            ['PUT', <code>/api/lambda/functions/{'{functionName}'}/configuration</code>, 'Atualiza timeout, memorySize e env vars'],
            ['DELETE', <code>/api/lambda/functions/{'{functionName}'}</code>, 'Deleta uma funcao Lambda'],
            ['POST', <code>/api/lambda/invoke/sync/{'{functionName}'}</code>, 'Invocacao sincrona (RequestResponse)'],
            ['POST', <code>/api/lambda/invoke/async/{'{functionName}'}</code>, 'Invocacao assincrona (Event, retorna 202)'],
            ['POST', <code>/api/lambda/invoke/dry-run/{'{functionName}'}</code>, 'Validacao sem execucao (DryRun, retorna 204)'],
            ['POST', <code>/api/lambda/event-sources</code>, 'Cria event source mapping (SQS -> Lambda)'],
            ['GET', <code>/api/lambda/event-sources/{'{functionName}'}</code>, 'Lista event source mappings da funcao'],
            ['PUT', <code>/api/lambda/event-sources/{'{uuid}'}</code>, 'Atualiza batch size e estado do mapping'],
            ['DELETE', <code>/api/lambda/event-sources/{'{uuid}'}</code>, 'Deleta um event source mapping'],
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

# Verificar recursos criados
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-lambda-java
./mvnw spring-boot:run

# Roda na porta 8084`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Criar funcao echo
curl -X POST "http://localhost:8084/api/lambda/functions/create-echo?functionName=my-echo"

# Listar funcoes
curl "http://localhost:8084/api/lambda/functions"

# Consultar configuracao
curl "http://localhost:8084/api/lambda/functions/my-echo"

# Invocar sincronamente
curl -X POST "http://localhost:8084/api/lambda/invoke/sync/my-echo" \\
  -H "Content-Type: application/json" \\
  -d '{"name": "Lambda", "message": "Hello!"}'

# Invocar assincronamente
curl -X POST "http://localhost:8084/api/lambda/invoke/async/my-echo" \\
  -H "Content-Type: application/json" \\
  -d '{"test": true}'

# Dry-run (validar sem executar)
curl -X POST "http://localhost:8084/api/lambda/invoke/dry-run/my-echo" \\
  -H "Content-Type: application/json" \\
  -d '{}'

# Atualizar configuracao (timeout e memorySize)
curl -X PUT "http://localhost:8084/api/lambda/functions/my-echo/configuration?timeout=60&memorySize=256"

# Criar funcao SQS trigger
curl -X POST "http://localhost:8084/api/lambda/functions/create-sqs-trigger?functionName=my-sqs-handler"

# Criar event source mapping (SQS -> Lambda)
curl -X POST "http://localhost:8084/api/lambda/event-sources" \\
  -H "Content-Type: application/json" \\
  -d '{"functionName":"my-sqs-handler","eventSourceArn":"arn:aws:sqs:us-east-1:000000000000:nameless-local-poc-lambda-trigger","batchSize":5,"enabled":true}'

# Listar event source mappings
curl "http://localhost:8084/api/lambda/event-sources/my-sqs-handler"

# Deletar funcao
curl -X DELETE "http://localhost:8084/api/lambda/functions/my-echo"`} />

        <Callout type="warning">
          <p>
            Certifique-se de que o LocalStack esta rodando e a infraestrutura foi
            provisionada com Terraform antes de iniciar a aplicacao. O ARN da role
            e os nomes dos recursos sao configurados em{' '}
            <code>application.yml</code>.
          </p>
        </Callout>

        <CodeBlock language="yaml" title="application.yml" code={`server:
  port: 8084

spring:
  application:
    name: poc-lambda-java

aws:
  region: us-east-1
  endpoint: http://localhost:4566
  access-key: test
  secret-key: test
  lambda:
    role-arn: arn:aws:iam::000000000000:role/nameless-local-poc-lambda-role
    artifacts-bucket: nameless-local-poc-lambda-artifacts
    trigger-queue-name: nameless-local-poc-lambda-trigger
    runtime: java21
    timeout: 30
    memory-size: 512`} />

        <PageNav currentPath="/pocs/lambda-java" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
