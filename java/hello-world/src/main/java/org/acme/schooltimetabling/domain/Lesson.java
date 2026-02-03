package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Lesson {

    @PlanningId
    private String id;

    private String subject;
    //private String teacher;
    private String studentGroup;
    private int studentCount;

    @PlanningVariable(allowsUnassigned = true)
    private Teacher teacher;

    @PlanningVariable(allowsUnassigned = true)
    private Timeslot timeslot;

    @PlanningVariable(allowsUnassigned = true)
    private Room room;

    // No-arg constructor required for Timefold
    public Lesson() {
    }

    public Lesson(String id, String subject, int studentCount) {
        this.id = id;
        this.subject = subject;
        this.studentCount = studentCount;
    }

    @Override
    public String toString() {
        return subject + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    // Add Getter/Setter for studentCount
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public String getStudentGroup() {
        return studentGroup;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

}
