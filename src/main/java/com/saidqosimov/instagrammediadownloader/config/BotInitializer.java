package com.saidqosimov.instagrammediadownloader.config;

import com.saidqosimov.instagrammediadownloader.controller.MainController;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    private final MainController mainController;

    public BotInitializer(MainController mainController) {
        this.mainController = mainController;
    }


    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        TelegramBotsApi telegramBotApi = null;
        try {
            telegramBotApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        try {
            telegramBotApi.registerBot(mainController);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}