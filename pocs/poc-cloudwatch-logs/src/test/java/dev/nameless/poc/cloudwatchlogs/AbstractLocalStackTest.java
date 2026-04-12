package dev.nameless.poc.cloudwatchlogs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.CLOUDWATCHLOGS;

/**
 * Base de testes de integracao. Sobe um LocalStack com CloudWatch Logs
 * e disponibiliza os clientes AWS para os testes filhos.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractLocalStackTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.3"))
            .withServices(CLOUDWATCHLOGS);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.endpoint", () -> localStack.getEndpoint().toString());
        registry.add("aws.region", () -> localStack.getRegion());
        registry.add("aws.access-key", () -> localStack.getAccessKey());
        registry.add("aws.secret-key", () -> localStack.getSecretKey());
    }

    @Autowired
    protected CloudWatchLogsClient logsClient;

    @Autowired
    protected CloudWatchClient cloudWatchClient;
}
