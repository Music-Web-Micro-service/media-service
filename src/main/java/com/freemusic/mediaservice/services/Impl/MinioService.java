package com.freemusic.mediaservice.services.Impl;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    @Autowired
    private MinioClient minioClient;

    public String getFileUrl(String objectName, String bucketName){

        try {

            // Get presigned URL valid for one hour
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(60, TimeUnit.MINUTES)
                            .build()
            );

            return url;

        } catch(MinioException e) {
            // Handle the exception accordingly
            throw new RuntimeException("Error getting URL for object", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectWriteResponse saveObject(String bucketName, String objectName, MultipartFile file) {
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                            file.getInputStream(), -1, 10485760).contentType(file.getContentType()).build());
            return objectWriteResponse;
        } catch (MinioException e) {
            logger.error("Error while trying to upload the file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while trying to upload the file", e);
        } catch (IOException e) {
            logger.error("Error while reading the file", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while reading the file", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

}
