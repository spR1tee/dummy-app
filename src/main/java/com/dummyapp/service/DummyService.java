package com.dummyapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Service osztály, amely periodikusan elküldi a JSON fájlokat a Digital Twin API-nak.
 */
@Service
public class DummyService {

    private static final String DATA_FOLDER = "src/main/resources/data/dataPred";
    private static final String API_URL = "http://localhost:8080/simulator/request";
    private static final String APP_ID_HEADER = "X-Tenant-ID";
    private static final String APP_ID = "database_new";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<File> jsonFiles = new ArrayList<>();

    private int currentFileIndex = 0;

    public DummyService() {
        loadJsonFiles(DATA_FOLDER);
    }

    /**
     * Betölti a JSON fájlokat egy adott mappából.
     */
    private void loadJsonFiles(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid folder path: " + folderPath);
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files != null) {
            jsonFiles.addAll(Arrays.asList(files));
        } else {
            System.err.println("No JSON files found in: " + folderPath);
        }
    }

    /**
     * 5 másodpercenként elküldi a következő JSON fájlt az API-nak.
     */
    @Scheduled(fixedRate = 5000)
    public void sendRequest() {
        if (jsonFiles.isEmpty()) {
            System.err.println("No JSON files available to send.");
            return;
        }

        File jsonFile = jsonFiles.get(currentFileIndex);
        sendJsonFile(jsonFile);

        // Következő fájl indexe (ciklikusan)
        currentFileIndex = (currentFileIndex + 1) % jsonFiles.size();
    }

    /**
     * Elküldi a JSON fájl tartalmát POST kérésben.
     */
    private void sendJsonFile(File jsonFile) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonFile);
            String jsonString = objectMapper.writeValueAsString(jsonNode);

            HttpHeaders headers = new HttpHeaders();
            headers.set(APP_ID_HEADER, APP_ID);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonString, headers);

            String response = restTemplate.postForObject(API_URL, requestEntity, String.class);
            System.out.println("Sent " + jsonFile.getName() + " → Response: " + response);

        } catch (IOException e) {
            System.err.println("Failed to send file: " + jsonFile.getName());
            e.printStackTrace();
        }
    }
}
