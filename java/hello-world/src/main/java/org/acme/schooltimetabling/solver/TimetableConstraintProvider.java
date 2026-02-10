package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import org.acme.schooltimetabling.domain.Lesson;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                // HARD
                teacherConflict(factory),
                teacherSkillConstraint(factory),
                teacherTimeAvailability(factory),
                teacherCapacityConstraint(factory),
                studentDemandCap(factory),
                teacherSubjectCoexistence(factory),

                // SOFT //

                //Aufgabe A-D:
                //maximizeStudents(factory),

                //Aufgabe E:
                maximizeRevenue(factory),
        };
    }

    // Aufgabe  E:
    Constraint maximizeRevenue(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getSubject() != null && lesson.getTeacher() != null)
                .reward(HardSoftScore.ONE_SOFT, lesson -> lesson.getStudentCount() * lesson.getFee())
                .asConstraint("Maximize revenue");
    }

    // für Aufgabe A-D
    Constraint maximizeStudents(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getSubject() != null && lesson.getTeacher() != null)
                .reward(HardSoftScore.ONE_SOFT, Lesson::getStudentCount)
                .asConstraint("Maximize students");
    }

    Constraint teacherSubjectCoexistence(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                // Penalize if (Subject is null AND Teacher is NOT null)
                .filter(lesson -> (lesson.getSubject() == null) != (lesson.getTeacher() == null))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher and Subject must be assigned together");
    }

    Constraint studentDemandCap(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getSubject() != null)
                .groupBy(Lesson::getSubject, ConstraintCollectors.sum(Lesson::getStudentCount))
                .filter((subject, assignedCount) -> assignedCount > getMaxDemand(subject))
                .penalize(HardSoftScore.ONE_HARD,
                        (subject, assignedCount) -> assignedCount - getMaxDemand(subject))
                .asConstraint("Too many students for subject");
    }

    // Helper für  Aufgabe C
    private int getMaxDemand(String subject) {
        return switch (subject) {
            case "EDV_01" -> 123;
            case "EDV_02" -> 50;
            case "Webdesign" -> 84;
            case "Malerei" -> 105;
            case "Tonformen" -> 39;
            default -> 0;
        };
    }

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
}