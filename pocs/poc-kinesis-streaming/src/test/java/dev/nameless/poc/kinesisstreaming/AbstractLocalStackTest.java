package dev.nameless.poc.kinesisstreaming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DeleteStreamRequest;
import software.amazon.awssdk.services.kinesis.model.ListStreamsRequest;
import software.amazon.awssdk.services.kinesis.model.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KINESIS;

/**
 * Base de testes de integracao. Sobe um LocalStack com Kinesis e, antes de
 * cada teste, limpa todos os streams existentes para garantir isolamento.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractLocalStackTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.3"))
            .withServices(KINESIS);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.endpoint", () -> localStack.getEndpoint().toString());
        registry.add("aws.region", () -> localStack.getRegion());
        registry.add("aws.access-key", () -> localStack.getAccessKey());
        registry.add("aws.secret-key", () -> localStack.getSecretKey());
    }

    @Autowired
    protected KinesisClient kinesisClient;

    @BeforeEach
    void cleanUpStreams() {
        var streams = kinesisClient.listStreams(ListStreamsRequest.builder().build())
                .streamNames();
        for (String stream : streams) {
            try {
                kinesisClient.deleteStream(DeleteStreamRequest.builder()
                        .streamName(stream)
                        .enforceConsumerDeletion(true)
                        .build());
            } catch (ResourceNotFoundException ignored) {
                // stream ja foi deletado — ok
            }
        }
    }

    /**
     * Cria um stream e aguarda ate que esteja ACTIVE.
     */
    protected void createAndWaitForStream(String streamName, int shardCount) {
        kinesisClient.createStream(b -> b.streamName(streamName).shardCount(shardCount));
        kinesisClient.waiter().waitUntilStreamExists(
                b -> b.streamName(streamName));
    }
}
