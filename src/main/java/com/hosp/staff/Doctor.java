package com.hosp.staff;

import java.io.Serializable;

public class Doctor implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String specialization;
    private String contactNumber; // encapsulated
    private boolean available;

    public Doctor(String id, String name, String specialization, String contactNumber, boolean available) 
    {
        this.id = id; this.name = name; this.specialization = specialization;
        this.contactNumber = contactNumber; this.available = available;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getContactNumber() { return contactNumber; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() 
    {
        return id + " | " + name + " (" + specialization + ") | Available: " + available;
    }
}