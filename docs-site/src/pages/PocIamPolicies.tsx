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
  { id: 'roles', title: 'Roles' },
  { id: 'policies', title: 'Policies' },
  { id: 'attach-detach', title: 'Attach / Detach' },
  { id: 'assume-role', title: 'Assume Role' },
  { id: 'policy-simulator', title: 'Policy Simulator' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocIamPolicies() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>IAM Policies</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora as operacoes fundamentais do <strong>AWS IAM</strong> (Identity
          and Access Management) em uma aplicacao Spring Boot conectada ao LocalStack.
          O objetivo e demonstrar como usar o <strong>AWS SDK for Java v2</strong> para
          gerenciar roles, policies, attachments e assume role via STS.
        </p>
        <p>
          As funcionalidades cobertas incluem criacao e gerenciamento de IAM Roles,
          criacao de IAM Policies com policy documents JSON, vinculacao e desvinculacao
          de policies a roles, assume role para obtencao de credenciais temporarias
          via STS, e simulacao de policies com o IAM Policy Simulator.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O IAM tem boa cobertura
            no tier gratuito, incluindo roles, policies, attachments e operacoes STS
            como AssumeRole e GetCallerIdentity.
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
          Cada area funcional (Roles, Policies, AssumeRole, PolicySimulator) tem seu
          proprio par controller/service.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.iampolicies/
  config/         IamConfig (IamClient + StsClient beans)
  controller/     RoleController, PolicyController,
                  AssumeRoleController
  service/        RoleService, PolicyService,
                  AssumeRoleService, PolicySimulatorService
  dto/            Records: CreateRoleDto, CreatePolicyDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8095</code> para nao conflitar com as
            demais POCs. A configuracao dos clients IAM e STS e centralizada
            em <code>IamConfig</code>, ambos apontando para o endpoint do LocalStack.
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
          O modulo Terraform em <code>infra/localstack/modules/iam</code> provisiona
          uma role de aplicacao, uma policy de leitura S3 e o attachment entre elas.
          Isso fornece recursos prontos para testar as operacoes da API.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_iam_role</code>, 'poc-app-role', 'Role de aplicacao com trust policy para sts:AssumeRole'],
            [<code>aws_iam_policy</code>, 'poc-s3-read', 'Policy que permite s3:GetObject e s3:ListBucket em todos os recursos'],
            [<code>aws_iam_role_policy_attachment</code>, '--', 'Vincula a policy s3-read a role app-role'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/iam/main.tf (trecho)" code={`resource "aws_iam_role" "app_role" {
  name = "\${var.project_name}-\${var.environment}-poc-app-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action    = "sts:AssumeRole"
        Effect    = "Allow"
        Principal = { AWS = "*" }
      }
    ]
  })
}

resource "aws_iam_policy" "s3_read" {
  name        = "\${var.project_name}-\${var.environment}-poc-s3-read"
  description = "Permite leitura em buckets S3"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "S3ReadAccess"
        Effect   = "Allow"
        Action   = ["s3:GetObject", "s3:ListBucket"]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "app_s3_read" {
  role       = aws_iam_role.app_role.name
  policy_arn = aws_iam_policy.s3_read.arn
}`} />

        {/* ================================================================ */}
        {/* ROLES */}
        {/* ================================================================ */}
        <h2 id="roles" className="heading-anchor">
          <a href="#roles" className="heading-anchor__link" aria-hidden="true">#</a>
          Roles
        </h2>
        <p>
          IAM Roles sao identidades com policies de permissao que determinam o que
          a identidade pode ou nao fazer na AWS. Diferente de usuarios, roles nao
          possuem credenciais permanentes — sao assumidas temporariamente via STS.
          A POC demonstra o CRUD completo de roles.
        </p>
        <CodeBlock language="java" title="RoleService.java" code={`// Criar role com trust policy (assume role policy document)
Role role = iamClient.createRole(CreateRoleRequest.builder()
    .roleName(dto.roleName())
    .assumeRolePolicyDocument(dto.assumeRolePolicyDocument())
    .description(dto.description())
    .build()).role();

// Listar todas as roles
List<Role> roles = iamClient.listRoles(
    ListRolesRequest.builder().build()).roles();

// Consultar role por nome
Role role = iamClient.getRole(GetRoleRequest.builder()
    .roleName(roleName).build()).role();

// Deletar role
iamClient.deleteRole(DeleteRoleRequest.builder()
    .roleName(roleName).build());`} />

        <Callout type="warning">
          <p>
            Para deletar uma role, e necessario primeiro desvincular todas as policies
            attached a ela. Caso contrario, a API retornara um erro{' '}
            <code>DeleteConflictException</code>.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* POLICIES */}
        {/* ================================================================ */}
        <h2 id="policies" className="heading-anchor">
          <a href="#policies" className="heading-anchor__link" aria-hidden="true">#</a>
          Policies
        </h2>
        <p>
          IAM Policies sao documentos JSON que definem permissoes. Cada policy contem
          um ou mais <em>statements</em> com Effect (Allow/Deny), Action (operacoes
          permitidas) e Resource (recursos afetados). A POC cobre criacao, listagem,
          consulta por ARN, exclusao e versionamento.
        </p>
        <CodeBlock language="java" title="PolicyService.java" code={`// Criar policy com document JSON
Policy policy = iamClient.createPolicy(CreatePolicyRequest.builder()
    .policyName(dto.policyName())
    .policyDocument(dto.policyDocument())
    .description(dto.description())
    .build()).policy();

// Listar policies locais (scope = "Local")
List<Policy> policies = iamClient.listPolicies(
    ListPoliciesRequest.builder()
        .scope("Local").build()).policies();

// Consultar policy por ARN
Policy policy = iamClient.getPolicy(GetPolicyRequest.builder()
    .policyArn(policyArn).build()).policy();

// Consultar versao especifica do document
PolicyVersion version = iamClient.getPolicyVersion(
    GetPolicyVersionRequest.builder()
        .policyArn(policyArn)
        .versionId(versionId).build()).policyVersion();

// Deletar policy
iamClient.deletePolicy(DeletePolicyRequest.builder()
    .policyArn(policyArn).build());`} />

        <Callout type="info">
          <p>
            O <code>scope("Local")</code> na listagem filtra apenas policies criadas
            pelo usuario, excluindo as policies gerenciadas pela AWS (como{' '}
            <code>AmazonS3ReadOnlyAccess</code>).
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ATTACH / DETACH */}
        {/* ================================================================ */}
        <h2 id="attach-detach" className="heading-anchor">
          <a href="#attach-detach" className="heading-anchor__link" aria-hidden="true">#</a>
          Attach / Detach
        </h2>
        <p>
          Vincular (attach) e desvincular (detach) policies a roles e a forma de
          conceder ou revogar permissoes. A POC permite realizar essas operacoes
          via API e tambem listar todas as policies atualmente vinculadas a uma role.
        </p>
        <CodeBlock language="java" title="RoleService.java — attach/detach" code={`// Vincular policy a uma role
iamClient.attachRolePolicy(AttachRolePolicyRequest.builder()
    .roleName(roleName)
    .policyArn(policyArn)
    .build());

// Desvincular policy de uma role
iamClient.detachRolePolicy(DetachRolePolicyRequest.builder()
    .roleName(roleName)
    .policyArn(policyArn)
    .build());

// Listar policies vinculadas a uma role
List<AttachedPolicy> attached = iamClient
    .listAttachedRolePolicies(
        ListAttachedRolePoliciesRequest.builder()
            .roleName(roleName).build())
    .attachedPolicies();`} />

        <Callout type="tip">
          <p>
            Cada role pode ter ate 10 managed policies attached simultaneamente (limite
            AWS padrao). No LocalStack, esse limite pode nao ser enforced.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ASSUME ROLE */}
        {/* ================================================================ */}
        <h2 id="assume-role" className="heading-anchor">
          <a href="#assume-role" className="heading-anchor__link" aria-hidden="true">#</a>
          Assume Role
        </h2>
        <p>
          O <strong>STS (Security Token Service)</strong> permite assumir uma role
          e obter credenciais temporarias (access key, secret key e session token).
          Isso e essencial para cenarios de cross-account access, delegacao de
          permissoes e principio do menor privilegio. A POC tambem demonstra{' '}
          <code>GetCallerIdentity</code> para verificar a identidade atual.
        </p>
        <CodeBlock language="java" title="AssumeRoleService.java" code={`// Assumir role e obter credenciais temporarias (1 hora)
Credentials credentials = stsClient.assumeRole(
    AssumeRoleRequest.builder()
        .roleArn(roleArn)
        .roleSessionName(sessionName)
        .durationSeconds(3600)
        .build()).credentials();

// credentials.accessKeyId()
// credentials.secretAccessKey()
// credentials.sessionToken()
// credentials.expiration()

// Verificar identidade atual
GetCallerIdentityResponse identity = stsClient.getCallerIdentity(
    GetCallerIdentityRequest.builder().build());
// identity.account(), identity.arn(), identity.userId()`} />

        <Callout type="info">
          <p>
            No LocalStack, o <code>AssumeRole</code> retorna credenciais fake mas
            funcionais para testes. O <code>GetCallerIdentity</code> retorna a
            identidade configurada no endpoint local.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* POLICY SIMULATOR */}
        {/* ================================================================ */}
        <h2 id="policy-simulator" className="heading-anchor">
          <a href="#policy-simulator" className="heading-anchor__link" aria-hidden="true">#</a>
          Policy Simulator
        </h2>
        <p>
          O IAM Policy Simulator permite avaliar se um principal (role ou usuario)
          tem permissao para executar determinadas acoes em determinados recursos,
          sem precisar executar a acao de fato. E util para validar policies antes
          de aplica-las em producao.
        </p>
        <CodeBlock language="java" title="PolicySimulatorService.java" code={`// Simular se uma role tem permissao para acoes em recursos
List<EvaluationResult> results = iamClient.simulatePrincipalPolicy(
    SimulatePrincipalPolicyRequest.builder()
        .policySourceArn(policySourceArn)   // ARN da role
        .actionNames(actionNames)            // ex: ["s3:GetObject"]
        .resourceArns(resourceArns)          // ex: ["arn:aws:s3:::*"]
        .build()).evaluationResults();

// Cada EvaluationResult contem:
// - evalActionName()      -> acao avaliada
// - evalDecision()        -> "allowed" ou "implicitDeny"
// - evalResourceName()    -> recurso avaliado`} />

        <Callout type="warning">
          <p>
            O suporte ao Policy Simulator no LocalStack Community pode ser limitado.
            Os resultados podem diferir do comportamento real da AWS. Use para fins
            de aprendizado e validacao de fluxo.
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
          A aplicacao roda na porta <code>8095</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            [<code>POST</code>, <code>/api/iam/roles</code>, 'Criar nova role (body: CreateRoleDto)'],
            [<code>GET</code>, <code>/api/iam/roles</code>, 'Listar todas as roles'],
            [<code>GET</code>, <code>{'/api/iam/roles/{roleName}'}</code>, 'Consultar role por nome'],
            [<code>DELETE</code>, <code>{'/api/iam/roles/{roleName}'}</code>, 'Deletar role por nome'],
            [<code>POST</code>, <code>{'/api/iam/roles/{roleName}/attach-policy?policyArn='}</code>, 'Vincular policy a role'],
            [<code>POST</code>, <code>{'/api/iam/roles/{roleName}/detach-policy?policyArn='}</code>, 'Desvincular policy de role'],
            [<code>GET</code>, <code>{'/api/iam/roles/{roleName}/policies'}</code>, 'Listar policies vinculadas a role'],
            [<code>POST</code>, <code>/api/iam/policies</code>, 'Criar nova policy (body: CreatePolicyDto)'],
            [<code>GET</code>, <code>/api/iam/policies</code>, 'Listar policies locais'],
            [<code>GET</code>, <code>/api/iam/policies/by-arn?policyArn=</code>, 'Consultar policy por ARN'],
            [<code>DELETE</code>, <code>/api/iam/policies?policyArn=</code>, 'Deletar policy por ARN'],
            [<code>GET</code>, <code>/api/iam/policies/version?policyArn=&versionId=</code>, 'Consultar versao do policy document'],
            [<code>POST</code>, <code>/api/iam/assume-role?roleArn=&sessionName=</code>, 'Assumir role e obter credenciais temporarias'],
            [<code>GET</code>, <code>/api/iam/assume-role/caller-identity</code>, 'Verificar identidade atual (GetCallerIdentity)'],
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

# Ver ARNs dos recursos criados
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-iam-policies
./mvnw spring-boot:run

# Roda na porta 8095`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Listar roles
curl "http://localhost:8095/api/iam/roles"

# Criar role
curl -X POST "http://localhost:8095/api/iam/roles" \\
  -H "Content-Type: application/json" \\
  -d '{
    "roleName": "my-test-role",
    "assumeRolePolicyDocument": "{\\"Version\\":\\"2012-10-17\\",\\"Statement\\":[{\\"Effect\\":\\"Allow\\",\\"Principal\\":{\\"AWS\\":\\"*\\"},\\"Action\\":\\"sts:AssumeRole\\"}]}",
    "description": "Role de teste"
  }'

# Criar policy
curl -X POST "http://localhost:8095/api/iam/policies" \\
  -H "Content-Type: application/json" \\
  -d '{
    "policyName": "my-s3-policy",
    "policyDocument": "{\\"Version\\":\\"2012-10-17\\",\\"Statement\\":[{\\"Effect\\":\\"Allow\\",\\"Action\\":[\\"s3:GetObject\\"],\\"Resource\\":\\"*\\"}]}",
    "description": "Policy de leitura S3"
  }'

# Vincular policy a role
curl -X POST "http://localhost:8095/api/iam/roles/my-test-role/attach-policy?policyArn=arn:aws:iam::000000000000:policy/my-s3-policy"

# Listar policies vinculadas
curl "http://localhost:8095/api/iam/roles/my-test-role/policies"

# Assumir role
curl -X POST "http://localhost:8095/api/iam/assume-role?roleArn=arn:aws:iam::000000000000:role/my-test-role&sessionName=test-session"

# Verificar identidade atual
curl "http://localhost:8095/api/iam/assume-role/caller-identity"`} />

        <Callout type="warning">
          <p>
            No LocalStack, o account ID padrao e <code>000000000000</code>. Os ARNs
            seguem o formato{' '}
            <code>arn:aws:iam::000000000000:role/nome-da-role</code> e{' '}
            <code>arn:aws:iam::000000000000:policy/nome-da-policy</code>.
            Use <code>terraform output</code> para obter os ARNs dos recursos
            provisionados.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/iam-policies" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
