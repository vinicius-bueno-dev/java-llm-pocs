package dev.nameless.poc.sesemail.service;

import dev.nameless.poc.sesemail.AbstractLocalStackTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.ses.model.IdentityVerificationAttributes;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityServiceTest extends AbstractLocalStackTest {

    @Autowired
    private IdentityService identityService;

    @Test
    void shouldVerifyAndListEmailIdentity() {
        identityService.verifyEmailIdentity("test@example.com");

        List<String> identities = identityService.listIdentities();
        assertThat(identities).contains("test@example.com");
    }

    @Test
    void shouldGetVerificationAttributes() {
        identityService.verifyEmailIdentity("verified@example.com");

        Map<String, IdentityVerificationAttributes> attrs =
                identityService.getIdentityVerificationAttributes(List.of("verified@example.com"));

        assertThat(attrs).containsKey("verified@example.com");
        assertThat(attrs.get("verified@example.com").verificationStatusAsString())
                .isEqualTo("Success");
    }

    @Test
    void shouldDeleteIdentity() {
        identityService.verifyEmailIdentity("delete-me@example.com");
        assertThat(identityService.listIdentities()).contains("delete-me@example.com");

        identityService.deleteIdentity("delete-me@example.com");

        assertThat(identityService.listIdentities()).doesNotContain("delete-me@example.com");
    }

    @Test
    void shouldListMultipleIdentities() {
        identityService.verifyEmailIdentity("first@example.com");
        identityService.verifyEmailIdentity("second@example.com");

        List<String> identities = identityService.listIdentities();
        assertThat(identities).containsExactlyInAnyOrder("first@example.com", "second@example.com");
    }
}
