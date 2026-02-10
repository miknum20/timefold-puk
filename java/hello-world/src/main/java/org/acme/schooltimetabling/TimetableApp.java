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
        rooms.add(new Room(Long.toString(nextRoomId++), "Raum 01", 10));
        rooms.add(new Room(Long.toString(nextRoomId++), "Raum 02", 12));
        rooms.add(new Room(Long.toString(nextRoomId++), "Raum 03", 8));
        rooms.add(new Room(Long.toString(nextRoomId++), "Raum 04", 5));

        // --- C. Create the Teachers (Constraints from PDF) ---
        List<Teacher> teachers = new ArrayList<>();
        // Ahorn: EDV_01, EDV_02. Cap 7.
        teachers.add(new Teacher("Frau Ahorn", Set.of("EDV_01", "EDV_02"), 7, "NONE"));
        // Eiche: EDV_02, Webdesign. Cap 5.
        teachers.add(new Teacher("Herr Eiche", Set.of("EDV_02", "Webdesign"), 5, "NONE"));
        // Birke: Webdesign, Malerei. Cap 4. Morning Only.
        teachers.add(new Teacher("Frau Birke", Set.of("Webdesign", "Malerei"), 4, "MORNING_ONLY"));
        // Kiefer: Malerei, Tonformen. Cap 5. Afternoon Only.
        teachers.add(new Teacher("Herr Kiefer", Set.of("Malerei", "Tonformen"), 5, "AFTERNOON_ONLY"));

        // --- D. Create Lesson Containers (One for every Room/Timeslot) ---
        List<Lesson> lessons = new ArrayList<>();
        long nextId = 0;

        for (Timeslot slot : timeslots) {
            for (Room room : rooms) {
                // We create a lesson, but subject and teacher are NOT set yet.
                // These will be assigned by the solver.
                Lesson lesson = new Lesson(String.valueOf(nextId++));
                lesson.setTimeslot(slot);
                lesson.setRoom(room);
                lessons.add(lesson);
            }
        }

        List<String> subjects = List.of("EDV_01", "EDV_02", "Webdesign", "Malerei", "Tonformen");

        return new Timetable(timeslots, rooms, teachers, lessons, subjects);
    }

    private static void printTimetable(Timetable timeTable) {
        LOGGER.info("");
        List<Room> rooms = timeTable.getRooms();
        List<Lesson> lessons = timeTable.getLessons();

        // Map lessons by timeslot and room for easy grid access
        Map<Timeslot, Map<Room, Lesson>> lessonMap = lessons.stream()
                .collect(Collectors.groupingBy(Lesson::getTimeslot,
                        Collectors.toMap(Lesson::getRoom, l -> l)));

        // Header
        LOGGER.info("|            | " + rooms.stream()
                .map(room -> String.format("%-12s", room.getName())).collect(Collectors.joining(" | ")) + " |");
        LOGGER.info("|" + "--------------|".repeat(rooms.size() + 1));

        for (Timeslot timeslot : timeTable.getTimeslots()) {
            Map<Room, Lesson> byRoomMap = lessonMap.getOrDefault(timeslot, Collections.emptyMap());

            // Row 1: Subject
            LOGGER.info("| " + String.format("%-10s",
                    timeslot.getDayOfWeek().toString().substring(0, 3) + " " + timeslot.getStartTime()) + " | "
                    + rooms.stream().map(room -> {
                Lesson l = byRoomMap.get(room);
                return String.format("%-12s", (l == null || l.getTeacher() == null || l.getSubject() == null) ? "      " : l.getSubject());
            }).collect(Collectors.joining(" | ")) + " |");

            // Row 2: Teacher
            LOGGER.info("|            | "
                    + rooms.stream().map(room -> {
                Lesson l = byRoomMap.get(room);
                return String.format("%-12s", (l == null || l.getTeacher() == null || l.getSubject() == null) ? "      " : l.getTeacher().getName());
            }).collect(Collectors.joining(" | ")) + " |");

            // Row 3: Capacity used
            LOGGER.info("|            | "
                    + rooms.stream().map(room -> {
                Lesson l = byRoomMap.get(room);
                return String.format("%-12s", (l == null || l.getTeacher() == null || l.getSubject() == null) ? "      " : "(" + l.getStudentCount() + " st.)");
            }).collect(Collectors.joining(" | ")) + " |");

            LOGGER.info("|" + "--------------|".repeat(rooms.size() + 1));
        }

        // --- TASK E: Statistics ---
        int totalRevenue = lessons.stream().mapToInt(Lesson::getTotalRevenue).sum();
        int totalStudents = lessons.stream().mapToInt(Lesson::getStudentCount).sum();

        LOGGER.info("");
        LOGGER.info("Total Revenue: " + totalRevenue + " €");
        LOGGER.info("Total Students Scheduled: " + totalStudents + " / 401");
    }


}
