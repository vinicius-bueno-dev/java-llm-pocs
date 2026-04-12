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
  { id: 'distributions', title: 'Distributions' },
  { id: 'origins', title: 'Origins' },
  { id: 'invalidations', title: 'Invalidations' },
  { id: 'cache-behaviors', title: 'Cache Behaviors' },
  { id: 'limitacoes', title: 'Limitacoes' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocCloudfrontCdn() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>CloudFront CDN</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora o Amazon CloudFront em uma aplicacao Spring Boot conectada ao
          LocalStack. O objetivo e demonstrar como usar o{' '}
          <strong>AWS SDK for Java v2</strong> para gerenciar distribuicoes CDN,
          configurar origens S3 e executar invalidacoes de cache.
        </p>
        <p>
          As funcionalidades cobertas incluem criacao e gerenciamento de distribuicoes,
          configuracao de buckets S3 como origens, invalidacao de cache por caminhos,
          e configuracao de cache behaviors com TTL e viewer protocol policy.
        </p>

        <Callout type="warning">
          <p>
            <strong>Atencao: Suporte limitado no LocalStack Community.</strong>{' '}
            O CloudFront tem cobertura parcial no LocalStack Community Edition.
            Operacoes como criar, atualizar e deletar distribuicoes podem retornar
            erros ou apresentar comportamento inesperado. Para testes completos,
            utilize AWS real ou LocalStack Pro. Esta POC foi projetada para
            funcionar contra a AWS real, usando o LocalStack apenas como referencia
            para desenvolvimento local.
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
          Dois controllers organizam os endpoints por responsabilidade: distribuicoes e
          invalidacoes.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.cloudfrontcdn/
  config/         CloudFrontConfig (CloudFrontClient + S3Client beans)
  controller/     DistributionController, InvalidationController
  service/        DistributionService, OriginService, InvalidationService
  dto/            Records: CreateDistributionDto, InvalidationDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8094</code> para nao conflitar com
            as demais POCs (S3 na 8080, SQS na 8081, DynamoDB na 8082).
          </p>
        </Callout>

        <p>
          A configuracao dos clientes AWS e feita em <code>CloudFrontConfig</code>,
          que cria beans para <code>CloudFrontClient</code> e <code>S3Client</code>.
          O <code>S3Client</code> e necessario para o <code>OriginService</code>,
          que configura buckets S3 como origens para as distribuicoes.
        </p>
        <CodeBlock language="java" title="CloudFrontConfig.java (trecho)" code={`@Bean
public CloudFrontClient cloudFrontClient(
        @Value("\${aws.endpoint}") String endpoint,
        @Value("\${aws.region}") String region,
        @Value("\${aws.access-key}") String accessKey,
        @Value("\${aws.secret-key}") String secretKey) {
    return CloudFrontClient.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(staticCredentials(accessKey, secretKey))
            .build();
}`} />

        {/* ================================================================ */}
        {/* DISTRIBUTIONS */}
        {/* ================================================================ */}
        <h2 id="distributions" className="heading-anchor">
          <a href="#distributions" className="heading-anchor__link" aria-hidden="true">#</a>
          Distributions
        </h2>
        <p>
          Uma distribuicao CloudFront e o recurso principal do servico. Ela define
          como o conteudo e entregue aos usuarios finais, incluindo a origem dos dados,
          o comportamento de cache e a politica de protocolo do viewer.
        </p>
        <p>
          O <code>DistributionService</code> oferece operacoes de CRUD completo:
          criar, listar, obter por ID, desabilitar e deletar distribuicoes.
        </p>
        <CodeBlock language="java" title="DistributionService.java — Criar distribuicao" code={`public Distribution createDistribution(CreateDistributionDto dto) {
    String callerReference = UUID.randomUUID().toString();
    String originId = "S3-" + dto.originDomainName();

    CreateDistributionRequest request = CreateDistributionRequest.builder()
            .distributionConfig(DistributionConfig.builder()
                    .callerReference(callerReference)
                    .comment(dto.comment())
                    .enabled(dto.enabled())
                    .origins(Origins.builder()
                            .quantity(1)
                            .items(Origin.builder()
                                    .id(originId)
                                    .domainName(dto.originDomainName())
                                    .build())
                            .build())
                    .defaultCacheBehavior(DefaultCacheBehavior.builder()
                            .targetOriginId(originId)
                            .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                            .forwardedValues(fv -> fv
                                    .queryString(false)
                                    .cookies(c -> c.forward("none")))
                            .minTTL(0L)
                            .defaultTTL(86400L)
                            .maxTTL(31536000L)
                            .build())
                    .build())
            .build();

    return cloudFrontClient.createDistribution(request).distribution();
}`} />

        <p>
          Para deletar uma distribuicao, e necessario primeiro desabilita-la e aguardar
          a propagacao. O metodo <code>disableDistribution</code> altera o campo{' '}
          <code>enabled</code> para <code>false</code> e retorna o ETag atualizado,
          que e obrigatorio na chamada de delete.
        </p>
        <CodeBlock language="java" title="DistributionService.java — Desabilitar e deletar" code={`// Desabilitar (passo obrigatorio antes de deletar)
public String disableDistribution(String distributionId) {
    GetDistributionResponse current = getDistribution(distributionId);
    String etag = current.eTag();
    DistributionConfig disabledConfig = current.distribution()
            .distributionConfig().toBuilder()
            .enabled(false)
            .build();

    cloudFrontClient.updateDistribution(
            UpdateDistributionRequest.builder()
                    .id(distributionId)
                    .ifMatch(etag)
                    .distributionConfig(disabledConfig)
                    .build());

    return getDistribution(distributionId).eTag();
}

// Deletar (distribuicao deve estar desabilitada + status Deployed)
public void deleteDistribution(String distributionId, String etag) {
    cloudFrontClient.deleteDistribution(
            DeleteDistributionRequest.builder()
                    .id(distributionId)
                    .ifMatch(etag)
                    .build());
}`} />

        <Callout type="info">
          <p>
            O <code>callerReference</code> e um identificador unico (UUID) usado pela AWS
            para garantir idempotencia na criacao de distribuicoes. O <code>ETag</code> funciona
            como controle de concorrencia otimista nas operacoes de update e delete.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ORIGINS */}
        {/* ================================================================ */}
        <h2 id="origins" className="heading-anchor">
          <a href="#origins" className="heading-anchor__link" aria-hidden="true">#</a>
          Origins
        </h2>
        <p>
          Origens sao os servidores de onde o CloudFront busca o conteudo original.
          Nesta POC, utilizamos buckets S3 como origem. O <code>OriginService</code>{' '}
          fornece operacoes para configurar buckets e listar as origens de uma distribuicao.
        </p>
        <CodeBlock language="java" title="OriginService.java — Configurar origem S3" code={`public Map<String, String> setupS3Origin(String bucketName) {
    boolean created = false;

    if (!bucketExists(bucketName)) {
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(bucketName)
                .build());
        created = true;
    }

    // Formato padrao do domain name S3 para uso como origem CloudFront
    String domainName = bucketName + ".s3.amazonaws.com";

    return Map.of(
            "bucketName", bucketName,
            "domainName", domainName,
            "created", String.valueOf(created));
}`} />

        <p>
          O metodo <code>setupS3Origin</code> verifica se o bucket existe (via{' '}
          <code>HeadBucket</code>), cria-o se necessario, e retorna o domain name
          no formato <code>bucket-name.s3.amazonaws.com</code>. Esse domain name
          deve ser usado como <code>originDomainName</code> ao criar a distribuicao.
        </p>
        <CodeBlock language="java" title="OriginService.java — Listar origens" code={`public List<Origin> listOrigins(String distributionId) {
    GetDistributionResponse response = cloudFrontClient.getDistribution(
            GetDistributionRequest.builder()
                    .id(distributionId)
                    .build());

    return response.distribution()
            .distributionConfig()
            .origins()
            .items();
}`} />

        {/* ================================================================ */}
        {/* INVALIDATIONS */}
        {/* ================================================================ */}
        <h2 id="invalidations" className="heading-anchor">
          <a href="#invalidations" className="heading-anchor__link" aria-hidden="true">#</a>
          Invalidations
        </h2>
        <p>
          Invalidacoes forcam o CloudFront a buscar novos objetos na origem,
          ignorando o cache existente. Sao uteis apos atualizar conteudo no S3
          e precisar que a mudanca se reflita imediatamente para os usuarios.
        </p>
        <p>
          O <code>InvalidationService</code> permite criar invalidacoes por lista de
          caminhos, consultar o status de uma invalidacao e listar todas as
          invalidacoes de uma distribuicao.
        </p>
        <CodeBlock language="java" title="InvalidationService.java — Criar invalidacao" code={`public CreateInvalidationResponse createInvalidation(InvalidationDto dto) {
    String callerReference = UUID.randomUUID().toString();

    CreateInvalidationRequest request = CreateInvalidationRequest.builder()
            .distributionId(dto.distributionId())
            .invalidationBatch(batch -> batch
                    .callerReference(callerReference)
                    .paths(Paths.builder()
                            .quantity(dto.paths().size())
                            .items(dto.paths())
                            .build()))
            .build();

    return cloudFrontClient.createInvalidation(request);
}`} />

        <p>
          O DTO <code>InvalidationDto</code> recebe o ID da distribuicao e uma lista
          de caminhos a invalidar. Os caminhos suportam wildcards — por exemplo,{' '}
          <code>/images/*</code> invalida todos os objetos dentro do prefixo{' '}
          <code>/images/</code>.
        </p>
        <CodeBlock language="json" title="Exemplo de payload para invalidacao" code={`{
  "distributionId": "EDFDVBD6EXAMPLE",
  "paths": ["/index.html", "/images/*", "/css/style.css"]
}`} />

        <Callout type="tip">
          <p>
            Na AWS real, as primeiras 1.000 invalidacoes por mes sao gratuitas.
            Use o wildcard <code>/*</code> com cautela — ele invalida todos os objetos
            da distribuicao e conta como uma unica invalidacao.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* CACHE BEHAVIORS */}
        {/* ================================================================ */}
        <h2 id="cache-behaviors" className="heading-anchor">
          <a href="#cache-behaviors" className="heading-anchor__link" aria-hidden="true">#</a>
          Cache Behaviors
        </h2>
        <p>
          Cache behaviors definem como o CloudFront processa as requisicoes para
          diferentes padroes de URL. Cada distribuicao tem um{' '}
          <strong>default cache behavior</strong> obrigatorio, e pode ter cache behaviors
          adicionais para caminhos especificos.
        </p>
        <p>
          Nesta POC, o default cache behavior e configurado na criacao da distribuicao
          com as seguintes propriedades:
        </p>
        <Table
          headers={['Propriedade', 'Valor', 'Descricao']}
          rows={[
            [<code>viewerProtocolPolicy</code>, 'REDIRECT_TO_HTTPS', 'Redireciona requisicoes HTTP para HTTPS'],
            [<code>queryString</code>, 'false', 'Nao encaminha query strings para a origem'],
            [<code>cookies</code>, 'none', 'Nao encaminha cookies para a origem'],
            [<code>minTTL</code>, '0s', 'Tempo minimo de cache (respeita headers da origem)'],
            [<code>defaultTTL</code>, '86400s (24h)', 'TTL padrao quando a origem nao envia Cache-Control'],
            [<code>maxTTL</code>, '31536000s (1 ano)', 'Tempo maximo de cache independente dos headers'],
          ]}
        />

        <CodeBlock language="java" title="Default cache behavior na criacao" code={`DefaultCacheBehavior.builder()
    .targetOriginId(originId)
    .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
    .forwardedValues(fv -> fv
            .queryString(false)
            .cookies(c -> c.forward("none")))
    .minTTL(0L)
    .defaultTTL(86400L)     // 24 horas
    .maxTTL(31536000L)      // 1 ano
    .build()`} />

        <Callout type="info">
          <p>
            O <code>ForwardedValues</code> e considerado legacy pela AWS. Em producao,
            recomenda-se usar <strong>Cache Policies</strong> e{' '}
            <strong>Origin Request Policies</strong> para maior controle e reutilizacao.
            Nesta POC, usamos <code>ForwardedValues</code> por simplicidade didatica.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* LIMITACOES */}
        {/* ================================================================ */}
        <h2 id="limitacoes" className="heading-anchor">
          <a href="#limitacoes" className="heading-anchor__link" aria-hidden="true">#</a>
          Limitacoes do LocalStack Community
        </h2>
        <p>
          O CloudFront e um dos servicos com suporte mais limitado no LocalStack
          Community Edition. Abaixo estao as principais limitacoes conhecidas:
        </p>
        <Table
          headers={['Funcionalidade', 'Status', 'Observacao']}
          rows={[
            ['Criar distribuicao', 'Parcial', 'A API aceita a chamada, mas a distribuicao nao funciona como CDN real'],
            ['Listar distribuicoes', 'Parcial', 'Retorna distribuicoes criadas localmente'],
            ['Obter distribuicao por ID', 'Parcial', 'Retorna os dados salvos localmente'],
            ['Atualizar distribuicao', 'Parcial', 'Pode apresentar erros ou comportamento inesperado'],
            ['Deletar distribuicao', 'Parcial', 'Aceita a chamada, mas sem validacao completa de pre-condicoes'],
            ['Invalidacoes', 'Parcial', 'A API aceita, mas nao ha cache real para invalidar'],
            ['Edge locations / CDN real', 'Nao suportado', 'Nao ha distribuicao de conteudo via edge locations'],
            ['Signed URLs / Cookies', 'Nao suportado', 'Funcionalidades de seguranca avancada nao disponiveis'],
            ['Lambda@Edge', 'Nao suportado', 'Requer LocalStack Pro'],
          ]}
        />

        <Callout type="warning">
          <p>
            Para testar o fluxo completo de CDN (distribuicao real de conteudo via edge
            locations, HTTPS com certificados ACM, signed URLs), e necessario usar a
            AWS real ou o LocalStack Pro. Esta POC foca na interacao com a API do
            CloudFront, nao na funcionalidade de CDN em si.
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
          O prefixo base e <code>/api/cloudfront</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            ['POST', <code>/api/cloudfront/distributions</code>, 'Criar uma nova distribuicao'],
            ['GET', <code>/api/cloudfront/distributions</code>, 'Listar todas as distribuicoes'],
            ['GET', <code>/api/cloudfront/distributions/{'{distributionId}'}</code>, 'Obter detalhes de uma distribuicao'],
            ['POST', <code>/api/cloudfront/distributions/{'{distributionId}'}/disable</code>, 'Desabilitar uma distribuicao'],
            ['DELETE', <code>/api/cloudfront/distributions/{'{distributionId}'}?etag=...</code>, 'Deletar uma distribuicao desabilitada'],
            ['POST', <code>/api/cloudfront/distributions/origins/s3?bucketName=...</code>, 'Configurar bucket S3 como origem'],
            ['GET', <code>/api/cloudfront/distributions/{'{distributionId}'}/origins</code>, 'Listar origens de uma distribuicao'],
            ['POST', <code>/api/cloudfront/invalidations</code>, 'Criar invalidacao de cache'],
            ['GET', <code>/api/cloudfront/invalidations/{'{distributionId}'}/{'{invalidationId}'}</code>, 'Obter status de uma invalidacao'],
            ['GET', <code>/api/cloudfront/invalidations/{'{distributionId}'}</code>, 'Listar invalidacoes de uma distribuicao'],
          ]}
        />

        <h3>DTOs utilizados</h3>
        <CodeBlock language="java" title="CreateDistributionDto.java" code={`public record CreateDistributionDto(
    String originDomainName,  // ex.: "meu-bucket.s3.amazonaws.com"
    String comment,           // descricao da distribuicao
    boolean enabled           // se deve iniciar habilitada
) {}`} />

        <CodeBlock language="java" title="InvalidationDto.java" code={`public record InvalidationDto(
    String distributionId,    // ID da distribuicao alvo
    List<String> paths        // ex.: ["/index.html", "/images/*"]
) {}`} />

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
        <CodeBlock language="bash" code={`cd pocs/poc-cloudfront-cdn
./mvnw spring-boot:run

# Roda na porta 8094`} />

        <h3>3. Testar com curl</h3>
        <CodeBlock language="bash" title="Configurar origem S3" code={`# Configurar um bucket como origem
curl -X POST "http://localhost:8094/api/cloudfront/distributions/origins/s3?bucketName=meu-site"

# Resposta: {"bucketName":"meu-site","domainName":"meu-site.s3.amazonaws.com","created":"true"}`} />

        <CodeBlock language="bash" title="Criar distribuicao" code={`# Criar distribuicao apontando para a origem S3
curl -X POST "http://localhost:8094/api/cloudfront/distributions" \\
  -H "Content-Type: application/json" \\
  -d '{
    "originDomainName": "meu-site.s3.amazonaws.com",
    "comment": "Distribuicao de teste",
    "enabled": true
  }'`} />

        <CodeBlock language="bash" title="Listar e consultar distribuicoes" code={`# Listar todas as distribuicoes
curl "http://localhost:8094/api/cloudfront/distributions"

# Obter detalhes de uma distribuicao
curl "http://localhost:8094/api/cloudfront/distributions/DISTRIBUTION_ID"

# Listar origens de uma distribuicao
curl "http://localhost:8094/api/cloudfront/distributions/DISTRIBUTION_ID/origins"`} />

        <CodeBlock language="bash" title="Invalidar cache" code={`# Criar invalidacao
curl -X POST "http://localhost:8094/api/cloudfront/invalidations" \\
  -H "Content-Type: application/json" \\
  -d '{
    "distributionId": "DISTRIBUTION_ID",
    "paths": ["/index.html", "/images/*"]
  }'

# Listar invalidacoes de uma distribuicao
curl "http://localhost:8094/api/cloudfront/invalidations/DISTRIBUTION_ID"

# Obter status de uma invalidacao
curl "http://localhost:8094/api/cloudfront/invalidations/DISTRIBUTION_ID/INVALIDATION_ID"`} />

        <CodeBlock language="bash" title="Desabilitar e deletar distribuicao" code={`# Desabilitar (retorna o etag necessario para delete)
curl -X POST "http://localhost:8094/api/cloudfront/distributions/DISTRIBUTION_ID/disable"

# Deletar (usar o etag retornado no passo anterior)
curl -X DELETE "http://localhost:8094/api/cloudfront/distributions/DISTRIBUTION_ID?etag=ETAG_VALUE"`} />

        <Callout type="warning">
          <p>
            Substitua <code>DISTRIBUTION_ID</code>, <code>INVALIDATION_ID</code> e{' '}
            <code>ETAG_VALUE</code> pelos valores reais retornados pelas chamadas anteriores.
            Lembre-se de que o CloudFront no LocalStack Community pode retornar erros
            em algumas dessas operacoes.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/cloudfront-cdn" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
