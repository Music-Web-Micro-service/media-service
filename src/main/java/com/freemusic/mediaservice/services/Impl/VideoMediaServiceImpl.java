package com.freemusic.mediaservice.services.Impl;

import com.freemusic.mediaservice.exceptions.ResourceNotFoundException;
import com.freemusic.mediaservice.models.ReviewStatus;
import com.freemusic.mediaservice.models.VideoMedia;
import com.freemusic.mediaservice.repositories.VideoMediaRepository;
import com.freemusic.mediaservice.services.VideoMediaService;
import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoMediaServiceImpl implements VideoMediaService {

    private static final Logger logger = LoggerFactory.getLogger(ImageMediaServiceImpl.class);

    @Autowired
    private VideoMediaRepository videoMediaRepository;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioService minioService;

    public int uploadFileAndSaveUrl(MultipartFile file, String bucketName, int userId) throws Exception {
        String objectKey = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Upload the object
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectKey).stream(
                        file.getInputStream(), -1, 10485760).contentType(file.getContentType()).build());

        // Save the object key to the database
        VideoMedia videoMedia = saveObjectKeyToDB(objectKey, file.getContentType(), bucketName, userId);

        return videoMedia.getVideoMediaId();
    }

    private VideoMedia saveObjectKeyToDB(String objectKey, String fileType, String bucketName, int userId) {
        VideoMedia videoMedia = new VideoMedia();
        videoMedia.setFileType(fileType);
        videoMedia.setUploaderId(userId);
        videoMedia.setBucketName(bucketName);
        videoMedia.setObjectName(objectKey);
        videoMedia.setStatus(ReviewStatus.PENDING);
        return videoMediaRepository.save(videoMedia);
    }

    @Override
    public String getVideoMediaUrl(int id) {
        VideoMedia videoMedia = videoMediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MusicResource", "id", id));


        String objectName = videoMedia.getObjectName();
        String bucketName = videoMedia.getBucketName();

        String url =  minioService.getFileUrl(objectName, bucketName);
        return url;
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
    public VideoMedia getVideoMedia(int id) {
        Optional<VideoMedia> videoMediaOptional = videoMediaRepository.findById(id);
        return videoMediaOptional.orElse(null);
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
        videoMediaRepository.deleteById(mediaId);
    }
}
