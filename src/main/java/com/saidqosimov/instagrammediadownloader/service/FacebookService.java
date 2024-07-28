package com.saidqosimov.instagrammediadownloader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class FacebookService {
    private final RestTemplate restTemplate;

    public FacebookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<PostType, String> getFbMedia(String mediaUrl) {
        try {
            Map<PostType, String> map = new HashMap<>();
            String result = restTemplate.getForObject(mediaUrl, String.class);
            JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(result)).getAsJsonObject();
            JsonArray fbUrls = jsonObject.getAsJsonArray("url");
            String url = fbUrls.get(0).getAsString();
            map.put(PostType.VIDEO, url);
            System.out.println("fb :" + map);
            return map;
        } catch (Exception e) {
            System.out.println("media not found");
            return null;
        }
    }
}
