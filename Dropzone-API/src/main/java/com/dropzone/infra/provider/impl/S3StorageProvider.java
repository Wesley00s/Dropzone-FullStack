package com.dropzone.infra.provider.impl;

import com.dropzone.infra.provider.StorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.time.Duration;

@Component
public class S3StorageProvider implements StorageProvider {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3StorageProvider(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public String save(String key, InputStream content, Long size, String contentType) {
        try {
            var putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putOb, RequestBody.fromInputStream(content, size));
        } catch (Exception e) {
            throw new RuntimeException("Error while connect to S3", e);
        }
        return key;
    }

    @Override
    public String generateUrl(String key, String originalName) {
        String safeFilename = originalName.replace("\"", "").replace(";", "");

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .responseContentDisposition("attachment; filename=\"" + safeFilename + "\"")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(objectRequest)
                .build();

        String url = s3Presigner.presignGetObject(presignRequest).url().toString();

        if (url.contains("localstack")) {
            return url.replace("localstack", "localhost");
        }

        return url;
    }

    @Override
    public void delete(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error while delete file from S3", e);
        }
    }
}