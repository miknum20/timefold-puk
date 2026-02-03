package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import org.acme.schooltimetabling.domain.Lesson;

public class TimetableConstraintProvider implements ConstraintProvider {

//    @Override
//    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
//        return new Constraint[] {
//                // Hard constraints
//                roomConflict(constraintFactory),
//                teacherConflict(constraintFactory),
//                studentGroupConflict(constraintFactory),
//                // Soft constraints
//                teacherRoomStability(constraintFactory),
//                teacherTimeEfficiency(constraintFactory),
//                studentGroupSubjectVariety(constraintFactory)
//        };
//    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                // Hard constraints
                roomConflict(factory),
                teacherConflict(factory),
                teacherSkillConstraint(factory),
                teacherAvailability(factory),
                teacherCapacityConstraint(factory),
                roomCapacityConstraint(factory),

                maximizeAssignedLessons(factory)
        };
    }

    Constraint maximizeAssignedLessons(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                // Filter for lessons that are successfully assigned
                .filter(lesson -> lesson.getTimeslot() != null && lesson.getRoom() != null && lesson.getTeacher() != null)
                // Reward them! (+1 Soft Score per lesson)
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Lesson assigned");
    }

    Constraint roomCapacityConstraint(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null) // Only check if assigned
                .filter(lesson -> lesson.getStudentCount() > lesson.getRoom().getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        // Penalize by how much they overflow (e.g. 12 students in room 10 = penalty of 2)
                        lesson -> lesson.getStudentCount() - lesson.getRoom().getCapacity())
                .asConstraint("Room capacity exceeded");
    }

    // 1. A room can only hold one lesson at a time
    Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getRoom),
                        Joiners.lessThan(Lesson::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }

    // 2. A teacher can only be in one place at a time
    Constraint teacherConflict(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher), // Now works because getTeacher returns an Object
                        Joiners.lessThan(Lesson::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    // 3. Teacher must have the skill for the subject
    // "Frau Ahorn kann EDV_01 und EDV_02 unterrichten..."
    Constraint teacherSkillConstraint(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeacher() != null) // Avoid null pointer if unassigned
                .filter(lesson -> !lesson.getTeacher().getSkills().contains(lesson.getSubject()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher missing skill");
    }

    // 4. Time Restrictions (Birke = Vormittag, Kiefer = Nachmittag)
    Constraint teacherAvailability(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> {
                    boolean isMorning = lesson.getTimeslot().isMorning();
                    if (isMorning && !lesson.getTeacher().isAvailableMorning()) return true;
                    if (!isMorning && !lesson.getTeacher().isAvailableAfternoon()) return true;
                    return false;
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher unavailable at this time");
    }


    // 5. Max Capacity (Ahorn=7, Eiche=5, etc.)
    Constraint teacherCapacityConstraint(ConstraintFactory factory) {
        return factory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeacher() != null)
                .groupBy(Lesson::getTeacher, ConstraintCollectors.count())
                .filter((teacher, count) -> count > teacher.getMaxCapacity())
                .penalize(HardSoftScore.ONE_HARD, (teacher, count) -> count - teacher.getMaxCapacity())
                .asConstraint("Teacher capacity exceeded");
    }

//    Constraint roomConflict(ConstraintFactory constraintFactory) {
//        // A room can accommodate at most one lesson at the same time.
//        return constraintFactory
//                // Select each pair of 2 different lessons ...
//                .forEachUniquePair(Lesson.class,
//                        // ... in the same timeslot ...
//                        Joiners.equal(Lesson::getTimeslot),
//                        // ... in the same room ...
//                        Joiners.equal(Lesson::getRoom))
//                // ... and penalize each pair with a hard weight.
//                .penalize(HardSoftScore.ONE_HARD)
//                .justifyWith((lesson1, lesson2, score) -> new RoomConflictJustification(lesson1.getRoom(), lesson1, lesson2))
//                .asConstraint("Room conflict");
//    }
//
//    Constraint teacherConflict(ConstraintFactory constraintFactory) {
//        // A teacher can teach at most one lesson at the same time.
//        return constraintFactory
//                .forEachUniquePair(Lesson.class,
//                        Joiners.equal(Lesson::getTimeslot),
//                        Joiners.equal(Lesson::getTeacher))
//                .penalize(HardSoftScore.ONE_HARD)
//                .justifyWith(
//                        (lesson1, lesson2, score) -> new TeacherConflictJustification(lesson1.getTeacher(), lesson1, lesson2))
//                .asConstraint("Teacher conflict");
//    }
//
//    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
//        // A student can attend at most one lesson at the same time.
//        return constraintFactory
//                .forEachUniquePair(Lesson.class,
//                        Joiners.equal(Lesson::getTimeslot),
//                        Joiners.equal(Lesson::getStudentGroup))
//                .penalize(HardSoftScore.ONE_HARD)
//                .justifyWith((lesson1, lesson2, score) -> new StudentGroupConflictJustification(lesson1.getStudentGroup(), lesson1, lesson2))
//                .asConstraint("Student group conflict");
//    }
//
//    Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
//        // A teacher prefers to teach in a single room.
//        return constraintFactory
//                .forEachUniquePair(Lesson.class,
//                        Joiners.equal(Lesson::getTeacher))
//                .filter((lesson1, lesson2) -> lesson1.getRoom() != lesson2.getRoom())
//                .penalize(HardSoftScore.ONE_SOFT)
//                .justifyWith((lesson1, lesson2, score) -> new TeacherRoomStabilityJustification(lesson1.getTeacher(), lesson1, lesson2))
//                .asConstraint("Teacher room stability");
//    }
//
//    Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
//        // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
//        return constraintFactory
//                .forEach(Lesson.class)
//                .join(Lesson.class, Joiners.equal(Lesson::getTeacher),
//                        Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek()))
//                .filter((lesson1, lesson2) -> {
//                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
//                            lesson2.getTimeslot().getStartTime());
//                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
//                })
//                .reward(HardSoftScore.ONE_SOFT)
//                .justifyWith((lesson1, lesson2, score) -> new TeacherTimeEfficiencyJustification(lesson1.getTeacher(), lesson1, lesson2))
//                .asConstraint("Teacher time efficiency");
//    }
//
//    Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
//        // A student group dislikes sequential lessons on the same subject.
//        return constraintFactory
//                .forEach(Lesson.class)
//                .join(Lesson.class,
//                        Joiners.equal(Lesson::getSubject),
//                        Joiners.equal(Lesson::getStudentGroup),
//                        Joiners.equal((lesson) -> lesson.getTimeslot().getDayOfWeek()))
//                .filter((lesson1, lesson2) -> {
//                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
//                            lesson2.getTimeslot().getStartTime());
//                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
//                })
//                .penalize(HardSoftScore.ONE_SOFT)
//                .justifyWith((lesson1, lesson2, score) -> new StudentGroupSubjectVarietyJustification(lesson1.getStudentGroup(), lesson1, lesson2))
//                .asConstraint("Student group subject variety");
//    }

}
