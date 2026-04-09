package dev.nameless.poc.sqs.dto;

import java.util.List;

public record BatchMessageDto(
        List<SendMessageDto> messages
) {}
