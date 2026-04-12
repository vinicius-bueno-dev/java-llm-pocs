package dev.nameless.poc.eventdriven.service;

import dev.nameless.poc.eventdriven.AbstractLocalStackTest;
import dev.nameless.poc.eventdriven.dto.CreateRuleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RuleServiceTest extends AbstractLocalStackTest {

    @Autowired
    private EventBusService eventBusService;

    @Autowired
    private RuleService ruleService;

    private String busName;

    @BeforeEach
    void setUp() {
        busName = "test-rule-bus-" + System.nanoTime();
        eventBusService.createEventBus(busName);
    }

    @Test
    void shouldCreateRuleWithEventPattern() {
        String pattern = """
                {
                  "source": ["com.nameless.orders"],
                  "detail-type": ["OrderCreated"]
                }
                """;
        CreateRuleDto dto = new CreateRuleDto(
                "test-rule-" + System.nanoTime(), "Test rule", pattern, null, true);

        Map<String, String> result = ruleService.createRule(busName, dto);
        assertNotNull(result.get("ruleArn"));
    }

    @Test
    void shouldCreateScheduledRule() {
        CreateRuleDto dto = new CreateRuleDto(
                "test-scheduled-" + System.nanoTime(), "Scheduled rule",
                null, "rate(5 minutes)", true);

        Map<String, String> result = ruleService.createRule(busName, dto);
        assertNotNull(result.get("ruleArn"));
    }

    @Test
    void shouldListRules() {
        CreateRuleDto dto = new CreateRuleDto(
                "test-list-rule-" + System.nanoTime(), null,
                "{\"source\":[\"test\"]}", null, true);
        ruleService.createRule(busName, dto);

        List<Map<String, String>> rules = ruleService.listRules(busName);
        assertFalse(rules.isEmpty());
    }

    @Test
    void shouldDescribeRule() {
        String ruleName = "test-describe-rule-" + System.nanoTime();
        CreateRuleDto dto = new CreateRuleDto(
                ruleName, "Describe test", "{\"source\":[\"test\"]}", null, true);
        ruleService.createRule(busName, dto);

        Map<String, String> rule = ruleService.describeRule(busName, ruleName);
        assertEquals(ruleName, rule.get("name"));
        assertEquals("ENABLED", rule.get("state"));
    }

    @Test
    void shouldDisableAndEnableRule() {
        String ruleName = "test-toggle-rule-" + System.nanoTime();
        CreateRuleDto dto = new CreateRuleDto(
                ruleName, null, "{\"source\":[\"test\"]}", null, true);
        ruleService.createRule(busName, dto);

        ruleService.disableRule(busName, ruleName);
        Map<String, String> disabled = ruleService.describeRule(busName, ruleName);
        assertEquals("DISABLED", disabled.get("state"));

        ruleService.enableRule(busName, ruleName);
        Map<String, String> enabled = ruleService.describeRule(busName, ruleName);
        assertEquals("ENABLED", enabled.get("state"));
    }
}
