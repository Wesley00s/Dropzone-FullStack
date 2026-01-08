package com.dropzone.infra.provider.impl;

import com.dropzone.infra.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class S3StorageProviderTest extends AbstractIntegrationTest {

    @Autowired
    private S3StorageProvider storageProvider;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @BeforeEach
    void setupBucket() {
        try {
            s3Client.createBucket(b -> b.bucket(bucketName));
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("Should upload file to S3 Bucket")
    void shouldSaveFile() throws IOException {
        String key = "integration-test-file.txt";
        String content = "This is a text content for S3 upload test.";

        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        long size = contentBytes.length;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);

        storageProvider.save(key, inputStream, size, "text/plain");

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(b -> b.bucket(bucketName).key(key));
        String savedContent = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(savedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("Should generate Presigned URL with localhost fix")
    void shouldGenerateUrl() {
        String key = "file-url.pdf";

        String url = storageProvider.generateUrl(key, "file-url.pdf");

        assertThat(url).isNotNull();
        assertThat(url).contains("http");
        assertThat(url).doesNotContain("localstack");
        assertThat(url).contains("X-Amz-Signature");
    }

    @Test
    @DisplayName("Should delete file from S3 Bucket")
    void shouldDeleteFile() {
        String key = "file-to-delete.txt";
        s3Client.putObject(b -> b.bucket(bucketName).key(key),
                software.amazon.awssdk.core.sync.RequestBody.fromString("trash"));

        assertThat(s3Client.headObject(b -> b.bucket(bucketName).key(key))).isNotNull();

        storageProvider.delete(key);

        assertThatThrownBy(() -> s3Client.headObject(b -> b.bucket(bucketName).key(key)))
                .isInstanceOf(NoSuchKeyException.class);
    }
}