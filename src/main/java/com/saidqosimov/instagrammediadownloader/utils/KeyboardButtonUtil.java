package com.saidqosimov.instagrammediadownloader.utils;


import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class KeyboardButtonUtil {
    public static KeyboardButton button(String text) {
        return new KeyboardButton(text);
    }

    public static KeyboardRow row(KeyboardButton... keyboardButtons) {
        KeyboardRow row = new KeyboardRow();
        row.addAll(Arrays.asList(keyboardButtons));
        return row;
    }

    public static List<KeyboardRow> collection(KeyboardRow... rows) {
        return new LinkedList<>(Arrays.asList(rows));
    }

    public static ReplyKeyboardMarkup keyboard(List<KeyboardRow> collection) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(collection);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }
}