package com.freemusic.mediaservice.controllers;

import com.freemusic.mediaservice.messages.MusicInfoRequest;
import com.freemusic.mediaservice.services.MusicResourceService;
import com.freemusic.musicwebcommon.messages.MusicResourceMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/MusicResource")
public class MusicResourceController {

    @Autowired
    private MusicResourceService musicResourceService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @RequestParam("music") MultipartFile music,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "userId") Integer userId,
            @ModelAttribute  MusicInfoRequest musicInfoRequest) {

        try {
            // Upload the files and save URLs
            Map<String, Integer> fileIds = musicResourceService.uploadFilesAndSaveUrls(music, image, video, userId);

            MusicResourceMessage musicResourceMessage = new MusicResourceMessage();
            if(fileIds.containsKey("music")) {
                musicResourceMessage.setMusicResourceId(fileIds.get("music"));
            }
            if(fileIds.containsKey("image")) {
                musicResourceMessage.setImageId(fileIds.get("image"));
            }

            if(fileIds.containsKey("video")) {
                musicResourceMessage.setVideoId(fileIds.get("video"));
            }

            File tempFile = File.createTempFile("prefix", "suffix." + getFileExtension(music.getOriginalFilename()));
            music.transferTo(tempFile); // This will write the uploaded file data to the temp file
            long durationMillis = musicResourceService.getDuration(tempFile); // get the duration of the temp file now
            double durationSecs = durationMillis;
            tempFile.delete(); // Delete the temp file after you're done with it


            musicResourceMessage.setTitle(musicInfoRequest.getTitle());
            musicResourceMessage.setBanned(musicInfoRequest.isBanned());
            musicResourceMessage.setArtistId(musicInfoRequest.getBannedOrArtistId());
            musicResourceMessage.setDuration(durationSecs);
            musicResourceMessage.setLyric(musicInfoRequest.getLyric());
            musicResourceMessage.setAlbumId(musicResourceMessage.getAlbumId());
            musicResourceMessage.setGenreIds(musicInfoRequest.getGenreIds());

            // Send music information to RabbitMQ
            rabbitTemplate.convertAndSend("MusicExchanges", "MusicMedia", musicResourceMessage);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
    public String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        //need security check
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    @GetMapping("/get/file/{musicResourceId}")
    public ResponseEntity<String> getFileURL(@PathVariable("musicResourceId") int musicResourceId) {
        String musicResourceUrl = musicResourceService.getMusicResourceUrl(musicResourceId);
        if (musicResourceUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return new ResponseEntity<>(musicResourceUrl, HttpStatus.OK);
    }


}

