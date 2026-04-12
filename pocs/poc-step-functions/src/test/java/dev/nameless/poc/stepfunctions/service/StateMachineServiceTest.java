package dev.nameless.poc.stepfunctions.service;

import dev.nameless.poc.stepfunctions.AbstractLocalStackTest;
import dev.nameless.poc.stepfunctions.dto.CreateStateMachineDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StateMachineServiceTest extends AbstractLocalStackTest {

    @Autowired
    private StateMachineService service;

    @Test
    void shouldCreateStateMachine() {
        CreateStateMachineDto dto = new CreateStateMachineDto(
                "test-machine-create",
                SIMPLE_PASS_DEFINITION,
                FAKE_ROLE_ARN);

        Map<String, String> result = service.create(dto);

        assertThat(result).containsKey("stateMachineArn");
        assertThat(result).containsKey("creationDate");
        assertThat(result.get("stateMachineArn")).contains("test-machine-create");
    }

    @Test
    void shouldListStateMachines() {
        service.create(new CreateStateMachineDto(
                "test-machine-list", SIMPLE_PASS_DEFINITION, FAKE_ROLE_ARN));

        List<Map<String, String>> machines = service.list();

        assertThat(machines).isNotEmpty();
        assertThat(machines).anyMatch(m -> "test-machine-list".equals(m.get("name")));
    }

    @Test
    void shouldDescribeStateMachine() {
        Map<String, String> created = service.create(new CreateStateMachineDto(
                "test-machine-describe", SIMPLE_PASS_DEFINITION, FAKE_ROLE_ARN));

        String arn = created.get("stateMachineArn");
        Map<String, String> described = service.describe(arn);

        assertThat(described.get("name")).isEqualTo("test-machine-describe");
        assertThat(described.get("stateMachineArn")).isEqualTo(arn);
        assertThat(described).containsKey("definition");
        assertThat(described).containsKey("status");
        assertThat(described).containsKey("roleArn");
    }

    @Test
    void shouldDeleteStateMachine() {
        Map<String, String> created = service.create(new CreateStateMachineDto(
                "test-machine-delete", SIMPLE_PASS_DEFINITION, FAKE_ROLE_ARN));

        String arn = created.get("stateMachineArn");
        Map<String, String> result = service.delete(arn);

        assertThat(result.get("stateMachineArn")).isEqualTo(arn);
        assertThat(result.get("status")).isEqualTo("DELETED");
    }

    @Test
    void shouldFailToDescribeDeletedStateMachine() {
        Map<String, String> created = service.create(new CreateStateMachineDto(
                "test-machine-fail", SIMPLE_PASS_DEFINITION, FAKE_ROLE_ARN));

        String arn = created.get("stateMachineArn");
        service.delete(arn);

        assertThatThrownBy(() -> service.describe(arn))
                .isInstanceOf(Exception.class);
    }
}
