package com.dropzone.infra.provider;

import java.io.InputStream;

public interface StorageProvider {
    String save(String key, InputStream content, Long size, String contentType);
    String generateUrl(String key, String originalName);
    void delete(String key);
}