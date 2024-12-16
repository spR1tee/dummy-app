package com.dummyapp;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;

@Service
public class DummyService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders headers = new HttpHeaders();

    public DummyService() {
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Scheduled(fixedRate = 5000) // timing: 5 sec
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
            String apiUrl = "http://localhost:8080/simulator/request";
            String response = restTemplate.postForObject(apiUrl, request, String.class);
            System.out.println("Response from API: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
