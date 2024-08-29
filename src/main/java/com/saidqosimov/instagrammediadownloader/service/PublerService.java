package com.saidqosimov.instagrammediadownloader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PublerService {
    private final RestTemplate restTemplate;

    public synchronized List<Map<PostType, String>> getMediaData(String mediaUrl) {
        List<Map<PostType, String>> mapList = new LinkedList<>();
        String result = restTemplate.getForObject(mediaUrl, String.class);
        JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(result)).getAsJsonObject();
        JsonArray mediaDataList = jsonObject.getAsJsonArray("publerPostData");
        for (int i = 0; i < mediaDataList.size(); i++) {
            Map<PostType, String> map = new HashMap<>();
            JsonObject mediaItem = mediaDataList.get(i).getAsJsonObject();
            String type = mediaItem.get("type").getAsString();
            if (type.equals("video")) {
                String videoUrl = mediaItem.get("downloadUrl").getAsString();
                map.put(PostType.VIDEO, videoUrl);
                mapList.add(map);
            } else if (type.equals("photo")) {
                String imageUrl = mediaItem.get("downloadUrl").getAsString();
                map.put(PostType.PHOTO, imageUrl);
                mapList.add(map);
            }else {
                return null;
            }
        }
        return mapList;
    }
}
