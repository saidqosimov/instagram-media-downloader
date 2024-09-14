package com.saidqosimov.instagrammediadownloader.controller;

import com.saidqosimov.instagrammediadownloader.config.BotConfig;
import com.saidqosimov.instagrammediadownloader.entity.TelegramUsers;
import com.saidqosimov.instagrammediadownloader.enums.Language;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import com.saidqosimov.instagrammediadownloader.model.CodeMessage;
import com.saidqosimov.instagrammediadownloader.service.FindMediaFromDBService;
import com.saidqosimov.instagrammediadownloader.service.TelegramUsersService;
import com.saidqosimov.instagrammediadownloader.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.*;


@Component
@RequiredArgsConstructor
public class MainController extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final TelegramUsersService telegramUsersService;
    private final FindMediaFromDBService findMediaFromDBService;
    private final GeneralController generalController;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final ConcurrentHashMap<Long, Boolean> userProcessing = new ConcurrentHashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        long userId = update.getMessage().getFrom().getId();

        // Agar foydalanuvchining so'rovi ishlanayotgan bo'lsa, yangi so'rovni qabul qilmaslik
        if (userProcessing.getOrDefault(userId, false)) {
            return;
        }
        userProcessing.put(userId, true);
        executorService.submit(() -> {
            try {
                handleUpdate(update);
            } finally {
                userProcessing.remove(userId);
            }
        });
    }


/*
     // 20 ta iplar soni

    @Override
    public void onUpdateReceived(Update update) {
        executorService.submit(() -> handleUpdate(update));
    }
*/

    private void handleUpdate(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            Message message = update.getMessage();
            if (update.getMessage().hasText()) {
                if (telegramUsersService.checkUser(chatId)) {
                    telegramUsersService.save(message);
                }
                TelegramUsers telegramUser = telegramUsersService.getTelegramUser(chatId);
                Language lang = telegramUser.getLang();
                int langId = switch (lang) {
                    case uz -> 0;
                    case en -> 1;
                    case ru -> 2;
                };
                if (!telegramUser.isEnabled()) {
                    sendMsg(Constants.IS_BLOCKED[langId], chatId);
                    return;
                }
                String text = message.getText();
                if (text.contains("https://www.instagram.com/")
                        || text.contains("https://www.youtube.com/")
                        || text.contains("https://youtube.com/")
                        || text.contains("https://www.tiktok.com/")
                        || text.contains("https://vt.tiktok.com/")
                        || text.contains("https://vm.tiktok.com/")
                        || text.contains("https://m.tiktok.com/")
                        || text.contains("https://youtu.be/")
                        || text.contains("https://fb.watch/")
                        || text.contains("https://www.facebook.com/")
                        || text.contains("https://x.com/")
                        || text.contains("https://www.pinterest.com/")
                        || text.contains("https://pin.it/")
                        || text.contains("https://www.linkedin.com/")
                        || text.contains("https://snapchat.com/")
                ) {
                    Integer processMessageId = inProcess(chatId);
                    try {
                        sendMsg(generalController.getMedia(message, langId));
                        deleteProcess(chatId, processMessageId);
                    } catch (Exception e) {
                        deleteProcess(chatId, processMessageId);
                    }
                } else {
                    sendMsg(generalController.handle(message, langId));
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            System.out.println(data);
            if (data.equals("uz") || data.equals("en") || data.equals("ru")) {
                sendMsg(generalController.handle(callbackQuery));
            }
        }

    }

/*    @SneakyThrows
    private Integer forwardMessage(Long fromChatId, Integer messageId) {
        ForwardMessage forwardMessage = ForwardMessage.builder()
                .chatId("@" + botConfig.getChannel())
                .messageId(messageId)
                .fromChatId(fromChatId)
                .build();
        Message execute = execute(forwardMessage);
        SendMessage sendMessage = SendMessage.builder()
                .chatId("@" + botConfig.getChannel())
                .replyToMessageId(execute.getMessageId())
                .text(fromChatId.toString())
                .build();
        execute(sendMessage);
        return execute.getMessageId();
    }*/

    @SneakyThrows
    private synchronized Integer inProcess(Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("🔎")
                .build();
        return execute(sendMessage).getMessageId();
    }

    @SneakyThrows
    private synchronized void sendMsg(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        execute(sendMessage);
    }

    @SneakyThrows
    private synchronized void deleteProcess(Long chatId, Integer processMessageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(processMessageId)
                .build();
        execute(deleteMessage);
    }

    private synchronized void sendMsg(List<CodeMessage> messageList) {
        for (CodeMessage message : messageList) {
            switch (message.getMessageType()) {
                case SEND_MESSAGE -> {
                    try {
                        execute(message.getSendMessage());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case EDIT_MESSAGE -> {
                    try {
                        execute(message.getEditMessageText());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case DELETE_MESSAGE -> {
                    try {
                        execute(message.getDeleteMessage());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case SEND_PHOTO -> {
                    try {
                        Message execute = execute(message.getSendPhoto());
                        if (message.getMediaUrl() != null) {
                            findMediaFromDBService.addMediaData(message.getMediaUrl(), execute.getPhoto().get(0).getFileId(), PostType.PHOTO);
                        }
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case SEND_VIDEO -> {
                    try {
                        Message execute = execute(message.getSendVideo());
                        if (message.getMediaUrl() != null) {
                            findMediaFromDBService.addMediaData(message.getMediaUrl(), execute.getVideo().getFileId(), PostType.VIDEO);
                        }
                    } catch (Exception e) {
                        URL url = null;
                        try {
                            url = new URL(message.getDownloadUrl());
                        } catch (MalformedURLException ex) {
                            throw new RuntimeException(ex);
                        }
                        InputStream inputStream = null;
                        try {
                            inputStream = url.openStream();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        InputFile inputFile = new InputFile(inputStream, "video.mp4");
                        SendVideo sendVideo = new SendVideo();
                        sendVideo.setChatId(message.getSendVideo().getChatId());
                        sendVideo.setCaption("⬇\uFE0F @Media_LoaderBot");
                        sendVideo.setVideo(inputFile);
                        try {
                            Message execute = execute(sendVideo);
/*                            GetFile getFileRequest = new GetFile(execute.getVideo().getFileId());
                            File file = execute(getFileRequest);
                            String fileUrl = "https://api.telegram.org/file/bot" + botConfig.getToken() + "/" + file.getFilePath();
                            System.out.println(fileUrl);*/
                            if (message.getMediaUrl() != null) {
                                findMediaFromDBService.addMediaData(message.getMediaUrl(), execute.getVideo().getFileId(), PostType.VIDEO);
                            }

                        } catch (Exception ex) {
                            SendMessage sendMessage = SendMessage
                                    .builder()
                                    .text(message.getDownloadUrl())
                                    .chatId(message.getSendVideo().getChatId())
                                    .build();
                            try {
                                execute(sendMessage);
                            } catch (TelegramApiException exc) {
                                throw new RuntimeException(exc);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
