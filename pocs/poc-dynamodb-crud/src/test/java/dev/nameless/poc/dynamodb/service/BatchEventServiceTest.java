package dev.nameless.poc.dynamodb.service;

import dev.nameless.poc.dynamodb.AbstractLocalStackTest;
import dev.nameless.poc.dynamodb.dto.EventDto;
import dev.nameless.poc.dynamodb.dto.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BatchEventServiceTest extends AbstractLocalStackTest {

    @Autowired
    private BatchEventService service;

    @Test
    void shouldBatchInsertAndScanAllItems() {
        List<EventDto> events = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            events.add(new EventDto("evt-" + i, "demo", "payload-" + i));
        }

        int written = service.batchInsert(events);
        assertThat(written).isEqualTo(60);

        Set<String> seen = new HashSet<>();
        String token = null;
        do {
            PageResult<EventDto> page = service.scan(25, token);
            page.items().forEach(e -> seen.add(e.eventId()));
            token = page.nextToken();
        } while (token != null);

        assertThat(seen).hasSize(60);
    }
}
