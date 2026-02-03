package org.acme.schooltimetabling;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.schooltimetabling.domain.*;
import org.acme.schooltimetabling.solver.TimetableConstraintProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class TimetableApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableApp.class);

    public enum DemoData {
        SMALL,
        LARGE
    }

    public static void main(String[] args) {
        SolverFactory<Timetable> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(Timetable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimetableConstraintProvider.class)
                // The solver runs only for 5 seconds on this small dataset.
                // It's recommended to run for at least 5 minutes ("5m") otherwise.
                .withTerminationSpentLimit(Duration.ofSeconds(5)));

        // Load the problem
        Timetable problem = generateDemoData(DemoData.SMALL);

        // Solve the problem
        Solver<Timetable> solver = solverFactory.buildSolver();
        Timetable solution = solver.solve(problem);

        // Visualize the solution
        printTimetable(solution);
    }

    public static Timetable generateDemoData(DemoData demoData) {
        List<Timeslot> timeslots = new ArrayList<>(10);
        long nextTimeslotId = 0L;

        // We simulate "Vormittag" as 09:00-12:00 and "Nachmittag" as 14:00-17:00
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), day, LocalTime.of(9, 0), LocalTime.of(12, 0)));  // Vormittag
            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), day, LocalTime.of(14, 0), LocalTime.of(17, 0))); // Nachmittag
        }

        // --- B. Create the 4 Rooms ---
        List<Room> rooms = new ArrayList<>();
        long nextRoomId = 0L;
        rooms.add(new Room(Long.toString(nextRoomId++), "Raum 01", 30));
        rooms.add(new Room(Long.toString(nextRoomId++), "Raum 02", 30));
        rooms.add(new Room(Long.toString(nextRoomId++), "Raum 03", 30));
        rooms.add(new Room(Long.toString(nextRoomId++), "Raum 04", 30));

        // --- C. Create the Teachers (Constraints from PDF) ---
        List<Teacher> teachers = new ArrayList<>();
        // Ahorn: EDV_01, EDV_02. Cap 7.
        teachers.add(new Teacher("Frau Ahorn", Set.of("EDV_01", "EDV_02"), 7, true, true));
        // Eiche: EDV_02, Webdesign. Cap 5.
        teachers.add(new Teacher("Herr Eiche", Set.of("EDV_02", "Webdesign"), 5, true, true));
        // Birke: Webdesign, Malerei. Cap 4. Morning Only.
        teachers.add(new Teacher("Frau Birke", Set.of("Webdesign", "Malerei"), 4, true, false));
        // Kiefer: Malerei, Tonformen. Cap 5. Afternoon Only.
        teachers.add(new Teacher("Herr Kiefer", Set.of("Malerei", "Tonformen"), 5, false, true));

        // --- D. Create Lessons (The Demand) ---

        // Define the demand for each course
        Map<String, Integer> courseDemand = new LinkedHashMap<>();
        courseDemand.put("EDV_01", 123);
        courseDemand.put("EDV_02", 50);
        courseDemand.put("Webdesign", 84);
        courseDemand.put("Malerei", 105);
        courseDemand.put("Tonformen", 39);

        List<Lesson> lessons = new ArrayList<>();
        long nextLessonId = 0L;
        int maxClassSize = 30;

        for (Map.Entry<String, Integer> entry : courseDemand.entrySet()) {
            String subject = entry.getKey();
            int studentsWaiting = entry.getValue();

            // Keep creating lessons until all students are assigned
            while (studentsWaiting > 0) {
                // Take 12 students, or however many are left if less than 12
                int groupSize = Math.min(studentsWaiting, maxClassSize);

                lessons.add(new Lesson(String.valueOf(nextLessonId++), subject, groupSize));

                studentsWaiting -= groupSize;
            }
        }

        // Pass teachers to the Timetable constructor
        return new Timetable(timeslots.get(0).getId(), timeslots, rooms, teachers, lessons);

    }

    private static void printTimetable(Timetable timeTable) {
        LOGGER.info("");
        List<Room> rooms = timeTable.getRooms();
        List<Lesson> lessons = timeTable.getLessons();
        Map<Timeslot, Map<Room, List<Lesson>>> lessonMap = lessons.stream()
                .filter(lesson -> lesson.getTimeslot() != null && lesson.getRoom() != null)
                .collect(Collectors.groupingBy(Lesson::getTimeslot, Collectors.groupingBy(Lesson::getRoom)));
        LOGGER.info("|            | " + rooms.stream()
                .map(room -> String.format("%-10s", room.getName())).collect(Collectors.joining(" | ")) + " |");
        LOGGER.info("|" + "------------|".repeat(rooms.size() + 1));
        for (Timeslot timeslot : timeTable.getTimeslots()) {
            List<List<Lesson>> cells = rooms.stream()
                    .map(room -> {
                        Map<Room, List<Lesson>> byRoomMap = lessonMap.get(timeslot);
                        if (byRoomMap == null) {
                            return Collections.<Lesson>emptyList();
                        }
                        List<Lesson> cellLessons = byRoomMap.get(room);
                        return Objects.requireNonNullElse(cellLessons, Collections.<Lesson>emptyList());
                    }).toList();

            // Row 1: Subject
            LOGGER.info("| " + String.format("%-10s",
                    timeslot.getDayOfWeek().toString().substring(0, 3) + " " + timeslot.getStartTime()) + " | "
                    + cells.stream().map(cellLessons -> String.format("%-10s",
                            cellLessons.stream().map(Lesson::getSubject).collect(Collectors.joining(", "))))
                    .collect(Collectors.joining(" | "))
                    + " |");

            // Row 2: Teacher (Assigned)
            LOGGER.info("|            | "
                    + cells.stream().map(cellLessons -> String.format("%-10s",
                            cellLessons.stream().map(l -> l.getTeacher() == null ? "Unassigned" : l.getTeacher().getName())
                                    .collect(Collectors.joining(", "))))
                    .collect(Collectors.joining(" | "))
                    + " |");

            // Row 3: Student Count (Useful to see!)
            LOGGER.info("|            | "
                    + cells.stream().map(cellLessons -> String.format("%-10s",
                            cellLessons.stream().map(l -> "(" + l.getStudentCount() + " students)")
                                    .collect(Collectors.joining(", "))))
                    .collect(Collectors.joining(" | "))
                    + " |");

            LOGGER.info("|" + "------------|".repeat(rooms.size() + 1));

        }

        List<Lesson> unassignedLessons = lessons.stream()
                .filter(lesson -> lesson.getTimeslot() == null || lesson.getRoom() == null || lesson.getTeacher() == null)
                .toList();

        if (!unassignedLessons.isEmpty()) {
            LOGGER.info("");
            LOGGER.info("Unassigned lessons (" + unassignedLessons.size() + "):");
            for (Lesson lesson : unassignedLessons) {
                // Fix: Added check for null teacher
                String teacherName = (lesson.getTeacher() == null) ? "No Teacher" : lesson.getTeacher().getName();
                LOGGER.info("  " + lesson.getSubject() + " [" + lesson.getStudentCount() + " students] - " + teacherName);
            }
        }
    }

}
