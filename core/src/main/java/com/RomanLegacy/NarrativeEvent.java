package com.RomanLegacy;

import java.util.ArrayList;
import java.util.List;

import com.RomanLegacy.ResourceManager.Resource;

import main.java.com.RomanLegacy.EventDef;
import main.java.com.RomanLegacy.EventDefLoader;

/**
 * Definisce un singolo evento narrativo: condizione di attivazione (trigger),
 * testo e le scelte disponibili al giocatore (anche una sola = evento
 * informativo).
 * Costruito con Builder pattern, sullo stesso schema di BuildingType.
 */
public class NarrativeEvent {

        public enum TriggerType {
                TIMER, POPULATION_THRESHOLD, FACTION_ARRIVED, FACTION_RELATION, BUILDING_EXISTS, NEW_WORLD_UNLOCKED,
                RESOURCE_THRESHOLD
        }

        // ── Campi finali ─────────────────────────────────────────────────────────────
        public final String id;
        public final String title;
        public final String description;
        public final TriggerType triggerType;
        public final boolean repeatable;
        public final int thresholdClass;
        public final String thresholdResource;
        public final int thresholdAmount;
        public final Faction faction;
        public final int minRelation;
        public final int maxRelation;
        public final float triggerDelay;
        public final boolean triggerConfigured; // ← NUOVO
        public final BuildingType requiredBuildingType; // ← NUOVO
        public final int requiredBuildingCount;

        public final EventChoice[] choices;

        private NarrativeEvent(Builder b) {
                this.id = b.id;
                this.title = b.title;
                this.description = b.description;
                this.triggerType = b.triggerType;
                this.repeatable = b.repeatable;
                this.thresholdClass = b.thresholdClass;
                this.thresholdResource = b.thresholdResource;
                this.thresholdAmount = b.thresholdAmount;
                this.faction = b.faction;
                this.minRelation = b.minRelation;
                this.maxRelation = b.maxRelation;
                this.triggerDelay = b.triggerDelay;
                this.triggerConfigured = b.triggerConfigured; // ← NUOVO
                this.requiredBuildingType = b.requiredBuildingType; // ← NUOVO
                this.requiredBuildingCount = b.requiredBuildingCount;
                this.choices = b.choices.toArray(new EventChoice[0]);
        }

        // ── Effetto applicabile da una scelta ───────────────────────────────────────
        public static class EventEffect {
                public enum Type {
                        RESOURCE, IDEOLOGY, FACTION_RELATION, DIFFICULTY
                }

                public final Type type;
                public final String resourceId;
                public final float resourceAmount;
                public final float ideologyShift;
                public final Faction targetFaction;
                public final int relationDelta;
                public final Difficulty difficulty;

                private EventEffect(Type type, String resourceId, float resourceAmount,
                                float ideologyShift, Faction targetFaction, int relationDelta, Difficulty difficulty) {
                        this.type = type;
                        this.resourceId = resourceId;
                        this.resourceAmount = resourceAmount;
                        this.ideologyShift = ideologyShift;
                        this.targetFaction = targetFaction;
                        this.relationDelta = relationDelta;
                        this.difficulty = difficulty;
                }

                public static EventEffect resource(String id, float amount) {
                        return new EventEffect(Type.RESOURCE, id, amount, 0f, null, 0, null);
                }

                public static EventEffect ideology(float shift) {
                        return new EventEffect(Type.IDEOLOGY, null, 0f, shift, null, 0, null);
                }

                public static EventEffect factionRelation(Faction f, int delta) {
                        return new EventEffect(Type.FACTION_RELATION, null, 0f, 0f, f, delta, null);
                }

                public static EventEffect difficulty(Difficulty d) {
                        return new EventEffect(Type.DIFFICULTY, null, 0f, 0f, null, 0, d);
                }
        }

        // ── Scelta del giocatore ─────────────────────────────────────────────────────
        public static class EventChoice {
                public final String label;
                public final EventEffect[] effects;

                public EventChoice(String label, EventEffect... effects) {
                        this.label = label;
                        this.effects = effects;
                }
        }

        // ── Builder ──────────────────────────────────────────────────────────────────
        public static class Builder {
                private final String id;
                private final String title;
                private final String description;
                private TriggerType triggerType = TriggerType.TIMER;
                private boolean repeatable = false;
                private int thresholdClass = 0;
                private String thresholdResource = null;
                private int thresholdAmount = 0;
                private Faction faction = null;
                private int minRelation = -100;
                private int maxRelation = 100;
                private float triggerDelay = 0f;
                private boolean triggerConfigured = false; // ← NUOVO
                private BuildingType requiredBuildingType = null; // ← NUOVO
                private int requiredBuildingCount = 1; // ← NUOVO
                private final List<EventChoice> choices = new ArrayList<>();

                public Builder(String id, String title, String description) {
                        this.id = id;
                        this.title = title;
                        this.description = description;
                }

                public Builder timer() {
                        this.triggerType = TriggerType.TIMER;
                        this.triggerConfigured = true;
                        return this;
                }

                public Builder populationThreshold(int classIndex, int amount) {
                        this.triggerType = TriggerType.POPULATION_THRESHOLD;
                        this.thresholdClass = classIndex;
                        this.thresholdAmount = amount;
                        this.triggerConfigured = true;
                        return this;
                }

                public Builder resourceThreshold(String resource, int amount) {
                        this.triggerType = TriggerType.RESOURCE_THRESHOLD;
                        this.thresholdResource = resource;
                        this.thresholdAmount = amount;
                        this.triggerConfigured = true;
                        return this;
                }

                public Builder factionArrived(Faction f) {
                        this.triggerType = TriggerType.FACTION_ARRIVED;
                        this.faction = f;
                        this.triggerConfigured = true;
                        return this;
                }

                public Builder factionRelation(Faction f, int min, int max) {
                        this.triggerType = TriggerType.FACTION_RELATION;
                        this.faction = f;
                        this.minRelation = min;
                        this.maxRelation = max;
                        this.triggerConfigured = true;
                        return this;
                }

                public Builder buildingExists(BuildingType type) {
                        return buildingExists(type, 1);
                }

                public Builder buildingExists(BuildingType type, int count) {
                        this.triggerType = TriggerType.BUILDING_EXISTS;
                        this.requiredBuildingType = type;
                        this.requiredBuildingCount = count;
                        this.triggerConfigured = true;
                        return this;
                }

                public Builder newWorldUnlocked() {
                        this.triggerType = TriggerType.NEW_WORLD_UNLOCKED;
                        this.triggerConfigured = true;
                        return this;
                }

                public Builder repeatable() {
                        this.repeatable = true;
                        return this;
                }

                public Builder choice(String label, EventEffect... effects) {
                        choices.add(new EventChoice(label, effects));
                        return this;
                }

                public Builder delay(float seconds) {
                        this.triggerDelay = seconds;
                        return this;
                }

                public Builder info() {
                        choices.add(new EventChoice("Continua"));
                        return this;
                }

                public NarrativeEvent build() {
                        if (choices.isEmpty())
                                choices.add(new EventChoice("Continua"));
                        return new NarrativeEvent(this);
                }
        }

        private static final List<NarrativeEvent> ALL_EVENTS = loadAll();

        private static List<NarrativeEvent> loadAll() {
                List<NarrativeEvent> list = new ArrayList<>();
                for (EventDef d : EventDefLoader.load("data/events.json")) {
                        try {
                                list.add(fromDef(d));
                        } catch (Exception e) {
                                Gdx.app.error("NarrativeEvent", "Invalid event '" + d.id + "': " + e);
                        }
                }
                return list;
        }

        private static NarrativeEvent fromDef(EventDef d) {
                if (d.title == null)
                        throw new IllegalArgumentException("missing title");

                Builder b = new Builder(d.id, d.title, d.description != null ? d.description : "");

                if (d.trigger != null) {
                        switch (TriggerType.valueOf(d.trigger)) {
                                case TIMER:
                                        b.timer();
                                        break;
                                case POPULATION_THRESHOLD:
                                        b.populationThreshold(d.thresholdClass, d.thresholdAmount);
                                        break;
                                case RESOURCE_THRESHOLD:
                                        b.resourceThreshold(d.thresholdResource, d.thresholdAmount);
                                        break;
                                case FACTION_ARRIVED:
                                        b.factionArrived(Faction.valueOf(d.faction));
                                        break;
                                case FACTION_RELATION:
                                        b.factionRelation(Faction.valueOf(d.faction), d.minRelation, d.maxRelation);
                                        break;
                                case BUILDING_EXISTS:
                                        b.buildingExists(BuildingType.valueOf(d.buildingType), d.buildingCount);
                                        break;
                                case NEW_WORLD_UNLOCKED:
                                        b.newWorldUnlocked();
                                        break;
                        }
                }

                if (d.repeatable)
                        b.repeatable();
                if (d.delay > 0f)
                        b.delay(d.delay);

                if (d.choices != null) {
                        for (EventDef.ChoiceDef c : d.choices) {
                                int n = c.effects != null ? c.effects.length : 0;
                                EventEffect[] effects = new EventEffect[n];
                                for (int i = 0; i < n; i++)
                                        effects[i] = effectFromDef(c.effects[i]);
                                b.choice(c.label, effects);
                        }
                }
                return b.build(); // senza scelte aggiunge da solo "Continue"
        }

        private static EventEffect effectFromDef(EventDef.EffectDef e) {
                switch (EventEffect.Type.valueOf(e.type)) {
                        case RESOURCE:
                                return EventEffect.resource(e.resource, e.amount);
                        case IDEOLOGY:
                                return EventEffect.ideology(e.shift);
                        case FACTION_RELATION:
                                return EventEffect.factionRelation(Faction.valueOf(e.faction), e.delta);
                        case DIFFICULTY:
                                return EventEffect.difficulty(Difficulty.valueOf(e.difficulty));
                        default:
                                throw new IllegalArgumentException("Unknown effect type: " + e.type);
                }
        }

        public static List<NarrativeEvent> getAllEvents() {
                return ALL_EVENTS;
        }

        public static NarrativeEvent byId(String id) {
                for (NarrativeEvent e : ALL_EVENTS) {
                        if (e.id.equals(id))
                                return e;
                }
                return null;
        }
}