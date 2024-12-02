package com.dummyapp;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;

@Service
public class SimulationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders headers = new HttpHeaders();

    public SimulationService() {
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Scheduled(fixedRate = 5000) // időzítés: 5mp
    public void sendRequest() {
        try {
            File jsonInputString = new File("src\\main\\resources\\data\\vmdata.json");
            BufferedReader br = new BufferedReader(new FileReader(jsonInputString));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }
            br.close();

            HttpEntity<String> request = new HttpEntity<>(jsonContent.toString(), headers);
            String apiUrl = "http://localhost:8080/api/startSimulation";
            String response = restTemplate.postForObject(apiUrl, request, String.class);
            System.out.println("Response from API: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Kérés a válasz végpontra
        /*String statusApiUrl = "http://localhost:8080/api/simulationStatus";
        String statusResponse = restTemplate.getForObject(statusApiUrl, String.class);
        System.out.println("Simulation status from API: " + statusResponse);*/
    }
}
