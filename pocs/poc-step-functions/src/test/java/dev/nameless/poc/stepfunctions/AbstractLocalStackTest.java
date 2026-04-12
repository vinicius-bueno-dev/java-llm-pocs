package dev.nameless.poc.stepfunctions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.sfn.SfnClient;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.STEPFUNCTIONS;

/**
 * Base de testes de integracao. Sobe um LocalStack com Step Functions
 * e configura as propriedades AWS dinamicamente para os testes.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractLocalStackTest {

    protected static final String FAKE_ROLE_ARN = "arn:aws:iam::012345678901:role/DummyRole";

    /**
     * Definicao ASL simples de um Pass state para testes.
     */
    protected static final String SIMPLE_PASS_DEFINITION = """
            {
              "Comment": "A simple pass state machine for testing",
              "StartAt": "PassState",
              "States": {
                "PassState": {
                  "Type": "Pass",
                  "Result": "Hello from Step Functions!",
                  "End": true
                }
              }
            }
            """;

    /**
     * Definicao ASL com Wait state para testar stop de execucao.
     */
    protected static final String WAIT_DEFINITION = """
            {
              "Comment": "A state machine with a wait state for testing stop",
              "StartAt": "WaitState",
              "States": {
                "WaitState": {
                  "Type": "Wait",
                  "Seconds": 300,
                  "Next": "DoneState"
                },
                "DoneState": {
                  "Type": "Pass",
                  "End": true
                }
              }
            }
            """;

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.3"))
            .withServices(STEPFUNCTIONS);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.endpoint", () -> localStack.getEndpoint().toString());
        registry.add("aws.region", () -> localStack.getRegion());
        registry.add("aws.access-key", () -> localStack.getAccessKey());
        registry.add("aws.secret-key", () -> localStack.getSecretKey());
    }

    @Autowired
    protected SfnClient sfnClient;
}
