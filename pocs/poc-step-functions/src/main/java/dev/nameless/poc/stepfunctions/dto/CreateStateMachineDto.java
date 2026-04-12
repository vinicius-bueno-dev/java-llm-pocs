package dev.nameless.poc.stepfunctions.dto;

/**
 * DTO para criacao de uma state machine.
 *
 * @param name       nome unico da state machine
 * @param definition definicao ASL (Amazon States Language) em JSON
 * @param roleArn    ARN do IAM role (pode ser fake no LocalStack)
 */
public record CreateStateMachineDto(
        String name,
        String definition,
        String roleArn) {
}
