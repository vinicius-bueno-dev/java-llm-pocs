package dev.nameless.poc.dynamodb.dto;

/**
 * Representacao imutavel de um usuario armazenado no DynamoDB.
 *
 * <p>Modelagem single-table:</p>
 * <ul>
 *   <li>pk = {@code "TENANT#<tenantId>"}</li>
 *   <li>sk = {@code "USER#<userId>"}</li>
 *   <li>email indexado pelo GSI {@code by-email}</li>
 *   <li>createdAt indexado pelo LSI {@code by-created-at}</li>
 *   <li>expiresAt (epoch seconds) usado como atributo de TTL</li>
 * </ul>
 */
public record UserDto(
        String tenantId,
        String userId,
        String email,
        String name,
        String createdAt,
        Long expiresAt,
        Long version
) {
    public String pk() {
        return "TENANT#" + tenantId;
    }

    public String sk() {
        return "USER#" + userId;
    }
}
