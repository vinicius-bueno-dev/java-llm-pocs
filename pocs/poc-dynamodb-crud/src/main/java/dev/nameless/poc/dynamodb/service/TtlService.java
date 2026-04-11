package dev.nameless.poc.dynamodb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DescribeTimeToLiveRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTimeToLiveResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.time.Instant;
import java.util.Map;

/**
 * Faz a manipulacao do atributo {@code expiresAt} usado como TTL. Demonstra
 * tambem {@link DynamoDbClient#describeTimeToLive(DescribeTimeToLiveRequest)}
 * para exibir a configuracao da tabela.
 *
 * <p>Importante: o LocalStack NAO remove itens expirados do jeito que o
 * DynamoDB real faz (o varredor do AWS roda a cada ~48h). Aqui o objetivo e
 * apenas verificar que o atributo esta presente e descrevivel.</p>
 */
@Service
public class TtlService {

    private final DynamoDbClient client;
    private final String tableName;

    public TtlService(
            DynamoDbClient client,
            @Value("${aws.dynamodb.users-table-name}") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    public String describeTtl() {
        DescribeTimeToLiveResponse response = client.describeTimeToLive(
                DescribeTimeToLiveRequest.builder().tableName(tableName).build());
        return response.timeToLiveDescription() != null
                ? response.timeToLiveDescription().timeToLiveStatusAsString()
                : "UNKNOWN";
    }

    public long setExpiresInSeconds(String tenantId, String userId, long seconds) {
        long expiresAt = Instant.now().getEpochSecond() + seconds;
        Map<String, AttributeValue> key = Map.of(
                "pk", AttributeValue.fromS("TENANT#" + tenantId),
                "sk", AttributeValue.fromS("USER#" + userId));
        client.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression("SET expiresAt = :exp")
                .expressionAttributeValues(Map.of(
                        ":exp", AttributeValue.fromN(Long.toString(expiresAt))))
                .build());
        return expiresAt;
    }
}
