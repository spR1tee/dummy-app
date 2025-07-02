package com.dummyapp.controller;

import com.dummyapp.model.SimulationResultDto;
import com.dummyapp.service.ChartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/dummy")
public class DummyController {

    private final List<SimulationResultDto> storedSimulations = new CopyOnWriteArrayList<>();
    private final ChartService chartService;
    private final ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    public DummyController(ChartService chartService) {
        this.chartService = chartService;
    }

    @PostMapping("/receiveData")
    public synchronized ResponseEntity<String> receiveData(@RequestBody String data) {
        try {
            String filename = "received_" + System.currentTimeMillis() + ".json";
            Path path = Paths.get("received_files/" + filename);
            Files.createDirectories(path.getParent());
            Files.writeString(path, data);
            System.out.println(data);
            SimulationResultDto simulation = mapper.readValue(data, SimulationResultDto.class);
            storedSimulations.add(simulation);

            if (storedSimulations.size() < 3) {
                return ResponseEntity.ok("Data received. Waiting for more data.");
            }

            chartService.generateCharts(storedSimulations);
            chartService.generateComparisonTableImage(storedSimulations);
            storedSimulations.clear();

            return ResponseEntity.ok("JSON file saved: " + filename);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while saving: " + e.getMessage());
        }
    }
}
