package com.saidqosimov.instagrammediadownloader.repository;


import com.saidqosimov.instagrammediadownloader.entity.TelegramUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelegramUsersRepository extends JpaRepository<TelegramUsers, Long> {
    List<TelegramUsers> findTelegramUsersByChatId(Long chatId);

    @Query(value = "select * from telegram_users", nativeQuery = true)
    List<TelegramUsers> getAllTelegramUser();

    TelegramUsers getTelegramUsersByChatId(Long chatId);
}

