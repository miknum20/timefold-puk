package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import java.util.List;

@PlanningSolution
public class MensaPlan {

    // Der Pool aus dem wir wählen können (die 31 Gerichte)
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Gericht> gerichte;

    // Die leeren Slots, die gefüllt werden müssen (60 Stück)
    @PlanningEntityCollectionProperty
    private List<MensaSlot> slots;

    @PlanningScore
    private HardSoftScore score;

    public MensaPlan() { }

    public MensaPlan(List<Gericht> gerichte, List<MensaSlot> slots) {
        this.gerichte = gerichte;
        this.slots = slots;
    }

    public List<Gericht> getGerichte() { return gerichte; }
    public List<MensaSlot> getSlots() { return slots; }
    public HardSoftScore getScore() { return score; }
    public void setScore(HardSoftScore score) { this.score = score; }
}