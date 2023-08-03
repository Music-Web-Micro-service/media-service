package com.freemusic.mediaservice.controllers;

import com.freemusic.mediaservice.models.VideoMedia;
import com.freemusic.mediaservice.services.VideoMediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/videoMedia")
public class VideoMediaController {
    private static final Logger logger = LoggerFactory.getLogger(VideoMediaController.class);

    @Autowired
    private VideoMediaService videoMediaService;

    // ... other methods here ...

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") int userId) throws Exception {
        String contentType = file.getContentType();
        String bucketName;

        if(contentType == null){
            return ResponseEntity.badRequest().body("Invalid content type.");
        }

        if(contentType.startsWith("video/")){
            bucketName = "video-bucket";
        } else {
            return ResponseEntity.badRequest().body("Invalid content type: " + contentType);
        }

        videoMediaService.uploadFileAndSaveUrl(file,bucketName,userId);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/get/file/{videoMediaId}")
    public ResponseEntity<String> getFileURL(@PathVariable("videoMediaId") int videoMediaId) {
        String videoMediaUrl = videoMediaService.getVideoMediaUrl(videoMediaId);
        if (videoMediaUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return new ResponseEntity<>(videoMediaUrl, HttpStatus.OK);
    }


    @DeleteMapping("/delete/file/{videoId}")
    public ResponseEntity<String> deleteFile(@PathVariable("videoId") int videoId) {
        VideoMedia videoMedia = videoMediaService.getVideoMedia(videoId);
        if (videoMedia == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("VideoMedia with id " + videoId + " not found.");
        }

        try {
            videoMediaService.deleteFile(videoMedia.getBucketName(), videoMedia.getObjectName(), videoId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while deleting file.");
        }

        return ResponseEntity.ok("File deleted successfully");
    }
}
