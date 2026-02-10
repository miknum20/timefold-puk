package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import java.util.List;
import java.util.Objects;

public class Gericht {

    @PlanningId
    private String id; // z.B. "V01"

    private String name;
    private boolean isVegetarisch;
    
    private String kohlenhydrat; 
    private String protein;
    private List<String> alleZutaten; 
    
    private int kostenInCent;    // Materialkosten (Aufgabe 3C)
    private double beliebtheitsFaktor; // Preis-Faktor (Aufgabe 3D)

    public Gericht() { }

    public Gericht(String id, String name, boolean isVegetarisch, 
                   String kohlenhydrat, String protein, List<String> alleZutaten, 
                   int kostenInCent, double beliebtheitsFaktor) {
        this.id = id;
        this.name = name;
        this.isVegetarisch = isVegetarisch;
        this.kohlenhydrat = kohlenhydrat;
        this.protein = protein;
        this.alleZutaten = alleZutaten;
        this.kostenInCent = kostenInCent;
        this.beliebtheitsFaktor = beliebtheitsFaktor;
    }

    // Getter
    public String getId() { return id; }
    public boolean isVegetarisch() { return isVegetarisch; }
    public String getKohlenhydrat() { return kohlenhydrat; }
    public String getProtein() { return protein; }
    public List<String> getAlleZutaten() { return alleZutaten; }
    
    // --- Methoden für Aufgabe 3C (Kosten) ---
    public int getMaterialKosten() {
        return kostenInCent;
    }

    // --- Methoden für Aufgabe 3D (Einnahmen/Gewinn) ---
    public int getVerkaufspreis() {
        // Verkaufspreis = Materialkosten * Beliebtheitsfaktor
        return (int) (kostenInCent * beliebtheitsFaktor);
    }

    public int getGewinn() {
        // Gewinn = Verkaufspreis - Materialkosten
        return getVerkaufspreis() - kostenInCent;
    }

    @Override
    public String toString() { return name; }

    // WICHTIG: Equals und HashCode für korrekte Vergleiche im Solver
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gericht gericht = (Gericht) o;
        return Objects.equals(id, gericht.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}