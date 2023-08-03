package com.freemusic.mediaservice.controllers;
import com.freemusic.mediaservice.models.ImageMedia;
import com.freemusic.mediaservice.services.ImageMediaService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/imageMedia")
public class ImageMediaController {
    private static final Logger logger = LoggerFactory.getLogger(ImageMediaController.class);

    @Autowired
    private ImageMediaService imageMediaService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") int userId) throws Exception {
        String contentType = file.getContentType();
        String bucketName;
        String fileType;

        if(contentType == null){
            return ResponseEntity.badRequest().body("Invalid content type.");
        }

        if(contentType.startsWith("image/")){
            bucketName = "image-bucket";
        } else {
            return ResponseEntity.badRequest().body("Invalid content type: " + contentType);
        }

        String fileName = file.getOriginalFilename();
        String title = (fileName != null) ? FilenameUtils.getBaseName(fileName) : null;

        imageMediaService.uploadFileAndSaveUrl(file, bucketName, userId);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/get/file/{imageMediaId}")
    public ResponseEntity<String> getFileURL(@PathVariable("imageMediaId") int imageMediaId) {
        String imageMediaUrl = imageMediaService.getImageMediaUrl(imageMediaId);
        if (imageMediaUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return new ResponseEntity<>(imageMediaUrl, HttpStatus.OK);
    }


    @DeleteMapping("/delete/file/{mediaId}")
    public ResponseEntity<String> deleteFile(@PathVariable("mediaId") int mediaId) {
        ImageMedia imageMedia = imageMediaService.getImageMedia(mediaId);
        if (imageMedia == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ImageMedia with id " + mediaId + " not found.");
        }

        try {
            imageMediaService.deleteFile(imageMedia.getBucketName(), imageMedia.getObjectName(), mediaId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while deleting file.");
        }

        return ResponseEntity.ok("File deleted successfully");
    }
}


