package dev.nameless.poc.dynamodb.service;

import dev.nameless.poc.dynamodb.dto.EventDto;
import dev.nameless.poc.dynamodb.dto.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstra {@code BatchWriteItem} (maximo 25 itens por chamada) e {@code Scan}
 * com pagination. Opera sobre a tabela {@code events}.
 */
@Service
public class BatchEventService {

    private static final int BATCH_LIMIT = 25;

    private final DynamoDbClient client;
    private final String tableName;

    public BatchEventService(
            DynamoDbClient client,
            @Value("${aws.dynamodb.events-table-name}") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    public int batchInsert(List<EventDto> events) {
        int written = 0;
        for (int i = 0; i < events.size(); i += BATCH_LIMIT) {
            List<EventDto> chunk = events.subList(i, Math.min(i + BATCH_LIMIT, events.size()));
            List<WriteRequest> writes = new ArrayList<>();
            for (EventDto event : chunk) {
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("eventId", AttributeValue.fromS(event.eventId()));
                item.put("type", AttributeValue.fromS(event.type()));
                item.put("payload", AttributeValue.fromS(event.payload()));
                writes.add(WriteRequest.builder()
                        .putRequest(PutRequest.builder().item(item).build())
                        .build());
            }
            client.batchWriteItem(BatchWriteItemRequest.builder()
                    .requestItems(Map.of(tableName, writes))
                    .build());
            written += chunk.size();
        }
        return written;
    }

    public PageResult<EventDto> scan(int limit, String pageToken) {
        ScanRequest request = ScanRequest.builder()
                .tableName(tableName)
                .limit(limit)
                .exclusiveStartKey(PageTokenCodec.decode(pageToken))
                .build();
        ScanResponse response = client.scan(request);
        List<EventDto> items = response.items().stream()
                .map(item -> new EventDto(
                        item.get("eventId").s(),
                        item.get("type").s(),
                        item.get("payload").s()))
                .toList();
        String nextToken = PageTokenCodec.encode(response.lastEvaluatedKey());
        return new PageResult<>(items, nextToken, items.size());
    }
}
