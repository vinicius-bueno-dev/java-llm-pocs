package dev.nameless.poc.dynamodb.service;

import dev.nameless.poc.dynamodb.dto.PageResult;
import dev.nameless.poc.dynamodb.dto.UserDto;
import dev.nameless.poc.dynamodb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;

/**
 * Demonstra as 3 formas de consulta usadas nesta POC:
 *
 * <ol>
 *   <li><b>Query direta na tabela</b>: lista todos os users de um tenant usando
 *       {@code pk begins_with}.</li>
 *   <li><b>Query pelo GSI {@code by-email}</b>: busca pelo atributo {@code email}
 *       sem precisar saber o tenant.</li>
 *   <li><b>Query pelo LSI {@code by-created-at}</b>: lista users de um tenant
 *       ordenados (ascendente/descendente) por {@code createdAt}.</li>
 * </ol>
 *
 * <p>Cada consulta usa o <b>Builder pattern</b> do SDK para construir o
 * {@link QueryRequest}. O metodo {@link #paginate(QueryRequest)} aplica um unico
 * ponto de execucao e conversao para {@link PageResult}.</p>
 */
@Service
public class UserQueryService {

    private final DynamoDbClient client;
    private final UserRepository repository;
    private final String tableName;
    private final String gsiByEmail;
    private final String lsiByCreatedAt;

    public UserQueryService(
            DynamoDbClient client,
            UserRepository repository,
            @Value("${aws.dynamodb.users-table-name}") String tableName,
            @Value("${aws.dynamodb.gsi-by-email}") String gsiByEmail,
            @Value("${aws.dynamodb.lsi-by-created-at}") String lsiByCreatedAt) {
        this.client = client;
        this.repository = repository;
        this.tableName = tableName;
        this.gsiByEmail = gsiByEmail;
        this.lsiByCreatedAt = lsiByCreatedAt;
    }

    public PageResult<UserDto> listByTenant(String tenantId, int limit, String pageToken) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("pk = :pk")
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.fromS("TENANT#" + tenantId)))
                .limit(limit)
                .exclusiveStartKey(PageTokenCodec.decode(pageToken))
                .build();
        return paginate(request);
    }

    public PageResult<UserDto> findByEmail(String email, int limit, String pageToken) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName(gsiByEmail)
                .keyConditionExpression("email = :email")
                .expressionAttributeValues(Map.of(":email", AttributeValue.fromS(email)))
                .limit(limit)
                .exclusiveStartKey(PageTokenCodec.decode(pageToken))
                .build();
        return paginate(request);
    }

    public PageResult<UserDto> listByCreatedAt(String tenantId, boolean ascending, int limit, String pageToken) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName(lsiByCreatedAt)
                .keyConditionExpression("pk = :pk")
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.fromS("TENANT#" + tenantId)))
                .scanIndexForward(ascending)
                .limit(limit)
                .exclusiveStartKey(PageTokenCodec.decode(pageToken))
                .build();
        return paginate(request);
    }

    // ------------------------------------------------------------------

    private PageResult<UserDto> paginate(QueryRequest request) {
        QueryResponse response = client.query(request);
        List<UserDto> items = response.items().stream()
                .map(repository::fromItem)
                .toList();
        String nextToken = PageTokenCodec.encode(response.lastEvaluatedKey());
        return new PageResult<>(items, nextToken, items.size());
    }
}
