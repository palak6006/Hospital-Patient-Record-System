package com.hosp.records;

import java.io.Serializable;

public class Prescription implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String medicineName;
    private String dosage; // e.g., "500mg"
    private int durationDays;
    private double estimatedCost;

    public Prescription(String medicineName, String dosage, int durationDays, double estimatedCost) 
    {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.durationDays = durationDays;
        this.estimatedCost = estimatedCost;
    }

    public String getMedicineName() { return medicineName; }
    public String getDosage() { return dosage; }
    public int getDurationDays() { return durationDays; }
    public double getEstimatedCost() { return estimatedCost; }

    @Override
    public String toString() 
    {
        return medicineName + " " + dosage + " x " + durationDays + " days (₹" + estimatedCost + ")";
    }
}