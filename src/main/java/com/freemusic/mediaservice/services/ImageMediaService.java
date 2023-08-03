package com.freemusic.mediaservice.services;

import com.freemusic.mediaservice.models.ImageMedia;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


public interface ImageMediaService {

    public int uploadFileAndSaveUrl(MultipartFile file, String bucketName, int userId);

    public InputStream getFile(String bucketName, String objectName) throws Exception;

    public String getImageMediaUrl(int id);

    public void deleteFile(String bucketName, String objectName, int mediaId) throws Exception;

    public ImageMedia getImageMedia(int media_id);
}
