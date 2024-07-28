package com.saidqosimov.instagrammediadownloader.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InlineKeyboardUtil {
    public static InlineKeyboardButton button(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public static InlineKeyboardButton buttonUrl(String text, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setUrl(url);
        return button;
    }

    public static List<InlineKeyboardButton> row(InlineKeyboardButton... inlineKeyboardButtons) {
        return new LinkedList<>(Arrays.asList(inlineKeyboardButtons));
    }

    public static List<List<InlineKeyboardButton>> collection(List<InlineKeyboardButton> rows) {
        return new LinkedList<>(Collections.singletonList(rows));
    }

    public static InlineKeyboardMarkup keyboard(List<List<InlineKeyboardButton>> collection) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(collection);
        return inlineKeyboardMarkup;
    }


}
