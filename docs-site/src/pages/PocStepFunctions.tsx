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
  { id: 'state-machines', title: 'State Machines' },
  { id: 'executions', title: 'Execucoes' },
  { id: 'asl-definition', title: 'Definicao ASL' },
  { id: 'parallel-choice-wait', title: 'Parallel, Choice e Wait' },
  { id: 'error-handling', title: 'Tratamento de Erros' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocStepFunctions() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>Step Functions</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora o <strong>AWS Step Functions</strong> em uma aplicacao
          Spring Boot conectada ao LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> (SfnClient) para criar e gerenciar
          state machines, iniciar e monitorar execucoes, e trabalhar com definicoes
          ASL (Amazon States Language).
        </p>
        <p>
          As funcionalidades cobertas incluem criacao e gerenciamento de state machines,
          execucao de workflows com diferentes tipos de estados (Pass, Choice, Parallel,
          Wait, Fail), consulta de historico de execucoes e interrupcao de execucoes
          em andamento.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O Step Functions tem
            boa cobertura no tier gratuito, incluindo estados Parallel, Choice,
            Wait e Fail.
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
          A aplicacao segue a estrutura padrao das POCs: camadas de{' '}
          <strong>Controller</strong> e <strong>Service</strong> com injecao por construtor.
          Ha dois pares controller/service — um para gerenciar state machines e outro
          para gerenciar execucoes.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.stepfunctions/
  config/         StepFunctionsConfig (SfnClient + LambdaClient beans)
  controller/     StateMachineController, ExecutionController
  service/        StateMachineService, ExecutionService
  dto/            Records: CreateStateMachineDto, StartExecutionDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8089</code> para nao conflitar com
            outras POCs. A configuracao esta em <code>application.yml</code>.
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
          O modulo Terraform em <code>infra/localstack/modules/stepfunctions</code>{' '}
          provisiona uma IAM role para execucao e uma state machine de exemplo
          que demonstra um workflow de processamento de pedidos com estados Parallel,
          Choice e Wait.
        </p>
        <Table
          headers={['Recurso', 'Nome', 'Proposito']}
          rows={[
            [<code>aws_iam_role</code>, 'poc-sfn-role', 'IAM role para execucao de state machines (AssumeRole para states.amazonaws.com)'],
            [<code>aws_sfn_state_machine</code>, 'poc-order-workflow', 'Workflow de processamento de pedidos com Parallel, Choice e Wait'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/stepfunctions/main.tf (trecho)" code={`resource "aws_iam_role" "sfn_role" {
  name = "\${var.project_name}-\${var.environment}-poc-sfn-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "states.amazonaws.com" }
    }]
  })
}

resource "aws_sfn_state_machine" "order_workflow" {
  name     = "\${var.project_name}-\${var.environment}-poc-order-workflow"
  role_arn = aws_iam_role.sfn_role.arn
  definition = jsonencode({ ... })  # Definicao ASL completa
}`} />

        {/* ================================================================ */}
        {/* STATE MACHINES */}
        {/* ================================================================ */}
        <h2 id="state-machines" className="heading-anchor">
          <a href="#state-machines" className="heading-anchor__link" aria-hidden="true">#</a>
          State Machines
        </h2>
        <p>
          O <code>StateMachineService</code> expoe operacoes de CRUD sobre state machines
          via <code>SfnClient</code>. E possivel criar state machines com definicoes ASL
          customizadas, listar todas as machines existentes, descrever detalhes (incluindo
          a definicao ASL atual) e deletar.
        </p>
        <CodeBlock language="java" title="StateMachineService.java" code={`// Criar uma nova state machine com definicao ASL
CreateStateMachineResponse response = sfnClient.createStateMachine(
    CreateStateMachineRequest.builder()
        .name(dto.name())
        .definition(dto.definition())  // JSON ASL
        .roleArn(dto.roleArn())
        .type(StateMachineType.STANDARD)
        .build());
// Retorna: stateMachineArn, creationDate

// Listar todas as state machines
List<StateMachineListItem> items = sfnClient.listStateMachines(
    ListStateMachinesRequest.builder().build()).stateMachines();
// Cada item: name, stateMachineArn, type, creationDate

// Descrever uma state machine pelo ARN
DescribeStateMachineResponse desc = sfnClient.describeStateMachine(
    DescribeStateMachineRequest.builder()
        .stateMachineArn(arn).build());
// Retorna: name, arn, status, definition, roleArn, type, creationDate

// Deletar uma state machine
sfnClient.deleteStateMachine(
    DeleteStateMachineRequest.builder()
        .stateMachineArn(arn).build());`} />

        <p>
          O DTO de criacao e um record Java com tres campos:
        </p>
        <CodeBlock language="java" title="CreateStateMachineDto.java" code={`public record CreateStateMachineDto(
    String name,        // nome unico da state machine
    String definition,  // definicao ASL em JSON
    String roleArn      // ARN do IAM role (fake no LocalStack)
) {}`} />

        {/* ================================================================ */}
        {/* EXECUTIONS */}
        {/* ================================================================ */}
        <h2 id="executions" className="heading-anchor">
          <a href="#executions" className="heading-anchor__link" aria-hidden="true">#</a>
          Execucoes
        </h2>
        <p>
          O <code>ExecutionService</code> permite iniciar execucoes de state machines,
          consultar status, listar execucoes de uma machine, parar execucoes em
          andamento e consultar o historico completo de eventos.
        </p>
        <CodeBlock language="java" title="ExecutionService.java" code={`// Iniciar uma execucao
StartExecutionResponse response = sfnClient.startExecution(
    StartExecutionRequest.builder()
        .stateMachineArn(dto.stateMachineArn())
        .input(dto.input())       // JSON de entrada
        .name(dto.name())         // nome opcional (deve ser unico)
        .build());
// Retorna: executionArn, startDate

// Descrever uma execucao pelo ARN
DescribeExecutionResponse desc = sfnClient.describeExecution(
    DescribeExecutionRequest.builder()
        .executionArn(executionArn).build());
// Retorna: executionArn, stateMachineArn, status, startDate,
//          stopDate, input, output

// Listar execucoes de uma state machine
List<ExecutionListItem> items = sfnClient.listExecutions(
    ListExecutionsRequest.builder()
        .stateMachineArn(stateMachineArn).build()).executions();

// Parar uma execucao em andamento
sfnClient.stopExecution(StopExecutionRequest.builder()
    .executionArn(executionArn)
    .cause("Motivo da interrupcao")
    .error("CodigoErro")
    .build());

// Consultar historico de eventos da execucao
GetExecutionHistoryResponse history = sfnClient.getExecutionHistory(
    GetExecutionHistoryRequest.builder()
        .executionArn(executionArn).build());
// Cada evento: id, type, timestamp, previousEventId`} />

        <p>
          O DTO de execucao e um record com tres campos:
        </p>
        <CodeBlock language="java" title="StartExecutionDto.java" code={`public record StartExecutionDto(
    String stateMachineArn,  // ARN da state machine a executar
    String name,             // nome opcional da execucao
    String input             // JSON de entrada para a execucao
) {}`} />

        <Callout type="tip">
          <p>
            O campo <code>name</code> da execucao e opcional. Quando omitido, o
            Step Functions gera um nome automaticamente. Se fornecido, deve ser
            unico para a state machine.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ASL DEFINITION */}
        {/* ================================================================ */}
        <h2 id="asl-definition" className="heading-anchor">
          <a href="#asl-definition" className="heading-anchor__link" aria-hidden="true">#</a>
          Definicao ASL
        </h2>
        <p>
          A <strong>Amazon States Language (ASL)</strong> e um JSON que define os estados
          e transicoes de um workflow. O modulo Terraform desta POC cria uma state machine
          de exemplo que modela um fluxo de processamento de pedidos com 6 estados.
        </p>
        <CodeBlock language="json" title="Definicao ASL — Order Processing Workflow" code={`{
  "Comment": "POC order processing workflow with Parallel, Choice and Wait",
  "StartAt": "ValidateOrder",
  "States": {
    "ValidateOrder": {
      "Type": "Pass",
      "Result": { "validated": true },
      "Next": "CheckInventory"
    },
    "CheckInventory": {
      "Type": "Choice",
      "Choices": [{
        "Variable": "$.validated",
        "BooleanEquals": true,
        "Next": "ProcessParallel"
      }],
      "Default": "OrderFailed"
    },
    "ProcessParallel": {
      "Type": "Parallel",
      "Branches": [
        {
          "StartAt": "ChargePayment",
          "States": {
            "ChargePayment": {
              "Type": "Pass",
              "Result": { "payment": "charged" },
              "End": true
            }
          }
        },
        {
          "StartAt": "ReserveInventory",
          "States": {
            "ReserveInventory": {
              "Type": "Pass",
              "Result": { "inventory": "reserved" },
              "End": true
            }
          }
        }
      ],
      "Next": "WaitForConfirmation"
    },
    "WaitForConfirmation": {
      "Type": "Wait",
      "Seconds": 1,
      "Next": "OrderComplete"
    },
    "OrderComplete": {
      "Type": "Pass",
      "Result": { "status": "completed" },
      "End": true
    },
    "OrderFailed": {
      "Type": "Fail",
      "Error": "OrderValidationFailed",
      "Cause": "Order did not pass validation"
    }
  }
}`} />

        <Callout type="info">
          <p>
            A definicao ASL e passada como string JSON no campo <code>definition</code>
            ao criar uma state machine via API. No Terraform, usamos <code>jsonencode()</code>
            para gerar o JSON a partir de HCL.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* PARALLEL, CHOICE E WAIT */}
        {/* ================================================================ */}
        <h2 id="parallel-choice-wait" className="heading-anchor">
          <a href="#parallel-choice-wait" className="heading-anchor__link" aria-hidden="true">#</a>
          Parallel, Choice e Wait
        </h2>
        <p>
          O workflow de exemplo utiliza tres tipos de estados avancados que
          demonstram o poder do Step Functions:
        </p>

        <h3>Choice — Decisao condicional</h3>
        <p>
          O estado <code>CheckInventory</code> avalia a variavel <code>$.validated</code>.
          Se for <code>true</code>, segue para o processamento paralelo. Caso contrario,
          vai para <code>OrderFailed</code>. O campo <code>Default</code> define o caminho
          quando nenhuma regra e satisfeita.
        </p>
        <CodeBlock language="json" title="Estado Choice" code={`{
  "CheckInventory": {
    "Type": "Choice",
    "Choices": [{
      "Variable": "$.validated",
      "BooleanEquals": true,
      "Next": "ProcessParallel"
    }],
    "Default": "OrderFailed"
  }
}`} />

        <h3>Parallel — Execucao paralela</h3>
        <p>
          O estado <code>ProcessParallel</code> executa dois branches simultaneamente:
          cobranca de pagamento e reserva de inventario. Ambos devem concluir
          antes de avancar para o proximo estado. O resultado e um array com a saida
          de cada branch.
        </p>
        <CodeBlock language="json" title="Estado Parallel" code={`{
  "ProcessParallel": {
    "Type": "Parallel",
    "Branches": [
      {
        "StartAt": "ChargePayment",
        "States": {
          "ChargePayment": {
            "Type": "Pass",
            "Result": { "payment": "charged" },
            "End": true
          }
        }
      },
      {
        "StartAt": "ReserveInventory",
        "States": {
          "ReserveInventory": {
            "Type": "Pass",
            "Result": { "inventory": "reserved" },
            "End": true
          }
        }
      }
    ],
    "Next": "WaitForConfirmation"
  }
}`} />

        <h3>Wait — Pausa temporizada</h3>
        <p>
          O estado <code>WaitForConfirmation</code> aguarda 1 segundo antes de
          prosseguir. Util para simular esperas por confirmacao, cooldowns ou
          rate limiting. Suporta <code>Seconds</code>, <code>Timestamp</code>,{' '}
          <code>SecondsPath</code> e <code>TimestampPath</code>.
        </p>
        <CodeBlock language="json" title="Estado Wait" code={`{
  "WaitForConfirmation": {
    "Type": "Wait",
    "Seconds": 1,
    "Next": "OrderComplete"
  }
}`} />

        {/* ================================================================ */}
        {/* ERROR HANDLING */}
        {/* ================================================================ */}
        <h2 id="error-handling" className="heading-anchor">
          <a href="#error-handling" className="heading-anchor__link" aria-hidden="true">#</a>
          Tratamento de Erros
        </h2>
        <p>
          O workflow demonstra tratamento de erros com o estado <code>Fail</code>,
          que encerra a execucao com um codigo de erro e uma causa descritiva.
          Alem disso, a API permite interromper execucoes em andamento via{' '}
          <code>stopExecution</code>.
        </p>
        <CodeBlock language="json" title="Estado Fail" code={`{
  "OrderFailed": {
    "Type": "Fail",
    "Error": "OrderValidationFailed",
    "Cause": "Order did not pass validation"
  }
}`} />

        <p>
          Para interromper uma execucao em andamento programaticamente:
        </p>
        <CodeBlock language="java" title="Parar execucao via API" code={`// Parar uma execucao com motivo e codigo de erro
sfnClient.stopExecution(StopExecutionRequest.builder()
    .executionArn(executionArn)
    .cause("Cancelado pelo usuario")
    .error("UserCancellation")
    .build());
// Retorna: stopDate, status = "ABORTED"`} />

        <Callout type="warning">
          <p>
            No LocalStack, execucoes com estados <code>Wait</code> podem completar
            mais rapidamente do que na AWS real. Use o endpoint{' '}
            <code>/executions/describe</code> para verificar o status final.
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
          A aplicacao roda na porta <code>8089</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            [<code>POST</code>, <code>/api/stepfunctions/machines</code>, 'Criar state machine (body: name, definition, roleArn)'],
            [<code>GET</code>, <code>/api/stepfunctions/machines</code>, 'Listar todas as state machines'],
            [<code>GET</code>, <code>/api/stepfunctions/machines/describe</code>, 'Descrever state machine (param: stateMachineArn)'],
            [<code>DELETE</code>, <code>/api/stepfunctions/machines</code>, 'Deletar state machine (param: stateMachineArn)'],
            [<code>POST</code>, <code>/api/stepfunctions/executions</code>, 'Iniciar execucao (body: stateMachineArn, name, input)'],
            [<code>GET</code>, <code>/api/stepfunctions/executions/describe</code>, 'Descrever execucao (param: executionArn)'],
            [<code>GET</code>, <code>/api/stepfunctions/executions</code>, 'Listar execucoes (param: stateMachineArn)'],
            [<code>POST</code>, <code>/api/stepfunctions/executions/stop</code>, 'Parar execucao (params: executionArn, cause, error)'],
            [<code>GET</code>, <code>/api/stepfunctions/executions/history</code>, 'Historico de eventos (param: executionArn)'],
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

# Ver ARN da state machine e role criados
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-step-functions
./mvnw spring-boot:run

# Roda na porta 8089`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Listar state machines
curl "http://localhost:8089/api/stepfunctions/machines"

# Criar uma state machine via API
curl -X POST "http://localhost:8089/api/stepfunctions/machines" \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "meu-workflow",
    "definition": "{\\"StartAt\\":\\"Hello\\",\\"States\\":{\\"Hello\\":{\\"Type\\":\\"Pass\\",\\"Result\\":\\"World\\",\\"End\\":true}}}",
    "roleArn": "arn:aws:iam::000000000000:role/fake-role"
  }'

# Descrever state machine
curl "http://localhost:8089/api/stepfunctions/machines/describe?stateMachineArn=<ARN>"

# Iniciar execucao
curl -X POST "http://localhost:8089/api/stepfunctions/executions" \\
  -H "Content-Type: application/json" \\
  -d '{
    "stateMachineArn": "<ARN>",
    "name": "execucao-teste",
    "input": "{\\"orderId\\": \\"123\\"}"
  }'

# Descrever execucao (ver status e output)
curl "http://localhost:8089/api/stepfunctions/executions/describe?executionArn=<EXEC_ARN>"

# Listar execucoes de uma state machine
curl "http://localhost:8089/api/stepfunctions/executions?stateMachineArn=<ARN>"

# Consultar historico de eventos
curl "http://localhost:8089/api/stepfunctions/executions/history?executionArn=<EXEC_ARN>"

# Parar execucao em andamento
curl -X POST "http://localhost:8089/api/stepfunctions/executions/stop?executionArn=<EXEC_ARN>&cause=teste&error=ManualStop"`} />

        <Callout type="warning">
          <p>
            Substitua <code>{'<ARN>'}</code> e <code>{'<EXEC_ARN>'}</code> pelos
            ARNs reais. Use <code>terraform output</code> para obter o ARN da
            state machine provisionada ou <code>/api/stepfunctions/machines</code>{' '}
            para listar todas.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/step-functions" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
