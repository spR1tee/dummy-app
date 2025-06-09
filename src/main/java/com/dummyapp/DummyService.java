package com.dummyapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DummyService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<File> jsonFiles = new ArrayList<>();
    private int currentFileIndex = 0;
    private static final String APPID = "database";

    public DummyService() {
        loadJsonFiles("src/main/resources/data/dataPred");
    }

    private void loadJsonFiles(String folderpath) {
        File folder = new File(folderpath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files != null) {
            jsonFiles.addAll(Arrays.asList(files));
        }
    }

    @Scheduled(fixedRate = 5000) // timing: 5 sec
    public void sendRequest() {
        if (jsonFiles.isEmpty()) {
            System.err.println("No JSON files found.");
            return;
        }

        if (currentFileIndex >= jsonFiles.size()) {
            currentFileIndex = 0;
        }

        File jsonFile = jsonFiles.get(currentFileIndex);

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonFile);
            String jsonString = objectMapper.writeValueAsString(jsonNode);
            String apiUrl = "http://localhost:8080/simulator/request";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-ID", APPID);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonString, headers);
            String response = restTemplate.postForObject(apiUrl, requestEntity, String.class);
            System.out.println("Response from API: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentFileIndex++;
    }
}
