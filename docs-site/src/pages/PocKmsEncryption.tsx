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
  { id: 'key-management', title: 'Gerenciamento de Chaves' },
  { id: 'aliases', title: 'Aliases' },
  { id: 'encrypt-decrypt', title: 'Encrypt / Decrypt' },
  { id: 'envelope-encryption', title: 'Envelope Encryption' },
  { id: 'key-rotation', title: 'Key Rotation' },
  { id: 'api-reference', title: 'Referencia da API' },
  { id: 'rodando', title: 'Como Rodar' },
]

export function PocKmsEncryption() {
  return (
    <>
      <main className="app-content">
        <Breadcrumbs />
        <div className="section-header">
          <div className="section-header__overline">POCs</div>
          <h1>KMS Encryption</h1>
        </div>

        {/* ================================================================ */}
        {/* VISAO GERAL */}
        {/* ================================================================ */}
        <h2 id="visao-geral" className="heading-anchor">
          <a href="#visao-geral" className="heading-anchor__link" aria-hidden="true">#</a>
          Visao Geral
        </h2>
        <p>
          Esta POC explora o <strong>AWS Key Management Service (KMS)</strong> em uma
          aplicacao Spring Boot conectada ao LocalStack. O objetivo e demonstrar como
          usar o <strong>AWS SDK for Java v2</strong> para gerenciar chaves criptograficas
          (CMKs), realizar criptografia direta e implementar o padrao de{' '}
          <strong>envelope encryption</strong>.
        </p>
        <p>
          As funcionalidades cobertas incluem criacao e gerenciamento de Customer Managed
          Keys (CMKs), aliases, criptografia e descriptografia direta via KMS, geracao
          de data keys, envelope encryption com AES-256, e rotacao automatica de chaves.
        </p>
        <Callout type="info">
          <p>
            Tudo roda localmente com LocalStack Community. O KMS tem boa cobertura
            no tier gratuito, incluindo operacoes de encrypt/decrypt, data keys e aliases.
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
          Os dois controllers delegam para dois services especializados: um para
          gerenciamento de chaves e outro para operacoes de criptografia.
        </p>
        <CodeBlock language="java" title="Estrutura de pacotes" code={`dev.nameless.poc.kmsencryption/
  config/         KmsConfig (KmsClient bean)
  controller/     KeyController, EncryptionController
  service/        KeyManagementService, EncryptionService
  dto/            Records: EncryptRequestDto, EnvelopeEncryptDto`} />

        <Callout type="tip">
          <p>
            A aplicacao roda na porta <code>8087</code> para nao conflitar com as
            demais POCs.
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
          O modulo Terraform em <code>infra/localstack/modules/kms</code> provisiona
          2 CMKs simetricas com aliases, cada uma com proposito distinto.
        </p>
        <Table
          headers={['Recurso', 'Nome / Alias', 'Proposito']}
          rows={[
            [<code>aws_kms_key</code>, 'main_key', 'CMK principal para criptografia direta, com key rotation habilitada'],
            [<code>aws_kms_alias</code>, 'alias/project-env-poc-main', 'Alias legivel para a CMK principal'],
            [<code>aws_kms_key</code>, 'envelope_key', 'CMK secundaria para demonstrar envelope encryption'],
            [<code>aws_kms_alias</code>, 'alias/project-env-poc-envelope', 'Alias legivel para a CMK de envelope'],
          ]}
        />

        <CodeBlock language="hcl" title="modules/kms/main.tf" code={`resource "aws_kms_key" "main_key" {
  description             = "CMK principal para POC de encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = {
    Name        = "\${var.project_name}-poc-main-key"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "kms-encryption"
    Type        = "symmetric"
  }
}

resource "aws_kms_alias" "main_key_alias" {
  name          = "alias/\${var.project_name}-\${var.environment}-poc-main"
  target_key_id = aws_kms_key.main_key.key_id
}

resource "aws_kms_key" "envelope_key" {
  description             = "CMK para demonstrar envelope encryption"
  deletion_window_in_days = 7

  tags = {
    Name        = "\${var.project_name}-poc-envelope-key"
    Environment = var.environment
    ManagedBy   = "terraform"
    POC         = "kms-encryption"
    Type        = "envelope"
  }
}

resource "aws_kms_alias" "envelope_key_alias" {
  name          = "alias/\${var.project_name}-\${var.environment}-poc-envelope"
  target_key_id = aws_kms_key.envelope_key.key_id
}`} />

        {/* ================================================================ */}
        {/* KEY MANAGEMENT */}
        {/* ================================================================ */}
        <h2 id="key-management" className="heading-anchor">
          <a href="#key-management" className="heading-anchor__link" aria-hidden="true">#</a>
          Gerenciamento de Chaves
        </h2>
        <p>
          O <code>KeyManagementService</code> encapsula todas as operacoes de ciclo de
          vida de CMKs: criacao (com tags opcionais), listagem, descricao detalhada,
          desabilitacao e agendamento de exclusao com janela configuravel.
        </p>
        <CodeBlock language="java" title="KeyManagementService.java" code={`// Criar CMK com descricao e tags
CreateKeyRequest.Builder builder = CreateKeyRequest.builder()
        .description(description);
if (tags != null && !tags.isEmpty()) {
    List<Tag> kmsTags = tags.entrySet().stream()
            .map(e -> Tag.builder()
                .tagKey(e.getKey()).tagValue(e.getValue()).build())
            .toList();
    builder.tags(kmsTags);
}
CreateKeyResponse response = kmsClient.createKey(builder.build());
KeyMetadata metadata = response.keyMetadata();

// Listar todas as chaves
ListKeysResponse response = kmsClient.listKeys(
    ListKeysRequest.builder().build());

// Descrever uma chave (estado, ARN, descricao, data de criacao)
DescribeKeyResponse response = kmsClient.describeKey(
    DescribeKeyRequest.builder().keyId(keyId).build());

// Desabilitar chave (nao pode mais ser usada para encrypt)
kmsClient.disableKey(
    DisableKeyRequest.builder().keyId(keyId).build());

// Agendar exclusao (janela minima de 7 dias)
ScheduleKeyDeletionResponse response = kmsClient.scheduleKeyDeletion(
    ScheduleKeyDeletionRequest.builder()
        .keyId(keyId)
        .pendingWindowInDays(pendingWindowDays)
        .build());`} />

        <Callout type="warning">
          <p>
            A exclusao de uma CMK e <strong>irreversivel</strong>. Na AWS real, existe
            uma janela de espera (minimo 7 dias) que permite cancelar a operacao.
            No LocalStack, a exclusao pode ser imediata dependendo da versao.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ALIASES */}
        {/* ================================================================ */}
        <h2 id="aliases" className="heading-anchor">
          <a href="#aliases" className="heading-anchor__link" aria-hidden="true">#</a>
          Aliases
        </h2>
        <p>
          Aliases sao nomes amigaveis que apontam para CMKs, facilitando a referencia
          sem precisar lembrar do Key ID (UUID). Um alias sempre comeca com o prefixo{' '}
          <code>alias/</code>. A POC suporta criacao de alias, criacao de chave com
          alias atomicamente e listagem de todos os aliases.
        </p>
        <CodeBlock language="java" title="KeyManagementService.java — Aliases" code={`// Criar alias para uma chave existente
String aliasName = alias.startsWith("alias/") ? alias : "alias/" + alias;
kmsClient.createAlias(CreateAliasRequest.builder()
        .aliasName(aliasName)
        .targetKeyId(keyId)
        .build());

// Criar chave + alias em uma unica operacao
Map<String, String> keyResult = createKey(description, null);
String keyId = keyResult.get("keyId");
createAlias("alias/" + alias, keyId);

// Listar todos os aliases
ListAliasesResponse response = kmsClient.listAliases(
    ListAliasesRequest.builder().build());
// Retorna: aliasName, aliasArn, targetKeyId`} />

        <Callout type="tip">
          <p>
            Aliases permitem trocar a CMK subjacente sem alterar o codigo da aplicacao.
            Basta atualizar o alias para apontar para uma nova chave.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ENCRYPT / DECRYPT */}
        {/* ================================================================ */}
        <h2 id="encrypt-decrypt" className="heading-anchor">
          <a href="#encrypt-decrypt" className="heading-anchor__link" aria-hidden="true">#</a>
          Encrypt / Decrypt
        </h2>
        <p>
          A criptografia direta via KMS usa a CMK para cifrar e decifrar dados
          pequenos (ate 4 KB). O texto plano e enviado como <code>SdkBytes</code> e
          o resultado (<code>ciphertextBlob</code>) e retornado em Base64. Para
          descriptografar, basta enviar o ciphertext de volta junto com o Key ID.
        </p>
        <CodeBlock language="java" title="EncryptionService.java — Encrypt/Decrypt" code={`// Criptografar texto plano
EncryptResponse response = kmsClient.encrypt(EncryptRequest.builder()
        .keyId(keyId)
        .plaintext(SdkBytes.fromUtf8String(plaintext))
        .build());
String ciphertextBase64 = Base64.getEncoder().encodeToString(
        response.ciphertextBlob().asByteArray());

// Descriptografar ciphertext
byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertextBase64);
DecryptResponse response = kmsClient.decrypt(DecryptRequest.builder()
        .keyId(keyId)
        .ciphertextBlob(SdkBytes.fromByteArray(ciphertextBytes))
        .build());
String plaintext = response.plaintext().asUtf8String();`} />

        <Callout type="info">
          <p>
            A criptografia direta via KMS e ideal para dados pequenos como chaves de
            sessao, tokens e senhas. Para dados maiores, use{' '}
            <strong>envelope encryption</strong> (secao abaixo).
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* ENVELOPE ENCRYPTION */}
        {/* ================================================================ */}
        <h2 id="envelope-encryption" className="heading-anchor">
          <a href="#envelope-encryption" className="heading-anchor__link" aria-hidden="true">#</a>
          Envelope Encryption
        </h2>
        <p>
          Envelope encryption e o padrao recomendado pela AWS para criptografar dados
          grandes. O fluxo e: (1) gerar uma <strong>data key</strong> via KMS, que
          retorna a chave em texto plano e cifrada; (2) usar a chave em texto plano
          para cifrar os dados localmente com AES-256; (3) descartar a chave em texto
          plano e armazenar apenas a versao cifrada junto com os dados.
        </p>
        <p>
          Para descriptografar: (1) enviar a data key cifrada ao KMS para obter a
          versao em texto plano; (2) usar a chave decifrada para descriptografar os
          dados localmente.
        </p>
        <CodeBlock language="java" title="EncryptionService.java — Gerar Data Key" code={`// Gerar data key (retorna plaintext + encrypted)
GenerateDataKeyResponse response = kmsClient.generateDataKey(
        GenerateDataKeyRequest.builder()
                .keyId(keyId)
                .keySpec(DataKeySpec.AES_256)
                .build());

String plaintextKey = Base64.getEncoder().encodeToString(
        response.plaintext().asByteArray());
String ciphertextKey = Base64.getEncoder().encodeToString(
        response.ciphertextBlob().asByteArray());`} />

        <CodeBlock language="java" title="EncryptionService.java — Envelope Encrypt" code={`// Envelope encrypt: gera data key + cifra dados com AES
Map<String, String> dataKey = generateDataKey(keyId);
byte[] plaintextKeyBytes = Base64.getDecoder().decode(
        dataKey.get("plaintextKey"));

SecretKeySpec aesKey = new SecretKeySpec(plaintextKeyBytes, "AES");
Cipher cipher = Cipher.getInstance("AES");
cipher.init(Cipher.ENCRYPT_MODE, aesKey);
byte[] encryptedData = cipher.doFinal(
        plaintext.getBytes(StandardCharsets.UTF_8));

// Retorna: encryptedData (Base64) + encryptedDataKey (Base64)
// A chave em texto plano e descartada apos o uso`} />

        <CodeBlock language="java" title="EncryptionService.java — Envelope Decrypt" code={`// Envelope decrypt: decifra data key via KMS + decifra dados com AES
DecryptResponse decryptResponse = kmsClient.decrypt(
        DecryptRequest.builder()
                .keyId(keyId)
                .ciphertextBlob(SdkBytes.fromByteArray(encryptedDataKeyBytes))
                .build());

byte[] plaintextKeyBytes = decryptResponse.plaintext().asByteArray();
SecretKeySpec aesKey = new SecretKeySpec(plaintextKeyBytes, "AES");
Cipher cipher = Cipher.getInstance("AES");
cipher.init(Cipher.DECRYPT_MODE, aesKey);
byte[] decryptedData = cipher.doFinal(
        Base64.getDecoder().decode(encryptedDataBase64));

String plaintext = new String(decryptedData, StandardCharsets.UTF_8);`} />

        <Callout type="tip">
          <p>
            Envelope encryption reduz chamadas ao KMS (apenas para gerar/decifrar a
            data key) e permite criptografar dados de qualquer tamanho localmente
            com AES-256.
          </p>
        </Callout>

        {/* ================================================================ */}
        {/* KEY ROTATION */}
        {/* ================================================================ */}
        <h2 id="key-rotation" className="heading-anchor">
          <a href="#key-rotation" className="heading-anchor__link" aria-hidden="true">#</a>
          Key Rotation
        </h2>
        <p>
          A rotacao automatica de chaves substitui periodicamente o material
          criptografico de uma CMK, mantendo o mesmo Key ID e alias. Dados cifrados
          com versoes anteriores continuam sendo descriptografados normalmente, pois
          o KMS mantem o historico de versoes.
        </p>
        <CodeBlock language="java" title="KeyManagementService.java — Rotation" code={`// Habilitar rotacao automatica para uma chave
kmsClient.enableKeyRotation(
    EnableKeyRotationRequest.builder().keyId(keyId).build());

// Consultar status da rotacao
GetKeyRotationStatusResponse response = kmsClient.getKeyRotationStatus(
    GetKeyRotationStatusRequest.builder().keyId(keyId).build());
boolean enabled = response.keyRotationEnabled();`} />

        <Callout type="info">
          <p>
            Na AWS real, a rotacao automatica ocorre a cada 365 dias. No Terraform,
            a CMK principal ja e provisionada com{' '}
            <code>enable_key_rotation = true</code>.
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
          A tabela abaixo lista todos os endpoints expostos pela POC. Os endpoints
          de gerenciamento de chaves usam o prefixo <code>/api/kms/keys</code> e os
          de criptografia usam <code>/api/kms/encrypt</code>.
        </p>
        <Table
          headers={['Metodo', 'Endpoint', 'Descricao']}
          rows={[
            ['POST', <code>/api/kms/keys</code>, 'Criar CMK com descricao e tags opcionais'],
            ['POST', <code>/api/kms/keys/with-alias</code>, 'Criar CMK com alias em uma unica operacao'],
            ['GET', <code>/api/kms/keys</code>, 'Listar todas as CMKs'],
            ['GET', <code>/api/kms/keys/:keyId</code>, 'Descrever uma CMK (estado, ARN, descricao)'],
            ['PUT', <code>/api/kms/keys/:keyId/rotation</code>, 'Habilitar rotacao automatica da chave'],
            ['GET', <code>/api/kms/keys/:keyId/rotation</code>, 'Consultar status da rotacao'],
            ['POST', <code>/api/kms/keys/aliases</code>, 'Criar alias para uma chave existente'],
            ['GET', <code>/api/kms/keys/aliases</code>, 'Listar todos os aliases'],
            ['PUT', <code>/api/kms/keys/:keyId/disable</code>, 'Desabilitar uma CMK'],
            ['DELETE', <code>/api/kms/keys/:keyId?pendingWindowDays=7</code>, 'Agendar exclusao de CMK'],
            ['POST', <code>/api/kms/encrypt</code>, 'Criptografar texto plano com uma CMK'],
            ['POST', <code>/api/kms/encrypt/decrypt</code>, 'Descriptografar ciphertext'],
            ['POST', <code>/api/kms/encrypt/generate-data-key</code>, 'Gerar data key (AES-256) para envelope encryption'],
            ['POST', <code>/api/kms/encrypt/envelope-encrypt</code>, 'Envelope encrypt: gera data key + cifra dados com AES'],
            ['POST', <code>/api/kms/encrypt/envelope-decrypt</code>, 'Envelope decrypt: decifra data key + decifra dados'],
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

# Ver IDs e aliases das chaves criadas
terraform output`} />

        <h3>3. Rodar a aplicacao Spring Boot</h3>
        <CodeBlock language="bash" code={`cd pocs/poc-kms-encryption
./mvnw spring-boot:run

# Roda na porta 8087`} />

        <h3>4. Testar com curl</h3>
        <CodeBlock language="bash" title="Exemplos de teste" code={`# Criar uma CMK
curl -X POST "http://localhost:8087/api/kms/keys" \\
  -H "Content-Type: application/json" \\
  -d '{"description": "Minha chave de teste", "tags": {"env": "dev"}}'

# Criar CMK com alias
curl -X POST "http://localhost:8087/api/kms/keys/with-alias" \\
  -H "Content-Type: application/json" \\
  -d '{"description": "Chave com alias", "alias": "minha-chave"}'

# Listar chaves
curl "http://localhost:8087/api/kms/keys"

# Descrever uma chave
curl "http://localhost:8087/api/kms/keys/<KEY_ID>"

# Criptografar texto
curl -X POST "http://localhost:8087/api/kms/encrypt" \\
  -H "Content-Type: application/json" \\
  -d '{"keyId": "<KEY_ID>", "plaintext": "Dados secretos"}'

# Descriptografar
curl -X POST "http://localhost:8087/api/kms/encrypt/decrypt" \\
  -H "Content-Type: application/json" \\
  -d '{"keyId": "<KEY_ID>", "ciphertextBlob": "<CIPHERTEXT_BASE64>"}'

# Envelope encrypt
curl -X POST "http://localhost:8087/api/kms/encrypt/envelope-encrypt" \\
  -H "Content-Type: application/json" \\
  -d '{"keyId": "<KEY_ID>", "plaintext": "Dados grandes para envelope encryption"}'

# Envelope decrypt
curl -X POST "http://localhost:8087/api/kms/encrypt/envelope-decrypt" \\
  -H "Content-Type: application/json" \\
  -d '{"keyId": "<KEY_ID>", "encryptedDataKey": "<ENC_KEY>", "encryptedData": "<ENC_DATA>"}'

# Habilitar rotacao
curl -X PUT "http://localhost:8087/api/kms/keys/<KEY_ID>/rotation"

# Listar aliases
curl "http://localhost:8087/api/kms/keys/aliases"`} />

        <Callout type="warning">
          <p>
            Substitua <code>{'<KEY_ID>'}</code> pelo ID real da chave retornado na
            criacao ou via <code>terraform output</code>. Os valores de{' '}
            <code>{'<CIPHERTEXT_BASE64>'}</code>, <code>{'<ENC_KEY>'}</code> e{' '}
            <code>{'<ENC_DATA>'}</code> sao obtidos nas respostas dos endpoints de
            criptografia.
          </p>
        </Callout>

        <PageNav currentPath="/pocs/kms-encryption" />
        <Footer />
      </main>
      <TOC items={tocItems} />
    </>
  )
}
