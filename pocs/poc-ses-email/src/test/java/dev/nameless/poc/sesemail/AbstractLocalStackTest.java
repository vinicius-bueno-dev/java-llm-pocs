package dev.nameless.poc.sesemail;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.DeleteIdentityRequest;
import software.amazon.awssdk.services.ses.model.DeleteTemplateRequest;
import software.amazon.awssdk.services.ses.model.ListIdentitiesRequest;
import software.amazon.awssdk.services.ses.model.ListTemplatesRequest;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SES;

/**
 * Base de testes de integracao. Sobe um LocalStack com SES e, antes de
 * cada teste, limpa identidades e templates para garantir isolamento.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractLocalStackTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.3"))
            .withServices(SES);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.endpoint", () -> localStack.getEndpoint().toString());
        registry.add("aws.region", () -> localStack.getRegion());
        registry.add("aws.access-key", () -> localStack.getAccessKey());
        registry.add("aws.secret-key", () -> localStack.getSecretKey());
    }

    @Autowired
    protected SesClient sesClient;

    @BeforeEach
    void cleanSesState() {
        // Remove todas as identidades
        sesClient.listIdentities(ListIdentitiesRequest.builder().build())
                .identities()
                .forEach(identity -> sesClient.deleteIdentity(
                        DeleteIdentityRequest.builder().identity(identity).build()));

        // Remove todos os templates
        sesClient.listTemplates(ListTemplatesRequest.builder().build())
                .templatesMetadata()
                .forEach(meta -> sesClient.deleteTemplate(
                        DeleteTemplateRequest.builder().templateName(meta.name()).build()));
    }
}
