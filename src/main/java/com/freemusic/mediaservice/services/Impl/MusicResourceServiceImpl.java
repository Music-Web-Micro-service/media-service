package com.freemusic.mediaservice.services.Impl;

import com.freemusic.mediaservice.exceptions.ResourceNotFoundException;
import com.freemusic.mediaservice.models.MusicResource;
import com.freemusic.mediaservice.models.ReviewStatus;
import com.freemusic.mediaservice.repositories.MusicResourceRepository;
import com.freemusic.mediaservice.services.ImageMediaService;
import com.freemusic.mediaservice.services.MusicResourceService;
import com.freemusic.mediaservice.services.VideoMediaService;
import io.minio.*;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class MusicResourceServiceImpl implements MusicResourceService {

    private static final Logger logger = LoggerFactory.getLogger(MusicResourceServiceImpl.class);

    @Autowired
    private MusicResourceRepository musicResourceRepository;

    @Autowired
    private VideoMediaService videoMediaService;

    @Autowired
    private ImageMediaService imageMediaService;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioService minioService;

    @Override
    public Map<String, Integer> uploadFilesAndSaveUrls(MultipartFile music, MultipartFile image, MultipartFile video, int userId) {
        Map<String, Integer> fileIds = new HashMap<>();
        String ImageBucketName = "image-bucket";
        String videoBucketName = "video-bucket";
        String musicBucketName = "music-bucket";

        // Upload music
        try {
            Integer musicId = uploadFileAndReturnId(music, musicBucketName, userId);
            if(musicId != null){
                fileIds.put("music", musicId);
            }
        } catch (ResponseStatusException e) {
            logger.error("Failed to upload music file: {}", e.getReason(), e);
        }

        // Upload video, if present
        if (video != null) {
            try {
                int videoId = videoMediaService.uploadFileAndSaveUrl(video, videoBucketName, userId);
                fileIds.put("video", videoId);
            } catch (ResponseStatusException e) {
                logger.error("Failed to upload video file: {}", e.getReason(), e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Upload image, if present
        if (image != null) {
            try {
                int imageId = imageMediaService.uploadFileAndSaveUrl(image, ImageBucketName, userId);
                fileIds.put("image", imageId);
            } catch (Exception e) {
                logger.error("Failed to upload image file: {}", e);
            }
        }

        return fileIds;
    }

    private int uploadFileAndReturnId(MultipartFile file, String bucketName, int userId) throws ResponseStatusException {
        String objectName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        try {
            // Upload the object
            minioService.saveObject(bucketName, objectName, file);
            return saveObjectKeyToDB(bucketName, objectName, file.getContentType(), userId);
        } catch (ResponseStatusException e) {
            logger.error("Failed to upload file: {}", e.getReason(), e);
            throw e;
        }
    }

    private int saveObjectKeyToDB(String bucketName, String objectName, String contentType, int userId) {
        MusicResource musicResource = new MusicResource();
        musicResource.setBucketName(bucketName);
        musicResource.setObjectName(objectName);
        musicResource.setFileType(contentType);
        musicResource.setUploaderId(userId);
        // Save the object key instead of URL
        musicResource.setStatus(ReviewStatus.PENDING);
        musicResource = musicResourceRepository.save(musicResource);

        return musicResource.getMusicResourceId();
    }

    @Override
    public MusicResource getMusicResource(int id) {
        return musicResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MusicResource", "id", id));
    }

    @Override
    public String getMusicResourceUrl(int id) {
        MusicResource musicResource = musicResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MusicResource", "id", id));


            String objectName = musicResource.getObjectName();
            String bucketName = musicResource.getBucketName();

           String url =  minioService.getFileUrl(objectName, bucketName);
           return url;
    }

    @Override
    public void deleteMusicSource(int mediaId) throws Exception {
        MusicResource musicResource = musicResourceRepository.findById(mediaId).orElse(null);
        String bucketName = "music-bucket";

        if (musicResource != null) {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(musicResource.getObjectName())
                            .build());

            musicResourceRepository.deleteById(mediaId);
        } else {
            throw new Exception("Media not found");
        }
    }


    @Override
    public void updateMusicResource(int id, MusicResource updatedMusicResource) throws Exception {
        MusicResource musicResource = musicResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MusicResource", "id", id));

        // Copy fields from updatedMusicResource to musicResource here

        musicResourceRepository.save(musicResource);
    }

    @Override
    public List<MusicResource> getAllMusicResources(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MusicResource> musicResourcePage = musicResourceRepository.findAll(pageable);

        return musicResourcePage.getContent();
    }

    public long getDuration(File file) throws ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException, IOException {
        AudioFile audioFile = AudioFileIO.read(file);
        return audioFile.getAudioHeader().getTrackLength();
    }
}

