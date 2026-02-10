package org.acme.schooltimetabling;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import org.acme.schooltimetabling.domain.Gericht;
import org.acme.schooltimetabling.domain.MensaPlan;
import org.acme.schooltimetabling.domain.MensaSlot;
import org.acme.schooltimetabling.solver.MensaConstraintProvider;

public class TimetableApp {

    public static void main(String[] args) {
        SolverFactory<MensaPlan> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(MensaPlan.class)
                .withEntityClasses(MensaSlot.class)
                .withConstraintProviderClass(MensaConstraintProvider.class)
                // 10 Sekunden Laufzeit für optimale Gewinnberechnung
                .withTerminationSpentLimit(Duration.ofSeconds(10)));

        MensaPlan problem = generateData();

        Solver<MensaPlan> solver = solverFactory.buildSolver();
        MensaPlan solution = solver.solve(problem);

        printTimetable(solution);
    }

    public static MensaPlan generateData() {
        List<Gericht> gerichte = new ArrayList<>();

        // Faktoren: 1.5 (Unbeliebt), 2.2 (Neutral), 2.5 (Beliebt), 3.0 (Begehrt)

        // TEIL 1: VEGETARISCH (15 Stück)
        gerichte.add(new Gericht("V01", "Tofu-Pasta Napoli", true, "Pasta", "Tofu", Arrays.asList("Pasta", "Tofu", "Tomate", "Tomatensauce"), 200, 2.2)); // Neutral
        gerichte.add(new Gericht("V02", "Cremige Pilz-Pasta", true, "Pasta", "Tofu", Arrays.asList("Pasta", "Tofu", "Pilze", "Sahnesauce"), 220, 2.5)); // Beliebt
        gerichte.add(new Gericht("V03", "Fleischersatz-Topf", true, "Kartoffeln", "Fleischersatz", Arrays.asList("Kartoffeln", "Fleischersatz", "Bohnen", "Tomatensauce"), 300, 1.5)); // Unbeliebt
        gerichte.add(new Gericht("V04", "Kartoffel-Zucchini-Pfanne", true, "Kartoffeln", "Tofu", Arrays.asList("Kartoffeln", "Tofu", "Zucchini", "Sour Cream"), 280, 2.2));
        gerichte.add(new Gericht("V05", "Asiatischer Reis-Teller", true, "Reis", "Tofu", Arrays.asList("Reis", "Tofu", "Brokkoli", "Sahnesauce"), 250, 2.5));
        gerichte.add(new Gericht("V06", "Feurige Bohnen-Reispfanne", true, "Reis", "Fleischersatz", Arrays.asList("Reis", "Fleischersatz", "Bohnen", "Pfeffersauce"), 270, 2.2));
        gerichte.add(new Gericht("V07", "Spinat-Pasta Verde", true, "Pasta", "Fleischersatz", Arrays.asList("Pasta", "Fleischersatz", "Spinat", "Sahnesauce"), 260, 2.2));
        gerichte.add(new Gericht("V08", "Zwiebel-Ragout", true, "Kartoffeln", "Fleischersatz", Arrays.asList("Kartoffeln", "Fleischersatz", "Zwiebeln", "Pfeffersauce"), 290, 1.5));
        gerichte.add(new Gericht("V09", "Tofu-Steak & Ofenkartoffeln", true, "Kartoffeln", "Tofu", Arrays.asList("Kartoffeln", "Tofu", "Tomate", "Sour Cream"), 310, 3.0)); // Begehrt
        gerichte.add(new Gericht("V10", "Reis-Pfanne Provencale", true, "Reis", "Tofu", Arrays.asList("Reis", "Tofu", "Zucchini", "Tomatensauce"), 240, 2.2));
        gerichte.add(new Gericht("V11", "Pasta Bolognese-Art", true, "Pasta", "Fleischersatz", Arrays.asList("Pasta", "Fleischersatz", "Zwiebeln", "Tomatensauce"), 250, 2.5));
        gerichte.add(new Gericht("V12", "Brokkoli-Kart.-Gratin", true, "Kartoffeln", "Fleischersatz", Arrays.asList("Kartoffeln", "Fleischersatz", "Brokkoli", "Sahnesauce"), 280, 2.5));
        gerichte.add(new Gericht("V13", "Reis-Bowl mit Pilzen", true, "Reis", "Fleischersatz", Arrays.asList("Reis", "Fleischersatz", "Pilze", "Sour Cream"), 260, 2.5));
        gerichte.add(new Gericht("V14", "Pasta mit Pfeffer-Tofu", true, "Pasta", "Tofu", Arrays.asList("Pasta", "Tofu", "Spinat", "Pfeffersauce"), 230, 2.2));
        gerichte.add(new Gericht("V15", "Bunter Reis-Topf", true, "Reis", "Fleischersatz", Arrays.asList("Reis", "Fleischersatz", "Tomate", "Pfeffersauce"), 250, 1.5));

        // TEIL 2: FLEISCH & FISCH (16 Stück)
        gerichte.add(new Gericht("F01", "Hähnchen-Curry", false, "Reis", "Hähnchen", Arrays.asList("Reis", "Hähnchen", "Zucchini", "Sahnesauce"), 350, 2.5)); // Beliebt
        gerichte.add(new Gericht("F02", "Schweinesteak & Bohnen", false, "Kartoffeln", "Schwein", Arrays.asList("Kartoffeln", "Schwein", "Bohnen", "Pfeffersauce"), 400, 3.0)); // Begehrt
        gerichte.add(new Gericht("F03", "Lachs-Pasta in Sahne", false, "Pasta", "Fisch", Arrays.asList("Pasta", "Fisch", "Spinat", "Sahnesauce"), 450, 3.0)); // Begehrt
        gerichte.add(new Gericht("F04", "Hähnchen Rustikal", false, "Kartoffeln", "Hähnchen", Arrays.asList("Kartoffeln", "Hähnchen", "Pilze", "Sour Cream"), 380, 2.5));
        gerichte.add(new Gericht("F05", "Schweinegeschnetzeltes", false, "Reis", "Schwein", Arrays.asList("Reis", "Schwein", "Pilze", "Sahnesauce"), 370, 2.5));
        gerichte.add(new Gericht("F06", "Fischfilet Mediterran", false, "Reis", "Fisch", Arrays.asList("Reis", "Fisch", "Tomate", "Tomatensauce"), 420, 2.5));
        gerichte.add(new Gericht("F07", "Pasta Pollo Pomodoro", false, "Pasta", "Hähnchen", Arrays.asList("Pasta", "Hähnchen", "Tomate", "Tomatensauce"), 320, 2.5));
        gerichte.add(new Gericht("F08", "Schweinebraten-Topf", false, "Kartoffeln", "Schwein", Arrays.asList("Kartoffeln", "Schwein", "Zwiebeln", "Pfeffersauce"), 410, 2.2)); // Neutral
        gerichte.add(new Gericht("F09", "Fischragout & Brokkoli", false, "Kartoffeln", "Fisch", Arrays.asList("Kartoffeln", "Fisch", "Brokkoli", "Sour Cream"), 430, 2.5));
        gerichte.add(new Gericht("F10", "Hähnchen-Pasta Alfredo", false, "Pasta", "Hähnchen", Arrays.asList("Pasta", "Hähnchen", "Brokkoli", "Sahnesauce"), 360, 2.5));
        gerichte.add(new Gericht("F11", "Schweinefilet Reisrand", false, "Reis", "Schwein", Arrays.asList("Reis", "Schwein", "Zucchini", "Pfeffersauce"), 450, 3.0)); // Begehrt
        gerichte.add(new Gericht("F12", "Gebackener Fisch", false, "Kartoffeln", "Fisch", Arrays.asList("Kartoffeln", "Fisch", "Zwiebeln", "Sour Cream"), 400, 2.2));
        gerichte.add(new Gericht("F13", "Hähnchen-Bohnen-Pfanne", false, "Reis", "Hähnchen", Arrays.asList("Reis", "Hähnchen", "Bohnen", "Tomatensauce"), 340, 2.2));
        gerichte.add(new Gericht("F14", "Schweinesteak Spezial", false, "Pasta", "Schwein", Arrays.asList("Pasta", "Schwein", "Spinat", "Pfeffersauce"), 390, 3.0)); // Begehrt
        gerichte.add(new Gericht("F15", "Fisch-Pasta Tomate", false, "Pasta", "Fisch", Arrays.asList("Pasta", "Fisch", "Zucchini", "Tomatensauce"), 410, 2.5));
        gerichte.add(new Gericht("F16", "Hähnchen-Spinat-Gratin", false, "Kartoffeln", "Hähnchen", Arrays.asList("Kartoffeln", "Hähnchen", "Spinat", "Pfeffersauce"), 370, 2.5));

        List<MensaSlot> slots = new ArrayList<>();
        long idCounter = 0;
        for (int tag = 1; tag <= 30; tag++) {
            slots.add(new MensaSlot(++idCounter, tag, 1));
            slots.add(new MensaSlot(++idCounter, tag, 2));
        }

        return new MensaPlan(gerichte, slots);
    }

    private static void printTimetable(MensaPlan solution) {
        System.out.println("");
        System.out.println("--- Mensa Plan (Optimiert auf Gewinnmaximierung - Aufgabe 3D) ---");
        System.out.println("| Tag | Slot | Gericht Name                   | Vegetarisch?  | Kosten   | Verkauf   | Gewinn    |");
        System.out.println("|-----|------|--------------------------------|---------------|----------|-----------|-----------|");
        
        solution.getSlots().sort((a, b) -> {
            if (a.getTag() != b.getTag()) return a.getTag() - b.getTag();
            return a.getSlotIndex() - b.getSlotIndex();
        });

        long gesamtGewinn = 0;
        long gesamtKosten = 0;

        for (MensaSlot slot : solution.getSlots()) {
            if (slot.getGericht() != null) {
                double kosten = slot.getGericht().getMaterialKosten() / 100.0;
                double preis = slot.getGericht().getVerkaufspreis() / 100.0;
                double gewinn = slot.getGericht().getGewinn() / 100.0;
                
                gesamtGewinn += slot.getGericht().getGewinn();
                gesamtKosten += slot.getGericht().getMaterialKosten(); // Kosten aufsummieren
                
                System.out.printf("| %3d | %4d | %-30s | %-12b | %5.2f EUR | %5.2f EUR | %5.2f EUR |%n", 
                    slot.getTag(), 
                    slot.getSlotIndex(), 
                    slot.getGericht().toString(), 
                    slot.getGericht().isVegetarisch(),
                    kosten, preis, gewinn);
            }
        }
        System.out.println("---------------------------------------------------------------------------------------------");
        // Ausgabe beider Werte
        System.out.printf("Gesamtkosten für 30 Tage: %.2f EUR%n", gesamtKosten / 100.0);
        System.out.printf("Gesamtgewinn für 30 Tage: %.2f EUR%n", gesamtGewinn / 100.0);
    }
}