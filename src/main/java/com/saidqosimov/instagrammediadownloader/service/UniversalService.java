package com.saidqosimov.instagrammediadownloader.service;

import com.saidqosimov.instagrammediadownloader.enums.MessageType;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import com.saidqosimov.instagrammediadownloader.enums.ServiceType;
import com.saidqosimov.instagrammediadownloader.model.CodeMessage;
import com.saidqosimov.instagrammediadownloader.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UniversalService {
    private final InstagramService instagramService;
    private final GetInDeviceService getInDeviceService;
    private final FindMediaFromDBService findMediaFromDBService;

    public synchronized List<CodeMessage> getMediaData(Message message, int langId) {
        String mediaUrl = extractURL(message.getText());
        System.out.println(mediaUrl);
        Long chatId = message.getChatId();
        List<CodeMessage> codeMessageList = new LinkedList<>();
        Map<ServiceType, String> requestUrl = getRequestUrl(mediaUrl);
        List<CodeMessage> mediaFromDB = findMediaFromDBService.getMediaFromDB(mediaUrl, chatId);
        if (mediaFromDB != null) {
            return mediaFromDB;
        }
        if (requestUrl == null) {
            CodeMessage codeMessage = CodeMessage.builder()
                    .messageType(MessageType.SEND_MESSAGE)
                    .sendMessage(SendMessage.builder()
                            .text(Constants.URL_ERROR[langId])
                            .chatId(chatId)
                            .build())
                    .build();
            codeMessageList.add(codeMessage);
            return codeMessageList;
        } else if (requestUrl.containsKey(ServiceType.INSTAGRAM)) {
            System.out.println(requestUrl.get(ServiceType.INSTAGRAM));
            List<Map<PostType, String>> instagramMedia = instagramService.getInstagramMedia(requestUrl.get(ServiceType.INSTAGRAM));
            if (instagramMedia != null) {
                return getCodeMessages(chatId, codeMessageList, instagramMedia, mediaUrl);
            }
        } else if (requestUrl.containsKey(ServiceType.YOUTUBE)) {

        } else if (requestUrl.containsKey(ServiceType.GET_IN_DEVICE) || requestUrl.containsKey(ServiceType.FACEBOOK)) {
            List<Map<PostType, String>> getInDeviceMedia = getInDeviceService.getInDeviceMedia(requestUrl.get(ServiceType.GET_IN_DEVICE));
            if (getInDeviceMedia != null) {
                return getCodeMessages(chatId, codeMessageList, getInDeviceMedia, mediaUrl);
            }
        }
        CodeMessage codeMessage = new CodeMessage();
        codeMessage.setMessageType(MessageType.SEND_MESSAGE);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(Constants.MEDIA_DOWNLOAD_ERROR[langId])
                .build();
        codeMessage.setSendMessage(sendMessage);
        codeMessageList.add(codeMessage);
        return codeMessageList;
    }

    private synchronized List<CodeMessage> getCodeMessage(Long chatId, List<CodeMessage> codeMessageList, Map<PostType, String> videoData, String mediaUrl) {
        CodeMessage codeMessage = CodeMessage.builder()
                .messageType(MessageType.SEND_VIDEO)
                .sendVideo(SendVideo.builder()
                        .video(new InputFile(videoData.get(PostType.VIDEO)))
                        .caption(Constants.CAPTION)
                        .chatId(chatId)
                        .build())
                .downloadUrl(videoData.get(PostType.VIDEO))
                .mediaUrl(mediaUrl)
                .build();
        codeMessageList.add(codeMessage);
        return codeMessageList;
    }

    private synchronized List<CodeMessage> getCodeMessages(Long chatId, List<CodeMessage> codeMessageList, List<Map<PostType, String>> postData, String mediaUrl) {
        for (Map<PostType, String> map : postData) {
            if (map.containsKey(PostType.VIDEO)) {
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_VIDEO)
                        .sendVideo(SendVideo.builder()
                                .video(new InputFile(map.get(PostType.VIDEO)))
                                .caption(Constants.CAPTION)
                                .chatId(chatId)
                                .build())
                        .downloadUrl(map.get(PostType.VIDEO))
                        .mediaUrl(mediaUrl)
                        .build();
                codeMessageList.add(codeMessage);
            } else if (map.containsKey(PostType.PHOTO)) {
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_PHOTO)
                        .sendPhoto(SendPhoto.builder()
                                .photo(new InputFile(map.get(PostType.PHOTO)))
                                .caption(Constants.CAPTION)
                                .chatId(chatId)
                                .build())
                        .downloadUrl(map.get(PostType.PHOTO))
                        .mediaUrl(mediaUrl)
                        .build();
                codeMessageList.add(codeMessage);
            }
        }
        return codeMessageList;
    }

    private synchronized Map<ServiceType, String> getRequestUrl(String mediaUrl) {
        Map<ServiceType, String> map = new HashMap<>();
        String baseUrl = "https://rest-instagram.up.railway.app/api/";
        //String baseUrl = "http://35.159.83.128:8085/api/";
        //String baseUrl = "http://localhost:8085/api/";
        if (
                mediaUrl.startsWith("https://www.pinterest.com/")
                        || mediaUrl.startsWith("https://pin.it/")
                        || mediaUrl.startsWith("https://snapchat.com/")
                        || mediaUrl.startsWith("https://www.snapchat.com/")
                        || mediaUrl.startsWith("https://www.facebook.com/")
                        || mediaUrl.startsWith("https://fb.watch/")
                        || mediaUrl.startsWith("https://www.tiktok.com/")
                        || mediaUrl.startsWith("https://vt.tiktok.com/")
                        || mediaUrl.startsWith("https://vm.tiktok.com/")
                        || mediaUrl.startsWith("https://www.instagram.com/reel")
                        || mediaUrl.startsWith("https://www.instagram.com/reels")
                        || mediaUrl.startsWith("https://www.instagram.com/p")
                        || mediaUrl.startsWith("https://www.instagram.com/tv")
                        || mediaUrl.startsWith("https://www.instagram.com/share")
            // mediaUrl.startsWith("https://x.com/")
        ) {
            map.put(ServiceType.GET_IN_DEVICE, baseUrl.concat("getindevice-downloader/param?url=").concat(mediaUrl));
            return map;
        } /*else if (mediaUrl.startsWith("https://www.instagram.com/reel")
                || mediaUrl.startsWith("https://www.instagram.com/reels")
                || mediaUrl.startsWith("https://www.instagram.com/p")
                || mediaUrl.startsWith("https://www.instagram.com/tv")) {
            map.put(ServiceType.INSTAGRAM, baseUrl.concat("instagram-photo-video-carousel-downloader/param?url=").concat(mediaUrl));

            return map;
        } else if (mediaUrl.startsWith("https://youtube.com/")
                || mediaUrl.startsWith("https://www.youtube.com/")
                || mediaUrl.startsWith("https://youtu.be/")) {
            map.put(ServiceType.YOUTUBE, baseUrl.concat("youtube-video-downloader/param?url=").concat(mediaUrl));
            return map;
        }*/
        return null;
    }

    public static String extractURL(String text) {
        String urlPattern = "\\b(?:https?://|www\\.)\\S+\\b";
        Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
