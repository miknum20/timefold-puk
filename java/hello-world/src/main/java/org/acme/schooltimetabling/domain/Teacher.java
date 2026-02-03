package org.acme.schooltimetabling.domain;

import java.util.Set;

public class Teacher {

    private String name;
    private Set<String> skills;       // e.g. ["EDV_01", "EDV_02"]
    private int maxCapacity;          // e.g. 7
    private boolean availableMorning;
    private boolean availableAfternoon;

    public Teacher(String name, Set<String> skills, int maxCapacity, boolean availableMorning, boolean availableAfternoon) {
        this.name = name;
        this.skills = skills;
        this.maxCapacity = maxCapacity;
        this.availableMorning = availableMorning;
        this.availableAfternoon = availableAfternoon;
    }

    // Add Getters and Setters here (crucial for Timefold)
    public String getName() { return name; }
    public Set<String> getSkills() { return skills; }
    public int getMaxCapacity() { return maxCapacity; }
    public boolean isAvailableMorning() {
        return availableMorning;
    }
    public boolean isAvailableAfternoon() {
        return availableAfternoon;
    }

    @Override
    public String toString() {
        return name;
    }
}