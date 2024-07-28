package com.saidqosimov.instagrammediadownloader.controller;

import com.saidqosimov.instagrammediadownloader.enums.Language;
import com.saidqosimov.instagrammediadownloader.enums.MessageType;
import com.saidqosimov.instagrammediadownloader.model.CodeMessage;
import com.saidqosimov.instagrammediadownloader.service.TelegramUsersService;
import com.saidqosimov.instagrammediadownloader.service.UniversalService;
import com.saidqosimov.instagrammediadownloader.utils.Constants;
import com.saidqosimov.instagrammediadownloader.utils.MainKeyboards;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.LinkedList;
import java.util.List;

@Component
public class GeneralController {
    private final MainKeyboards mainKeyboards;
    private final UniversalService universalService;
    private final TelegramUsersService telegramUsersService;

    public GeneralController(MainKeyboards mainKeyboards, UniversalService universalService, TelegramUsersService telegramUsersService) {
        this.mainKeyboards = mainKeyboards;
        this.universalService = universalService;
        this.telegramUsersService = telegramUsersService;
    }

    public List<CodeMessage> handle(Message message, int langId) {
        List<CodeMessage> response = new LinkedList<>();
        Long chatId = message.getChatId();
        CodeMessage codeMessage;
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals(Constants.START)) {
                SendMessage sendMessage = SendMessage
                        .builder()
                        .text(Constants.START_GUIDE[langId])
                        .replyMarkup(mainKeyboards.getGuideButton(langId))
                        .chatId(chatId)
                        .build();
                codeMessage = CodeMessage
                        .builder()
                        .messageType(MessageType.SEND_MESSAGE)
                        .sendMessage(sendMessage)
                        .build();
                response.add(codeMessage);
                return response;
            } else if (text.startsWith("https://")) {
                List<CodeMessage> urlData = universalService.getMediaData(message, langId);
                if (!urlData.isEmpty()) {
                    return urlData;
                }
            } else if (text.equals(Constants.HELP)) {
                SendMessage sendMessage = SendMessage
                        .builder()
                        .text(Constants.HELP_GUIDE[langId])
                        .chatId(chatId)
                        .build();
                codeMessage = CodeMessage
                        .builder()
                        .messageType(MessageType.SEND_MESSAGE)
                        .sendMessage(sendMessage)
                        .build();
                response.add(codeMessage);
                return response;
            } else if (text.equals(Constants.LANG)) {
                SendMessage sendMessage = SendMessage
                        .builder()
                        .text(Constants.CHOOSE_LANG[langId])
                        .replyMarkup(mainKeyboards.getLanguageButton())
                        .chatId(chatId)
                        .build();
                codeMessage = CodeMessage
                        .builder()
                        .messageType(MessageType.SEND_MESSAGE)
                        .sendMessage(sendMessage)
                        .build();
                response.add(codeMessage);
                return response;
            } else {
                SendMessage sendMessage = SendMessage
                        .builder()
                        .text(Constants.COMMAND_NOT_FOUND[langId])
                        .chatId(chatId)
                        .build();
                codeMessage = CodeMessage
                        .builder()
                        .messageType(MessageType.SEND_MESSAGE)
                        .sendMessage(sendMessage)
                        .build();
                response.add(codeMessage);
                return response;
            }
        }
        return null;
    }

    public List<CodeMessage> handle(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        System.out.println(data);
        Long chatId = callbackQuery.getFrom().getId();
        List<CodeMessage> codeMessageList = new LinkedList<>();
        CodeMessage codeMessage = new CodeMessage();
        codeMessage.setMessageType(MessageType.EDIT_MESSAGE);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessageText.setReplyMarkup(mainKeyboards.getLanguageButton());
        switch (data) {
            case "uz":
                telegramUsersService.changeLanguage(chatId, Language.uz);
                editMessageText.setText(Constants.CHOOSE_LANG[0]);
                break;
            case "en":
                telegramUsersService.changeLanguage(chatId, Language.en);
                editMessageText.setText(Constants.CHOOSE_LANG[1]);
                break;
            case "ru":
                telegramUsersService.changeLanguage(chatId, Language.ru);
                editMessageText.setText(Constants.CHOOSE_LANG[2]);
                break;
        }
        codeMessage.setEditMessageText(editMessageText);
        codeMessageList.add(codeMessage);
        return codeMessageList;
    }
}
