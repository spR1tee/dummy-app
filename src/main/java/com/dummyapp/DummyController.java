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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
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

            List<String> metrics = List.of(
                    "Total IoT cost in USD",
                    "Total energy consumption in kWh",
                    "Total moved data in MB",
                    "Total number of simulated VM tasks"
            );

            for (String metric : metrics) {
                CategoryChart chart = new CategoryChartBuilder()
                        .width(900).height(600)
                        .title("Simulation Comparison - " + metric)
                        .xAxisTitle("Type of Simulation")
                        .yAxisTitle(metric)
                        .build();
                chart.getStyler().setDecimalPattern("#,##0.###");

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

                String chartname = "received_files/chart_" + metric + "_" + System.currentTimeMillis() + ".png";
                Path outputPath = Paths.get(chartname);
                Files.createDirectories(outputPath.getParent());
                BitmapEncoder.saveBitmap(chart, chartname, BitmapEncoder.BitmapFormat.PNG);
            }
            generateComparisonTableImage(storedSimulations, "received_files");
            storedSimulations.clear();

            return ResponseEntity.ok("JSON file saved: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while saving: " + e.getMessage());
        }
    }

    public static void generateComparisonTableImage(List<SimulationResult> storedSimulations, String outputFolder) throws IOException {
        int cellWidth = 250;
        int cellHeight = 40;
        int cols = 1 + storedSimulations.size(); // 1 metric oszlop + annyi, ahány szimuláció
        int rows = 1 + 4; // 1 fejléc + 4 metrika
        int width = cellWidth * cols;
        int height = cellHeight * rows;
        DecimalFormat formatter = new DecimalFormat("0.#####");

        BufferedImage tableImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = tableImage.createGraphics();

        // Háttér
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Vonalak
        g.setColor(Color.BLACK);
        for (int r = 0; r <= rows; r++) {
            g.drawLine(0, r * cellHeight, width, r * cellHeight);
        }
        for (int c = 0; c <= cols; c++) {
            g.drawLine(c * cellWidth, 0, c * cellWidth, height);
        }

        Font font = new Font("Arial", Font.PLAIN, 14);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        // Fejléc háttér
        g.setColor(new Color(220, 220, 220));
        g.fillRect(cellWidth, 0, cellWidth * storedSimulations.size(), cellHeight);
        g.setColor(Color.BLACK);

        // Fejléc szöveg
        String[] labels = {"Baseline", "Prediction without scaling", "Prediction with scaling"};
        for (int c = 0; c < cols; c++) {
            String text = (c == 0) ? "Metric" : labels[c - 1];
            int textWidth = fm.stringWidth(text);
            int x = c * cellWidth + (cellWidth - textWidth) / 2;
            int y = (cellHeight + fm.getAscent()) / 2 - 2;
            g.drawString(text, x, y);
        }

        // Metrikák
        String[] metrics = {
                "Total IoT cost in USD",
                "Total energy consumption in kWh",
                "Total moved data in MB",
                "Total number of simulated VM tasks"
        };

        for (int r = 0; r < metrics.length; r++) {
            // Metric név
            String metricName = metrics[r];
            int textWidth = fm.stringWidth(metricName);
            int x = 5 + (cellWidth - textWidth) / 2;
            int y = (r + 1) * cellHeight + (cellHeight + fm.getAscent()) / 2 - 2;
            g.drawString(metricName, x, y);

            // Értékek
            for (int c = 0; c < storedSimulations.size(); c++) {
                SimulationResult sim = storedSimulations.get(c);
                double val = switch (metricName) {
                    case "Total IoT cost in USD" -> sim.total_iot_cost_usd;
                    case "Total energy consumption in kWh" -> sim.total_energy_consumption_kwh;
                    case "Total moved data in MB" -> sim.total_moved_data_mb;
                    case "Total number of simulated VM tasks" -> sim.total_vm_tasks_simulated;
                    default -> 0.0;
                };
                String valStr = formatter.format(val);
                int valWidth = fm.stringWidth(valStr);
                int valX = (c + 1) * cellWidth + (cellWidth - valWidth) / 2;
                g.drawString(valStr, valX, y);
            }
        }

        g.dispose();

        // Mappa létrehozása, ha nem létezik
        Path outputPath = Path.of(outputFolder);
        Files.createDirectories(outputPath);

        String filename = outputFolder + "/simulation_table_" + System.currentTimeMillis() + ".png";
        ImageIO.write(tableImage, "png", new File(filename));
        }
    }

