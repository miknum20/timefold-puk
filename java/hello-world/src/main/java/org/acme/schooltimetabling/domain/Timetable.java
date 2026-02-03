package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

import java.util.List;

@PlanningSolution
public class Timetable {

    private String name;

    @ValueRangeProvider(id = "subjectRange") // This ID must match Lesson.java exactly
    @ProblemFactCollectionProperty
    private List<String> subjectList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Timeslot> timeslots;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Room> rooms;

    @ValueRangeProvider(id = "teacherRange")
    @ProblemFactCollectionProperty
    private List<Teacher> teachers;

    @PlanningEntityCollectionProperty
    private List<Lesson> lessons;

    @PlanningScore
    private HardSoftScore score;

    // Ignored by Timefold, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    // No-arg constructor required for Timefold
    public Timetable() {
    }

    public Timetable(String name, HardSoftScore score, SolverStatus solverStatus) {
        this.name = name;
        this.score = score;
        this.solverStatus = solverStatus;
    }

    public Timetable(List<Timeslot> timeslots, List<Room> rooms, List<Teacher> teachers, List<Lesson> lessons, List<String> subjects) {
        this.timeslots = timeslots;
        this.rooms = rooms;
        this.teachers = teachers;
        this.lessons = lessons;
        this.subjectList = subjects; // This is the "menu" the solver picks from
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public List<Teacher> getTeachers() { return teachers; }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

    public List<String> getSubjectList() {
        return subjectList;
    }

    public List<Teacher> getTeacherList() {
        return teachers;
    }

}
