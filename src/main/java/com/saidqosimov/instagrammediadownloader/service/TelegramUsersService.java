package com.saidqosimov.instagrammediadownloader.service;


import com.saidqosimov.instagrammediadownloader.entity.TelegramUsers;
import com.saidqosimov.instagrammediadownloader.enums.Language;
import com.saidqosimov.instagrammediadownloader.repository.TelegramUsersRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Service
public class TelegramUsersService {
    private final TelegramUsersRepository telegramUsersRepository;

    public TelegramUsersService(TelegramUsersRepository telegramUsersRepository) {
        this.telegramUsersRepository = telegramUsersRepository;
    }

    public synchronized Boolean checkUser(Long chatId) {
        return telegramUsersRepository.findTelegramUsersByChatId(chatId).isEmpty();
    }

    public synchronized TelegramUsers getTelegramUser(Long chatId) {
        return telegramUsersRepository.getTelegramUsersByChatId(chatId);
    }

    public synchronized void changeLanguage(Long chatId, Language language) {
        TelegramUsers telegramUsers = telegramUsersRepository.getTelegramUsersByChatId(chatId);
        telegramUsers.setLang(language);
        telegramUsersRepository.save(telegramUsers);
    }

    public synchronized void blockUser(Long chatId) {
        TelegramUsers telegramUsers = telegramUsersRepository.getTelegramUsersByChatId(chatId);
        telegramUsers.setEnabled(false);
        telegramUsersRepository.save(telegramUsers);
    }

    public synchronized List<TelegramUsers> getAllTelegramUsers() {
        return telegramUsersRepository.getAllTelegramUser();
    }

    public synchronized void save(Message message) {
        Language language = Language.en;
        if (message.getFrom().getLanguageCode().equals("uz")) {
            language = Language.uz;
        } else if (message.getFrom().getLanguageCode().equals("ru")) {
            language = Language.ru;
        }
        TelegramUsers telegramBotUsers = TelegramUsers.builder()
                .chatId(message.getChatId())
                .firstName(message.getFrom().getFirstName())
                .username(message.getFrom().getUserName())
                .lang(language)
                .enabled(true)
                .build();
        System.out.println(telegramBotUsers);
        telegramUsersRepository.save(telegramBotUsers);
    }

    public synchronized void unblockUser(long chatId) {
        TelegramUsers telegramUsers = telegramUsersRepository.getTelegramUsersByChatId(chatId);
        telegramUsers.setEnabled(true);
        telegramUsersRepository.save(telegramUsers);
    }
}