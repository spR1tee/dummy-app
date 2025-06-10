package com.dummyapp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * REST API kontroller az adatok fogadásához és mentéséhez.
 * Ez egy egyszerű végpont, amely JSON adatokat fogad és fájlként tárolja.
 */
@RestController
@RequestMapping("/dummy")
public class DummyController {
    /**
     * POST endpoint JSON adatok fogadásához és fájlba mentéséhez.
     *
     * @param data A bejövő JSON adat String formátumban
     * @return ResponseEntity a művelet eredményével és státuszkóddal
     */
    @PostMapping("/receiveData")
    public ResponseEntity<String> receiveData(@RequestBody String data) {
        System.out.println(data);
        try {
            String filename = "received_" + System.currentTimeMillis() + ".json";
            Path path = Paths.get("received_files/" + filename);
            Files.createDirectories(path.getParent());
            Files.writeString(path, data);

            return ResponseEntity.ok("JSON file saved: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while saving: " + e.getMessage());
        }
    }
}

