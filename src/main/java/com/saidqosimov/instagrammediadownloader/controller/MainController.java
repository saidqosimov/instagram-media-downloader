package com.saidqosimov.instagrammediadownloader.controller;

import com.saidqosimov.instagrammediadownloader.config.BotConfig;
import com.saidqosimov.instagrammediadownloader.entity.TelegramUsers;
import com.saidqosimov.instagrammediadownloader.enums.Language;
import com.saidqosimov.instagrammediadownloader.model.CodeMessage;
import com.saidqosimov.instagrammediadownloader.service.TelegramUsersService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class MainController extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final TelegramUsersService telegramUsersService;
    private final GeneralController generalController;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20); // 20 ta iplar soni

    @Override
    public void onUpdateReceived(Update update) {
        executorService.submit(() -> handleUpdate(update));
    }

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
                String text = message.getText();
                if (text.startsWith("https://www.instagram.com/")
                        || text.startsWith("https://www.youtube.com/")
                        || text.startsWith("https://youtube.com/")
                        || text.startsWith("https://www.tiktok.com/")
                        || text.startsWith("https://vt.tiktok.com/")
                        || text.startsWith("https://vm.tiktok.com/")
                        || text.startsWith("https://youtu.be/")
                        || text.startsWith("https://fb.watch/")
                        || text.startsWith("https://www.facebook.com/")
                        || text.startsWith("https://x.com/")
                        || text.startsWith("https://www.pinterest.com/")
                        || text.startsWith("https://pin.it/")
                        || text.startsWith("https://www.linkedin.com/")
                        || text.startsWith("https://snapchat.com/")
                ) {
                    Integer processMessageId = inProcess(chatId);
                    sendMsg(generalController.handle(message, langId));
                    deleteProcess(chatId, processMessageId);
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


    @SneakyThrows
    private synchronized Integer inProcess(Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("🔎")
                .build();
        return execute(sendMessage).getMessageId();
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
                        execute(message.getSendPhoto());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case SEND_VIDEO -> {
                    try {
                        execute(message.getSendVideo());
                    } catch (Exception e) {
                        URL url = null;
                        try {
                            url = new URL(message.getUrl());
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
                            execute(sendVideo);
                        } catch (Exception ex) {
                            SendMessage sendMessage = SendMessage
                                    .builder()
                                    .text(message.getUrl())
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
