package com.saidqosimov.instagrammediadownloader.controller;

import com.saidqosimov.instagrammediadownloader.config.BotConfig;
import com.saidqosimov.instagrammediadownloader.enums.Language;
import com.saidqosimov.instagrammediadownloader.enums.MessageType;
import com.saidqosimov.instagrammediadownloader.model.CodeMessage;
import com.saidqosimov.instagrammediadownloader.service.TelegramUsersService;
import com.saidqosimov.instagrammediadownloader.service.UniversalService;
import com.saidqosimov.instagrammediadownloader.utils.Constants;
import com.saidqosimov.instagrammediadownloader.utils.MainKeyboards;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GeneralController {
    private final MainKeyboards mainKeyboards;
    private final UniversalService universalService;
    private final TelegramUsersService telegramUsersService;
    private final BotConfig botConfig;

    public synchronized List<CodeMessage> getMedia(Message message, int langId) {
        List<CodeMessage> urlData = universalService.getMediaData(message, langId);
        if (urlData.isEmpty()) {
            urlData.add(getCodeMessage(Constants.COMMAND_NOT_FOUND[langId], message.getChatId(), null));
        }
        return urlData;
    }

    public synchronized List<CodeMessage> handle(Message message, int langId) {
        List<CodeMessage> response = new LinkedList<>();
        Long chatId = message.getChatId();
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals(Constants.START)) {
                response.add(getCodeMessage(Constants.START_GUIDE[langId], chatId, mainKeyboards.getGuideButton(langId)));
                return response;
            } else if (text.equals(Constants.HELP)) {
                response.add(getCodeMessage(Constants.HELP_GUIDE[langId], chatId, null));
                return response;
            } else if (text.equals(Constants.LANG)) {
                response.add(getCodeMessage(Constants.CHOOSE_LANG[langId], chatId, mainKeyboards.getLanguageButton()));
                return response;
            } else if (text.startsWith("/block ") && chatId.equals(botConfig.getAdmin())) {
                String s = text.split(" ")[1];
                if (!s.equals(botConfig.getAdmin().toString())) {
                    try {
                        response.add(getCodeMessage("Bloklandi", chatId, null));
                        telegramUsersService.blockUser(Long.parseLong(s));
                    } catch (Exception e) {
                        response.add(getCodeMessage("Xatolik", chatId, null));
                    }
                }
                return response;
            } else if (text.startsWith("/unblock ") && chatId.equals(botConfig.getAdmin())) {
                String s = text.split(" ")[1];
                try {
                    response.add(getCodeMessage("Blokdan chiqarildi", chatId, null));
                    telegramUsersService.unblockUser(Long.parseLong(s));
                } catch (Exception e) {
                    response.add(getCodeMessage("Xatolik", chatId, null));
                }
                return response;
            } else {
                response.add(getCodeMessage(Constants.COMMAND_NOT_FOUND[langId], chatId, null));
                return response;
            }
        }
        return null;
    }

    protected CodeMessage getCodeMessage(String text, Long chatId, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = SendMessage
                .builder()
                .text(text)
                .chatId(chatId)
                .build();
        if (replyKeyboard != null) {
            sendMessage.setReplyMarkup(replyKeyboard);
        }
        return CodeMessage
                .builder()
                .messageType(MessageType.SEND_MESSAGE)
                .sendMessage(sendMessage)
                .build();
    }

    public synchronized List<CodeMessage> handle(CallbackQuery callbackQuery) {
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
