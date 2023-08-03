package com.freemusic.mediaservice.repositories;

import com.freemusic.mediaservice.models.VideoMedia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoMediaRepository extends JpaRepository<VideoMedia,Integer> {
}
