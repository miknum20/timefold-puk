package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class MensaSlot {

    @PlanningId
    private Long id;

    // Diese Werte sind FEST (Problem Facts)
    private int tag;       // Tag 1 bis 30
    private int slotIndex; // 1 oder 2 (Es gibt 2 Slots pro Tag)

    // Das ist die Variable, die Timefold ändern darf
    @PlanningVariable
    private Gericht gericht;

    public MensaSlot() { }

    public MensaSlot(Long id, int tag, int slotIndex) {
        this.id = id;
        this.tag = tag;
        this.slotIndex = slotIndex;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public int getTag() { return tag; }
    public int getSlotIndex() { return slotIndex; }

    public Gericht getGericht() { return gericht; }
    public void setGericht(Gericht gericht) { this.gericht = gericht; }

    @Override
    public String toString() {
        return "Tag " + tag + " Slot " + slotIndex;
    }
}