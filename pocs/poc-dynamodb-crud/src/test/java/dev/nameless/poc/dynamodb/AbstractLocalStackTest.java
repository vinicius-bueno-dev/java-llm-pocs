package dev.nameless.poc.dynamodb;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;

/**
 * Base de testes de integracao. Sobe um LocalStack com DynamoDB e, antes de
 * cada teste, recria as tabelas {@code users} e {@code events} do zero para
 * garantir isolamento. Replica fielmente o esquema definido no modulo
 * Terraform {@code infra/localstack/modules/dynamodb}.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractLocalStackTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.3"))
            .withServices(DYNAMODB);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.endpoint", () -> localStack.getEndpoint().toString());
        registry.add("aws.region", () -> localStack.getRegion());
        registry.add("aws.access-key", () -> localStack.getAccessKey());
        registry.add("aws.secret-key", () -> localStack.getSecretKey());
    }

    @Autowired
    protected DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.users-table-name}")
    protected String usersTable;

    @Value("${aws.dynamodb.events-table-name}")
    protected String eventsTable;

    @Value("${aws.dynamodb.gsi-by-email}")
    protected String gsiByEmail;

    @Value("${aws.dynamodb.lsi-by-created-at}")
    protected String lsiByCreatedAt;

    @BeforeEach
    void resetTables() {
        safeDelete(usersTable);
        safeDelete(eventsTable);
        createUsersTable();
        createEventsTable();
    }

    private void safeDelete(String name) {
        try {
            dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName(name).build());
        } catch (ResourceNotFoundException ignored) {
            // tabela nao existe ainda — ok
        }
    }

    private void createUsersTable() {
        try {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(usersTable)
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("email").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("createdAt").attributeType(ScalarAttributeType.S).build())
                    .keySchema(
                            KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build())
                    .globalSecondaryIndexes(GlobalSecondaryIndex.builder()
                            .indexName(gsiByEmail)
                            .keySchema(KeySchemaElement.builder().attributeName("email").keyType(KeyType.HASH).build())
                            .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                            .build())
                    .localSecondaryIndexes(LocalSecondaryIndex.builder()
                            .indexName(lsiByCreatedAt)
                            .keySchema(
                                    KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build(),
                                    KeySchemaElement.builder().attributeName("createdAt").keyType(KeyType.RANGE).build())
                            .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                            .build())
                    .build());

            dynamoDbClient.updateTimeToLive(UpdateTimeToLiveRequest.builder()
                    .tableName(usersTable)
                    .timeToLiveSpecification(TimeToLiveSpecification.builder()
                            .attributeName("expiresAt")
                            .enabled(true)
                            .build())
                    .build());
        } catch (ResourceInUseException ignored) {
            // tabela ja existe — ok
        }
    }

    private void createEventsTable() {
        try {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(eventsTable)
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("eventId").attributeType(ScalarAttributeType.S).build())
                    .keySchema(
                            KeySchemaElement.builder().attributeName("eventId").keyType(KeyType.HASH).build())
                    .build());
        } catch (ResourceInUseException ignored) {
            // ok
        }
    }
}
