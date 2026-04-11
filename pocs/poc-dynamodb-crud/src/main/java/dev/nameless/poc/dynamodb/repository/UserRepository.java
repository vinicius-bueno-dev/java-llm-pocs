package dev.nameless.poc.dynamodb.repository;

import dev.nameless.poc.dynamodb.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Repository pattern aplicado sobre a tabela {@code users}. Centraliza a
 * traducao entre {@link UserDto} e {@code Map<String, AttributeValue>} e
 * encapsula comandos de baixo nivel do DynamoDB SDK v2.
 *
 * <p>Patterns didaticos usados aqui:</p>
 * <ul>
 *   <li><b>Repository</b>: abstrai a persistencia do ponto de vista dos services.</li>
 *   <li><b>Optimistic locking</b>: a escrita condicional usa o atributo
 *       {@code version} + {@code ConditionExpression} para detectar conflitos.</li>
 * </ul>
 */
@Repository
public class UserRepository {

    private final DynamoDbClient client;
    private final String tableName;

    public UserRepository(
            DynamoDbClient client,
            @Value("${aws.dynamodb.users-table-name}") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    public String tableName() {
        return tableName;
    }

    // ------------------------------------------------------------------
    // Create / Put
    // ------------------------------------------------------------------

    /** Insere exigindo que o item ainda nao exista (condicional sobre pk). */
    public UserDto create(UserDto user) {
        Map<String, AttributeValue> item = toItem(user, 1L);
        client.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(pk)")
                .build());
        return withVersion(user, 1L);
    }

    // ------------------------------------------------------------------
    // Read
    // ------------------------------------------------------------------

    public Optional<UserDto> findByTenantAndUserId(String tenantId, String userId) {
        Map<String, AttributeValue> key = Map.of(
                "pk", AttributeValue.fromS("TENANT#" + tenantId),
                "sk", AttributeValue.fromS("USER#" + userId));
        Map<String, AttributeValue> result = client.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .consistentRead(true)
                .build()).item();
        if (result == null || result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromItem(result));
    }

    // ------------------------------------------------------------------
    // Update (optimistic locking)
    // ------------------------------------------------------------------

    public UserDto updateName(String tenantId, String userId, String newName, long expectedVersion) {
        Map<String, AttributeValue> key = Map.of(
                "pk", AttributeValue.fromS("TENANT#" + tenantId),
                "sk", AttributeValue.fromS("USER#" + userId));

        Map<String, String> names = Map.of("#n", "name", "#v", "version");
        Map<String, AttributeValue> values = Map.of(
                ":newName", AttributeValue.fromS(newName),
                ":expectedVersion", AttributeValue.fromN(Long.toString(expectedVersion)),
                ":inc", AttributeValue.fromN("1"));

        try {
            UpdateItemResponse response = client.updateItem(UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .updateExpression("SET #n = :newName ADD #v :inc")
                    .conditionExpression("#v = :expectedVersion")
                    .expressionAttributeNames(names)
                    .expressionAttributeValues(values)
                    .returnValues(ReturnValue.ALL_NEW)
                    .build());
            return fromItem(response.attributes());
        } catch (ConditionalCheckFailedException ex) {
            throw new IllegalStateException(
                    "Versao esperada %d nao confere — update rejeitado".formatted(expectedVersion), ex);
        }
    }

    // ------------------------------------------------------------------
    // Delete
    // ------------------------------------------------------------------

    public void delete(String tenantId, String userId) {
        Map<String, AttributeValue> key = Map.of(
                "pk", AttributeValue.fromS("TENANT#" + tenantId),
                "sk", AttributeValue.fromS("USER#" + userId));
        client.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build());
    }

    // ------------------------------------------------------------------
    // Mapping helpers
    // ------------------------------------------------------------------

    public Map<String, AttributeValue> toItem(UserDto user, long version) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("pk", AttributeValue.fromS(user.pk()));
        item.put("sk", AttributeValue.fromS(user.sk()));
        item.put("tenantId", AttributeValue.fromS(user.tenantId()));
        item.put("userId", AttributeValue.fromS(user.userId()));
        item.put("email", AttributeValue.fromS(user.email()));
        item.put("name", AttributeValue.fromS(user.name()));
        item.put("createdAt", AttributeValue.fromS(user.createdAt()));
        item.put("version", AttributeValue.fromN(Long.toString(version)));
        if (user.expiresAt() != null) {
            item.put("expiresAt", AttributeValue.fromN(Long.toString(user.expiresAt())));
        }
        return item;
    }

    public UserDto fromItem(Map<String, AttributeValue> item) {
        return new UserDto(
                item.get("tenantId").s(),
                item.get("userId").s(),
                item.get("email").s(),
                item.get("name").s(),
                item.get("createdAt").s(),
                item.containsKey("expiresAt") ? Long.parseLong(item.get("expiresAt").n()) : null,
                item.containsKey("version") ? Long.parseLong(item.get("version").n()) : null);
    }

    private UserDto withVersion(UserDto user, long version) {
        return new UserDto(
                user.tenantId(), user.userId(), user.email(), user.name(),
                user.createdAt(), user.expiresAt(), version);
    }
}
