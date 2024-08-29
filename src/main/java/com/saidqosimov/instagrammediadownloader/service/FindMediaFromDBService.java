package com.saidqosimov.instagrammediadownloader.service;

import com.saidqosimov.instagrammediadownloader.config.BotConfig;
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

    public synchronized void addMediaData(String mediaUrl, Integer messageId, PostType postType) {
        if (mediaUrl.startsWith("https://www.instagram.com/p/") || mediaUrl.startsWith("https://www.instagram.com/reels/") || mediaUrl.startsWith("https://www.instagram.com/reel/")) {
            mediaUrl = "https://www.instagram.com/p/" + mediaUrl.split("/")[4];
        }
        MediaDataEntity mediaDataEntity = new MediaDataEntity();
        mediaDataEntity.setMediaUrl(mediaUrl);
        mediaDataEntity.setMessageId(messageId);
        mediaDataEntity.setMediaType(postType);
        mediaDataRepository.save(mediaDataEntity);
    }

    public synchronized List<CodeMessage> getMediaFromDB(String mediaUrl, Long chatId, String channelId) {
        if (mediaUrl.startsWith("https://www.instagram.com/p/") || mediaUrl.startsWith("https://www.instagram.com/reels/") || mediaUrl.startsWith("https://www.instagram.com/reel/")) {
            mediaUrl = "https://www.instagram.com/p/" + mediaUrl.split("/")[4];
        }
        List<CodeMessage> codeMessageList = new LinkedList<>();
        List<MediaDataEntity> mediaDataEntitiesByMediaUrl = mediaDataRepository.findMediaDataEntitiesByMediaUrl(mediaUrl);
        if (mediaDataEntitiesByMediaUrl.isEmpty()) {
            return null;
        }
        for (MediaDataEntity mediaDataEntity : mediaDataEntitiesByMediaUrl) {
            String downloadUrl = "https://t.me/" + channelId + "/" + mediaDataEntity.getMessageId();
            if (mediaDataEntity.getMediaType().equals(PostType.VIDEO)) {
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_VIDEO)
                        .sendVideo(SendVideo.builder()
                                .video(new InputFile(downloadUrl))
                                .caption(Constants.CAPTION)
                                .chatId(chatId)
                                .build())
                        .downloadUrl(downloadUrl)
                        .build();
                codeMessageList.add(codeMessage);
            } else if (mediaDataEntity.getMediaType().equals(PostType.PHOTO)) {
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_PHOTO)
                        .sendPhoto(SendPhoto.builder()
                                .photo(new InputFile(downloadUrl))
                                .caption(Constants.CAPTION)
                                .chatId(chatId)
                                .build())
                        .downloadUrl(downloadUrl)
                        .build();
                codeMessageList.add(codeMessage);
            }
        }
        return codeMessageList;
    }
}
