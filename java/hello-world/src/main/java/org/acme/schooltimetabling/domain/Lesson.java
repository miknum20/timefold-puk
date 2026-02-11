package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Lesson {

    @PlanningId
    private String id;

    // Make subject a planning variable
    @PlanningVariable(valueRangeProviderRefs = "subjectRange", allowsUnassigned = true)
    private String subject;

    @PlanningVariable(valueRangeProviderRefs = "teacherRange", allowsUnassigned = true)
    private Teacher teacher;

    private Timeslot timeslot;
    private Room room;

    public Lesson() {
    }

    public Lesson(String id) {
        this.id = id;
    }


    // The number of students is exactly what the room can hold.
    public int getStudentCount() {
        if (subject == null || room == null || teacher == null) {
            return 0;
        }
        return room.getCapacity();
    }

    // Fee and Revenue logic
    public int getFee() {
        if (subject == null) return 0;
        return switch (subject) {
            case "EDV_01" -> 20;
            case "EDV_02" -> 30;
            case "Webdesign" -> 25;
            case "Malerei" -> 30;
            case "Tonformen" -> 50;
            default -> 0;
        };
    }

    public int getTotalRevenue() {
        // calculate revenue
        return getStudentCount() * getFee();
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public Room getRoom() {
        return room;
    }

}
