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
  { id: 'cache-aside', title: 'Padrao Cache-Aside' },
  { id: 'cache-operations', title: 'Operacoes de Cache' },
  { id: 'rate-limiting', title: 'Rate Limiting' },
  { id: 'sessions', title: 'Gerenciamento de Sessoes' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocElasticacheRedis() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>ElastiCache Redis</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora 3 casos de uso classicos do Redis em uma aplicacao
          Spring Boot: <strong>cache de dados</strong> com TTL configuravel,{' '}
          <strong>rate limiting</strong> por sliding-window counter e{' '}
          <strong>gerenciamento de sessoes</strong> com Redis Hashes. O objetivo e
          demonstrar como o <strong>Spring Data Redis</strong> (via{' '}
          <code>StringRedisTemplate</code>) simplifica a integracao com Redis,
          simulando o que seria feito com o Amazon ElastiCache em producao.
        </p>
        <p>
          As funcionalidades cobertas incluem operacoes CRUD de cache (GET, SET, DELETE),
          controle de TTL, busca por pattern de chaves, rate limiting com janela
          deslizante, criacao e extensao de sessoes e armazenamento de atributos
          via Redis Hashes.
        </p>

        <Callout type="warning">
          <p>
            O Amazon ElastiCache <strong>NAO esta disponivel</strong> no LocalStack Community.
            Esta POC utiliza o Redis diretamente via Docker, sem depender do LocalStack.
            Em producao, o ElastiCache gerencia clusters Redis/Memcached com alta
            disponibilidade, replicas e failover automatico -- funcionalidades que nao
            sao replicadas aqui.
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
          Cada caso de uso (cache, rate limiter, sessions) tem seu proprio par
          controller/service. O acesso ao Redis e feito exclusivamente via{' '}
          <code>StringRedisTemplate</code> do Spring Data Redis.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.elasticacheredis/
  controller/     CacheController, RateLimiterController, SessionController
  service/        CacheService, RateLimiterService, SessionService
  dto/            CacheEntryDto (record com key, value, ttlSeconds)`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8091</code> para nao conflitar com as
            demais POCs. O Redis e acessado em <code>localhost:6379</code> (padrao).
          </p>
        </Callout>

        <CodeBlock language="yaml" title="application.yml" code={`server:
  port: 8091

spring:
  application:
    name: poc-elasticache-redis
  data:
    redis:
      host: localhost
      port: 6379`} />

        {/* ================================================================ */}
        {/* CACHE-ASIDE */}
        {/* ================================================================ */}
        <h2 id="cache-aside" className="heading-anchor">
          <a href="#cache-aside" className="heading-anchor__link" aria-hidden="true">#</a>
          Padrao Cache-Aside
        </h2>
        <p>
          O padrao <strong>Cache-Aside</strong> (ou Lazy Loading) e a estrategia mais
          comum ao usar Redis como cache. O fluxo funciona assim:
        </p>
        <ol>
          <li>A aplicacao tenta ler o dado do cache (Redis)</li>
          <li>Se encontrar (<strong>cache hit</strong>), retorna diretamente</li>
          <li>Se nao encontrar (<strong>cache miss</strong>), busca na origem (banco de dados, API externa, etc.)</li>
          <li>Grava o resultado no cache com um TTL e retorna ao chamador</li>
        </ol>
        <p>
          Nesta POC, o <code>CacheService</code> implementa as operacoes basicas de
          leitura e escrita no Redis que servem de bloco de construcao para esse padrao.
          O TTL e configuravel por entrada, permitindo diferentes politicas de expiracao.
        </p>
        <CodeBlock language="java" title="CacheService.java -- escrita com TTL" code={`public void setWithTtl(String key, String value, long ttlSeconds) {
    redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
}

public String get(String key) {
    return redisTemplate.opsForValue().get(key);
}`} />

        <Callout type="info">
          <p>
            O <code>StringRedisTemplate</code> usa serializacao <code>StringRedisSerializer</code>{' '}
            para chaves e valores, ideal para dados textuais simples. Para objetos
            complexos, considere <code>RedisTemplate</code> com <code>GenericJackson2JsonRedisSerializer</code>.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* CACHE OPERATIONS */}
        {/* ================================================================ */}
        <h2 id="cache-operations" className="heading-anchor">
          <a href="#cache-operations" className="heading-anchor__link" aria-hidden="true">#</a>
          Operacoes de Cache
        </h2>
        <p>
          O <code>CacheService</code> expoe 6 operacoes fundamentais para manipulacao
          de cache no Redis, todas usando <code>StringRedisTemplate</code>:
        </p>
        <CodeBlock language="java" title="CacheService.java" code={`@Service
public class CacheService {

    private final StringRedisTemplate redisTemplate;

    // Gravar valor sem TTL (permanece ate ser deletado)
    public void put(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // Gravar valor com TTL (expira automaticamente)
    public void setWithTtl(String key, String value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    // Ler valor pela chave (retorna null se nao existir)
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Deletar chave
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    // Verificar se chave existe
    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    // Buscar chaves por pattern (ex: "user:*")
    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}`} />

        <p>
          O controller recebe um DTO com <code>key</code>, <code>value</code> e um{' '}
          <code>ttlSeconds</code> opcional. Se o TTL for informado e maior que zero,
          a entrada expira automaticamente apos o periodo configurado.
        </p>
        <CodeBlock language="java" title="CacheController.java -- POST com TTL condicional" code={`@PostMapping
public ResponseEntity<Map<String, String>> put(@RequestBody CacheEntryDto entry) {
    if (entry.ttlSeconds() != null && entry.ttlSeconds() > 0) {
        cacheService.setWithTtl(entry.key(), entry.value(), entry.ttlSeconds());
    } else {
        cacheService.put(entry.key(), entry.value());
    }
    return ResponseEntity.ok(Map.of("status", "OK", "key", entry.key()));
}`} />

        <Callout type="warning">
          <p>
            O metodo <code>keys(pattern)</code> usa o comando <code>KEYS</code> do Redis,
            que faz scan completo e pode bloquear o servidor em bases grandes.
            Em producao, prefira <code>SCAN</code> via <code>redisTemplate.scan()</code>.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* RATE LIMITING */}
        {/* ================================================================ */}
        <h2 id="rate-limiting" className="heading-anchor">
          <a href="#rate-limiting" className="heading-anchor__link" aria-hidden="true">#</a>
          Rate Limiting
        </h2>
        <p>
          O <code>RateLimiterService</code> implementa um rate limiter baseado em{' '}
          <strong>sliding-window counter</strong> usando os comandos <code>INCR</code> e{' '}
          <code>EXPIRE</code> do Redis. Cada cliente recebe uma chave unica com prefixo{' '}
          <code>ratelimit:</code> e o contador e incrementado a cada requisicao.
        </p>
        <p>
          O algoritmo funciona assim:
        </p>
        <ol>
          <li>Incrementa o contador da chave <code>ratelimit:{'<clientId>'}</code> com <code>INCR</code></li>
          <li>Se o contador retornar 1 (primeira requisicao na janela), define o TTL com <code>EXPIRE</code></li>
          <li>Se o contador for menor ou igual a <code>maxRequests</code>, a requisicao e permitida</li>
          <li>Caso contrario, retorna <code>429 Too Many Requests</code></li>
        </ol>
        <CodeBlock language="java" title="RateLimiterService.java" code={`private static final String RATE_LIMIT_PREFIX = "ratelimit:";

public boolean isAllowed(String clientId, int maxRequests, int windowSeconds) {
    String key = RATE_LIMIT_PREFIX + clientId;

    Long currentCount = redisTemplate.opsForValue().increment(key);
    if (currentCount == null) {
        return false;
    }

    // Na primeira requisicao, define o TTL da janela
    if (currentCount == 1) {
        redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
    }

    return currentCount <= maxRequests;
}`} />

        <p>
          O controller aceita parametros configuraveis: <code>maxRequests</code> (padrao 10)
          e <code>windowSeconds</code> (padrao 60). Isso permite testar diferentes
          configuracoes de rate limiting sem alterar codigo.
        </p>
        <CodeBlock language="java" title="RateLimiterController.java" code={`@GetMapping("/{clientId}")
public ResponseEntity<Map<String, Object>> checkRateLimit(
        @PathVariable String clientId,
        @RequestParam(defaultValue = "10") int maxRequests,
        @RequestParam(defaultValue = "60") int windowSeconds) {

    boolean allowed = rateLimiterService.isAllowed(clientId, maxRequests, windowSeconds);

    if (allowed) {
        return ResponseEntity.ok(body);
    }
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
}`} />

        <Callout type="tip">
          <p>
            Este algoritmo e simples e eficiente, mas permite um burst no inicio de cada
            janela. Para controle mais fino, considere o algoritmo <strong>token bucket</strong>{' '}
            ou <strong>sliding window log</strong> com Redis Sorted Sets (<code>ZADD</code>/<code>ZRANGEBYSCORE</code>).
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* SESSIONS */}
        {/* ================================================================ */}
        <h2 id="sessions" className="heading-anchor">
          <a href="#sessions" className="heading-anchor__link" aria-hidden="true">#</a>
          Gerenciamento de Sessoes
        </h2>
        <p>
          O <code>SessionService</code> demonstra como usar <strong>Redis Hashes</strong>{' '}
          para armazenar sessoes de usuario. Cada sessao e uma hash no Redis com
          prefixo <code>session:</code>, um UUID como identificador e TTL padrao de
          30 minutos (1800 segundos).
        </p>
        <p>
          As operacoes disponiveis sao:
        </p>
        <ul>
          <li><strong>Criar sessao</strong>: gera UUID, grava atributos como hash, define TTL</li>
          <li><strong>Consultar sessao</strong>: retorna todos os atributos da hash</li>
          <li><strong>Deletar sessao</strong>: remove a chave do Redis</li>
          <li><strong>Estender sessao</strong>: redefine o TTL para manter a sessao ativa</li>
        </ul>
        <CodeBlock language="java" title="SessionService.java" code={`private static final String SESSION_PREFIX = "session:";
private static final long DEFAULT_SESSION_TTL_SECONDS = 1800; // 30 minutos

public String createSession(Map<String, String> attributes) {
    String sessionId = UUID.randomUUID().toString();
    String key = SESSION_PREFIX + sessionId;

    // Armazena atributos como Redis Hash (HSET)
    redisTemplate.opsForHash().putAll(key, attributes);
    redisTemplate.expire(key, DEFAULT_SESSION_TTL_SECONDS, TimeUnit.SECONDS);

    return sessionId;
}

public Map<Object, Object> getSession(String sessionId) {
    String key = SESSION_PREFIX + sessionId;
    return redisTemplate.opsForHash().entries(key);  // HGETALL
}

public Boolean extendSession(String sessionId, long extraSeconds) {
    String key = SESSION_PREFIX + sessionId;
    return redisTemplate.expire(key, extraSeconds, TimeUnit.SECONDS);
}`} />

        <p>
          O controller expoe endpoints REST para todas as operacoes. A extensao de
          sessao aceita um parametro <code>extraSeconds</code> (padrao 1800) que permite
          renovar o tempo de vida da sessao sob demanda.
        </p>
        <CodeBlock language="java" title="SessionController.java -- extensao de sessao" code={`@PatchMapping("/{sessionId}/extend")
public ResponseEntity<Map<String, Object>> extendSession(
        @PathVariable String sessionId,
        @RequestParam(defaultValue = "1800") long extraSeconds) {
    Boolean extended = sessionService.extendSession(sessionId, extraSeconds);
    return ResponseEntity.ok(Map.of("sessionId", sessionId, "extended", extended));
}`} />

        <Callout type="info">
          <p>
            Redis Hashes sao ideais para sessoes porque permitem ler e atualizar campos
            individuais sem carregar toda a estrutura. Use <code>HGET</code>/<code>HSET</code>{' '}
            para acesso granular em vez de <code>HGETALL</code> em sessoes com muitos campos.
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
          A aplicacao roda na porta <code>8091</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            [<code>POST</code>, <code>/api/redis/cache</code>, 'Gravar entrada no cache (com TTL opcional)'],
            [<code>GET</code>, <code>/api/redis/cache/{'<key>'}</code>, 'Ler valor pela chave'],
            [<code>DELETE</code>, <code>/api/redis/cache/{'<key>'}</code>, 'Deletar chave do cache'],
            [<code>GET</code>, <code>/api/redis/cache/{'<key>'}/exists</code>, 'Verificar se chave existe'],
            [<code>GET</code>, <code>/api/redis/cache/keys?pattern=*</code>, 'Listar chaves por pattern'],
            [<code>GET</code>, <code>/api/redis/ratelimit/{'<clientId>'}</code>, 'Verificar rate limit (params: maxRequests, windowSeconds)'],
            [<code>POST</code>, <code>/api/redis/sessions</code>, 'Criar nova sessao (body: atributos JSON)'],
            [<code>GET</code>, <code>/api/redis/sessions/{'<sessionId>'}</code>, 'Consultar atributos da sessao'],
            [<code>DELETE</code>, <code>/api/redis/sessions/{'<sessionId>'}</code>, 'Deletar sessao'],
            [<code>PATCH</code>, <code>/api/redis/sessions/{'<sessionId>'}/extend</code>, 'Estender TTL da sessao (param: extraSeconds)'],
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
          Siga os 3 passos abaixo para executar a POC localmente.
        </p>

        <h3>1. Subir o Redis via Docker</h3>
        <CodeBlock language="bash" code={`# Subir um container Redis na porta 6379
docker run -d --name redis-poc -p 6379:6379 redis:7-alpine

# Verificar se esta rodando
docker exec redis-poc redis-cli ping
# Resposta esperada: PONG`} />

        <Callout type="info">
          <p>
            Se o <code>docker-compose.yml</code> do projeto ja incluir um servico Redis,
            basta usar <code>docker-compose up -d</code> na pasta <code>infra/</code>.
          </p>
        </Callout>

        <h3>2. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-elasticache-redis
./mvnw spring-boot:run

# Roda na porta 8091`} />

        <h3>3. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# ===== CACHE =====

# Gravar entrada com TTL de 60 segundos
curl -X POST "http://localhost:8091/api/redis/cache" \\
  -H "Content-Type: application/json" \\
  -d '{"key": "user:1", "value": "Joao Silva", "ttlSeconds": 60}'

# Ler valor
curl "http://localhost:8091/api/redis/cache/user:1"

# Verificar existencia
curl "http://localhost:8091/api/redis/cache/user:1/exists"

# Listar chaves com pattern
curl "http://localhost:8091/api/redis/cache/keys?pattern=user:*"

# Deletar chave
curl -X DELETE "http://localhost:8091/api/redis/cache/user:1"

# ===== RATE LIMITING =====

# Verificar rate limit (10 req em 60s)
curl "http://localhost:8091/api/redis/ratelimit/client-abc"

# Configurar limite customizado (3 req em 30s)
curl "http://localhost:8091/api/redis/ratelimit/client-abc?maxRequests=3&windowSeconds=30"

# ===== SESSOES =====

# Criar sessao
curl -X POST "http://localhost:8091/api/redis/sessions" \\
  -H "Content-Type: application/json" \\
  -d '{"username": "joao", "role": "admin", "locale": "pt-BR"}'

# Consultar sessao (substitua <SESSION_ID> pelo UUID retornado)
curl "http://localhost:8091/api/redis/sessions/<SESSION_ID>"

# Estender sessao por mais 1 hora
curl -X PATCH "http://localhost:8091/api/redis/sessions/<SESSION_ID>/extend?extraSeconds=3600"

# Deletar sessao
curl -X DELETE "http://localhost:8091/api/redis/sessions/<SESSION_ID>"`} />

        <Callout type="warning">
          <p>
            Substitua <code>{'<SESSION_ID>'}</code> pelo UUID retornado na criacao da sessao.
            As sessoes expiram automaticamente apos 30 minutos (1800 segundos) por padrao.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/elasticache-redis" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
