package com.hosp.staff;

import java.io.Serializable;

public class Nurse implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String contact;

    public Nurse(String id, String name, String contact) 
    {
        this.id = id; this.name = name; this.contact = contact;
    }

    public String getId(){ return id;}
    public String getName(){ return name; }
    public String getContact(){ return contact; }

    @Override
    public String toString() { return id + " | " + name; }
}