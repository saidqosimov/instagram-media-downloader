package com.saidqosimov.instagrammediadownloader.service;

import com.saidqosimov.instagrammediadownloader.entity.TelegramUsers;
import com.saidqosimov.instagrammediadownloader.enums.MessageType;
import com.saidqosimov.instagrammediadownloader.model.CodeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdsService {
    private final TelegramUsersService users;

    public List<CodeMessage> adsSend(Message message) {
        List<CodeMessage> codeMessages = new LinkedList<>();
        List<TelegramUsers> allUsers = users.getAllTelegramUsers();
        if (message.hasPhoto()) {
            for (TelegramUsers user : allUsers) {
                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(user.getChatId())
                        .photo(new InputFile(message.getPhoto().get(0).getFileId()))
                        .caption(message.getCaption())
                        .captionEntities(message.getCaptionEntities())
                        .replyMarkup(message.getReplyMarkup())
                        .build();
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_PHOTO)
                        .sendPhoto(sendPhoto)
                        .build();
                codeMessages.add(codeMessage);
            }
        } else if (message.hasVideo()) {
            for (TelegramUsers user : allUsers) {
                SendVideo sendVideo = SendVideo.builder()
                        .chatId(user.getChatId())
                        .video(new InputFile(message.getVideo().getFileId()))
                        .caption(message.getCaption())
                        .captionEntities(message.getCaptionEntities())
                        .replyMarkup(message.getReplyMarkup())
                        .build();
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_VIDEO)
                        .sendVideo(sendVideo)
                        .build();
                codeMessages.add(codeMessage);
            }
        } else if (message.hasText()) {
            for (TelegramUsers user : allUsers) {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(user.getChatId())
                        .text(message.getText())
                        .entities(message.getEntities())
                        .replyMarkup(message.getReplyMarkup())
                        .build();
                CodeMessage codeMessage = CodeMessage.builder()
                        .messageType(MessageType.SEND_MESSAGE)
                        .sendMessage(sendMessage)
                        .build();
                codeMessages.add(codeMessage);
            }
        }
        return codeMessages;
    }
}
