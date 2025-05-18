package com.dummyapp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/dummy")
public class DummyController {

    @PostMapping("/receiveData")
    public ResponseEntity<String> receiveData(@RequestBody String data) {
        System.out.println(data);
        try {
            String filename = "received_" + System.currentTimeMillis() + ".json";
            Path path = Paths.get("src/main/resources/received_files/" + filename);
            Files.createDirectories(path.getParent());
            Files.writeString(path, data);

            return ResponseEntity.ok("JSON fájl elmentve: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hiba a fájl mentésekor: " + e.getMessage());
        }
    }
}

