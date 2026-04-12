package dev.nameless.poc.stepfunctions.dto;

/**
 * DTO para iniciar uma execucao de state machine.
 *
 * @param stateMachineArn ARN da state machine a executar
 * @param name            nome opcional da execucao (deve ser unico)
 * @param input           JSON de entrada para a execucao
 */
public record StartExecutionDto(
        String stateMachineArn,
        String name,
        String input) {
}
