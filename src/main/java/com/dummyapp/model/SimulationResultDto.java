package com.dummyapp.model;

public class SimulationResultDto {
    private double runtimeMs;
    private double runtimeMinutes;
    private double runtimeHours;
    private double totalIotCostUsd;
    private double totalEnergyConsumptionKwh;
    private double totalMovedDataMb;
    private int totalVmTasksSimulated;
    private int numberOfVmsUtilized;

    // Getters and Setters
    public double getRuntimeMs() {
        return runtimeMs;
    }

    public void setRuntimeMs(double runtimeMs) {
        this.runtimeMs = runtimeMs;
    }

    public double getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public void setRuntimeMinutes(double runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }

    public double getRuntimeHours() {
        return runtimeHours;
    }

    public void setRuntimeHours(double runtimeHours) {
        this.runtimeHours = runtimeHours;
    }

    public double getTotalIotCostUsd() {
        return totalIotCostUsd;
    }

    public void setTotalIotCostUsd(double totalIotCostUsd) {
        this.totalIotCostUsd = totalIotCostUsd;
    }

    public double getTotalEnergyConsumptionKwh() {
        return totalEnergyConsumptionKwh;
    }

    public void setTotalEnergyConsumptionKwh(double totalEnergyConsumptionKwh) {
        this.totalEnergyConsumptionKwh = totalEnergyConsumptionKwh;
    }

    public double getTotalMovedDataMb() {
        return totalMovedDataMb;
    }

    public void setTotalMovedDataMb(double totalMovedDataMb) {
        this.totalMovedDataMb = totalMovedDataMb;
    }

    public int getTotalVmTasksSimulated() {
        return totalVmTasksSimulated;
    }

    public void setTotalVmTasksSimulated(int totalVmTasksSimulated) {
        this.totalVmTasksSimulated = totalVmTasksSimulated;
    }

    public int getNumberOfVmsUtilized() {
        return numberOfVmsUtilized;
    }

    public void setNumberOfVmsUtilized(int numberOfVmsUtilized) {
        this.numberOfVmsUtilized = numberOfVmsUtilized;
    }
}
