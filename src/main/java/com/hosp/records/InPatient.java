package com.hosp.records;

import java.time.LocalDate;

public class InPatient extends Patient 
{
    private static final long serialVersionUID = 1L;
    private String assignedBedId; // ward-room-bed identifier
    private LocalDate admitDate;
    private int daysAdmitted;
    private double roomChargePerDay;

    public InPatient(String id, String name, int age, String gender, LocalDate admitDate, int daysAdmitted, double roomChargePerDay) 
    {
        super(id, name, age, gender);
        this.admitDate = admitDate;
        this.daysAdmitted = daysAdmitted;
        this.roomChargePerDay = roomChargePerDay;
    }

    public String getAssignedBedId() { return assignedBedId; }
    public void setAssignedBedId(String assignedBedId) { this.assignedBedId = assignedBedId; }

    @Override
    public double calculateCost() 
    {
        double meds = prescriptions.stream().mapToDouble(Prescription::getEstimatedCost).sum();
        return daysAdmitted * roomChargePerDay + meds;
    }

    public LocalDate getAdmitDate() { return admitDate; }
    public int getDaysAdmitted() { return daysAdmitted; }
}