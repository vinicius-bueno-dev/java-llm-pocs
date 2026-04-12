package dev.nameless.poc.stepfunctions.service;

import dev.nameless.poc.stepfunctions.AbstractLocalStackTest;
import dev.nameless.poc.stepfunctions.dto.CreateStateMachineDto;
import dev.nameless.poc.stepfunctions.dto.StartExecutionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionServiceTest extends AbstractLocalStackTest {

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private StateMachineService stateMachineService;

    private String passStateMachineArn;
    private String waitStateMachineArn;

    @BeforeEach
    void setUp() {
        // Cria state machine com Pass state (execucao instantanea)
        Map<String, String> passResult = stateMachineService.create(new CreateStateMachineDto(
                "exec-test-pass-" + System.nanoTime(),
                SIMPLE_PASS_DEFINITION,
                FAKE_ROLE_ARN));
        passStateMachineArn = passResult.get("stateMachineArn");

        // Cria state machine com Wait state (para testar stop)
        Map<String, String> waitResult = stateMachineService.create(new CreateStateMachineDto(
                "exec-test-wait-" + System.nanoTime(),
                WAIT_DEFINITION,
                FAKE_ROLE_ARN));
        waitStateMachineArn = waitResult.get("stateMachineArn");
    }

    @Test
    void shouldStartExecution() {
        StartExecutionDto dto = new StartExecutionDto(
                passStateMachineArn,
                "exec-start-" + System.nanoTime(),
                "{\"key\": \"value\"}");

        Map<String, String> result = executionService.startExecution(dto);

        assertThat(result).containsKey("executionArn");
        assertThat(result).containsKey("startDate");
        assertThat(result.get("executionArn")).isNotBlank();
    }

    @Test
    void shouldDescribeExecution() {
        Map<String, String> started = executionService.startExecution(new StartExecutionDto(
                passStateMachineArn,
                "exec-describe-" + System.nanoTime(),
                "{\"key\": \"value\"}"));

        String executionArn = started.get("executionArn");
        Map<String, String> described = executionService.describeExecution(executionArn);

        assertThat(described.get("executionArn")).isEqualTo(executionArn);
        assertThat(described.get("stateMachineArn")).isEqualTo(passStateMachineArn);
        assertThat(described).containsKey("status");
        assertThat(described).containsKey("input");
    }

    @Test
    void shouldListExecutions() {
        executionService.startExecution(new StartExecutionDto(
                passStateMachineArn,
                "exec-list-1-" + System.nanoTime(),
                "{}"));
        executionService.startExecution(new StartExecutionDto(
                passStateMachineArn,
                "exec-list-2-" + System.nanoTime(),
                "{}"));

        List<Map<String, String>> executions = executionService.listExecutions(passStateMachineArn);

        assertThat(executions).hasSizeGreaterThanOrEqualTo(2);
        assertThat(executions).allSatisfy(exec -> {
            assertThat(exec).containsKey("executionArn");
            assertThat(exec).containsKey("status");
            assertThat(exec).containsKey("startDate");
        });
    }

    @Test
    void shouldStopExecution() {
        Map<String, String> started = executionService.startExecution(new StartExecutionDto(
                waitStateMachineArn,
                "exec-stop-" + System.nanoTime(),
                "{}"));

        String executionArn = started.get("executionArn");
        Map<String, String> stopped = executionService.stopExecution(
                executionArn, "Test cancellation", "TestError");

        assertThat(stopped.get("executionArn")).isEqualTo(executionArn);
        assertThat(stopped.get("status")).isEqualTo("ABORTED");
        assertThat(stopped).containsKey("stopDate");
    }

    @Test
    void shouldGetExecutionHistory() {
        Map<String, String> started = executionService.startExecution(new StartExecutionDto(
                passStateMachineArn,
                "exec-history-" + System.nanoTime(),
                "{\"key\": \"value\"}"));

        String executionArn = started.get("executionArn");
        List<Map<String, String>> history = executionService.getExecutionHistory(executionArn);

        assertThat(history).isNotEmpty();
        assertThat(history).allSatisfy(event -> {
            assertThat(event).containsKey("id");
            assertThat(event).containsKey("type");
            assertThat(event).containsKey("timestamp");
        });
    }
}
