package com.saidqosimov.instagrammediadownloader.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Data
public class BotConfig {
    @Value("${bot.name}")
    String username;
    @Value("${bot.token}")
    String token;
    @Value("${bot.admin}")
    Long admin;
    @Value("${bot.channel}")
    String channelId;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}