package com.freemusic.mediaservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="musicResource")
public class MusicResource {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer musicResourceId;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    private Integer uploaderId;

    private String bucketName;

    private String objectName;

    private String fileType;

    @CreationTimestamp
    private Date createdAt;

    @CreationTimestamp
    private Date updatedAt;
}

