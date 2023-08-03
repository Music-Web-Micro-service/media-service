package com.freemusic.mediaservice.repositories;


import com.freemusic.mediaservice.models.MusicResource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicResourceRepository extends JpaRepository<MusicResource,Integer> {
}
