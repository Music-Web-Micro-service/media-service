package com.freemusic.mediaservice.services.Impl;

import com.freemusic.mediaservice.exceptions.ResourceNotFoundException;
import com.freemusic.mediaservice.models.ImageMedia;
import com.freemusic.mediaservice.models.ReviewStatus;
import com.freemusic.mediaservice.repositories.ImageMediaRepository;
import com.freemusic.mediaservice.services.ImageMediaService;
import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImageMediaServiceImpl implements ImageMediaService {
    private static final Logger logger = LoggerFactory.getLogger(ImageMediaServiceImpl.class);

    @Autowired
    private ImageMediaRepository imageMediaRepository;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioService minioService;

    public int uploadFileAndSaveUrl(MultipartFile file, String bucketName, int userId){
        String objectName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        try {
            // Upload the object
            minioService.saveObject(bucketName, objectName, file);
            ImageMedia imageMedia = saveObjectKeyToDB(objectName, file.getContentType(), bucketName, userId);
            return imageMedia.getImageMediaId();
        } catch (ResponseStatusException e) {
            logger.error("Failed to upload file: {}", e.getReason(), e);
            throw e;
        }
    }

    private ImageMedia saveObjectKeyToDB(String objectName, String fileType, String bucketName, int userId) {
        ImageMedia imageMedia = new ImageMedia();
        imageMedia.setFileType(fileType);
        imageMedia.setUploaderId(userId);
        imageMedia.setBucketName(bucketName);
        imageMedia.setObjectName(objectName);
        imageMedia.setStatus(ReviewStatus.PENDING);
        return imageMediaRepository.save(imageMedia);
    }


    // Get file
    public InputStream getFile(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    @Override
    public String getImageMediaUrl(int id) {
        ImageMedia imageMedia = imageMediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MusicResource", "id", id));


        String objectName = imageMedia.getObjectName();
        String bucketName = imageMedia.getBucketName();

        String url =  minioService.getFileUrl(objectName, bucketName);
        return url;
    }

    @Override
    public ImageMedia getImageMedia(int id) {
        Optional<ImageMedia> mediaOptional = imageMediaRepository.findById(id);
        return mediaOptional.orElse(null);
    }

    // Delete file
    public void deleteFile(String bucketName, String objectName, int mediaId) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        // Delete from DB only if file deleted successfully from MinIO
        imageMediaRepository.deleteById(mediaId);
    }
}

