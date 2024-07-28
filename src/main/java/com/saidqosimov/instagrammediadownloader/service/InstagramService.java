package com.saidqosimov.instagrammediadownloader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saidqosimov.instagrammediadownloader.enums.PostType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class InstagramService {
    private final RestTemplate restTemplate;

    public InstagramService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<PostType, String>> getInstagramMedia(String mediaUrl) {
        List<Map<PostType, String>> mapList = new LinkedList<>();
        try {
            String result = restTemplate.getForObject(mediaUrl, String.class);
            System.out.println(result);
            JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(result)).getAsJsonObject();
            String mediaType = jsonObject.get("mediaType").getAsString();
            switch (mediaType) {
                case "VIDEO" -> {
                    Map<PostType, String> map = new HashMap<>();
                    JsonObject videoVersion = jsonObject.getAsJsonObject("videoVersion");
                    String videoUrl = videoVersion.get("videoUrl").getAsString();
                    map.put(PostType.VIDEO, videoUrl);
                    mapList.add(map);
                    return mapList;
                }
                case "PHOTO" -> {
                    Map<PostType, String> map = new HashMap<>();
                    JsonObject videoVersion = jsonObject.getAsJsonObject("imageVersion2");
                    String imageUrl = videoVersion.get("imageUrl").getAsString();
                    map.put(PostType.PHOTO, imageUrl);
                    mapList.add(map);
                    return mapList;
                }
                case "CAROUSEL" -> {
                    JsonArray carouselMedia = jsonObject.getAsJsonArray("carouselMedia");
                    for (int i = 0; i < carouselMedia.size(); i++) {
                        Map<PostType, String> map = new HashMap<>();
                        JsonObject mediaItem = carouselMedia.get(i).getAsJsonObject();
                        boolean isVideo = mediaItem.get("isVideo").getAsBoolean();
                        if (isVideo) {
                            String videoUrl = mediaItem.get("videoUrl").getAsString();
                            map.put(PostType.VIDEO, videoUrl);
                            mapList.add(map);
                        } else {
                            String imageUrl = mediaItem.get("imageUrl").getAsString();
                            map.put(PostType.PHOTO, imageUrl);
                            mapList.add(map);
                        }
                    }
                    System.out.println("instagram: " + mapList);
                    return mapList;
                }
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }
}
