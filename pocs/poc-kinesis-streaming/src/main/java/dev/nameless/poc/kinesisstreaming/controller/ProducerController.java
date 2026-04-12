package dev.nameless.poc.kinesisstreaming.controller;

import dev.nameless.poc.kinesisstreaming.dto.BatchPutDto;
import dev.nameless.poc.kinesisstreaming.dto.PutRecordDto;
import dev.nameless.poc.kinesisstreaming.service.ProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/kinesis/producer")
public class ProducerController {

    private final ProducerService producerService;

    public ProducerController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/record")
    public ResponseEntity<Map<String, String>> putRecord(@RequestBody PutRecordDto dto) {
        return ResponseEntity.ok(producerService.putRecord(dto));
    }

    @PostMapping("/records")
    public ResponseEntity<Map<String, Object>> putRecords(@RequestBody BatchPutDto dto) {
        return ResponseEntity.ok(producerService.putRecords(dto));
    }
}
