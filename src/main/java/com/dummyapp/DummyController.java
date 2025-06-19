package com.dummyapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private final List<SimulationResult> storedSimulations = new CopyOnWriteArrayList<>();
    @PostMapping("/receiveData")
    public synchronized ResponseEntity<String> receiveData(@RequestBody String data) {
        System.out.println(data);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String filename = "received_" + System.currentTimeMillis() + ".json";
            Path path = Paths.get("received_files/" + filename);
            Files.createDirectories(path.getParent());
            Files.writeString(path, data);

            SimulationResult simulation = mapper.readValue(data, SimulationResult.class);
            storedSimulations.add(simulation);

            if (storedSimulations.size() < 3) {
                return ResponseEntity.ok("Data received. Waiting for more data.");
            }

            List<String> labels = List.of("Baseline", "Prediction without scaling", "Prediction with scaling");

            // Az egyes metric-ek listája
            List<String> metrics = List.of(
                    "Total IoT cost in USD",
                    "Total energy consumption in kWh",
                    "Total moved data in MB",
                    "Total number of simulated VM tasks"
            );

            // Minden metric-hez külön chart
            for (String metric : metrics) {
                CategoryChart chart = new CategoryChartBuilder()
                        .width(900).height(600)
                        .title("Simulation Comparison - " + metric)
                        .xAxisTitle("Simulation")
                        .yAxisTitle(metric)
                        .build();
                chart.getStyler().setDecimalPattern("#,###");

                List<Double> values = new ArrayList<>();
                for (SimulationResult sim : storedSimulations) {
                    double val = switch (metric) {
                        case "Total IoT cost in USD" -> sim.total_iot_cost_usd;
                        case "Total energy consumption in kWh" -> sim.total_energy_consumption_kwh;
                        case "Total moved data in MB" -> sim.total_moved_data_mb;
                        case "Total number of simulated VM tasks" -> sim.total_vm_tasks_simulated;
                        default -> 0.0;
                    };
                    values.add(val);
                }

                chart.addSeries(metric, labels, values);

                // Mentés külön fájlba, metric neve is legyen a fájlnévben
                String chartname = "received_files/chart_" + metric + "_" + System.currentTimeMillis() + ".png";
                Path outputPath = Paths.get(chartname);
                Files.createDirectories(outputPath.getParent());
                BitmapEncoder.saveBitmap(chart, chartname, BitmapEncoder.BitmapFormat.PNG);
            }

            storedSimulations.clear();

            return ResponseEntity.ok("JSON file saved: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while saving: " + e.getMessage());
        }
    }
}

