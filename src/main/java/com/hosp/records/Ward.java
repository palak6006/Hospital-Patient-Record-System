package com.hosp.records;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ward implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private final String wardId;
    private final String wardType; // General, ICU, Pediatrics etc.
    private final List<Room> rooms = new ArrayList<>();

    public Ward(String wardId, String wardType) 
    {
        this.wardId = wardId; this.wardType = wardType;
    }

    public String getWardId() { return wardId; }
    public String getWardType() { return wardType; }
    public void addRoom(Room r) { rooms.add(r); }
    public List<Room> getRooms() { return rooms; }

    public int totalBeds() { return rooms.stream().mapToInt(Room::getCapacity).sum(); }
    public int freeBeds() { return rooms.stream().mapToInt(Room::freeBedsCount).sum(); }

    @Override
    public String toString() 
    {
        return wardId + " (" + wardType + ") Rooms: " + rooms.size() + " FreeBeds: " + freeBeds();
    }
}