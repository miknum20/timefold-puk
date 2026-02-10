package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import org.acme.schooltimetabling.domain.Gericht;
import org.acme.schooltimetabling.domain.MensaSlot;

public class MensaConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            // --- HARD CONSTRAINTS  ---
            fleischUndVeggieMix(constraintFactory),           
            keineGleichenKomponentenAnFolgetagen(constraintFactory),
            unterschiedlicheGerichteAmTag(constraintFactory), 
            gerichtWiederholungVermeiden(constraintFactory),  
            
            // --- SOFT CONSTRAINTS ---
            zutatenSynergieNutzen(constraintFactory),
            
            // --- AUFGABE 3C: KOSTENMINIMIERUNG (DEAKTIVIERT FÜR 3D) ---
            // kostenMinimieren(constraintFactory), 

            // --- AUFGABE 3D: GEWINNMAXIMIERUNG (AKTIV) ---
            gewinnMaximieren(constraintFactory)
        };
    }

    // Erfüllt "Mindestens ein vegetarisches Gericht pro Tag" 
    // Strategie: Wir erzwingen einen Mix (1x Veggie, 1x Fleisch), was die Regel impliziert.
    Constraint fleischUndVeggieMix(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(MensaSlot.class)
                .groupBy(MensaSlot::getTag, 
                         ConstraintCollectors.toList(MensaSlot::getGericht))
                .filter((tag, gerichte) -> {
                    boolean hatVeggie = gerichte.stream().anyMatch(Gericht::isVegetarisch);
                    boolean hatFleisch = gerichte.stream().anyMatch(g -> !g.isVegetarisch());
                    // Fehler, wenn nicht BEIDES vorhanden ist
                    return !(hatVeggie && hatFleisch);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Mix aus Fleisch und Vegetarisch gefordert");
    }

    // Verhindert Reis-Reis an Tag X und Tag X+1
    Constraint keineGleichenKomponentenAnFolgetagen(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(MensaSlot.class)
                .join(MensaSlot.class, 
                      Joiners.equal(slot -> slot.getTag() + 1, MensaSlot::getTag))
                .filter((slot1, slot2) -> {
                    if (slot1.getGericht() == null || slot2.getGericht() == null) return false;
                    String kh1 = slot1.getGericht().getKohlenhydrat();
                    String prot1 = slot1.getGericht().getProtein();
                    String kh2 = slot2.getGericht().getKohlenhydrat();
                    String prot2 = slot2.getGericht().getProtein();
                    return kh1.equals(kh2) || prot1.equals(prot2);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Gleiche Komponenten an Folgetagen");
    }

    // Verhindert, dass Gericht X zweimal am selben Tag serviert wird
    Constraint unterschiedlicheGerichteAmTag(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(MensaSlot.class)
                .join(MensaSlot.class,
                        Joiners.equal(MensaSlot::getTag),
                        Joiners.lessThan(MensaSlot::getSlotIndex))
                .filter((slot1, slot2) -> slot1.getGericht().equals(slot2.getGericht()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Doppeltes Gericht am Tag");
    }

    // Sorgt für Abwechslung über den Monat (Wiederholung erst nach 5 Tagen)
    Constraint gerichtWiederholungVermeiden(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(MensaSlot.class)
                .join(MensaSlot.class,
                        Joiners.equal(MensaSlot::getGericht), 
                        Joiners.lessThan(MensaSlot::getId))
                .filter((slot1, slot2) -> Math.abs(slot1.getTag() - slot2.getTag()) < 5)
                .penalize(HardSoftScore.ONE_HARD) 
                .asConstraint("Gericht zu oft wiederholt");
    }

    // Soft Constraint: Belohnt gleiche Zutaten am selben Tag (Einkaufslogistik)
    Constraint zutatenSynergieNutzen(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(MensaSlot.class)
                .join(MensaSlot.class,
                        Joiners.equal(MensaSlot::getTag),
                        Joiners.lessThan(MensaSlot::getSlotIndex)) 
                .reward(HardSoftScore.ONE_SOFT, (slot1, slot2) -> {
                    int matches = 0;
                    for (String zutat1 : slot1.getGericht().getAlleZutaten()) {
                        if (slot2.getGericht().getAlleZutaten().contains(zutat1)) {
                            matches++;
                        }
                    }
                    return matches * 10; 
                })
                .asConstraint("Zutaten Synergie");
    }

    /* // --- AUFGABE 3C: KOSTENMINIMIERUNG (Auskommentiert für 3D) ---
    Constraint kostenMinimieren(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(MensaSlot.class)
                .filter(slot -> slot.getGericht() != null)
                .penalize(HardSoftScore.ONE_SOFT, 
                        slot -> slot.getGericht().getMaterialKosten()) 
                .asConstraint("Kostenminimierung");
    }
    */

    // --- AUFGABE 3D: GEWINNMAXIMIERUNG ---
    Constraint gewinnMaximieren(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(MensaSlot.class)
                .filter(slot -> slot.getGericht() != null)
                // Wir belohnen den Gewinn (Verkauf - Kosten)
                .reward(HardSoftScore.ONE_SOFT, 
                        slot -> slot.getGericht().getGewinn()) 
                .asConstraint("Gewinnmaximierung");
    }
}