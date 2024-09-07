package com.saidqosimov.instagrammediadownloader.service;

import com.saidqosimov.instagrammediadownloader.entity.MediaDataEntity;
import com.saidqosimov.instagrammediadownloader.enums.MessageType;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import com.saidqosimov.instagrammediadownloader.model.CodeMessage;
import com.saidqosimov.instagrammediadownloader.repository.MediaDataRepository;
import com.saidqosimov.instagrammediadownloader.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FindMediaFromDBService {
    private final MediaDataRepository mediaDataRepository;

    public synchronized void addMediaData(String mediaUrl, String fileId, PostType postType) {
        if (mediaUrl.startsWith("https://www.instagram.com/p/") || mediaUrl.startsWith("https://www.instagram.com/reels/") || mediaUrl.startsWith("https://www.instagram.com/reel/") || mediaUrl.startsWith("https://www.instagram.com/tv/")) {
            mediaUrl = "https://www.instagram.com/p/" + mediaUrl.split("/")[4];
        } else if (mediaUrl.startsWith("https://www.youtube.com/") || mediaUrl.startsWith("https://youtube.com/") || mediaUrl.startsWith("https://youtu.be/")) {
            mediaUrl = "https://www.youtube.com/watch?v=" + extractVideoId(mediaUrl);
        }
        MediaDataEntity mediaDataEntity = new MediaDataEntity();
        mediaDataEntity.setMediaUrl(mediaUrl);
        mediaDataEntity.setFileId(fileId);
        mediaDataEntity.setMediaType(postType);
        mediaDataRepository.save(mediaDataEntity);
    }

    public synchronized List<CodeMessage> getMediaFromDB(String mediaUrl, Long chatId) {
        if (mediaUrl.startsWith("https://www.instagram.com/p/") || mediaUrl.startsWith("https://www.instagram.com/reels/") || mediaUrl.startsWith("https://www.instagram.com/reel/") || mediaUrl.startsWith("https://www.instagram.com/tv/")) {
            mediaUrl = "https://www.instagram.com/p/" + mediaUrl.split("/")[4];
        } else if (mediaUrl.startsWith("https://www.youtube.com/") || mediaUrl.startsWith("https://youtube.com/") || mediaUrl.startsWith("https://youtu.be/")) {
            mediaUrl = "https://www.youtube.com/watch?v=" + extractVideoId(mediaUrl);
        }
        List<CodeMessage> codeMessageList = new LinkedList<>();
        List<MediaDataEntity> mediaDataEntitiesByMediaUrl = mediaDataRepository.findMediaDataEntitiesByMediaUrl(mediaUrl);
        if (mediaDataEntitiesByMediaUrl.isEmpty()) {
            return null;
        }
        for (MediaDataEntity mediaDataEntity : mediaDataEntitiesByMediaUrl) {
            if (mediaDataEntity.getMediaType().equals(PostType.VIDEO)) {
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_VIDEO)
                        .sendVideo(SendVideo.builder()
                                .video(new InputFile(mediaDataEntity.getFileId()))
                                .caption(Constants.CAPTION)
                                .chatId(chatId)
                                .build())
                        .downloadUrl(mediaDataEntity.getFileId())
                        .build();
                codeMessageList.add(codeMessage);
            } else if (mediaDataEntity.getMediaType().equals(PostType.PHOTO)) {
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_PHOTO)
                        .sendPhoto(SendPhoto.builder()
                                .photo(new InputFile(mediaDataEntity.getFileId()))
                                .caption(Constants.CAPTION)
                                .chatId(chatId)
                                .build())
                        .downloadUrl(mediaDataEntity.getFileId())
                        .build();
                codeMessageList.add(codeMessage);
            }
        }
        return codeMessageList;
    }

    public static String extractVideoId(String url) {
        if (url.contains("youtu.be/")) {
            // Extract video ID for youtu.be links
            int indexOfBe = url.indexOf("youtu.be/");
            return url.substring(indexOfBe + 9, indexOfBe + 20);
        } else if (url.contains("youtube.com/shorts/")) {
            // Extract video ID for youtube.com/shorts links
            int indexOfShorts = url.indexOf("shorts/");
            return url.substring(indexOfShorts + 7, indexOfShorts + 18);
        } else if (url.contains("youtube.com/watch?v=")) {
            // Extract video ID for youtube.com/watch links
            int indexOfV = url.indexOf("v=");
            return url.substring(indexOfV + 2, indexOfV + 13);
        } else {
            return ""; // Return null if the URL format is not recognized
        }
    }
}
