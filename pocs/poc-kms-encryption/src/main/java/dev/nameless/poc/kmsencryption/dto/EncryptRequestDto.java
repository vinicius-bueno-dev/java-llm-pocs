package dev.nameless.poc.kmsencryption.dto;

public record EncryptRequestDto(String keyId, String plaintext) {
}
