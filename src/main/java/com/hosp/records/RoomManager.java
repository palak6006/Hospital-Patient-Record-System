package com.hosp.records;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoomManager implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private final List<Ward> wards = new ArrayList<>();

    public void addWard(Ward w) { wards.add(w); }
    public List<Ward> getWards(){ return wards; }

    // Try to assign ICU if required else normal ward
    public AssignedBed findAndAssignBed(String patientId, boolean needsICU) 
    {
        if (needsICU) 
        {
            for (Ward w : wards) 
            {
                if (w.getWardType().equalsIgnoreCase("ICU") || w.getWardType().toLowerCase().contains("icu")) 
                {
                    for (Room r : w.getRooms()) 
                    {
                        if (r.isICU() && r.freeBedsCount() > 0) 
                        {
                            Bed b = r.assignFirstFreeBed(patientId);
                            return new AssignedBed(w, r, b);
                        }
                    }
                }
            }
            return null;
        } else {
            // Normal wards first
            for (Ward w : wards) 
            {
                if (!w.getWardType().equalsIgnoreCase("ICU")) 
                {
                    for (Room r : w.getRooms()) 
                    {
                        if (!r.isICU() && r.freeBedsCount() > 0) 
                        {
                            Bed b = r.assignFirstFreeBed(patientId);
                            return new AssignedBed(w, r, b);
                        }
                    }
                }
            }
            // fallback: any ward with free bed (including ICU)
            for (Ward w : wards) 
            {
                for (Room r : w.getRooms()) 
                {
                    if (r.freeBedsCount() > 0) 
                    {
                        Bed b = r.assignFirstFreeBed(patientId);
                        return new AssignedBed(w, r, b);
                    }
                }
            }
            return null;
        }
    }

    public static class AssignedBed 
    {
        public final Ward ward;
        public final Room room;
        public final Bed bed;
        public AssignedBed(Ward ward, Room room, Bed bed) { this.ward = ward; this.room = room; this.bed = bed; }
        @Override public String toString() { return ward.getWardId() + " / " + room.getRoomId() + " / " + bed.getBedId(); }
    }
}