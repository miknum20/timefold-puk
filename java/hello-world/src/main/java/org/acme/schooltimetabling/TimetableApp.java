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
                .withTerminationSpentLimit(Duration.ofSeconds(30)));



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
        int maxClassSize = 10;

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

        // OLD CODE:

//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
//
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
//        timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
//        if (demoData == DemoData.LARGE) {
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.WEDNESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.THURSDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.FRIDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.FRIDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.FRIDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId++), DayOfWeek.FRIDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
//            timeslots.add(new Timeslot(Long.toString(nextTimeslotId), DayOfWeek.FRIDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));
//        }
//
//        List<Room> rooms = new ArrayList<>(3);
//        long nextRoomId = 0L;
//        rooms.add(new Room(Long.toString(nextRoomId++), "Room A"));
//        rooms.add(new Room(Long.toString(nextRoomId++), "Room B"));
//        rooms.add(new Room(Long.toString(nextRoomId++), "Room C"));
//        if (demoData == DemoData.LARGE) {
//            rooms.add(new Room(Long.toString(nextRoomId++), "Room D"));
//            rooms.add(new Room(Long.toString(nextRoomId++), "Room E"));
//            rooms.add(new Room(Long.toString(nextRoomId), "Room F"));
//        }
//
//        List<Lesson> lessons = new ArrayList<>();
//        long nextLessonId = 0L;
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Mathe", "A. Turing", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Mathe", "A. Turing", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Biology", "C. Darwin", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "9th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "9th grade"));
//        if (demoData == DemoData.LARGE) {
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Mathe", "A. Turing", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Mathe", "A. Turing", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Mathe", "A. Turing", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "ICT", "A. Turing", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geography", "C. Darwin", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geology", "C. Darwin", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Drama", "I. Jones", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
//        }
//
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "French", "M. Curie", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Geography", "C. Darwin", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
//        lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "10th grade"));
//        if (demoData == DemoData.LARGE) {
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "ICT", "A. Turing", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Biology", "C. Darwin", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geology", "C. Darwin", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Drama", "I. Jones", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));
//
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "ICT", "A. Turing", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "French", "M. Curie", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geography", "C. Darwin", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Biology", "C. Darwin", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geology", "C. Darwin", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Drama", "P. Cruz", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));
//
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "ICT", "A. Turing", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "French", "M. Curie", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physics", "M. Curie", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geography", "C. Darwin", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Biology", "C. Darwin", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Geology", "C. Darwin", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "History", "I. Jones", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Drama", "P. Cruz", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Art", "S. Dali", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "12th grade"));
//            lessons.add(new Lesson(Long.toString(nextLessonId), "Physical education", "C. Lewis", "12th grade"));
//        }
//        return new Timetable(demoData.name(), timeslots, rooms, lessons);
    }

    private static void printTimetable(Timetable timeTable) {
        LOGGER.info("");
        List<Room> rooms = timeTable.getRooms();
        List<Lesson> lessons = timeTable.getLessons();
        Map<Timeslot, Map<Room, List<Lesson>>> lessonMap = lessons.stream()
                .filter(lesson -> lesson.getTimeslot() != null && lesson.getRoom() != null && lesson.getTeacher() != null)
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

            LOGGER.info("| " + String.format("%-10s",
                    timeslot.getDayOfWeek().toString().substring(0, 3) + " " + timeslot.getStartTime()) + " | "
                    + cells.stream().map(cellLessons -> String.format("%-10s",
                            cellLessons.stream().map(Lesson::getSubject).collect(Collectors.joining(", "))))
                            .collect(Collectors.joining(" | "))
                    + " |");
//            LOGGER.info("|            | "
//                    + cells.stream().map(cellLessons -> String.format("%-10s",
//                            cellLessons.stream().map(Lesson::getTeacher).collect(Collectors.joining(", "))))
//                            .collect(Collectors.joining(" | "))
//                    + " |");
//            LOGGER.info("|            | "
//                    + cells.stream().map(cellLessons -> String.format("%-10s",
//                            cellLessons.stream().map(Lesson::getStudentGroup).collect(Collectors.joining(", "))))
//                            .collect(Collectors.joining(" | "))
//                    + " |");

            // Print the ASSIGNED Teacher
            LOGGER.info("|            | "
                    + cells.stream().map(cellLessons -> String.format("%-10s",
                            cellLessons.stream().map(l -> l.getTeacher() == null ? "Unassigned" : l.getTeacher().getName())
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
            LOGGER.info("Unassigned lessons (Total: " + unassignedLessons.size() + ")");
            for (Lesson lesson : unassignedLessons) {
                // Hier fangen wir null-Werte sicher ab für die Ausgabe
                String tName = lesson.getTeacher() == null ? "No Teacher" : lesson.getTeacher().getName();
                LOGGER.info("  " + lesson.getSubject() + " (" + lesson.getStudentCount() + ") - " + tName);
            }
        }
    }

}
