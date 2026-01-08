package com.dropzone.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String originalName;
    private String storageKey;
    private String contentType;
    private Long size;
    @Column(updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}