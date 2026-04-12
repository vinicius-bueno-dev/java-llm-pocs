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
  { id: 'rest-api', title: 'REST APIs' },
  { id: 'resources', title: 'Resources' },
  { id: 'methods', title: 'Methods' },
  { id: 'integrations', title: 'Integrations' },
  { id: 'deployments', title: 'Deployments' },
  { id: 'stages', title: 'Stages' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocApiGateway() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>API Gateway</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora o Amazon API Gateway (REST APIs) em uma aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para gerenciar o ciclo de vida completo
          de uma REST API: criacao, configuracao de resources, methods, integrations,
          deployments e stages.
        </p>
        <p>
          As funcionalidades cobertas incluem criacao e listagem de REST APIs,
          gerenciamento de resources hierarquicos, configuracao de metodos HTTP
          com tipos de autorizacao, integracao com backends (HTTP, Lambda, Mock),
          criacao de deployments e gerenciamento de stages.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O API Gateway tem
            boa cobertura no tier gratuito, permitindo criar e testar REST APIs
            completas sem custo.
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
          Dois controllers dividem as responsabilidades: um para REST APIs, resources,
          methods e integrations, e outro para deployments e stages.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.apigateway/
  config/         ApiGatewayConfig (ApiGatewayClient + LambdaClient beans)
  controller/     RestApiController, DeploymentController
  service/        RestApiService, DeploymentService
  dto/            Records: CreateApiDto, CreateResourceDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8090</code> para nao conflitar com
            as demais POCs do projeto.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* REST API */}
        {/* ================================================================ */}
        <h2 id="rest-api" className="heading-anchor">
          <a href="#rest-api" className="heading-anchor__link" aria-hidden="true">#</a>
          REST APIs
        </h2>
        <p>
          O ponto de partida do API Gateway e a criacao de uma REST API.
          Cada API recebe um identificador unico (<code>restApiId</code>) e
          ja vem com um resource raiz (<code>/</code>) automaticamente.
          A POC permite criar, listar, consultar e deletar REST APIs.
        </p>
        <CodeBlock language="java" title="RestApiService.java" code={`// Criar uma REST API
apiGatewayClient.createRestApi(CreateRestApiRequest.builder()
    .name(dto.name())
    .description(dto.description())
    .build());

// Listar todas as REST APIs
apiGatewayClient.getRestApis();

// Consultar uma REST API por ID
apiGatewayClient.getRestApi(GetRestApiRequest.builder()
    .restApiId(restApiId)
    .build());

// Deletar uma REST API
apiGatewayClient.deleteRestApi(DeleteRestApiRequest.builder()
    .restApiId(restApiId)
    .build());`} />

        <Callout type="info">
          <p>
            O DTO <code>CreateApiDto</code> e um Java record com dois campos:{' '}
            <code>name</code> e <code>description</code>. Ambos sao enviados
            como JSON no corpo da requisicao.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* RESOURCES */}
        {/* ================================================================ */}
        <h2 id="resources" className="heading-anchor">
          <a href="#resources" className="heading-anchor__link" aria-hidden="true">#</a>
          Resources
        </h2>
        <p>
          Resources representam os caminhos (paths) da API, organizados em
          uma arvore hierarquica. Toda REST API ja possui um resource raiz{' '}
          <code>/</code> com seu proprio <code>resourceId</code>. Novos resources
          sao criados como filhos de um resource existente, formando paths
          como <code>/users</code>, <code>/users/{'{id}'}</code>, etc.
        </p>
        <CodeBlock language="java" title="RestApiService.java" code={`// Listar resources de uma API
apiGatewayClient.getResources(GetResourcesRequest.builder()
    .restApiId(restApiId)
    .build());

// Criar um novo resource (ex: /users)
apiGatewayClient.createResource(CreateResourceRequest.builder()
    .restApiId(dto.restApiId())
    .parentId(dto.parentId())    // ID do resource pai (raiz ou outro)
    .pathPart(dto.pathPart())    // Segmento do path (ex: "users")
    .build());`} />

        <Callout type="tip">
          <p>
            Para criar um resource filho do raiz, primeiro liste os resources
            da API para obter o <code>id</code> do resource <code>/</code>,
            e use-o como <code>parentId</code>.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* METHODS */}
        {/* ================================================================ */}
        <h2 id="methods" className="heading-anchor">
          <a href="#methods" className="heading-anchor__link" aria-hidden="true">#</a>
          Methods
        </h2>
        <p>
          Cada resource pode ter um ou mais metodos HTTP associados (GET, POST,
          PUT, DELETE, etc.). O metodo define como o API Gateway aceita requisicoes
          naquele path, incluindo o tipo de autorizacao. O valor padrao e{' '}
          <code>NONE</code> (sem autorizacao).
        </p>
        <CodeBlock language="java" title="RestApiService.java" code={`// Associar um metodo HTTP a um resource
apiGatewayClient.putMethod(PutMethodRequest.builder()
    .restApiId(restApiId)
    .resourceId(resourceId)
    .httpMethod("GET")              // GET, POST, PUT, DELETE, etc.
    .authorizationType("NONE")      // NONE, AWS_IAM, CUSTOM, COGNITO
    .build());`} />

        <p>
          O endpoint aceita o tipo de autorizacao como query parameter opcional.
          Os tipos suportados pelo API Gateway sao:
        </p>
        <Table
          headers={['Tipo', 'Descricao']}
          rows={[
            [<code>NONE</code>, 'Sem autorizacao — acesso publico'],
            [<code>AWS_IAM</code>, 'Autorizacao via credenciais IAM assinadas (Signature V4)'],
            [<code>CUSTOM</code>, 'Lambda Authorizer personalizado'],
            [<code>COGNITO_USER_POOLS</code>, 'Autorizacao via Amazon Cognito'],
          ]}
        />

        {/* ================================================================ */}
        {/* INTEGRATIONS */}
        {/* ================================================================ */}
        <h2 id="integrations" className="heading-anchor">
          <a href="#integrations" className="heading-anchor__link" aria-hidden="true">#</a>
          Integrations
        </h2>
        <p>
          Apos definir um metodo, e necessario configurar a integracao que
          conecta o API Gateway ao backend. A integracao define o tipo de
          backend (HTTP, Lambda, Mock, etc.) e a URI de destino. O metodo
          HTTP da integracao e sempre <code>POST</code> para invocacoes Lambda.
        </p>
        <CodeBlock language="java" title="RestApiService.java" code={`// Configurar integracao de um metodo com um backend
apiGatewayClient.putIntegration(PutIntegrationRequest.builder()
    .restApiId(restApiId)
    .resourceId(resourceId)
    .httpMethod(httpMethod)
    .type(IntegrationType.AWS_PROXY)   // Tipo da integracao
    .integrationHttpMethod("POST")     // Metodo usado para invocar o backend
    .uri(uri)                          // URI do backend (ARN Lambda, URL HTTP, etc.)
    .build());`} />

        <p>
          Os principais tipos de integracao disponveis sao:
        </p>
        <Table
          headers={['Tipo', 'Descricao']}
          rows={[
            [<code>HTTP</code>, 'Proxy para um endpoint HTTP/HTTPS externo'],
            [<code>HTTP_PROXY</code>, 'Proxy HTTP com passthrough completo de request/response'],
            [<code>AWS</code>, 'Integracao direta com servico AWS (requer mapeamento de templates)'],
            [<code>AWS_PROXY</code>, 'Lambda Proxy — passa o request completo para a funcao Lambda'],
            [<code>MOCK</code>, 'Retorna resposta simulada sem chamar backend'],
          ]}
        />

        <Callout type="tip">
          <p>
            Para integrar com Lambda no LocalStack, use o ARN no formato:{' '}
            <code>arn:aws:apigateway:us-east-1:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-1:000000000000:function:NOME/invocations</code>
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* DEPLOYMENTS */}
        {/* ================================================================ */}
        <h2 id="deployments" className="heading-anchor">
          <a href="#deployments" className="heading-anchor__link" aria-hidden="true">#</a>
          Deployments
        </h2>
        <p>
          Um deployment e um snapshot imutavel da configuracao da API em um
          determinado momento. Para que a API fique acessivel, e necessario
          criar um deployment e associa-lo a um stage. Cada deployment recebe
          um <code>deploymentId</code> unico.
        </p>
        <CodeBlock language="java" title="DeploymentService.java" code={`// Criar um deployment (snapshot da configuracao atual)
apiGatewayClient.createDeployment(CreateDeploymentRequest.builder()
    .restApiId(restApiId)
    .stageName("dev")               // Stage associado automaticamente
    .description("Deploy inicial")
    .build());`} />

        <Callout type="info">
          <p>
            Ao criar um deployment com <code>stageName</code>, o API Gateway
            cria o stage automaticamente se ele ainda nao existir. Caso o stage
            ja exista, ele e atualizado para apontar para o novo deployment.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* STAGES */}
        {/* ================================================================ */}
        <h2 id="stages" className="heading-anchor">
          <a href="#stages" className="heading-anchor__link" aria-hidden="true">#</a>
          Stages
        </h2>
        <p>
          Stages representam ambientes nomeados (ex: <code>dev</code>,{' '}
          <code>staging</code>, <code>prod</code>) que apontam para um
          deployment especifico. A URL final da API inclui o nome do stage:{' '}
          <code>{'https://{restApiId}.execute-api.{region}.amazonaws.com/{stageName}'}</code>.
          E possivel listar, criar e deletar stages independentemente.
        </p>
        <CodeBlock language="java" title="DeploymentService.java" code={`// Listar todos os stages de uma API
apiGatewayClient.getStages(GetStagesRequest.builder()
    .restApiId(restApiId)
    .build());

// Criar um stage apontando para um deployment existente
apiGatewayClient.createStage(CreateStageRequest.builder()
    .restApiId(restApiId)
    .stageName("prod")
    .deploymentId(deploymentId)
    .description("Ambiente de producao")
    .build());

// Deletar um stage
apiGatewayClient.deleteStage(DeleteStageRequest.builder()
    .restApiId(restApiId)
    .stageName("prod")
    .build());`} />

        <Callout type="warning">
          <p>
            No LocalStack, a URL de invocacao do API Gateway segue o
            formato <code>http://localhost:4566/restapis/{'{restApiId}'}/{'{stageName}'}/_user_request_/</code>.
            Consulte a documentacao do LocalStack para detalhes de invocacao.
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
          A tabela abaixo lista todos os endpoints expostos pela POC,
          divididos entre os dois controllers. O prefixo base e{' '}
          <code>/api/gateway</code>.
        </p>

        <h3>RestApiController</h3>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            [<code>POST</code>, <code>/api/gateway/apis</code>, 'Criar uma nova REST API'],
            [<code>GET</code>, <code>/api/gateway/apis</code>, 'Listar todas as REST APIs'],
            [<code>GET</code>, <code>/api/gateway/apis/{'{restApiId}'}</code>, 'Consultar uma REST API por ID'],
            [<code>DELETE</code>, <code>/api/gateway/apis/{'{restApiId}'}</code>, 'Deletar uma REST API'],
            [<code>GET</code>, <code>/api/gateway/apis/{'{restApiId}'}/resources</code>, 'Listar resources de uma API'],
            [<code>POST</code>, <code>/api/gateway/apis/resources</code>, 'Criar um novo resource'],
            [<code>PUT</code>, <code>/api/gateway/apis/{'{restApiId}'}/resources/{'{resourceId}'}/methods/{'{httpMethod}'}</code>, 'Associar metodo HTTP a um resource'],
            [<code>PUT</code>, <code>/api/gateway/apis/{'{restApiId}'}/resources/{'{resourceId}'}/methods/{'{httpMethod}'}/integration</code>, 'Configurar integracao de um metodo'],
          ]}
        />

        <h3>DeploymentController</h3>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            [<code>POST</code>, <code>/api/gateway/deployments/{'{restApiId}'}</code>, 'Criar deployment (query: stageName, description)'],
            [<code>GET</code>, <code>/api/gateway/deployments/{'{restApiId}'}/stages</code>, 'Listar stages de uma API'],
            [<code>POST</code>, <code>/api/gateway/deployments/{'{restApiId}'}/stages</code>, 'Criar stage (query: stageName, deploymentId, description)'],
            [<code>DELETE</code>, <code>/api/gateway/deployments/{'{restApiId}'}/stages/{'{stageName}'}</code>, 'Deletar um stage'],
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
          Siga os passos abaixo para executar a POC localmente.
        </p>

        <h3>1. Subir o LocalStack</h3>
        <CodeBlock language="bash" code={`cd infra && docker-compose up -d

# Verificar se esta pronto
curl http://localhost:4566/_localstack/health`} />

        <h3>2. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-api-gateway
./mvnw spring-boot:run

# Roda na porta 8090`} />

        <h3>3. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Criar uma REST API
curl -X POST "http://localhost:8090/api/gateway/apis" \\
  -H "Content-Type: application/json" \\
  -d '{"name": "minha-api", "description": "API de teste"}'

# Listar REST APIs
curl "http://localhost:8090/api/gateway/apis"

# Consultar uma API (substitua <restApiId>)
curl "http://localhost:8090/api/gateway/apis/<restApiId>"

# Listar resources da API
curl "http://localhost:8090/api/gateway/apis/<restApiId>/resources"

# Criar resource /users (use o parentId do resource raiz)
curl -X POST "http://localhost:8090/api/gateway/apis/resources" \\
  -H "Content-Type: application/json" \\
  -d '{"restApiId": "<restApiId>", "parentId": "<rootResourceId>", "pathPart": "users"}'

# Associar metodo GET ao resource
curl -X PUT "http://localhost:8090/api/gateway/apis/<restApiId>/resources/<resourceId>/methods/GET"

# Configurar integracao MOCK
curl -X PUT "http://localhost:8090/api/gateway/apis/<restApiId>/resources/<resourceId>/methods/GET/integration?type=MOCK&uri=http://example.com"

# Criar deployment no stage dev
curl -X POST "http://localhost:8090/api/gateway/deployments/<restApiId>?stageName=dev&description=Deploy%20inicial"

# Listar stages
curl "http://localhost:8090/api/gateway/deployments/<restApiId>/stages"

# Criar stage prod apontando para deployment existente
curl -X POST "http://localhost:8090/api/gateway/deployments/<restApiId>/stages?stageName=prod&deploymentId=<deploymentId>&description=Producao"

# Deletar stage
curl -X DELETE "http://localhost:8090/api/gateway/deployments/<restApiId>/stages/prod"

# Deletar REST API
curl -X DELETE "http://localhost:8090/api/gateway/apis/<restApiId>"`} />

        <Callout type="warning">
          <p>
            Substitua <code>{'<restApiId>'}</code>, <code>{'<rootResourceId>'}</code>,{' '}
            <code>{'<resourceId>'}</code> e <code>{'<deploymentId>'}</code> pelos
            valores reais retornados pelas chamadas anteriores.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/api-gateway" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
