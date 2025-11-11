package com.hosp.records;

import java.io.Serializable;

public class Bed implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private final String bedId; // unique: ward-room-bed
    private boolean occupied;
    private String patientId; // assigned patient id or null

    public Bed(String bedId) 
    {
        this.bedId = bedId;
        this.occupied = false;
        this.patientId = null;
    }

    public String getBedId() { return bedId; }
    public boolean isOccupied() { return occupied; }
    public String getPatientId() { return patientId; }

    public void assign(String patientId) 
    {
        this.patientId = patientId;
        this.occupied = true;
    }

    public void vacate() 
    {
        this.patientId = null;
        this.occupied = false;
    }

    @Override
    public String toString() 
    {
        return bedId + (occupied ? " (Occ: " + patientId + ")" : " (Free)");
    }
}