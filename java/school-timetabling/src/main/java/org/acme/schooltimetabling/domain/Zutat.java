package org.acme.schooltimetabling.domain;

public class Zutat {
    private String name;
    private ZutatTyp typ;
    private int kostenInCent;

    public Zutat(String name, ZutatTyp typ, int kostenInCent) {
        this.name = name;
        this.typ = typ;
        this.kostenInCent = kostenInCent;
    }

    public String getName() { return name; }
    public ZutatTyp getTyp() { return typ; }
    public int getKostenInCent() { return kostenInCent; }
    
    @Override
    public String toString() { return name; }
}