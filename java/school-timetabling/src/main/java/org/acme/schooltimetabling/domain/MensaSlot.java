package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class MensaSlot {

    @PlanningId
    private Long id;

    private int tag;
    private int slotIndex;

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