package com.dummyapp.service;

import com.dummyapp.model.SimulationResultDto;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChartService {

    private static final List<String> LABELS = List.of("Baseline", "Prediction without scaling", "Prediction with scaling");
    private static final List<String> METRICS = List.of(
            "Total IoT cost in USD",
            "Total energy consumption in kWh",
            "Total moved data in MB",
            "Total number of simulated VM tasks"
    );

    public void generateCharts(List<SimulationResultDto> simulations) throws IOException {
        for (String metric : METRICS) {
            CategoryChart chart = new CategoryChartBuilder()
                    .width(900).height(600)
                    .title("Simulation Comparison - " + metric)
                    .xAxisTitle("Type of Simulation")
                    .yAxisTitle(metric)
                    .build();

            chart.getStyler().setDecimalPattern("#,##0.###");

            List<Double> values = new ArrayList<>();
            for (SimulationResultDto sim : simulations) {
                double val = switch (metric) {
                    case "Total IoT cost in USD" -> sim.getTotalIotCostUsd();
                    case "Total energy consumption in kWh" -> sim.getTotalEnergyConsumptionKwh();
                    case "Total moved data in MB" -> sim.getTotalMovedDataMb();
                    case "Total number of simulated VM tasks" -> sim.getTotalVmTasksSimulated();
                    default -> 0.0;
                };
                values.add(val);
            }

            chart.addSeries(metric, LABELS, values);

            String chartName = "received_files/chart_" + metric.replace(" ", "_") + "_" + System.currentTimeMillis() + ".png";
            Path outputPath = Path.of(chartName);
            Files.createDirectories(outputPath.getParent());
            BitmapEncoder.saveBitmap(chart, chartName, BitmapEncoder.BitmapFormat.PNG);
        }
    }

    public void generateComparisonTableImage(List<SimulationResultDto> simulations) throws IOException {
        int cellWidth = 250, cellHeight = 40;
        int cols = 1 + simulations.size();
        int rows = 1 + METRICS.size();
        int width = cellWidth * cols, height = cellHeight * rows;

        DecimalFormat formatter = new DecimalFormat("0.#####");
        BufferedImage tableImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = tableImage.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        for (int r = 0; r <= rows; r++) g.drawLine(0, r * cellHeight, width, r * cellHeight);
        for (int c = 0; c <= cols; c++) g.drawLine(c * cellWidth, 0, c * cellWidth, height);

        Font font = new Font("Arial", Font.PLAIN, 14);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        g.setColor(new Color(220, 220, 220));
        g.fillRect(cellWidth, 0, cellWidth * simulations.size(), cellHeight);
        g.setColor(Color.BLACK);

        for (int c = 0; c < cols; c++) {
            String text = (c == 0) ? "Metric" : LABELS.get(c - 1);
            int textWidth = fm.stringWidth(text);
            int x = c * cellWidth + (cellWidth - textWidth) / 2;
            int y = (cellHeight + fm.getAscent()) / 2 - 2;
            g.drawString(text, x, y);
        }

        for (int r = 0; r < METRICS.size(); r++) {
            String metric = METRICS.get(r);
            int textWidth = fm.stringWidth(metric);
            int x = 5 + (cellWidth - textWidth) / 2;
            int y = (r + 1) * cellHeight + (cellHeight + fm.getAscent()) / 2 - 2;
            g.drawString(metric, x, y);

            for (int c = 0; c < simulations.size(); c++) {
                SimulationResultDto sim = simulations.get(c);
                double val = switch (metric) {
                    case "Total IoT cost in USD" -> sim.getTotalIotCostUsd();
                    case "Total energy consumption in kWh" -> sim.getTotalEnergyConsumptionKwh();
                    case "Total moved data in MB" -> sim.getTotalMovedDataMb();
                    case "Total number of simulated VM tasks" -> sim.getTotalVmTasksSimulated();
                    default -> 0.0;
                };
                String valStr = formatter.format(val);
                int valWidth = fm.stringWidth(valStr);
                int valX = (c + 1) * cellWidth + (cellWidth - valWidth) / 2;
                g.drawString(valStr, valX, y);
            }
        }

        g.dispose();
        Path outputPath = Path.of("received_files");
        Files.createDirectories(outputPath);
        String filename = "received_files/simulation_table_" + System.currentTimeMillis() + ".png";
        ImageIO.write(tableImage, "png", new File(filename));
    }
}

