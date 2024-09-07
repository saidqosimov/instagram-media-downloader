package com.saidqosimov.instagrammediadownloader.entity;

import com.saidqosimov.instagrammediadownloader.enums.PostType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "media_urls")
public class MediaDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mediaUrl;
    private String fileId;
    @Enumerated(EnumType.STRING)
    private PostType mediaType;
}
