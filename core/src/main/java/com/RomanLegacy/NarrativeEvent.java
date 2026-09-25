package com.RomanLegacy;

import java.util.ArrayList;
import java.util.List;

import com.RomanLegacy.ResourceManager.Resource;

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

        // DICHIARAZIONE EVENTI — esempi, aggiungine quanti vuoi
        public static final NarrativeEvent THE_NEW_CITY = new Builder(
                        "THE_NEW_CITY", "The new city",
                        "The new colonists have landed on a recently discovered island. Now they have to start the new city of the Roman legacy. (choose your difficulty)")
                        .populationThreshold(0, 0)
                        .delay(0f)
                        .choice("EASY",
                                        EventEffect.resource("wood", 150),
                                        EventEffect.resource("brick", 50),
                                        EventEffect.resource("coin", 20000),
                                        EventEffect.difficulty(Difficulty.EASY))
                        .choice("MEDIUM",
                                        EventEffect.resource("wood", 100),
                                        EventEffect.resource("brick", 40),
                                        EventEffect.resource("coin", 15000),
                                        EventEffect.difficulty(Difficulty.MEDIUM))
                        .choice("HARD",
                                        EventEffect.resource("wood", 50),
                                        EventEffect.resource("brick", 20),
                                        EventEffect.resource("coin", 7500),
                                        EventEffect.difficulty(Difficulty.HARD))
                        .build();

        public static final NarrativeEvent SERVICES = new Builder(
                        "SERVICES", "City's services",
                        "Your city is prospering, but citizen don't require only goods, they need clean homes, feel safe, rest and entertain themeselves")
                        .populationThreshold(0, 1)
                        .info()
                        .build();

        /*
         * public static final NarrativeEvent GRAIN = new Builder(
         * "GRAIN", "",
         * "")
         * .resourcesThreshold("grain", 1)
         * .info()
         * .build();
         */

        public static final NarrativeEvent BETTER_SERVICES = new Builder(
                        "BETTER_SERVICES", "City's services",
                        "Your city has grown tremendously and the demand for services has increased; it is time to build more powerful buildings")
                        .populationThreshold(3, 500)
                        .info()
                        .build();

        public static final NarrativeEvent BOILERS = new Builder("BOILERS", "Steam unlocked",
                        "From Rome arrives a revolutionary invention, BOILERS! capable to convert water into steam using coal")
                        .buildingExists(BuildingType.STEELWORK)
                        .choice("Promise to build a boiler",
                                        EventEffect.ideology(10))
                        .choice("Not now")
                        .build();

        public static final NarrativeEvent ELECTRICITY = new Builder(
                        "ELECTRICITY", "Electricity unlocked",
                        "A Roman engineer has demonstrated a revolutionary invention: ELECTRICITY! This new power source could transform your industries, making them faster and more efficient")
                        .populationThreshold(3, 500)
                        .choice("Invest in this new technology",
                                        EventEffect.ideology(30f))
                        .choice("take the invention but don't spend a single money",
                                        EventEffect.ideology(10f))
                        .choice("We don't need such novelties",
                                        EventEffect.ideology(-10f))
                        .build();

        public static final NarrativeEvent STEELWORKS = new Builder("STEELWORKS", "Steel unlocked",
                        "There are rumors circulating about a material stronger than pure iron. It's called STEEL.")
                        .buildingExists(BuildingType.COALMINE)
                        .delay(20f)
                        .choice("Promise to build a steelwork",
                                        EventEffect.ideology(10))
                        .choice("Not now")
                        .build();

        public static final NarrativeEvent MORE_EFFICIENT_WAY = new Builder("MORE_EFFICIENT_WAY",
                        "COAL, a more efficient energy source", "Coal has been proven a better energy source than wood")
                        .populationThreshold(2, 1)
                        .choice("trust the source",
                                        EventEffect.resource("coal", 20),
                                        EventEffect.resource("tools", 10),
                                        EventEffect.ideology(20f))
                        .choice("defy the source",
                                        EventEffect.resource("wood", 20),
                                        EventEffect.resource("planks", 20),
                                        EventEffect.ideology(-20f))
                        .build();

        public static final NarrativeEvent STEAM_MOTOR = new Builder(
                        "STEAM_MOTOR", "A new use for steam",
                        "The STEAM MOTOR can burn coal or run on steam tu produce mecchanical force. New methods of transportation are arriving for every citizen of the empire")
                        .populationThreshold(3, 200)
                        .choice("Convert all factories to steam powered machines (-2000 coins)",
                                        EventEffect.resource("coin", -2000),
                                        EventEffect.ideology(25f))
                        // sblocca il motore
                        .choice("convert some factories to steam powered machines (-1000 coins)",
                                        EventEffect.resource("coin", -1000),
                                        EventEffect.ideology(5f))
                        // sblocca il motore
                        .choice("Man power is good for now",
                                        EventEffect.ideology(-15f))
                        .build();

        public static final NarrativeEvent ELECTRIC_MOTOR = new Builder(
                        "ELECTRIC_MOTOR", "the new engine",
                        "The ELECTRIC MOTOR is smaller, cleaner, and more powerful than steam engines. It uses electricity to produce more mecchanical force")
                        .populationThreshold(4, 800)
                        .choice("Convert all factories to electric powered machines (-5000 coin)",
                                        EventEffect.resource("coin", -5000),
                                        EventEffect.ideology(45f))
                        // sblocca il motore elettrico
                        .choice("convert some factories to electric powered machines (-3000 coins)",
                                        EventEffect.resource("coin", -3000),
                                        // sblocca il motore elettrico
                                        EventEffect.ideology(20f))
                        .choice("Steam is good enough for now",
                                        EventEffect.ideology(-5f))
                        .build();

        public static final NarrativeEvent OIL_DISCOVERY = new Builder(
                        "OIL_DISCOVERY", "Black Gold",
                        "Workers have found a strange black liquid seeping from the ground. It burns well and could be used as fuel. The New World has large deposits of this new material")
                        .newWorldUnlocked()
                        .delay(45f)
                        .choice("Build an oil dock",
                                        EventEffect.ideology(25f))
                        // sblocca il pozzo petrolifero
                        .choice("Ignore it for now")
                        .build();

        public static final NarrativeEvent ARTISANS = new Builder("ARTISANS", "Artisans arrival",
                        "your population have started creating small businesses producing handmade objects")
                        .populationThreshold(1, 1)
                        .choice("help these small workshops",
                                        EventEffect.ideology(-10f))
                        .choice("They are ready and can take care of themeselves",
                                        EventEffect.resource("tools", 10),
                                        EventEffect.resource("ceramics", 10),
                                        EventEffect.ideology(10f))
                        .build();

        public static final NarrativeEvent EQUITES = new Builder("EQUITES", "Equites arrival",
                        "your population has got richer, they started upgrading theur houses with superior and raffined objects")
                        .populationThreshold(2, 1)
                        .choice("promise to produce more of these objects",
                                        EventEffect.ideology(15f))
                        .choice("promise nothing")
                        .build();

        public static final NarrativeEvent PATRICIANS = new Builder("PATRICIANS", "Patricians arrival",
                        "some elité class from the continent has started docking in our city, they are the richest class, the patricians")
                        .populationThreshold(3, 1)
                        .choice("Make their life more comfortable",
                                        EventEffect.ideology(-20f))
                        .choice("They can adapt",
                                        EventEffect.ideology(5f))
                        .build();

        public static final NarrativeEvent ENGINEERS = new Builder("ENGINEERS", "Engineers arrival",
                        "From the smartest minds of our city is born a new social class. The engineers")
                        .populationThreshold(4, 1)
                        .choice("Fulfill their demands (have built a motor factory, have built at least 3 boiler)",
                                        EventEffect.ideology(20f))
                        .choice("This 'industrial revolution' has yet to arrive",
                                        EventEffect.ideology(-5f))
                        .build();

        public static final NarrativeEvent MAGNATES = new Builder("MAGNATES", "Magnates arrival",
                        "Your wealthiest citizens have accumulated so much that they have decided to invest in our city")
                        .populationThreshold(5, 1)
                        .choice("Accept all types of investments (+20000 coins)",
                                        EventEffect.resource("coin", 20000),
                                        EventEffect.ideology(30f))
                        .choice("Accept only public investments (+10000 coins)",
                                        EventEffect.resource("coin", 10000),
                                        EventEffect.ideology(10f))
                        .choice("Accept only industrial investments (+15000 coins)",
                                        EventEffect.resource("coin", 15000),
                                        EventEffect.ideology(20f))
                        .choice("Reject all investments",
                                        EventEffect.ideology(-40f))
                        .build();

        public static final NarrativeEvent SENATORS = new Builder(
                        "SENATORS", "New faction arrived: Senators",
                        "")
                        .factionArrived(Faction.SENATORS)
                        .delay(20f)
                        .choice("Welcome them with enthusiasm",
                                        EventEffect.ideology(-20f))
                        .choice("They don't think the way we do. Why welcome them?",
                                        EventEffect.factionRelation(Faction.SENATORS, -10),
                                        EventEffect.ideology(20f))
                        .build();

        public static final NarrativeEvent LEGIONARIES = new Builder(
                        "LEGIONARIES", "New faction arrived: Legionaries",
                        "")
                        .factionArrived(Faction.LEGIONARIES)
                        .delay(20f)
                        .choice("Welcome them with enthusiasm",
                                        EventEffect.ideology(-20f))
                        .choice("They don't think the way we do. Why welcome them?",
                                        EventEffect.factionRelation(Faction.LEGIONARIES, -10),
                                        EventEffect.ideology(20f))
                        .build();

        public static final NarrativeEvent THE_BLUES = new Builder(
                        "THE_BLUES", "New faction arrived: The blues",
                        "")
                        .factionArrived(Faction.THE_BLUES)
                        .delay(20f)
                        .choice("Welcome them with enthusiasm",
                                        EventEffect.ideology(-20f))
                        .choice("They don't think the way we do. Why welcome them?",
                                        EventEffect.factionRelation(Faction.THE_BLUES, -10),
                                        EventEffect.ideology(20f))
                        .build();

        public static final NarrativeEvent OLDBLOODS = new Builder(
                        "OLDBLOODS", "New faction arrived: Oldbloods",
                        "")
                        .factionArrived(Faction.OLDBLOODS)
                        .delay(20f)
                        .choice("Welcome them with enthusiasm",
                                        EventEffect.ideology(-20f))
                        .choice("They don't think the way we do. Why welcome them?",
                                        EventEffect.factionRelation(Faction.OLDBLOODS, -10),
                                        EventEffect.ideology(20f))
                        .build();

        public static final NarrativeEvent ACADEMICS = new Builder(
                        "ACADEMICS", "New faction arrived: Academics",
                        "")
                        .factionArrived(Faction.ACADEMICS)
                        .delay(20f)
                        .choice("Welcome them with enthusiasm",
                                        EventEffect.ideology(20f))
                        .choice("They don't think the way we do. Why welcome them?",
                                        EventEffect.factionRelation(Faction.ACADEMICS, -10),
                                        EventEffect.ideology(-20f))
                        .build();

        public static final NarrativeEvent FUTURISTS = new Builder(
                        "FUTURISTS", "New faction arrived: Futurists",
                        "")
                        .factionArrived(Faction.FUTURISTS)
                        .delay(20f)
                        .choice("Welcome them with enthusiasm",
                                        EventEffect.ideology(20f))
                        .choice("They don't think the way we do. Why welcome them?",
                                        EventEffect.factionRelation(Faction.FUTURISTS, -10),
                                        EventEffect.ideology(-20f))
                        .build();

        public static final NarrativeEvent INDUSTRIALISTS = new Builder(
                        "INDUSTRIALISTS", "New faction arrived: Industrialists",
                        "")
                        .factionArrived(Faction.INDUSTRIALISTS)
                        .delay(20f)
                        .choice("Welcome them with enthusiasm",
                                        EventEffect.ideology(20f))
                        .choice("They don't think the way we do. Why welcome them?",
                                        EventEffect.factionRelation(Faction.INDUSTRIALISTS, -10),
                                        EventEffect.ideology(-20f))
                        .build();

        public static final NarrativeEvent EXPLORERS = new Builder(
                        "EXPLORERS", "New faction arrived: Explorers",
                        "")
                        .factionArrived(Faction.EXPLORERS)
                        .delay(20f)
                        .choice("Welcome them with enthusiasm",
                                        EventEffect.ideology(20f))
                        .choice("They don't think the way we do. Why welcome them?",
                                        EventEffect.factionRelation(Faction.EXPLORERS, -10),
                                        EventEffect.ideology(-20f))
                        .build();

        public static final NarrativeEvent NEW_WORLD_EXPEDITION = new Builder(
                        "NEW_WORLD_EXPEDITION", "New World expedition",
                        "Your explorers have taken some ships and sailed beyon the pillars of Ercules")
                        .populationThreshold(3, 300)
                        .choice("Fund the expedition")
                        .choice("We are not ready yet")
                        .build();

        private static final List<NarrativeEvent> ALL_EVENTS = new ArrayList<>();

        static {
                ALL_EVENTS.add(THE_NEW_CITY);
                ALL_EVENTS.add(SERVICES);
                ALL_EVENTS.add(BETTER_SERVICES);
                ALL_EVENTS.add(BOILERS);
                ALL_EVENTS.add(STEELWORKS);
                ALL_EVENTS.add(ELECTRICITY);
                ALL_EVENTS.add(MORE_EFFICIENT_WAY);
                ALL_EVENTS.add(STEAM_MOTOR);
                ALL_EVENTS.add(ELECTRIC_MOTOR);
                ALL_EVENTS.add(OIL_DISCOVERY);
                ALL_EVENTS.add(ARTISANS);
                ALL_EVENTS.add(EQUITES);
                ALL_EVENTS.add(PATRICIANS);
                ALL_EVENTS.add(ENGINEERS);
                ALL_EVENTS.add(MAGNATES);
                ALL_EVENTS.add(SENATORS);
                ALL_EVENTS.add(LEGIONARIES);
                ALL_EVENTS.add(THE_BLUES);
                ALL_EVENTS.add(OLDBLOODS);
                ALL_EVENTS.add(ACADEMICS);
                ALL_EVENTS.add(FUTURISTS);
                ALL_EVENTS.add(INDUSTRIALISTS);
                ALL_EVENTS.add(EXPLORERS);
                ALL_EVENTS.add(NEW_WORLD_EXPEDITION);

                // ALL_EVENTS.add(GRAIN);
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