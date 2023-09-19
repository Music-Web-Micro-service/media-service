package com.freemusic.mediaservice.services;



import com.freemusic.mediaservice.models.MusicResource;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MusicResourceService {
    public Map<String, Integer> uploadFilesAndSaveUrls(MultipartFile music, MultipartFile image, MultipartFile video, int userId) throws Exception;

    public MusicResource getMusicResource(int id);

    public String getMusicResourceUrl(int id);

    public String getDownloadMusicResourceUrl(int id);

    public void deleteMusicSource(int mediaId) throws Exception;

    public void updateMusicResource(int id, MusicResource updatedMusicResource) throws Exception;

    public List<MusicResource> getAllMusicResources(int page, int size);

    public long getDuration(File file) throws ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException, IOException;
}

