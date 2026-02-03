package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Room {

    @PlanningId
    private String id;

    private String name;
    private int capacity;

    public Room() {
    }

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return name + " (Cap: " + capacity + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public int getCapacity() { return capacity; } // <--- NEW

    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
