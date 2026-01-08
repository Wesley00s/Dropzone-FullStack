package com.dropzone.infra.provider.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageProviderUnitTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;
    @InjectMocks
    private S3StorageProvider storageProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageProvider, "bucketName", "test-bucket-mock");
    }

    @Test
    @DisplayName("Should throw RuntimeException when S3 upload fails")
    void shouldThrowExceptionWhenUploadFails() {
        String key = "error-file.txt";
        ByteArrayInputStream content = new ByteArrayInputStream("data".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(
                        S3Exception
                                .builder()
                                .message("Connection Refused")
                                .build()
                );

        assertThatThrownBy(() -> storageProvider.save(key, content, 4L, "text/plain"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error while connect to S3")
                .hasCauseInstanceOf(S3Exception.class);
    }

    @Test
    @DisplayName("Should throw RuntimeException when S3 delete fails")
    void shouldThrowExceptionWhenDeleteFails() {
        String key = "error-file.txt";

        doThrow(
                S3Exception
                        .builder()
                        .message("Access Denied")
                        .build()
        ).when(s3Client)
                .deleteObject(any(DeleteObjectRequest.class));

        assertThatThrownBy(() -> storageProvider.delete(key))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error while delete file from S3")
                .hasCauseInstanceOf(S3Exception.class);
    }

    @Test
    @DisplayName("Should replace 'localstack' with 'localhost' in generated URL")
    void shouldReplaceLocalstackHost() throws MalformedURLException {
        String key = "document.pdf";
        String rawUrl = "http://localstack:4566/test-bucket-mock/document.pdf?signature=123";

        PresignedGetObjectRequest presignedResponse = mock(PresignedGetObjectRequest.class);
        when(presignedResponse.url()).thenReturn(new URL(rawUrl));

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedResponse);

        String resultUrl = storageProvider.generateUrl(key, "document.pdf");

        assertThat(resultUrl).isEqualTo("http://localhost:4566/test-bucket-mock/document.pdf?signature=123");
        assertThat(resultUrl).doesNotContain("localstack");
    }

    @Test
    @DisplayName("Should NOT modify URL if it is a production URL (AWS real)")
    void shouldNotModifyProductionUrl() throws MalformedURLException {
        String key = "image.png";
        String productionUrl = "https://s3.us-east-1.amazonaws.com/test-bucket-mock/image.png?sig=abc";

        PresignedGetObjectRequest presignedResponse = mock(PresignedGetObjectRequest.class);
        when(presignedResponse.url()).thenReturn(URI.create(productionUrl).toURL());

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedResponse);

        String resultUrl = storageProvider.generateUrl(key, "image.png");

        assertThat(resultUrl).isEqualTo(productionUrl);
    }
}