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

/**
 * Service osztály, amely periodikusan küldi el JSON fájlok tartalmát egy külső API-nak.
 * Ez a szolgáltatás szimulálja az adatküldést egy meghatározott könyvtárban található JSON fájlokkal.
 */
@Service // Spring komponens, amely automatikusan létrehozódik és injektálható
public class DummyService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<File> jsonFiles = new ArrayList<>();
    private int currentFileIndex = 0;
    // Statikus alkalmazás azonosító a HTTP header-ben való használatra
    private static final String APPID = "database";

    public DummyService() {
        loadJsonFiles("src/main/resources/data/dataPred");
    }

    /**
     * Betölti az összes JSON fájlt a megadott könyvtárból.
     *
     * @param folderpath A könyvtár elérési útja, ahonnan a JSON fájlokat be kell tölteni
     */
    private void loadJsonFiles(String folderpath) {
        File folder = new File(folderpath);

        // Csak a .json kiterjesztésű fájlokat szűri ki (case-insensitive)
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (files != null) {
            jsonFiles.addAll(Arrays.asList(files));
        }
    }

    /**
     * Periodikusan meghívódó metódus, amely elküldi a JSON fájlok tartalmát.
     * 5 másodpercenként fut le automatikusan.
     */
    @Scheduled(fixedRate = 5000) // timing: 5 sec
    public void sendRequest() {
        if (jsonFiles.isEmpty()) {
            System.err.println("No JSON files found.");
            return;
        }

        // Ha elértük a lista végét, visszatérünk az elejére (ciklikus működés)
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
