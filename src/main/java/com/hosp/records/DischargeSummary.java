package com.hosp.records;

import java.io.Serializable;
import java.time.LocalDate;

public class DischargeSummary implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private String patientId;
    private String patientName;
    private String doctorName;
    private String finalDiagnosis;
    private double totalCost;
    private LocalDate date;

    public DischargeSummary(String patientId, String patientName, String doctorName, String finalDiagnosis, double totalCost) 
    {
        this.patientId = patientId; this.patientName = patientName; this.doctorName = doctorName;
        this.finalDiagnosis = finalDiagnosis; this.totalCost = totalCost;
        this.date = LocalDate.now();
    }

    public String generateSummaryText() 
    {
        StringBuilder sb = new StringBuilder();
        sb.append("---- Discharge Summary ----\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append("Patient: ").append(patientName).append(" (").append(patientId).append(")\n");
        sb.append("Doctor: ").append(doctorName).append("\n");
        sb.append("Diagnosis: ").append(finalDiagnosis).append("\n");
        sb.append(String.format("Total Cost: ₹%.2f\n", totalCost));
        sb.append("---------------------------\n");
        return sb.toString();
    }
}