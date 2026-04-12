package dev.nameless.poc.iampolicies.service;

import dev.nameless.poc.iampolicies.AbstractLocalStackTest;
import dev.nameless.poc.iampolicies.dto.CreateRoleDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AssumeRoleServiceTest extends AbstractLocalStackTest {

    private static final String ASSUME_ROLE_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Principal": {"AWS": "arn:aws:iam::000000000000:root"},
                "Action": "sts:AssumeRole"
              }]
            }
            """;

    @Autowired
    private AssumeRoleService assumeRoleService;

    @Autowired
    private RoleService roleService;

    @Test
    void shouldAssumeRoleAndGetTemporaryCredentials() {
        Role role = roleService.createRole(
                new CreateRoleDto("assumable-role", ASSUME_ROLE_POLICY, "Role for STS test"));

        Credentials credentials = assumeRoleService.assumeRole(role.arn(), "test-session");

        assertThat(credentials.accessKeyId()).isNotBlank();
        assertThat(credentials.secretAccessKey()).isNotBlank();
        assertThat(credentials.sessionToken()).isNotBlank();
        assertThat(credentials.expiration()).isNotNull();
    }

    @Test
    void shouldGetCallerIdentity() {
        GetCallerIdentityResponse identity = assumeRoleService.getCallerIdentity();

        assertThat(identity.account()).isNotBlank();
        assertThat(identity.arn()).isNotBlank();
    }
}
