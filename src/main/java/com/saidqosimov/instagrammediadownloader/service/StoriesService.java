package com.saidqosimov.instagrammediadownloader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class StoriesService {
    private final RestTemplate restTemplate;

    public StoriesService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public synchronized List<Map<PostType, String>> getStories(String mediaUrl) {
        List<Map<PostType, String>> mapList = new LinkedList<>();
        try {
            String result = restTemplate.getForObject(mediaUrl, String.class);
            System.out.println(result);
            JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(result)).getAsJsonObject();
            JsonArray resultArray = jsonObject.getAsJsonObject().getAsJsonArray("result");
            for (JsonElement resultElement : resultArray) {
                JsonObject resultObject = resultElement.getAsJsonObject();
                Map<PostType, String> map = new HashMap<>();
                if (resultObject.has("video_versions") && !resultObject.get("video_versions").isJsonNull()) {
                    JsonArray videoVersions = resultObject.getAsJsonArray("video_versions");
                    for (JsonElement videoElement : videoVersions) {
                        JsonObject videoObject = videoElement.getAsJsonObject();
                        String videoUrl = videoObject.get("url").getAsString();
                        map.put(PostType.VIDEO, videoUrl);
                    }
                } else {
                    JsonObject imageVersions2 = resultObject.getAsJsonObject("image_versions2");
                    JsonArray candidates = imageVersions2.getAsJsonArray("candidates");
                    if (!candidates.isEmpty()) {
                        JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                        String imageUrl = firstCandidate.get("url").getAsString();
                        map.put(PostType.PHOTO, imageUrl);
                    }
                }
                mapList.add(map);
            }
        }catch (Exception e){
         return null;
        }
        return mapList;
    }

}
