package com.RomanLegacy;

/**
 * Rappresentazione JSON di un NarrativeEvent. I campi assenti nel JSON
 * restano ai default Java qui sotto (coerenti con il Builder).
 * title/description/label sono il testo di fallback: i testi tradotti
 * si risolvono da .properties con event.ID.title / .description / .choice.N
 */
public class EventDef {
    public String id;
    public String title;
    public String description;

    // TIMER, POPULATION_THRESHOLD, RESOURCE_THRESHOLD, FACTION_ARRIVED,
    // FACTION_RELATION, BUILDING_EXISTS, NEW_WORLD_UNLOCKED
    // Se assente l'evento non scatta mai (come triggerConfigured = false).
    public String trigger;
    public boolean repeatable = false;
    public float delay = 0f;

    public int thresholdClass;
    public String thresholdResource;
    public int thresholdAmount;

    public String faction; // nome di Faction
    public int minRelation = -100;
    public int maxRelation = 100;

    public String buildingType; // id di BuildingType
    public int buildingCount = 1;

    public ChoiceDef[] choices; // vuoto/assente = evento informativo

    public static class ChoiceDef {
        public String label;
        public EffectDef[] effects;
    }

    public static class EffectDef {
        public String type; // RESOURCE, IDEOLOGY, FACTION_RELATION, DIFFICULTY
        public String resource; // RESOURCE
        public float amount; // RESOURCE
        public float shift; // IDEOLOGY
        public String faction; // FACTION_RELATION
        public int delta; // FACTION_RELATION
        public String difficulty; // DIFFICULTY
    }
}