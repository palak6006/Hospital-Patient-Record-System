package com.hosp.records;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Patient implements Serializable 
{
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String name;
    protected int age;
    protected String gender;
    private String diagnosis; // encapsulated
    protected String assignedDoctorId;
    protected List<Prescription> prescriptions = new ArrayList<>();

    public Patient(String id, String name, int age, String gender) 
    {
        this.id = id; this.name = name; this.age = age; this.gender = gender;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getAssignedDoctorId() { return assignedDoctorId; }
    public void setAssignedDoctorId(String id) { this.assignedDoctorId = id; }

    public void addPrescription(Prescription p) { prescriptions.add(p); }

    public List<Prescription> getPrescriptions() { return prescriptions; }

    public abstract double calculateCost();

    public String brief() 
    {
        return id + " | " + name + " | " + age + " | " + getClass().getSimpleName() + " | Doctor: " + (assignedDoctorId==null?"-":assignedDoctorId);
    }
}