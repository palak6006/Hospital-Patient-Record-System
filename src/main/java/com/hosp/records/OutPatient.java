package com.hosp.records;

import java.time.LocalDate;

public class OutPatient extends Patient 
{
    private static final long serialVersionUID = 1L;
    private LocalDate visitDate;
    private double consultationFee;

    public OutPatient(String id, String name, int age, String gender, LocalDate visitDate, double consultationFee) 
    {
        super(id, name, age, gender);
        this.visitDate = visitDate;
        this.consultationFee = consultationFee;
    }

    @Override
    public double calculateCost() 
    {
        double meds = prescriptions.stream().mapToDouble(Prescription::getEstimatedCost).sum();
        return consultationFee + meds;
    }

    public LocalDate getVisitDate() { return visitDate; }
}