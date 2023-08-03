package com.freemusic.mediaservice.models;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="imageMedia")
public class ImageMedia {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer imageMediaId;

    private Integer uploaderId;

    private String bucketName;

    private String objectName;

    private String fileType;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}



