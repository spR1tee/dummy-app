package com.dummyapp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dummy")
public class DummyController {

    @PostMapping("/receiveData")
    public ResponseEntity<String> receiveData(@RequestBody String data) {
        // Process the received data
        System.out.println(data);
        return ResponseEntity.ok("Data received successfully");
    }
}

