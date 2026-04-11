package dev.nameless.poc.dynamodb.dto;

import java.util.List;
import java.util.Map;

/**
 * Resultado paginado generico. {@code nextToken} carrega o
 * {@code LastEvaluatedKey} serializado por {@link dev.nameless.poc.dynamodb.service.PageTokenCodec}.
 */
public record PageResult<T>(
        List<T> items,
        String nextToken,
        int count
) {
    public static <T> PageResult<T> of(List<T> items, Map<String, ?> lastEvaluatedKey, String nextToken) {
        return new PageResult<>(items, nextToken, items.size());
    }
}
