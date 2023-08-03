package com.freemusic.mediaservice.services;

import com.freemusic.mediaservice.models.VideoMedia;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface VideoMediaService {

    public int uploadFileAndSaveUrl(MultipartFile file, String bucketName, int userId) throws Exception;

    public InputStream getFile(String bucketName, String objectName) throws Exception;

    public String getVideoMediaUrl(int id);

    public void deleteFile(String bucketName, String objectName, int mediaId) throws Exception;

    public VideoMedia getVideoMedia(int media_id);
}
