package com.saidqosimov.instagrammediadownloader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GetInDeviceService {
    private final RestTemplate restTemplate;

    public List<Map<PostType, String>> getInDeviceMedia(String mediaUrl) {
        try {
            List<Map<PostType, String>> mapList = new LinkedList<>();
            String result = restTemplate.getForObject(mediaUrl, String.class);
            JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(result)).getAsJsonObject();
            JsonArray medias = jsonObject.getAsJsonArray("medias");
            for (JsonElement mediaElement : medias) {
                JsonObject mediaObject = mediaElement.getAsJsonObject();
                String quality = mediaObject.get("quality").getAsString();
                if (quality != null && !quality.equals("hd_no_watermark") && !quality.equals("watermark") && !quality.equals("audio")) {
                    Map<PostType, String> map = new HashMap<>();
                    if (mediaObject.get("extension").getAsString().equals("mp4")) {
                        map.put(PostType.VIDEO, mediaObject.get("url").getAsString());
                    } else if (mediaObject.get("extension").getAsString().equals("jpg")) {
                        map.put(PostType.PHOTO, mediaObject.get("url").getAsString());
                    }
                    mapList.add(map);
                }
            }
            return mapList;
        } catch (Exception e) {
            return null;
        }
    }
}
