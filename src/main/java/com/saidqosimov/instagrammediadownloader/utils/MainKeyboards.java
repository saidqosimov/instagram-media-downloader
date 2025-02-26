package com.saidqosimov.instagrammediadownloader.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class MainKeyboards {
    public InlineKeyboardMarkup getGuideButton(int langId) {
        return InlineKeyboardUtil.keyboard
                (InlineKeyboardUtil.collection(
                        InlineKeyboardUtil.row(InlineKeyboardUtil.buttonUrl(Constants.TERMS_OF_USE[langId], Constants.TERMS_OF_USE_URL))));
    }


    public InlineKeyboardMarkup getLanguageButton() {
        return InlineKeyboardUtil.keyboard
                (InlineKeyboardUtil.collection(
                        InlineKeyboardUtil.row(InlineKeyboardUtil.button("\uD83C\uDDFA\uD83C\uDDFF O'zbekcha", "uz"),
                                InlineKeyboardUtil.button("\uD83C\uDDEC\uD83C\uDDE7 English", "en"),
                                InlineKeyboardUtil.button("\uD83C\uDDF7\uD83C\uDDFA Русский", "ru")
                        )
                ));
    }
}
