package com.saidqosimov.instagrammediadownloader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class YoutubeService {
    private final RestTemplate restTemplate;

    public YoutubeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public Map<PostType, String> getYoutubeMedia(String mediaUrl) {
        Map<PostType, String> map = new HashMap<>();
        String result = restTemplate.getForObject(mediaUrl, String.class);
        JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
        String downloadUrl = jsonObject.getAsJsonObject("data").get("downloadUrl").getAsString();
        map.put(PostType.VIDEO, downloadUrl);
        return map;
    }
}
