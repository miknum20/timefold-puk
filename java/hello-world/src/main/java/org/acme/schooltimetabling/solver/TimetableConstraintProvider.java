package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import org.acme.schooltimetabling.domain.Lesson;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                // --- HARD CONSTRAINTS (Regeln) ---
                roomConflict(factory),           // Keine Doppelbelegung im Raum
                teacherConflict(factory),        // Lehrer nicht zweiteilen
                teacherSkillConstraint(factory), // Lehrer muss Fach können
                teacherTimeAvailability(factory),// Vormittag/Nachmittag
                teacherCapacityConstraint(factory), // Wochenlimit
                roomCapacityConstraint(factory), // Raumgröße
                partialAssignmentConflict(factory), // <--- NEU: Keine Räume ohne Lehrer blockieren!

                // --- SOFT CONSTRAINT (Motivation) ---
                maximizeAssignedLessons(factory)
        };
    }

    // SOFT: Belohne jeden Kurs, der KOMPLETT (Raum+Zeit+Lehrer) geplant ist
    Constraint maximizeAssignedLessons(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeslot() != null && lesson.getRoom() != null && lesson.getTeacher() != null)
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Lesson fully assigned");
    }

    // HARD: Verhindere, dass ein Raum belegt wird, wenn kein Lehrer da ist
    Constraint partialAssignmentConflict(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null && lesson.getTeacher() == null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room assigned without teacher");
    }

    // HARD: Raumkonflikt (Zählen statt Join -> Robuster gegen Doppelbelegung)
    Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null && lesson.getTimeslot() != null)
                .groupBy(Lesson::getRoom, Lesson::getTimeslot, count())
                .filter((room, timeslot, count) -> count > 1)
                .penalize(HardSoftScore.ONE_HARD, (room, timeslot, count) -> count - 1)
                .asConstraint("Room conflict");
    }

    // HARD: Lehrerkonflikt
    Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeacher() != null && lesson.getTimeslot() != null)
                .groupBy(Lesson::getTeacher, Lesson::getTimeslot, count())
                .filter((teacher, timeslot, count) -> count > 1)
                .penalize(HardSoftScore.ONE_HARD, (teacher, timeslot, count) -> count - 1)
                .asConstraint("Teacher conflict");
    }

    Constraint teacherSkillConstraint(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeacher() != null)
                .filter(lesson -> !lesson.getTeacher().getSkills().contains(lesson.getSubject()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher missing skill");
    }

    Constraint teacherTimeAvailability(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeacher() != null && lesson.getTimeslot() != null)
                .filter(lesson -> {
                    String restriction = lesson.getTeacher().getTimeRestriction();
                    if (lesson.getTimeslot().getStartTime() == null) return false;
                    boolean isMorning = lesson.getTimeslot().getStartTime().getHour() < 12;
                    if ("MORNING_ONLY".equals(restriction) && !isMorning) return true;
                    if ("AFTERNOON_ONLY".equals(restriction) && isMorning) return true;
                    return false;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher unavailable at this time");
    }

    Constraint teacherCapacityConstraint(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeacher() != null)
                .groupBy(Lesson::getTeacher, count())
                .filter((teacher, count) -> count > teacher.getMaxCapacity())
                .penalize(HardSoftScore.ONE_HARD, (teacher, count) -> count - teacher.getMaxCapacity())
                .asConstraint("Teacher capacity exceeded");
    }

    Constraint roomCapacityConstraint(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null)
                .filter(lesson -> lesson.getStudentCount() > lesson.getRoom().getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        lesson -> lesson.getStudentCount() - lesson.getRoom().getCapacity())
                .asConstraint("Room capacity exceeded");
    }
}