package com.saidqosimov.instagrammediadownloader.repository;

import com.saidqosimov.instagrammediadownloader.entity.MediaDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaDataRepository extends JpaRepository<MediaDataEntity, Long> {
    List<MediaDataEntity> findMediaDataEntitiesByMediaUrl(String mediaUrl);
}
