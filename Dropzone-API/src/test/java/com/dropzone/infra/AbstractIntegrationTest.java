package com.dropzone.infra;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static final LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.2")
    ).withServices(LocalStackContainer.Service.S3);

    static {
        postgres.start();
        localstack.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);

        registry.add("aws.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("aws.region", localstack::getRegion);
        registry.add("aws.accessKeyId", localstack::getAccessKey);
        registry.add("aws.secretAccessKey", localstack::getSecretKey);
        registry.add("aws.bucket", () -> "dropzone-test-bucket");
    }
}