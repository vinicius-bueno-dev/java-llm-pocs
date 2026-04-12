package dev.nameless.poc.sns.dto;

import java.util.List;

public record BatchPublishDto(
        List<PublishMessageDto> messages
) {}
