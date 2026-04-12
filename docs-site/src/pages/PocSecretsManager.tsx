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
  { id: 'create-retrieve', title: 'Criar e Recuperar Segredos' },
  { id: 'versioning', title: 'Versionamento' },
  { id: 'rotation', title: 'Rotacao de Segredos' },
  { id: 'tags', title: 'Tags' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocSecretsManager() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>Secrets Manager</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora as principais funcionalidades do AWS Secrets Manager em uma
          aplicacao Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar
          o <strong>AWS SDK for Java v2</strong> para criar, recuperar, versionar, rotacionar
          e gerenciar segredos de forma programatica.
        </p>
        <p>
          As funcionalidades cobertas incluem criacao de segredos (string simples e JSON),
          recuperacao por ID e por version stage, versionamento com{' '}
          <code>PutSecretValue</code>, rotacao manual, listagem de versoes, tags,
          descricao detalhada, exclusao com e sem recuperacao, e restauracao de segredos deletados.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O Secrets Manager tem boa
            cobertura no tier gratuito, incluindo versionamento e operacoes de tags.
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
          A logica esta dividida em dois pares controller/service: um para operacoes
          gerais de segredos e outro para rotacao e versionamento.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.secretsmanager/
  config/         SecretsManagerConfig (SecretsManagerClient bean)
  controller/     SecretController (CRUD + tags)
                  SecretRotationController (rotacao + versoes)
  service/        SecretService (criar, ler, atualizar, deletar, tags)
                  SecretRotationService (rotacionar, config, versoes)
  dto/            Records: CreateSecretDto, UpdateSecretDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8086</code> para nao conflitar com as demais POCs.
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
          O modulo Terraform em <code>infra/localstack/modules/secretsmanager</code> provisiona
          3 segredos com diferentes formatos e propositos, cada um com sua versao inicial.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_secretsmanager_secret</code>, 'poc-app-secret', 'Segredo simples (string) para configuracao da aplicacao'],
            [<code>aws_secretsmanager_secret_version</code>, 'app_secret_value', 'Versao inicial do segredo simples'],
            [<code>aws_secretsmanager_secret</code>, 'poc-db-credentials', 'Segredo JSON com credenciais de banco de dados'],
            [<code>aws_secretsmanager_secret_version</code>, 'db_credentials_value', 'Versao inicial das credenciais (JSON com user, pass, host, port, dbname)'],
            [<code>aws_secretsmanager_secret</code>, 'poc-api-key', 'Segredo para armazenar API key'],
            [<code>aws_secretsmanager_secret_version</code>, 'api_key_value', 'Versao inicial da API key'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/secretsmanager/main.tf (trecho)" code={`# Segredo simples (string)
resource "aws_secretsmanager_secret" "app_secret" {
  name = "\${var.project_name}-\${var.environment}-poc-app-secret"

  tags = {
    Name        = "\${var.project_name}-poc-app-secret"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "secrets-manager"
    Type        = "application"
  }
}

resource "aws_secretsmanager_secret_version" "app_secret_value" {
  secret_id     = aws_secretsmanager_secret.app_secret.id
  secret_string = "my-super-secret-value-v1"
}

# Segredo JSON (credenciais de DB)
resource "aws_secretsmanager_secret" "db_credentials" {
  name = "\${var.project_name}-\${var.environment}-poc-db-credentials"
}

resource "aws_secretsmanager_secret_version" "db_credentials_value" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = "admin"
    password = "s3cret-p@ssw0rd"
    host     = "localhost"
    port     = 5432
    dbname   = "pocdb"
  })
}`} />

        {/* ================================================================ */}
        {/* CREATE & RETRIEVE */}
        {/* ================================================================ */}
        <h2 id="create-retrieve" className="heading-anchor">
          <a href="#create-retrieve" className="heading-anchor__link" aria-hidden="true">#</a>
          Criar e Recuperar Segredos
        </h2>
        <p>
          O <code>SecretService</code> encapsula as operacoes basicas do Secrets Manager:
          criar segredos com valor string e tags opcionais, recuperar o valor atual,
          listar todos os segredos, obter metadados detalhados com <code>describeSecret</code>,
          atualizar o valor, deletar (com ou sem force delete) e restaurar segredos excluidos.
        </p>
        <CodeBlock language="java" title="SecretService.java — Criar segredo" code={`// Criar segredo com tags opcionais
CreateSecretRequest.Builder builder = CreateSecretRequest.builder()
        .name(name)
        .secretString(value);

if (tags != null && !tags.isEmpty()) {
    List<Tag> awsTags = tags.entrySet().stream()
            .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
            .toList();
    builder.tags(awsTags);
}

CreateSecretResponse response = secretsManagerClient.createSecret(builder.build());`} />

        <CodeBlock language="java" title="SecretService.java — Recuperar e descrever" code={`// Recuperar valor do segredo
GetSecretValueResponse response = secretsManagerClient.getSecretValue(
        GetSecretValueRequest.builder()
                .secretId(secretId)
                .build());
// response.secretString(), response.versionId(), response.versionStages()

// Recuperar por version stage (ex: AWSCURRENT, AWSPREVIOUS)
GetSecretValueResponse response = secretsManagerClient.getSecretValue(
        GetSecretValueRequest.builder()
                .secretId(secretId)
                .versionStage(versionStage)
                .build());

// Descrever segredo (metadados completos)
DescribeSecretResponse desc = secretsManagerClient.describeSecret(
        DescribeSecretRequest.builder()
                .secretId(secretId)
                .build());
// desc.tags(), desc.versionIdsToStages(), desc.lastChangedDate()`} />

        <CodeBlock language="java" title="SecretService.java — Deletar e restaurar" code={`// Deletar segredo (com opcao de force delete)
DeleteSecretRequest.Builder builder = DeleteSecretRequest.builder()
        .secretId(secretId);
if (forceDelete) {
    builder.forceDeleteWithoutRecovery(true);
}
secretsManagerClient.deleteSecret(builder.build());

// Restaurar segredo deletado (antes do periodo de retencao expirar)
RestoreSecretResponse response = secretsManagerClient.restoreSecret(
        RestoreSecretRequest.builder()
                .secretId(secretId)
                .build());`} />

        <Callout type="warning">
          <p>
            Ao usar <code>forceDeleteWithoutRecovery(true)</code>, o segredo e excluido
            permanentemente e nao pode ser restaurado. Sem essa flag, o segredo fica em
            estado de exclusao programada e pode ser restaurado dentro do periodo de retencao.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* VERSIONING */}
        {/* ================================================================ */}
        <h2 id="versioning" className="heading-anchor">
          <a href="#versioning" className="heading-anchor__link" aria-hidden="true">#</a>
          Versionamento
        </h2>
        <p>
          O Secrets Manager suporta versionamento automatico de segredos. Cada vez que
          o valor e atualizado (via <code>UpdateSecret</code> ou <code>PutSecretValue</code>),
          uma nova versao e criada com um <code>versionId</code> unico. As versoes sao
          organizadas por <strong>version stages</strong> como <code>AWSCURRENT</code> e{' '}
          <code>AWSPREVIOUS</code>.
        </p>
        <p>
          O metodo <code>putSecretValue</code> permite controlar explicitamente os version
          stages atribuidos a nova versao, enquanto <code>updateSecretValue</code> atribui
          automaticamente <code>AWSCURRENT</code>.
        </p>
        <CodeBlock language="java" title="SecretService.java — Versionamento" code={`// Atualizar valor (cria nova versao automaticamente como AWSCURRENT)
UpdateSecretResponse response = secretsManagerClient.updateSecret(
        UpdateSecretRequest.builder()
                .secretId(secretId)
                .secretString(newValue)
                .build());
// response.versionId() — ID da nova versao

// PutSecretValue com version stages customizados
PutSecretValueRequest.Builder builder = PutSecretValueRequest.builder()
        .secretId(secretId)
        .secretString(value);
if (versionStages != null && !versionStages.isEmpty()) {
    builder.versionStages(versionStages);
}
PutSecretValueResponse response = secretsManagerClient.putSecretValue(builder.build());
// response.versionStages() — stages atribuidos`} />

        <CodeBlock language="java" title="SecretRotationService.java — Listar versoes" code={`// Listar todas as versoes de um segredo
ListSecretVersionIdsResponse response = secretsManagerClient.listSecretVersionIds(
        ListSecretVersionIdsRequest.builder()
                .secretId(secretId)
                .build());

// Cada versao tem: versionId, versionStages, createdDate
response.versions().stream().map(version -> {
    // version.versionId()
    // version.versionStages() — ex: ["AWSCURRENT"] ou ["AWSPREVIOUS"]
    // version.createdDate()
});`} />

        <Callout type="tip">
          <p>
            Use <code>GET /api/secrets/{'{'}secretId{'}'}/stage?versionStage=AWSPREVIOUS</code> para
            recuperar a versao anterior do segredo apos uma atualizacao.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ROTATION */}
        {/* ================================================================ */}
        <h2 id="rotation" className="heading-anchor">
          <a href="#rotation" className="heading-anchor__link" aria-hidden="true">#</a>
          Rotacao de Segredos
        </h2>
        <p>
          A rotacao permite trocar o valor de um segredo de forma automatica ou manual.
          Na AWS, a rotacao automatica utiliza uma funcao Lambda para gerar o novo valor.
          O <code>SecretRotationController</code> expoe endpoints para disparar rotacao
          manual, consultar a configuracao de rotacao e listar versoes.
        </p>
        <CodeBlock language="java" title="SecretRotationService.java — Rotacionar e consultar config" code={`// Disparar rotacao manual do segredo
RotateSecretResponse response = secretsManagerClient.rotateSecret(
        RotateSecretRequest.builder()
                .secretId(secretId)
                .build());
// response.versionId() — ID da nova versao criada pela rotacao

// Consultar configuracao de rotacao
DescribeSecretResponse desc = secretsManagerClient.describeSecret(
        DescribeSecretRequest.builder()
                .secretId(secretId)
                .build());
// desc.rotationEnabled()
// desc.rotationLambdaARN()
// desc.rotationRules() — automaticallyAfterDays, duration, scheduleExpression
// desc.lastRotatedDate()
// desc.nextRotationDate()`} />

        <Callout type="info">
          <p>
            No LocalStack Community, a rotacao via <code>RotateSecret</code> e aceita pela API,
            mas a execucao automatica de uma Lambda de rotacao pode ter limitacoes.
            A POC demonstra a chamada de API e a consulta de configuracao.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* TAGS */}
        {/* ================================================================ */}
        <h2 id="tags" className="heading-anchor">
          <a href="#tags" className="heading-anchor__link" aria-hidden="true">#</a>
          Tags
        </h2>
        <p>
          Tags sao pares chave-valor associados a segredos para organizacao, controle de
          custos e filtragem. O Secrets Manager permite adicionar tags na criacao do segredo
          ou posteriormente via <code>TagResource</code>. A POC demonstra ambos os cenarios.
        </p>
        <CodeBlock language="java" title="SecretService.java — Gerenciar tags" code={`// Adicionar tags na criacao do segredo
List<Tag> awsTags = tags.entrySet().stream()
        .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
        .toList();
builder.tags(awsTags);

// Adicionar tags a um segredo existente
List<Tag> awsTags = tags.entrySet().stream()
        .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
        .toList();

secretsManagerClient.tagResource(TagResourceRequest.builder()
        .secretId(secretId)
        .tags(awsTags)
        .build());

// Consultar tags via describeSecret
DescribeSecretResponse desc = secretsManagerClient.describeSecret(...);
Map<String, String> tagMap = desc.tags().stream()
        .collect(Collectors.toMap(Tag::key, Tag::value));`} />

        {/* ================================================================ */}
        {/* API REFERENCE */}
        {/* ================================================================ */}
        <h2 id="api-reference" className="heading-anchor">
          <a href="#api-reference" className="heading-anchor__link" aria-hidden="true">#</a>
          Referencia da API
        </h2>
        <p>
          A tabela abaixo lista todos os endpoints expostos pela POC.
          Os endpoints estao divididos entre <code>/api/secrets</code> (operacoes gerais)
          e <code>/api/secrets/rotation</code> (rotacao e versoes).
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            ['POST', <code>/api/secrets</code>, 'Criar novo segredo com valor e tags opcionais'],
            ['GET', <code>/api/secrets/{'{'}secretId{'}'}</code>, 'Recuperar valor atual do segredo'],
            ['GET', <code>/api/secrets/{'{'}secretId{'}'}/stage?versionStage=...</code>, 'Recuperar valor por version stage (AWSCURRENT, AWSPREVIOUS)'],
            ['GET', <code>/api/secrets</code>, 'Listar todos os segredos'],
            ['GET', <code>/api/secrets/{'{'}secretId{'}'}/describe</code>, 'Obter metadados detalhados do segredo'],
            ['PUT', <code>/api/secrets</code>, 'Atualizar valor do segredo (UpdateSecret)'],
            ['PUT', <code>/api/secrets/{'{'}secretId{'}'}/value?value=...&versionStages=...</code>, 'Inserir nova versao com stages customizados (PutSecretValue)'],
            ['DELETE', <code>/api/secrets/{'{'}secretId{'}'}</code>, 'Deletar segredo (com opcao forceDelete)'],
            ['POST', <code>/api/secrets/{'{'}secretId{'}'}/restore</code>, 'Restaurar segredo deletado'],
            ['POST', <code>/api/secrets/{'{'}secretId{'}'}/tags</code>, 'Adicionar tags a um segredo existente'],
            ['POST', <code>/api/secrets/rotation/{'{'}secretId{'}'}</code>, 'Disparar rotacao manual do segredo'],
            ['GET', <code>/api/secrets/rotation/{'{'}secretId{'}'}/config</code>, 'Consultar configuracao de rotacao'],
            ['GET', <code>/api/secrets/rotation/{'{'}secretId{'}'}/versions</code>, 'Listar todas as versoes do segredo'],
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

# Ver outputs dos segredos criados
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-secrets-manager
./mvnw spring-boot:run

# Roda na porta 8086`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Criar um segredo simples
curl -X POST "http://localhost:8086/api/secrets" \\
  -H "Content-Type: application/json" \\
  -d '{"name": "meu-segredo", "secretValue": "valor-secreto", "tags": {"env": "dev"}}'

# Listar todos os segredos
curl "http://localhost:8086/api/secrets"

# Recuperar valor do segredo
curl "http://localhost:8086/api/secrets/meu-segredo"

# Descrever segredo (metadados)
curl "http://localhost:8086/api/secrets/meu-segredo/describe"

# Atualizar valor do segredo
curl -X PUT "http://localhost:8086/api/secrets" \\
  -H "Content-Type: application/json" \\
  -d '{"secretId": "meu-segredo", "secretValue": "novo-valor-secreto"}'

# Recuperar versao anterior
curl "http://localhost:8086/api/secrets/meu-segredo/stage?versionStage=AWSPREVIOUS"

# Inserir nova versao com PutSecretValue
curl -X PUT "http://localhost:8086/api/secrets/meu-segredo/value?value=valor-v3"

# Listar versoes do segredo
curl "http://localhost:8086/api/secrets/rotation/meu-segredo/versions"

# Adicionar tags
curl -X POST "http://localhost:8086/api/secrets/meu-segredo/tags" \\
  -H "Content-Type: application/json" \\
  -d '{"team": "backend", "project": "poc"}'

# Deletar segredo (soft delete)
curl -X DELETE "http://localhost:8086/api/secrets/meu-segredo"

# Restaurar segredo deletado
curl -X POST "http://localhost:8086/api/secrets/meu-segredo/restore"

# Deletar permanentemente (force delete)
curl -X DELETE "http://localhost:8086/api/secrets/meu-segredo?forceDelete=true"`} />

        <Callout type="warning">
          <p>
            Substitua <code>meu-segredo</code> pelo nome ou ARN do segredo desejado.
            Use <code>GET /api/secrets</code> para listar os segredos disponiveis,
            incluindo os provisionados pelo Terraform.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/secrets-manager" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
