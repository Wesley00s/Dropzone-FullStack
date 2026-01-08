package com.dropzone.domain.repo;

import com.dropzone.domain.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM FileMetadata f")
    Long getTotalStorageUsage();
}