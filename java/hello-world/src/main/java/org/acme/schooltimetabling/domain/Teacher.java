package org.acme.schooltimetabling.domain;

import java.util.Set;

public class Teacher {

    private String name;
    private Set<String> skills;       // e.g. ["EDV_01", "EDV_02"]
    private int maxCapacity;          // e.g. 7
    private String timeRestriction;   // "NONE", "MORNING_ONLY", "AFTERNOON_ONLY"

    public Teacher(String name, Set<String> skills, int maxCapacity, String timeRestriction) {
        this.name = name;
        this.skills = skills;
        this.maxCapacity = maxCapacity;
        this.timeRestriction = timeRestriction;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getName() { return name; }
    public Set<String> getSkills() { return skills; }
    public int getMaxCapacity() { return maxCapacity; }
    public String getTimeRestriction() { return timeRestriction; }

    @Override
    public String toString() {
        return name;
    }
}