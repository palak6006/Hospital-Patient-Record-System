package com.hosp.records;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private final String roomId; // e.g., "R101"
    private final boolean isICU;
    private final int capacity;
    private final List<Bed> beds = new ArrayList<>();

    public Room(String roomId, boolean isICU, int capacity) 
    {
        this.roomId = roomId; this.isICU = isICU; this.capacity = capacity;
        for (int i = 1; i <= capacity; i++) beds.add(new Bed(roomId + "-B" + i));
    }

    public String getRoomId() { return roomId; }
    public boolean isICU() { return isICU; }
    public int getCapacity() { return capacity; }
    public List<Bed> getBeds() { return beds; }
    public int freeBedsCount() { return (int) beds.stream().filter(b -> !b.isOccupied()).count(); }

    public Bed assignFirstFreeBed(String patientId) 
    {
        for (Bed b : beds) 
        {
            if (!b.isOccupied()) { b.assign(patientId); return b; }
        }
        return null;
    }

    public boolean vacateBed(String bedId) 
    {
        for (Bed b : beds) 
        {
            if (b.getBedId().equals(bedId)) { b.vacate(); return true; }
        }
        return false;
    }

    @Override
    public String toString() 
    {
        return roomId + " | ICU:" + isICU + " | FreeBeds:" + freeBedsCount();
    }
}