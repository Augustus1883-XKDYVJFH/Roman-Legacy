package com.RomanLegacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Definisce tutti gli edifici disponibili nel gioco.
 *
 * Costruiti con Builder pattern per evitare errori di ordine nei parametri.
 *
 * SISTEMA CASE:
 * - Solo CABIN è buildable = true.
 * - Le altre case si ottengono tramite upgrade.
 */
public class BuildingType {

        // ── Campi finali ─────────────────────────────────────────────────────────────
        public final String id;
        public final String label;
        public final String colorHex;
        public final Map<String, Float> cost;
        public final Map<String, Float> input;
        public final String prod;
        public final int rate;
        public final int cycleTime;
        public final int w, h;
        public final int[] onlyTerrain;
        public final boolean isHouse;
        public final Integer houseLevel;
        public final String[] houseNeeds;
        public final boolean buildable;
        public final Region[] allowedRegions;
        public final ConnectionType requiredConnection;
        public final float maintenanceCost;
        public final Category category;
        public final PowerType requiredPower;
        public final int influenceRadius;
        public final boolean consumableOnly;
        public final boolean isMonument;
        public final String monumentChainId;
        public final int monumentLevel;
        public final Map<String, Float> upgradeCost;
        public final float upgradeTime;
        public final float axisShift;
        public final ServiceCategory serviceCategory;
        public final int serviceCapacity;
        public final ConsumptionServiceCategory consumptionServiceCategory;
        public final Integer workforceClass;
        public final int workforceAmount;
        public final Integer unlockClass;
        public final int unlockAmount;
        public final String requiredEventId;
        public final String[] penIds; // solo allevamenti: id dei recinti richiesti (null = nessuno)
        public final String penOwnerId; // solo recinti: id dell'allevamento a cui appartengono
        public final String fieldTypeId; // solo fattorie: id del campo che usano
        public final int fieldCap; // solo fattorie: numero massimo di campi
        public final String fieldOwnerId; // solo campi: id della fattoria a cui appartengono
        public final boolean requiresDeposit;

        private BuildingType(Builder b) {
                this.id = b.id;
                this.label = b.label;
                this.colorHex = b.colorHex;
                this.cost = b.cost;
                this.input = b.input;
                this.prod = b.prod;
                this.rate = b.rate;
                this.cycleTime = b.cycleTime;
                this.w = b.w;
                this.h = b.h;
                this.onlyTerrain = b.onlyTerrain;
                this.isHouse = b.isHouse;
                this.houseLevel = b.houseLevel;
                this.houseNeeds = b.houseNeeds;
                this.buildable = b.buildable;
                this.allowedRegions = b.allowedRegions;
                this.requiredConnection = b.requiredConnection;
                this.maintenanceCost = b.maintenanceCost;
                this.category = b.category;
                this.requiredPower = b.requiredPower;
                this.influenceRadius = b.influenceRadius;
                this.consumableOnly = b.consumableOnly;
                this.isMonument = b.isMonument;
                this.monumentChainId = b.monumentChainId;
                this.monumentLevel = b.monumentLevel;
                this.upgradeCost = b.upgradeCost;
                this.upgradeTime = b.upgradeTime;
                this.axisShift = b.axisShift;
                this.serviceCategory = b.serviceCategory;
                this.serviceCapacity = b.serviceCapacity;
                this.consumptionServiceCategory = b.consumptionServiceCategory;
                this.workforceClass = b.workforceClass;
                this.workforceAmount = b.workforceAmount;
                this.unlockClass = b.unlockClass;
                this.unlockAmount = b.unlockAmount;
                this.requiredEventId = b.requiredEventId;
                this.penIds = b.penIds;
                this.penOwnerId = b.penOwnerId;
                this.fieldTypeId = b.fieldTypeId;
                this.fieldCap = b.fieldCap;
                this.fieldOwnerId = b.fieldOwnerId;
                this.requiresDeposit = b.requiresDeposit;
        }

        // ── Builder ──────────────────────────────────────────────────────────────────
        public static class Builder {
                private String id;
                private final String label;
                private final String colorHex;
                private final int w, h;
                private Category category = null;
                private Map<String, Float> cost = new HashMap<>();
                private Map<String, Float> input = null;
                private String prod = null;
                private int rate = 0;
                private int cycleTime = 0;
                private int[] onlyTerrain = null;
                private boolean isHouse = false;
                private Integer houseLevel = null;
                private String[] houseNeeds = null;
                private boolean buildable = true;
                private Region[] allowedRegions = null;
                private ConnectionType requiredConnection = ConnectionType.NONE;
                private PowerType requiredPower = PowerType.NONE;
                private float maintenanceCost = 0f;
                private int influenceRadius = 0;
                private boolean consumableOnly = false;
                private boolean isMonument = false;
                private String monumentChainId = null;
                private int monumentLevel = 0;
                private Map<String, Float> upgradeCost = new HashMap<>();
                private float upgradeTime = 0f;
                private float axisShift = 0f;
                private ServiceCategory serviceCategory = null;
                private int serviceCapacity = 0;
                private ConsumptionServiceCategory consumptionServiceCategory = null;
                private Integer workforceClass = null;
                private int workforceAmount = 0;
                private Integer unlockClass = null;
                private int unlockAmount = 0;
                private String requiredEventId = null;
                private String[] penIds = null;
                private String penOwnerId = null;
                private String fieldTypeId = null;
                private int fieldCap = 0;
                private String fieldOwnerId = null;
                private boolean requiresDeposit = false;

                public Builder(String id, String label, String colorHex, int w, int h) {
                        this.id = id;
                        this.label = label;
                        this.colorHex = colorHex;
                        this.w = w;
                        this.h = h;
                }

                public Builder cost(Object... kv) {
                        this.cost = kvMap(kv);
                        return this;
                }

                public Builder input(Object... kv) {
                        this.input = kvMap(kv);
                        return this;
                }

                public Builder prod(String prod, int rate, int cycleTime) {
                        this.prod = prod;
                        this.rate = rate;
                        this.cycleTime = cycleTime;
                        return this;
                }

                public Builder terrain(int... types) {
                        this.onlyTerrain = types;
                        return this;
                }

                public Builder house(int level, String... needs) {
                        this.isHouse = true;
                        this.houseLevel = level;
                        this.houseNeeds = needs;
                        return this;
                }

                public Builder notBuildable() {
                        this.buildable = false;
                        return this;
                }

                public Builder region(Region... regions) {
                        this.allowedRegions = regions;
                        return this;
                }

                public Builder requires(ConnectionType conn) {
                        this.requiredConnection = conn;
                        return this;
                }

                public Builder requires(PowerType power) {
                        this.requiredPower = power;
                        return this;
                }

                public Builder influenceRadius(int radius) {
                        this.influenceRadius = radius;
                        return this;
                }

                public Builder service(ServiceCategory category, int capacity) {
                        this.serviceCategory = category;
                        this.serviceCapacity = capacity;
                        return this;
                }

                public Builder consumptionService(ConsumptionServiceCategory category) { // ← nuovo
                        this.consumptionServiceCategory = category;
                        return this;
                }

                public Builder consumableOnly() {
                        this.consumableOnly = true;
                        return this;
                }

                public Builder maintenance(float cost) {
                        this.maintenanceCost = cost;
                        return this;
                }

                public Builder monument(String chainId, int level) {
                        this.isMonument = true;
                        this.monumentChainId = chainId;
                        this.monumentLevel = level;
                        return this;
                }

                public Builder upgradeCost(Object... kv) {
                        this.upgradeCost = kvMap(kv);
                        return this;
                }

                public Builder upgradeTime(float seconds) {
                        this.upgradeTime = seconds;
                        return this;
                }

                public Builder ideology(float shift) {
                        this.axisShift = shift;
                        return this;
                }

                public Builder workforce(int classIndex, int amount) {
                        this.workforceClass = classIndex;
                        this.workforceAmount = amount;
                        return this;
                }

                public Builder unlockRequirement(int classIndex, int amount) {
                        this.unlockClass = classIndex;
                        this.unlockAmount = amount;
                        return this;
                }

                public Builder unlockEvent(String eventId) {
                        this.requiredEventId = eventId;
                        return this;
                }

                public Builder pens(String... ids) {
                        this.penIds = ids;
                        return this;
                }

                public Builder penOf(String ownerId) {
                        this.penOwnerId = ownerId;
                        return this;
                }

                public Builder field(String fieldTypeId, int cap) {
                        this.fieldTypeId = fieldTypeId;
                        this.fieldCap = cap;
                        return this;
                }

                public Builder fieldOf(String ownerId) {
                        this.fieldOwnerId = ownerId;
                        return this;
                }

                public Builder requiresDeposit() {
                        this.requiresDeposit = true;
                        return this;
                }

                public BuildingType build() {
                        return new BuildingType(this);
                }

                private static Map<String, Float> kvMap(Object... kv) {
                        Map<String, Float> m = new HashMap<>();
                        for (int i = 0; i < kv.length - 1; i += 2) {
                                m.put((String) kv[i], ((Number) kv[i + 1]).floatValue());
                        }
                        return m;
                }
        }

        // ── RECINTI ──────────────────────────────────────────────────────────────────
        // notBuildable: non compaiono nella bottombar, si piazzano dal pannello
        // dell'allevamento.
        // TODO: dimensioni, costi e colori sono SEGNAPOSTO. Gli id finiscono nei
        // salvataggi.
        private static BuildingType pen(String id, String label, String colorHex, int w, int h,
                        String ownerId, Object... cost) {
                return new Builder(id, label, colorHex, w, h)
                                .cost(cost)
                                .penOf(ownerId)
                                .notBuildable()
                                .build();
        }

        // ── Enum ─────────────────────────────────────────────────────────────────────
        public enum ConnectionType {
                ROAD, PIPE, BOTH, NONE
        }

        public enum PowerType {
                NONE, STEAM, ELECTRICITY, BOTH
        }

        public enum Category {
                LOGISTICA, CASE, SERVIZI, ESTRAZIONE, INDUSTRIA, MONUMENTI
        }

        public enum Region {
                OLD_WORLD, NEW_WORLD
        }

        public enum ServiceCategory {
                WATER("Water", 1, 10),
                WASTE("Waste", 1, 10),
                FIRE("Fire", 1, 10),
                EDUCATION("Education", 1, 10),
                PARKS("Parks", 1, 10);

                public final String label;
                public final int baseCapacity;
                public final float consumptionPerHouse;

                ServiceCategory(String label, int baseCapacity, float consumptionPerHouse) {
                        this.label = label;
                        this.baseCapacity = baseCapacity;
                        this.consumptionPerHouse = consumptionPerHouse;
                }
        }

        /**
         * Servizi "a consumo" (stile Anno 1800): l'edificio consuma una ricetta
         * ad ogni ciclo tramite consumableOnly(); finché ha materiali è attivo,
         * altrimenti no. Nessuna capacità, nessun raggio: la soddisfazione è la
         * percentuale di edifici della categoria attivi sul totale costruito.
         */
        public enum ConsumptionServiceCategory {
                HEALTH("Health"),
                RELIGION("Religion"),
                ENTERTAINMENT("Entertainment"),
                COMMUNICATION("Communication");

                public final String label;

                ConsumptionServiceCategory(String label) {
                        this.label = label;
                }
        }

        // ── Capacità case per livello ────────────────────────────────────────────────
        public static final int[] HOUSE_CAPS = { 10, 20, 30, 40, 50, 60 };
        public static final String[] SOCIAL_CLASS_NAMES = {
                        "Plebeians", "Artisans", "Equites", "Patricians", "Engineers", "Magnates"
        };

        public static final BuildingType[] HOUSE_CHAIN = new BuildingType[6];
        private static final Map<String, BuildingType[]> MONUMENT_CHAINS = new HashMap<>();

        // ── Metodi utilità ───────────────────────────────────────────────────────────
        public int houseCap() {
                if (houseLevel == null || houseLevel < 0 || houseLevel >= HOUSE_CAPS.length)
                        return 0;
                return HOUSE_CAPS[houseLevel];
        }

        public boolean isPen() {
                return penOwnerId != null;
        }

        public boolean hasPens() {
                return penIds != null && penIds.length > 0;
        }

        public boolean isField() {
                return fieldOwnerId != null;
        }

        public boolean hasFields() {
                return fieldTypeId != null && fieldCap > 0;
        }

        public BuildingType nextHouseLevel() {
                if (!isHouse || houseLevel == null)
                        return null;
                int next = houseLevel + 1;
                if (next < 0 || next >= HOUSE_CHAIN.length)
                        return null;
                return HOUSE_CHAIN[next];
        }

        public boolean allowedOnTerrain(int terrainType) {
                if (onlyTerrain == null)
                        return true;
                for (int t : onlyTerrain) {
                        if (t == terrainType)
                                return true;
                }
                return false;
        }

        public boolean isAllowedInRegion(Region r) {
                if (allowedRegions == null)
                        return true;
                for (Region ar : allowedRegions) {
                        if (ar == r)
                                return true;
                }
                return false;
        }

        public Category getCategory() {
                if (category != null)
                        return category;
                if (serviceCategory != null || consumptionServiceCategory != null)
                        return Category.SERVIZI;
                if (this == BOILER || this == POWERPLANT || this == WAREHOUSE_CARRIAGE || this == WAREHOUSE_TRUCK
                                || this == ROAD || this == PIPELINE || this == DOCK || this == OIL_DOCK)
                        return Category.LOGISTICA;
                if (isHouse)
                        return Category.CASE;
                if (prod != null && input == null)
                        return Category.ESTRAZIONE;
                if (isMonument)
                        return Category.MONUMENTI;
                return Category.INDUSTRIA;
        }

        public BuildingType nextMonumentLevel() {
                if (!isMonument || monumentChainId == null)
                        return null;
                BuildingType[] chain = MONUMENT_CHAINS.get(monumentChainId);
                if (chain == null)
                        return null;
                int next = monumentLevel + 1;
                if (next >= chain.length)
                        return null;
                return chain[next];
        }

        public boolean isLastMonumentLevel() {
                if (!isMonument || monumentChainId == null)
                        return false;
                BuildingType[] chain = MONUMENT_CHAINS.get(monumentChainId);
                return chain != null && monumentLevel >= chain.length - 1;
        }

        public boolean isFirstMonumentLevel() {
                return isMonument && monumentLevel == 0;
        }

        public int getMonumentChainLength() {
                if (!isMonument || monumentChainId == null)
                        return 1;
                BuildingType[] chain = MONUMENT_CHAINS.get(monumentChainId);
                return chain != null ? chain.length : 1;
        }

        public static BuildingType[] getMonumentChain(String chainId) {
                return MONUMENT_CHAINS.get(chainId);
        }

        public static BuildingType valueOf(String id) {
                for (BuildingType bt : ALL_TYPES) {
                        if (bt.id.equals(id))
                                return bt;
                }
                throw new IllegalArgumentException("Unknown building type: " + id);
        }

        public static List<BuildingType> getAllBuildable() {
                List<BuildingType> list = new ArrayList<>();
                for (BuildingType bt : ALL_TYPES) {
                        if (bt.buildable)
                                list.add(bt);
                }
                return list;
        }

        // ── Info lines per tooltip hover ─────────────────────────────────────────────
        public List<String> getInfoLines(ResourceManager rm) {
                List<String> lines = new ArrayList<>();
                lines.add(label);

                if (cost != null && !cost.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Cost: ");
                        boolean first = true;
                        for (Map.Entry<String, Float> e : cost.entrySet()) {
                                if (!first)
                                        sb.append(", ");
                                sb.append((int) (float) e.getValue()).append("x ")
                                                .append(resourceLabel(rm, e.getKey()));
                                first = false;
                        }
                        lines.add(sb.toString());
                }

                if (requiredConnection != null && requiredConnection != ConnectionType.NONE) {
                        String connLabel = switch (requiredConnection) {
                                case ROAD -> "Road";
                                case PIPE -> "Pipe";
                                case BOTH -> "Road + Pipe";
                                default -> "";
                        };
                        lines.add("Requires: " + connLabel);
                }

                if (requiredPower != null && requiredPower != PowerType.NONE) {
                        String powerLabel = switch (requiredPower) {
                                case STEAM -> "Steam";
                                case ELECTRICITY -> "Electricity";
                                case BOTH -> "Steam + Electricity";
                                default -> "";
                        };
                        lines.add("Power: " + powerLabel);
                }

                if (input != null && !input.isEmpty()) {
                        StringBuilder sb = new StringBuilder(consumableOnly ? "Consumes: " : "Input: ");
                        boolean first = true;
                        for (Map.Entry<String, Float> e : input.entrySet()) {
                                if (!first)
                                        sb.append(", ");
                                sb.append((int) (float) e.getValue()).append("x ")
                                                .append(resourceLabel(rm, e.getKey()));
                                first = false;
                        }
                        lines.add(sb.toString());
                }

                if (!consumableOnly && prod != null) {
                        lines.add("Output: +" + rate + "x " + resourceLabel(rm, prod)
                                        + " every " + cycleTime + "s");
                }

                if (isHouse && houseNeeds != null && houseNeeds.length > 0) {
                        StringBuilder sb = new StringBuilder("Needs: ");
                        for (int i = 0; i < houseNeeds.length; i++) {
                                if (i > 0)
                                        sb.append(", ");
                                sb.append(resourceLabel(rm, houseNeeds[i]));
                        }
                        lines.add(sb.toString());
                }

                if (isMonument) {
                        lines.add("Monument: phase " + (monumentLevel + 1) + "/"
                                        + getMonumentChainLength());
                }

                if (requiresDeposit) {
                        lines.add("Requires: matching deposit");
                }

                if (influenceRadius > 0) {
                        lines.add("Radius: " + influenceRadius + " cells");
                }

                if (maintenanceCost > 0) {
                        lines.add("Maintenance: " + (int) maintenanceCost + "/min");
                }

                if (axisShift != 0) {
                        lines.add(axisShift > 0
                                        ? "Evolution +" + (int) axisShift
                                        : "Tradition " + (int) axisShift);
                }

                if (workforceClass != null && workforceAmount > 0) {
                        lines.add("Manodopera: " + workforceAmount + "x " + SOCIAL_CLASS_NAMES[workforceClass]);
                }

                if (unlockClass != null && unlockAmount > 0) {
                        lines.add("Richiede: " + unlockAmount + " " + SOCIAL_CLASS_NAMES[unlockClass]);
                }

                return lines;
        }

        private String resourceLabel(ResourceManager rm, String resId) {
                if (rm != null && rm.getAll().containsKey(resId))
                        return rm.getAll().get(resId).label;
                return resId;
        }

        // ── Caricamento dati esterni ─────────────────────────────────────────────
        private static final Map<String, BuildingDef> DEFS = BuildingDefLoader.load("data/buildings.json");

        private static Object[] toKv(Map<String, Float> map) {
                if (map == null)
                        return new Object[0];
                Object[] kv = new Object[map.size() * 2];
                int i = 0;
                for (Map.Entry<String, Float> e : map.entrySet()) {
                        kv[i++] = e.getKey();
                        kv[i++] = e.getValue();
                }
                return kv;
        }

        private static Region[] parseRegions(String[] names) {
                if (names == null)
                        return null;
                Region[] r = new Region[names.length];
                for (int i = 0; i < names.length; i++)
                        r[i] = Region.valueOf(names[i]);
                return r;
        }

        private static BuildingType fromDef(String id) {
                BuildingDef d = DEFS.get(id);
                if (d == null)
                        throw new IllegalStateException("Missing BuildingDef for id: " + id);

                Builder b = new Builder(d.id, d.label, d.colorHex, d.w, d.h);

                if (d.cost != null)
                        b.cost(toKv(d.cost));
                if (d.input != null)
                        b.input(toKv(d.input));
                if (d.prod != null)
                        b.prod(d.prod, d.rate, d.cycleTime);
                if (d.onlyTerrain != null)
                        b.terrain(d.onlyTerrain);
                if (d.isHouse)
                        b.house(d.houseLevel, d.houseNeeds);
                if (!d.buildable)
                        b.notBuildable();
                if (d.allowedRegions != null)
                        b.region(parseRegions(d.allowedRegions));
                if (d.requiredConnection != null)
                        b.requires(ConnectionType.valueOf(d.requiredConnection));
                if (d.requiredPower != null)
                        b.requires(PowerType.valueOf(d.requiredPower));
                if (d.influenceRadius > 0)
                        b.influenceRadius(d.influenceRadius);
                if (d.serviceCategory != null)
                        b.service(ServiceCategory.valueOf(d.serviceCategory), d.serviceCapacity);
                if (d.consumptionServiceCategory != null)
                        b.consumptionService(ConsumptionServiceCategory.valueOf(d.consumptionServiceCategory));
                if (d.consumableOnly)
                        b.consumableOnly();
                if (d.maintenanceCost > 0)
                        b.maintenance(d.maintenanceCost);
                if (d.isMonument)
                        b.monument(d.monumentChainId, d.monumentLevel);
                if (d.upgradeCost != null)
                        b.upgradeCost(toKv(d.upgradeCost));
                if (d.upgradeTime > 0)
                        b.upgradeTime(d.upgradeTime);
                if (d.axisShift != 0)
                        b.ideology(d.axisShift);
                if (d.workforceClass != null)
                        b.workforce(d.workforceClass, d.workforceAmount);
                if (d.unlockClass != null)
                        b.unlockRequirement(d.unlockClass, d.unlockAmount);
                if (d.requiredEventId != null)
                        b.unlockEvent(d.requiredEventId);
                if (d.penIds != null)
                        b.pens(d.penIds);
                if (d.penOwnerId != null)
                        b.penOf(d.penOwnerId);
                if (d.fieldTypeId != null)
                        b.field(d.fieldTypeId, d.fieldCap);
                if (d.fieldOwnerId != null)
                        b.fieldOf(d.fieldOwnerId);
                if (d.requiresDeposit)
                        b.requiresDeposit();

                return b.build();
        }

        // #region dichiarazioni
        public static final BuildingType WOOD_CAMP = fromDef("WOOD_CAMP");
        public static final BuildingType IRONMINE = fromDef("IRONMINE");
        public static final BuildingType COALMINE = fromDef("COALMINE");
        public static final BuildingType COPPERMINE = fromDef("COPPERMINE");
        public static final BuildingType ZINCMINE = fromDef("ZINCMINE");
        public static final BuildingType SANDPIT = fromDef("SANDPIT");
        public static final BuildingType MARBLE_QUARRY = fromDef("MARBLE_QUARRY");
        public static final BuildingType CLAYPIT = fromDef("CLAYPIT");
        public static final BuildingType OILRIG = fromDef("OILRIG");

        public static final BuildingType GRAIN_FARM = fromDef("GRAIN_FARM");
        public static final BuildingType VINEYARD = fromDef("VINEYARD");
        public static final BuildingType HOPS_FARM = fromDef("HOPS_FARM");
        public static final BuildingType LAVANDER_FARM = fromDef("LAVANDER_FARM");
        public static final BuildingType OLIVE_GROVE = fromDef("OLIVE_GROVE");
        public static final BuildingType FLAX_FARM = fromDef("FLAX_FARM");
        public static final BuildingType RUBBER_PLANTATION = fromDef("RUBBER_PLANTATION");
        public static final BuildingType COFFEE_PLANTATION = fromDef("COFFEE_PLANTATION");
        public static final BuildingType TOBACCO_PLANTATION = fromDef("TOBACCO_PLANTATION");
        public static final BuildingType SUGAR_PLANTATION = fromDef("SUGAR_PLANTATION");
        public static final BuildingType COTTON_FARM = fromDef("COTTON_FARM");

        public static final BuildingType GRAIN_FIELD = fromDef("GRAIN_FIELD");
        public static final BuildingType GRAPES_FIELD = fromDef("GRAPES_FIELD");
        public static final BuildingType HOPS_FIELD = fromDef("HOPS_FIELD");
        public static final BuildingType LAVANDER_FIELD = fromDef("LAVANDER_FIELD");
        public static final BuildingType OLIVE_FIELD = fromDef("OLIVE_FIELD");
        public static final BuildingType FLAX_FIELD = fromDef("FLAX_FIELD");
        public static final BuildingType RUBBER_FIELD = fromDef("RUBBER_FIELD");
        public static final BuildingType COFFEE_FIELD = fromDef("COFFEE_FIELD");
        public static final BuildingType TOBACCO_FIELD = fromDef("TOBACCO_FIELD");
        public static final BuildingType SUGAR_FIELD = fromDef("SUGAR_FIELD");
        public static final BuildingType COTTON_FIELD = fromDef("COTTON_FIELD");

        // ── LOGISTICA ────────────────────────────────────────────────────────────────
        public static final BuildingType ROAD = new Builder("ROAD", "Road", "#ebd09a", 1, 1)
                        .cost("coin", 1)
                        .terrain(1, 2, 3, 4)
                        .unlockEvent("THE_NEW_CITY")
                        .build();

        public static final BuildingType PIPELINE = new Builder("PIPELINE", "Pipeline", "#696969", 1, 1)
                        .cost("coin", 20, "steel", 1)
                        .terrain(1, 2, 3, 4)
                        .unlockEvent("OIL_DISCOVERY")
                        .build();

        public static final BuildingType DOCK = new Builder("DOCK", "Dock", "#3a6ea5", 4, 4)
                        .cost("coin", 500)
                        .terrain(1)
                        .unlockEvent("THE_NEW_CITY")
                        .build();

        public static final BuildingType OIL_DOCK = new Builder("OIL_DOCK", "Oil dock",
                        "#2a4a6a", 4, 4)
                        .cost("coin", 800, "timber", 50)
                        .terrain(1)
                        .unlockEvent("OIL_DISCOVERY")
                        .build();

        // ── CASE ─────────────────────────────────────────────────────────────────────
        public static final BuildingType CABIN = new Builder("CABIN", "Cabin", "#d4a373", 3, 3)
                        .cost("timber", 2)
                        .house(0, "bread", "fabric", "tools")
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .build();

        public static final BuildingType HOUSE = new Builder("HOUSE", "House", "#b8956a", 3, 3)
                        .house(1, "bread", "fabric", "tools", "cheese", "olive_oil", "ceramics", "toga")
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .notBuildable()
                        .build();

        public static final BuildingType DOMUS = new Builder("DOMUS", "Domus", "#8b7d5c", 3, 3)
                        .house(2, "cheese", "olive_oil", "ceramics", "toga", "soap", "cannedfood", "beer", "perfume")
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .notBuildable()
                        .build();

        public static final BuildingType VILLA = new Builder("VILLA", "Villa", "#7a5a3a", 3, 3)
                        .house(3, "soap", "cannedfood", "beer", "perfume", "fur_coats", "purse", "spectacles", "rum")
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .notBuildable()
                        .build();

        public static final BuildingType PALACE = new Builder("PALACE", "Palace", "#6a4f2e", 3, 3)
                        .house(4, "fur_coats", "purse", "spectacles", "rum", "sewing_machine", "coffee", "lightbulb",
                                        "steam_carriage")
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .notBuildable()
                        .build();

        public static final BuildingType SKYSCRAPER = new Builder("SKYSCRAPER", "Skyscraper", "#4d3d2e", 3, 3)
                        .house(5, "sewing_machine", "coffee", "lightbulb", "steam_carriage", "fan", "radio", "wine",
                                        "cigars")
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .notBuildable()
                        .build();

        // ── ESTRAZIONE BASE ──────────────────────────────────────────────────────────

        public static final BuildingType SHEEP_RANCH = new Builder("SHEEP_RANCH", "Sheep ranch", "#d8d0b0", 4, 3)
                        .cost("coin", 100, "timber", 4)
                        .prod("wool", 1, 10)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .workforce(0, 5)
                        .pens("SHEEP_PEN_1", "SHEEP_PEN_2", "SHEEP_PEN_3")
                        .build();

        public static final BuildingType COW_RANCH = new Builder("COW_RANCH", "Cow ranch", "#6b522b", 4, 3)
                        .cost("coin", 100, "timber", 4)
                        .prod("leather", 1, 10)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("PATRICIANS")
                        .workforce(0, 5)
                        .pens("COW_PEN_1", "COW_PEN_2", "COW_PEN_3")
                        .build();

        public static final BuildingType GOAT_RANCH = new Builder("GOAT_RANCH", "Goat ranch", "#9ba05c", 4, 3)
                        .cost("coin", 100, "timber", 4)
                        .prod("milk", 1, 10)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ARTISANS")
                        .workforce(0, 5)
                        .pens("GOAT_PEN_1", "GOAT_PEN_2", "GOAT_PEN_3")
                        .build();

        public static final BuildingType SHEEP_PEN_1 = pen("SHEEP_PEN_1", "Sheep pen 1", "#b9c48f", 3, 3,
                        "SHEEP_RANCH", "coin", 100, "timber", 10);
        public static final BuildingType SHEEP_PEN_2 = pen("SHEEP_PEN_2", "Sheep pen 2", "#a9b87f", 3, 3,
                        "SHEEP_RANCH", "coin", 100, "timber", 10);
        public static final BuildingType SHEEP_PEN_3 = pen("SHEEP_PEN_3", "Sheep pen 3", "#99ab6f", 3, 3,
                        "SHEEP_RANCH", "coin", 100, "timber", 10);

        public static final BuildingType COW_PEN_1 = pen("COW_PEN_1", "Cow pen 1", "#8a6a3a", 3, 3,
                        "COW_RANCH", "coin", 100, "timber", 10);
        public static final BuildingType COW_PEN_2 = pen("COW_PEN_2", "Cow pen 2", "#7a5a2a", 3, 3,
                        "COW_RANCH", "coin", 100, "timber", 10);
        public static final BuildingType COW_PEN_3 = pen("COW_PEN_3", "Cow pen 3", "#6a4a1a", 3, 3,
                        "COW_RANCH", "coin", 100, "timber", 10);

        public static final BuildingType GOAT_PEN_1 = pen("GOAT_PEN_1", "Goat pen 1", "#a8ad6b", 3, 3,
                        "GOAT_RANCH", "coin", 100, "timber", 10);
        public static final BuildingType GOAT_PEN_2 = pen("GOAT_PEN_2", "Goat pen 2", "#98a05b", 3, 3,
                        "GOAT_RANCH", "coin", 100, "timber", 10);
        public static final BuildingType GOAT_PEN_3 = pen("GOAT_PEN_3", "Goat pen 3", "#88934b", 3, 3,
                        "GOAT_RANCH", "coin", 100, "timber", 10);

        public static final BuildingType FISHERY = new Builder("FISHERY", "Fishery", "#3c89cd", 2, 5)
                        .cost("coin", 300, "timber", 15)
                        .prod("fish", 1, 30)
                        .terrain(0)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("EQUITES")
                        .workforce(0, 20)
                        .build();

        public static final BuildingType NITRATE_EXTRACTOR = new Builder("NITRATE_EXTRACTOR",
                        "Nitrate extractor", "#e5e5b2", 2, 5)
                        .cost("coin", 500, "timber", 15, "window", 10)
                        .prod("saltpeter", 1, 30)
                        .terrain(0)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("EQUITES")
                        .workforce(0, 20)
                        .build();

        // ── INDUSTRIA BASE ───────────────────────────────────────────────────────────
        public static final BuildingType SAWMILL = new Builder("SAWMILL", "Sawmill", "#805020", 3, 3)
                        .cost("coin", 1000)
                        .input("wood_log", 1)
                        .prod("timber", 3, 10)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .workforce(0, 5)
                        .build();

        public static final BuildingType MILL = new Builder("MILL", "Mill", "#c8a060", 2, 2)
                        .cost("coin", 300, "timber", 5)
                        .input("grain", 1)
                        .prod("flour", 2, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .maintenance(3f)
                        .workforce(0, 10)
                        .build();

        public static final BuildingType BAKERY = new Builder("BAKERY", "Bakery", "#d4946a", 3, 3)
                        .cost("coin", 400, "timber", 5)
                        .input("flour", 2)
                        .prod("bread", 3, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .maintenance(3f)
                        .workforce(0, 10)
                        .build();

        public static final BuildingType BREWERY = new Builder("BREWERY", "Brewery", "#d5bd73", 4, 5)
                        .cost("coin", 700, "timber", 20, "brick", 10)
                        .input("grain", 1, "hops", 2)
                        .prod("beer", 2, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("EQUITES")
                        .maintenance(3f)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType OIL_PREES = new Builder("OIL_PREES", "Oil press", "#5a7f3b", 3, 5)
                        .cost("coin", 700, "timber", 20, "brick", 10)
                        .input("olive", 2)
                        .prod("olive_oil", 2, 20)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ARTISANS")
                        .maintenance(3f)
                        .workforce(1, 20)
                        .build();

        public static final BuildingType CHEESE_MAKER = new Builder("CHEESE_MAKER", "Cheese maker",
                        "#e1e1ac", 4, 4)
                        .cost("coin", 800, "timber", 50, "brick", 10)
                        .input("milk", 1)
                        .prod("cheese", 3, 60)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ARTISANS")
                        .maintenance(3f)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType SOAP_MAKER = new Builder("SOAP_MAKER", "Soap maker",
                        "#a575ac", 4, 6)
                        .cost("coin", 800, "timber", 30, "brick", 15)
                        .input("lavander", 2, "saltpeter", 1)
                        .prod("soap", 2, 45)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("EQUITES")
                        .maintenance(3f)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType PERFUME_MIXER = new Builder("PERFUME_MIXER", "Perfume mixer",
                        "#a02e7a", 3, 4)
                        .cost("coin", 1000, "brick", 30, "timber", 20)
                        .input("lavander", 1, "olive_oil", 1)
                        .prod("perfume", 2, 90)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("PATRICIANS")
                        .maintenance(3f)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType SMITHY = new Builder("SMITHY", "Smithy", "#4c4747", 3, 2)
                        .cost("coin", 500, "timber", 15)
                        .input("iron", 2, "wood_log", 2)
                        .prod("tools", 2, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .maintenance(5f)
                        .workforce(0, 10)
                        .build();

        public static final BuildingType FABRIC_FACTORY = new Builder("FABRIC_FACTORY", "fabric factory",
                        "#f0e6ff", 5, 8)
                        .cost("coin", 1000, "timber", 30, "steel", 10, "concrete", 10)
                        .input("flax", 1)
                        .prod("fabric", 4, 20)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .requires(PowerType.BOTH)
                        .maintenance(3f)
                        .ideology(5)
                        .workforce(2, 20)
                        .build();

        public static final BuildingType WOOLEN_MILL = new Builder("WOOLEN_MILL", "Woolen mill", "#e7d3a9", 4, 4)
                        .cost("coin", 500, "timber", 10)
                        .input("wool", 2)
                        .prod("fabric", 3, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("THE_NEW_CITY")
                        .maintenance(3f)
                        .workforce(0, 10)
                        .build();

        public static final BuildingType KILN = new Builder("KILN", "Kiln", "#b15327", 4, 4)
                        .cost("coin", 1000, "timber", 40, "brick", 20)
                        .input("clay", 1)
                        .prod("ceramics", 2, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ARTISANS")
                        .maintenance(3f)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType TAILORING_WORKSHOP = new Builder("TAILORING_WORKSHOP",
                        "Tailoring workshop", "#4e73bb", 6, 6)
                        .cost("coin", 1200, "timber", 30, "brick", 10)
                        .input("fabric", 2)
                        .prod("toga", 3, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ARTISANS")
                        .maintenance(3f)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType GLASSWORKS = new Builder("GLASSWORKS", "Glasswork", "#70a8b0", 5, 5)
                        .cost("coin", 1000, "timber", 30, "brick", 10)
                        .input("sand", 1)
                        .prod("glass", 2, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("EQUITES")
                        .maintenance(3f)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType STEELWORK = new Builder("STEELWORK", "Steelwork", "#313338", 4, 7)
                        .cost("coin", 1000, "timber", 30, "brick", 20)
                        .input("iron", 1, "coal", 2)
                        .prod("steel", 1, 5)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("STEELWORKS")
                        .requires(PowerType.STEAM)
                        .maintenance(10f)
                        .ideology(20)
                        .workforce(1, 20)
                        .build();

        public static final BuildingType BRASS_FOUNDRY = new Builder("BRASS_FOUNDRY", "Brass foundry", "#a1732a",
                        4, 4)
                        .cost("coin", 800, "timber", 20, "steel", 10)
                        .input("zinc", 1, "copper", 1)
                        .prod("brass", 1, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .requires(PowerType.STEAM)
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(1, 20)
                        .build();

        public static final BuildingType BRICKFACTORY = new Builder("BRICKFACTORY", "Brick factory", "#f07743",
                        4, 4)
                        .cost("coin", 500, "timber", 30)
                        .input("clay", 1)
                        .prod("brick", 1, 20)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ARTISANS")
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType WINDOWMAKER = new Builder("WINDOWMAKER", "Windowmaker", "#78d5d8",
                        5, 5)
                        .cost("coin", 700, "timber", 30, "brick", 10)
                        .input("glass", 1, "wood_log", 2)
                        .prod("window", 2, 40)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("EQUITES")
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(1, 15)
                        .build();

        public static final BuildingType PAPER_MAKER = new Builder("PAPER_MAKER",
                        "Paper maker", "#a1c4ca", 4, 6)
                        .cost("coin", 400, "timber", 30, "steel", 5)
                        .input("timber", 2)
                        .prod("paper", 3, 45)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(10f)
                        .workforce(1, 10)
                        .build();

        public static final BuildingType SPECTACLES_FACTORY = new Builder("SPECTACLES_FACTORY",
                        "Spectacle factory", "#48757c", 3, 6)
                        .cost("coin", 800, "timber", 30, "steel", 10, "window", 10)
                        .input("glass", 2, "brass", 1)
                        .prod("spectacles", 3, 90)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(2, 10)
                        .build();

        public static final BuildingType CEMENTMIXER = new Builder("CEMENTMIXER", "Cementmixer", "#46627c", 3, 6)
                        .cost("coin", 1500, "brick", 30, "steel", 10, "window", 10)
                        .input("clay", 2, "sand", 2, "steel", 1)
                        .prod("concrete", 2, 45)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .requires(PowerType.ELECTRICITY)
                        .maintenance(10f)
                        .ideology(10)
                        .workforce(2, 10)
                        .build();

        public static final BuildingType CANNERY = new Builder("CANNERY", "Cannery", "#607850", 3, 5)
                        .cost("coin", 1500, "brick", 30, "window", 10)
                        .input("fish", 2, "iron", 1)
                        .prod("cannedfood", 3, 45)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("EQUITES")
                        .requires(PowerType.STEAM)
                        .maintenance(10f)
                        .ideology(10)
                        .workforce(2, 10)
                        .build();

        public static final BuildingType PURSE_FACTORY = new Builder("PURSE_FACTORY", "Purse factory", "#695018",
                        5, 6)
                        .cost("coin", 1500, "brick", 30, "window", 10)
                        .input("leather", 1, "fabric", 3)
                        .prod("purse", 2, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("PATRICIANS")
                        .requires(PowerType.STEAM)
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(1, 20)
                        .build();

        public static final BuildingType MOTORFACTORY = new Builder("MOTORFACTORY", "Motor factory", "#1c2f55",
                        7, 9)
                        .cost("coin", 2000, "brick", 30, "window", 10, "steel", 10, "concrete", 20)
                        .input("steel", 2, "brass", 1)
                        .prod("motor", 2, 60)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("STEAM_MOTOR")
                        .requires(PowerType.STEAM)
                        .maintenance(30f)
                        .ideology(20)
                        .workforce(4, 30)
                        .build();

        public static final BuildingType WINE_CELLAR = new Builder("WINE_CELLAR", "Wine cellar", "#421442", 4, 6)
                        .cost("coin", 900, "timber", 20, "window", 10)
                        .input("grapes", 1, "glass", 1)
                        .prod("wine", 3, 45)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("MAGNATES")
                        .maintenance(4f)
                        .workforce(3, 10)
                        .build();

        public static final BuildingType FUEL_REFINERY = new Builder("FUEL_REFINERY",
                        "Fuel refinery", "#282830", 7, 8)
                        .cost("coin", 3000, "timber", 40, "steel", 20, "window", 10)
                        .input("oil", 5)
                        .prod("fuel", 3, 10)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.BOTH)
                        .unlockEvent("MAGNATES")
                        .requires(PowerType.BOTH)
                        .maintenance(10f)
                        .ideology(10)
                        .workforce(4, 30)
                        .build();

        public static final BuildingType PLASTIC_REFINERY = new Builder("PLASTIC_REFINERY",
                        "Plastic refinery", "#304848", 7, 8)
                        .cost("coin", 3000, "timber", 40, "steel", 20, "window", 10)
                        .input("oil", 10)
                        .prod("plastic", 5, 10)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.BOTH)
                        .unlockEvent("MAGNATES")
                        .requires(PowerType.BOTH)
                        .maintenance(10f)
                        .ideology(10)
                        .workforce(4, 30)
                        .build();

        public static final BuildingType CABLE_FACTORY = new Builder("CABLE_FACTORY", "Cable factory", "#9c4167",
                        5, 4)
                        .cost("coin", 3000, "timber", 40, "window", 10)
                        .input("copper", 1, "rubber", 4)
                        .prod("cable", 2, 30)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .requires(PowerType.ELECTRICITY)
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(4, 10)
                        .build();

        public static final BuildingType ELECTRICMOTOR_FACTORY = new Builder("ELECTRICMOTOR_FACTORY",
                        "Electric motor factory", "#384858", 7, 11)
                        .cost("coin", 3000, "timber", 40, "steel", 20, "window", 10, "concrete", 20)
                        .input("steel", 1, "plastic", 10, "cable", 3)
                        .prod("electricmotor", 2, 45)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ELECTRIC_MOTOR")
                        .requires(PowerType.ELECTRICITY)
                        .maintenance(10f)
                        .ideology(20)
                        .workforce(4, 10)
                        .build();

        public static final BuildingType LIGHTBULB_FACTORY = new Builder("LIGHTBULB_FACTORY",
                        "Light bulb factory", "#dbc336", 7, 7)
                        .cost("coin", 3000, "timber", 40, "window", 10)
                        .input("cable", 2, "glass", 1)
                        .prod("lightbulb", 2, 60)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(2, 40)
                        .build();

        public static final BuildingType COFFEE_ROASTER = new Builder("COFFEE_ROASTER", "Coffee roaster",
                        "#643018", 5, 5)
                        .cost("coin", 3000, "timber", 30, "brick", 10, "window", 10)
                        .input("coffee_beans", 1)
                        .prod("coffee", 2, 60)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("PATRICIANS")
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(1, 20)
                        .build();

        public static final BuildingType CIGAR_FACTORY = new Builder("CIGAR_FACTORY", "Cigar factory",
                        "#402012", 3, 5)
                        .cost("coin", 3000, "timber", 40, "brick", 10, "window", 10)
                        .input("tobacco", 1, "timber", 1)
                        .prod("cigars", 2, 90)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("MAGNATES")
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(1, 20)
                        .build();

        public static final BuildingType RUM_DISTILLERY = new Builder("RUM_DISTILLERY", "Rum distillery", "#a78451", 6,
                        5)
                        .cost("coin", 2500, "timber", 40, "steel", 20, "window", 10)
                        .input("sugar", 1, "wood_log", 2)
                        .prod("rum", 2, 60)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .maintenance(10f)
                        .ideology(5)
                        .workforce(3, 20)
                        .build();

        public static final BuildingType RADIO_FACTORY = new Builder("RADIO_FACTORY", "Radio factory",
                        "#2b4557", 9, 9)
                        .cost("coin", 4000, "timber", 40, "steel", 20, "window", 10, "concrete", 10)
                        .input("steel", 1, "rubber", 2, "cable", 3)
                        .prod("radio", 2, 90)
                        .region(Region.NEW_WORLD)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("MAGNATES")
                        .requires(PowerType.ELECTRICITY)
                        .maintenance(20f)
                        .ideology(20)
                        .workforce(5, 25)
                        .build();

        public static final BuildingType STEAM_CARRIAGES_FACTORY = new Builder("STEAM_CARRIAGES_FACTORY",
                        "Steam carriages factory", "#8b9e20", 7, 7)
                        .cost("coin", 2000, "brick", 30, "window", 10, "steel", 10, "concrete", 20)
                        .input("timber", 10, "motor", 1, "steel", 3)
                        .prod("steam_carriage", 2, 90)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .requires(PowerType.STEAM)
                        .maintenance(40f)
                        .ideology(30)
                        .workforce(4, 20)
                        .build();

        public static final BuildingType TRUCK_FACTORY = new Builder("TRUCK_FACTORY", "Truck factory",
                        "#776811", 7, 7)
                        .cost("coin", 2000, "brick", 30, "window", 10, "steel", 10, "concrete", 20)
                        .input("rubber", 5, "motor", 3, "steel", 5)
                        .prod("truck", 2, 120)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("MAGNATES")
                        .requires(PowerType.BOTH)
                        .maintenance(40f)
                        .ideology(40)
                        .workforce(4, 20)
                        .build();

        public static final BuildingType FAN_FACTORY = new Builder("FAN_FACTORY", "Fan factory",
                        "#776811", 7, 7)
                        .cost("coin", 2000, "brick", 30, "window", 10, "steel", 10, "concrete", 20)
                        .input("electricmotor", 2, "plastic", 5, "cable", 1)
                        .prod("fan", 3, 60)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("MAGNATES")
                        .requires(PowerType.BOTH)
                        .maintenance(40f)
                        .ideology(40)
                        .workforce(4, 20)
                        .build();

        public static final BuildingType FUR_DEALER = new Builder("FUR_DEALER", "Fur dealer",
                        "#776811", 7, 7)
                        .cost("coin", 2000, "brick", 30, "window", 10, "steel", 10, "concrete", 20)
                        .input("leather", 1, "fabric", 2, "cotton", 5)
                        .prod("fur_coats", 2, 90)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("MAGNATES")
                        .requires(PowerType.BOTH)
                        .maintenance(40f)
                        .ideology(40)
                        .workforce(4, 20)
                        .build();

        public static final BuildingType SEWING_MACHINE_FACTORY = new Builder("SEWING_MACHINE_FACTORY",
                        "Sewing machine factory",
                        "#776811", 7, 7)
                        .cost("coin", 2000, "brick", 30, "window", 10, "steel", 10, "concrete", 20)
                        .input("timber", 2, "iron", 1)
                        .prod("sewing_machine", 1, 30)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("MAGNATES")
                        .requires(PowerType.BOTH)
                        .maintenance(40f)
                        .ideology(40)
                        .workforce(4, 20)
                        .build();

        public static final BuildingType WAREHOUSE_CARRIAGE = new Builder("WAREHOUSE_CARRIAGE",
                        "Warehouse (steam carriages)", "#af1d1d", 4, 4)
                        .cost("coin", 500, "steel", 40, "concrete", 30)
                        .input("steam_carriage", 3, "coal", 5)
                        .prod(null, 0, 60)
                        .consumableOnly()
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("ENGINEERS")
                        .requires(PowerType.STEAM)
                        .influenceRadius(20)
                        .maintenance(10f)
                        .ideology(10)
                        .workforce(2, 50)
                        .build();

        public static final BuildingType WAREHOUSE_TRUCK = new Builder("WAREHOUSE_TRUCK",
                        "Warehouse (truck)", "#a8135d", 4, 4)
                        .cost("coin", 1500, "steel", 40, "concrete", 30)
                        .input("truck", 3, "fuel", 15)
                        .prod(null, 0, 90)
                        .consumableOnly()
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("MAGNATES")
                        .requires(PowerType.ELECTRICITY)
                        .influenceRadius(25)
                        .maintenance(40f)
                        .ideology(10)
                        .workforce(4, 50)
                        .build();

        public static final BuildingType POWERPLANT = new Builder("POWERPLANT", "Powerplant", "#ffcc25",
                        5, 5)
                        .cost("coin", 1000, "timber", 50, "steel", 10, "window", 20)
                        .input("oil", 3)
                        .prod(null, 0, 60)
                        .consumableOnly()
                        .requires(ConnectionType.BOTH)
                        .unlockEvent("ELECTRICITY")
                        .influenceRadius(30)
                        .maintenance(50f)
                        .ideology(30)
                        .workforce(4, 50)
                        .build();

        public static final BuildingType BOILER = new Builder("BOILER", "Boiler", "#c0c0c0", 2, 2)
                        .cost("coin", 1000, "timber", 50, "brick", 10)
                        .input("coal", 1)
                        .prod(null, 0, 60)
                        .consumableOnly()
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BOILERS")
                        .influenceRadius(10)
                        .maintenance(3f)
                        .ideology(15)
                        .workforce(2, 50)
                        .build();

        // ____MONUMENTI_______________
        // FORO
        public static final BuildingType FORUM_FOUNDATIONS = new Builder("FORUM_FOUNDATIONS", "Forum foundation",
                        "#b8a030", 5, 5)
                        .cost("coin", 1000, "timber", 50, "brick", 30)
                        .monument("FORUM", 0)
                        .requires(ConnectionType.ROAD)
                        .ideology(-50f)
                        .workforce(0, 200)
                        .build();

        public static final BuildingType FORUM_MAIN_STRUCTRE = new Builder("FORUM_MAIN_STRUCTRE",
                        "Forum main structure", "#c9a83a", 6, 6)
                        .monument("FORUM", 1)
                        .upgradeCost("coin", 2000, "marble", 20, "steel", 10)
                        .upgradeTime(60f)
                        .requires(ConnectionType.ROAD)
                        .workforce(1, 200)
                        .notBuildable()
                        .build();

        public static final BuildingType FORUM_INFRASTUCTURE = new Builder("FORUM_INFRASTUCTURE", "Forum infrastucture",
                        "#d4b84a", 7,
                        7)
                        .monument("FORUM", 2)
                        .upgradeCost("coin", 4000, "marble", 40, "gold", 10, "concrete", 20)
                        .upgradeTime(90f)
                        .requires(ConnectionType.ROAD)
                        .workforce(2, 200)
                        .influenceRadius(15)
                        .notBuildable()
                        .build();

        public static final BuildingType FORUM_FINISHING = new Builder("FORUM_FINISHING", "Forum finishing", "#e6c84a",
                        7,
                        7)
                        .monument("FORUM", 3)
                        .upgradeCost("coin", 4000, "marble", 40, "gold", 10, "concrete", 20)
                        .upgradeTime(90f)
                        .requires(ConnectionType.ROAD)
                        .ideology(-20f)
                        .workforce(4, 200)
                        .influenceRadius(15)
                        .notBuildable()
                        .build();

        // COLOSSEO
        public static final BuildingType CL_SITE_PREPARATION = new Builder("CL_SITE_PREPARATION",
                        "Colosseum site preparation",
                        "#7a3d12", 6, 4)
                        .cost("coin", 1500, "timber", 40, "brick", 20)
                        .monument("COLOSSEUM", 0)
                        .requires(ConnectionType.ROAD)
                        .ideology(-50f)
                        .workforce(0, 300)
                        .build();

        public static final BuildingType CL_FOUNDATION = new Builder("CL_FOUNDATION", "Colosseum foundation", "#a8780a",
                        7, 5)
                        .monument("COLOSSEUM", 1)
                        .upgradeCost("coin", 3000, "marble", 30, "steel", 15, "concrete", 10)
                        .upgradeTime(75f)
                        .requires(ConnectionType.ROAD)
                        .workforce(1, 300)
                        .influenceRadius(10)
                        .notBuildable()
                        .build();

        public static final BuildingType CL_EXTERIOR = new Builder(
                        "CL_EXTERIOR",
                        "Colosseum exterior", "#c89618", 8, 6)
                        .monument("COLOSSEUM", 2)
                        .upgradeCost("coin", 5000, "marble", 50, "gold", 15, "concrete", 30)
                        .upgradeTime(120f)
                        .requires(ConnectionType.ROAD)
                        .workforce(2, 300)
                        .influenceRadius(20)
                        .consumptionService(ConsumptionServiceCategory.ENTERTAINMENT)
                        .notBuildable()
                        .build();

        public static final BuildingType CL_INTERIOR = new Builder(
                        "CL_INTERIOR",
                        "Colosseum interior", "#d9a020", 8, 6)
                        .monument("COLOSSEUM", 3)
                        .upgradeCost("coin", 5000, "marble", 50, "gold", 15, "concrete", 30)
                        .upgradeTime(120f)
                        .requires(ConnectionType.ROAD)
                        .workforce(3, 300)
                        .influenceRadius(20)
                        .consumptionService(ConsumptionServiceCategory.ENTERTAINMENT)
                        .notBuildable()
                        .build();

        public static final BuildingType CL_FINISHING = new Builder(
                        "CL_FINISHING",
                        "Colosseum finishing", "#e0a828", 8, 6)
                        .monument("COLOSSEUM", 4)
                        .upgradeCost("coin", 5000, "marble", 50, "gold", 15, "concrete", 30)
                        .upgradeTime(120f)
                        .requires(ConnectionType.ROAD)
                        .ideology(-20f)
                        .workforce(4, 300)
                        .influenceRadius(20)
                        .consumptionService(ConsumptionServiceCategory.ENTERTAINMENT)
                        .notBuildable()
                        .build();

        // ESPOSIZIONE UNIVERSALE
        public static final BuildingType UE_FOUNDATION = new Builder("UE_FOUNDATION", "Universal exposition foundation",
                        "#b0b8c0", 5, 5)
                        .cost("coin", 1000, "timber", 50, "brick", 30)
                        .monument("UNIVERSAL_EXPOSITION", 0)
                        .requires(ConnectionType.ROAD)
                        .ideology(20f)
                        .workforce(0, 500)
                        .build();

        public static final BuildingType UE_STRUCTURE = new Builder("UE_STRUCTURE", "Universal exposition structure",
                        "#c8c8d0", 6, 6)
                        .monument("UNIVERSAL_EXPOSITION", 1)
                        .upgradeCost("coin", 2000, "marble", 20, "steel", 10)
                        .upgradeTime(60f)
                        .requires(ConnectionType.ROAD)
                        .workforce(1, 500)
                        .notBuildable()
                        .build();

        public static final BuildingType UE_INTERIOR = new Builder("UE_INTERIOR", "Universal exposition interior",
                        "#d8d8e0", 7,
                        7)
                        .monument("UNIVERSAL_EXPOSITION", 2)
                        .upgradeCost("coin", 4000, "marble", 40, "gold", 10, "concrete", 20)
                        .upgradeTime(90f)
                        .requires(ConnectionType.ROAD)
                        .workforce(3, 500)
                        .influenceRadius(15)
                        .notBuildable()
                        .build();

        public static final BuildingType UE_FINISHING = new Builder("UE_FINISHING", "Universal exposition finishing",
                        "#e8e8f0", 7,
                        7)
                        .monument("UNIVERSAL_EXPOSITION", 3)
                        .upgradeCost("coin", 4000, "marble", 40, "gold", 10, "concrete", 20)
                        .upgradeTime(90f)
                        .requires(ConnectionType.ROAD)
                        .ideology(20f)
                        .workforce(5, 100)
                        .influenceRadius(15)
                        .notBuildable()
                        .build();

        // TORRE D'ACCIAIO
        public static final BuildingType ST_FOUNATION = new Builder("ST_FOUNATION", "Steel tower foundation", "#8a9aad",
                        5, 5)
                        .cost("coin", 1000, "timber", 50, "brick", 30)
                        .monument("STEEL_TOWER", 0)
                        .requires(ConnectionType.ROAD)
                        .ideology(50f)
                        .workforce(0, 200)
                        .build();

        public static final BuildingType ST_MAIN_FRAME = new Builder("ST_MAIN_FRAME", "Steel tower main frame",
                        "#9aabb8", 6, 6)
                        .monument("STEEL_TOWER", 1)
                        .upgradeCost("coin", 2000, "marble", 20, "steel", 10)
                        .upgradeTime(60f)
                        .requires(ConnectionType.ROAD)
                        .workforce(1, 200)
                        .notBuildable()
                        .build();

        public static final BuildingType ST_FINISHING = new Builder("ST_FINISHING", "Steel tower finishing", "#b0c0d0",
                        7,
                        7)
                        .monument("STEEL_TOWER", 2)
                        .upgradeCost("coin", 4000, "marble", 40, "gold", 10, "concrete", 20)
                        .upgradeTime(90f)
                        .requires(ConnectionType.ROAD)
                        .ideology(20f)
                        .workforce(4, 600)
                        .influenceRadius(15)
                        .notBuildable()
                        .build();

        // ACCADEMIA
        public static final BuildingType AC_FOUNDATION = new Builder("AC_FOUNDATION", "Accademy foundation", "#8b6b4a",
                        5, 5)
                        .cost("coin", 1000, "timber", 50, "brick", 30)
                        .monument("ACCADEMY", 0)
                        .requires(ConnectionType.ROAD)
                        .ideology(-20f)
                        .workforce(0, 300)
                        .build();

        public static final BuildingType AC_STRUCTURE = new Builder("AC_STRUCTURE", "Accademy structure", "#a87b5a", 6,
                        6)
                        .monument("ACCADEMY", 1)
                        .upgradeCost("coin", 2000, "marble", 20, "steel", 10)
                        .upgradeTime(60f)
                        .requires(ConnectionType.ROAD)
                        .workforce(1, 300)
                        .notBuildable()
                        .build();

        public static final BuildingType AC_INTERIOR = new Builder("AC_INTERIOR", "Accademy interior", "#c89b6a", 7,
                        7)
                        .monument("ACCADEMY", 2)
                        .upgradeCost("coin", 4000, "marble", 40, "gold", 10, "concrete", 20)
                        .upgradeTime(90f)
                        .requires(ConnectionType.ROAD)
                        .workforce(3, 300)
                        .influenceRadius(15)
                        .notBuildable()
                        .build();

        public static final BuildingType AC_EXTERIOR = new Builder("AC_EXTERIOR", "Accademy exterior", "#d8ab7a", 7,
                        7)
                        .monument("ACCADEMY", 3)
                        .upgradeCost("coin", 4000, "marble", 40, "gold", 10, "concrete", 20)
                        .upgradeTime(90f)
                        .requires(ConnectionType.ROAD)
                        .workforce(4, 300)
                        .influenceRadius(15)
                        .notBuildable()
                        .build();

        public static final BuildingType AC_FINISHING = new Builder("AC_FINISHING", "Accademy finishing", "#e8bb8a", 7,
                        7)
                        .monument("ACCADEMY", 4)
                        .upgradeCost("coin", 4000, "marble", 40, "gold", 10, "concrete", 20)
                        .upgradeTime(90f)
                        .requires(ConnectionType.ROAD)
                        .ideology(-20f)
                        .workforce(4, 300)
                        .influenceRadius(15)
                        .notBuildable()
                        .build();

        // ── ACQUA ──────────────────────────────────────────────────────
        public static final BuildingType WELL = new Builder("WELL", "Well", "#4A90D9", 2, 2)
                        .cost("coin", 100, "timber", 5)
                        .service(ServiceCategory.WATER, 100)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(2f)
                        .build();

        public static final BuildingType AQUEDUCT = new Builder("AQUEDUCT", "Acqueduct", "#2E6DB4", 3, 5)
                        .cost("coin", 500, "brick", 20, "steel", 10)
                        .service(ServiceCategory.WATER, 300)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(8f)
                        .ideology(10f)
                        .build();

        // ── RIFIUTI ────────────────────────────────────────────────────
        public static final BuildingType DUMP = new Builder("DUMP", "Dump", "#8B7355", 3, 3)
                        .cost("coin", 150, "timber", 8)
                        .service(ServiceCategory.WASTE, 300)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(3f)
                        .workforce(0, 20)
                        .build();

        public static final BuildingType INCINERATOR = new Builder("INCINERATOR", "Incinerator", "#8B4513", 4, 4)
                        .cost("coin", 800, "steel", 20, "brick", 20)
                        .service(ServiceCategory.WASTE, 800)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(12f)
                        .ideology(10f)
                        .workforce(0, 40)
                        .build();

        // ── VIGILI DEL FUOCO ──────────────────────────────────────────
        public static final BuildingType SMALL_FIRE_STATION = new Builder("SMALL_FIRE_STATION", "Small fire station",
                        "#FF4500", 3, 4)
                        .cost("coin", 400, "timber", 15)
                        .service(ServiceCategory.FIRE, 500)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(6f)
                        .workforce(2, 20)
                        .build();

        public static final BuildingType BIG_FIRE_STATION = new Builder("BIG_FIRE_STATION", "Big fire station",
                        "#be3a22", 4, 5)
                        .cost("coin", 800, "timber", 25, "steel", 15, "concrete", 10)
                        .service(ServiceCategory.FIRE, 1500)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(12f)
                        .ideology(10)
                        .workforce(2, 60)
                        .build();

        // ── SANITÀ ─────────────────────────────────────────────────────
        // SANITÀ
        public static final BuildingType THERMAL_BATHS = new Builder("THERMAL_BATHS", "Thermal baths", "#4d7796", 3, 4)
                        .cost("coin", 300, "timber", 10, "brick", 15)
                        .input("timber", 1, "fabric", 1)
                        .prod(null, 0, 30)
                        .consumableOnly()
                        .consumptionService(ConsumptionServiceCategory.HEALTH)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(5f)
                        .ideology(-5f)
                        .influenceRadius(20)
                        .build();

        public static final BuildingType HOSPITAL = new Builder("HOSPITAL", "Hospital", "#285879", 3, 5)
                        .cost("coin", 1000, "brick", 30, "steel", 15, "concrete", 10)
                        .input("lavander", 2, "fabric", 3, "rum", 1)
                        .prod(null, 0, 45)
                        .consumableOnly()
                        .consumptionService(ConsumptionServiceCategory.HEALTH)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(15f)
                        .ideology(-10f)
                        .influenceRadius(35)
                        .workforce(3, 30)
                        .build();

        // RELIGIONE
        public static final BuildingType TEMPLE = new Builder("TEMPLE", "Temple", "#af8f23", 3, 3)
                        .cost("coin", 400, "timber", 10, "marble", 10)
                        .input("toga", 1)
                        .prod(null, 0, 30)
                        .consumableOnly()
                        .consumptionService(ConsumptionServiceCategory.RELIGION)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(5f)
                        .ideology(-15f)
                        .influenceRadius(10)
                        .build();

        public static final BuildingType CHURCH = new Builder("CHURCH", "Church", "#b88608", 3, 5)
                        .cost("coin", 1200, "timber", 20, "marble", 30)
                        .input("toga", 3, "wine", 2)
                        .prod(null, 0, 60)
                        .consumableOnly()
                        .consumptionService(ConsumptionServiceCategory.RELIGION)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(12f)
                        .ideology(-30f)
                        .influenceRadius(25)
                        .build();

        // INTRATTENIMENTO
        public static final BuildingType THEATER = new Builder("THEATER", "Theater", "#a5265b", 4, 4)
                        .cost("coin", 1200, "timber", 20, "brick", 30)
                        .input("fabric", 3, "cheese", 2, "beer", 1)
                        .prod(null, 0, 60)
                        .consumableOnly()
                        .consumptionService(ConsumptionServiceCategory.ENTERTAINMENT)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(12f)
                        .ideology(-20f)
                        .influenceRadius(18)
                        .build();

        public static final BuildingType CINEMA = new Builder("CINEMA", "Cinema", "#bb5a82", 5, 5)
                        .cost("coin", 1200, "timber", 20, "brick", 30, "steel", 20)
                        .input("spectacles", 3, "cigars", 2, "wine", 1)
                        .prod(null, 0, 60)
                        .consumableOnly()
                        .consumptionService(ConsumptionServiceCategory.ENTERTAINMENT)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(12f)
                        .ideology(20f)
                        .influenceRadius(34)
                        .build();

        // COMUNICAZIONE
        public static final BuildingType POST_OFFICE = new Builder("POST_OFFICE", "Post office", "#94cfc0", 2, 2)
                        .cost("coin", 1200, "timber", 30)
                        .input("paper", 1)
                        .prod(null, 0, 60)
                        .consumableOnly()
                        .consumptionService(ConsumptionServiceCategory.COMMUNICATION)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(12f)
                        .ideology(-10)
                        .influenceRadius(10)
                        .build();

        public static final BuildingType RADIO_TOWER = new Builder("RADIO_TOWER", "Radio tower", "#61e6d4", 6, 6)
                        .cost("coin", 1200, "timber", 20, "steel", 20, "concrete", 20)
                        .input("radio", 3, "cable", 2)
                        .prod(null, 0, 60)
                        .consumableOnly()
                        .consumptionService(ConsumptionServiceCategory.COMMUNICATION)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(12f)
                        .ideology(30)
                        .influenceRadius(50)
                        .workforce(4, 10)
                        .build();

        // EDUCAZIONE
        public static final BuildingType SCHOOL = new Builder("SCHOOL", "School", "#3327e7", 4, 4)
                        .cost("coin", 100, "timber", 30)
                        .service(ServiceCategory.EDUCATION, 200)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(2f)
                        .workforce(3, 20)
                        .build();

        public static final BuildingType UNIVERSITY = new Builder("UNIVERSITY", "University", "#4d46b6", 6, 8)
                        .cost("coin", 100, "timber", 50, "steel", 10, "window", 20)
                        .service(ServiceCategory.EDUCATION, 800)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(2f)
                        .ideology(-20f)
                        .workforce(3, 80)
                        .build();

        // PARCHI
        public static final BuildingType SMALL_PARK = new Builder("SMALL_PARK", "Small park", "#29d175", 3, 3)
                        .cost("coin", 100, "timber", 5)
                        .service(ServiceCategory.PARKS, 100)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("SERVICES")
                        .maintenance(2f)
                        .ideology(-10f)
                        .build();

        public static final BuildingType BIG_PARK = new Builder("BIG_PARK", "Big park", "#159e09", 7, 7)
                        .cost("coin", 100, "timber", 50, "brick", 30)
                        .service(ServiceCategory.PARKS, 1000)
                        .requires(ConnectionType.ROAD)
                        .unlockEvent("BETTER_SERVICES")
                        .maintenance(2f)
                        .ideology(10f)
                        .build();

        // ── Inizializzazione ─────────────────────────────────────────────────────────
        private static final List<BuildingType> ALL_TYPES = new ArrayList<>();

        static {
                HOUSE_CHAIN[0] = CABIN;
                HOUSE_CHAIN[1] = HOUSE;
                HOUSE_CHAIN[2] = DOMUS;
                HOUSE_CHAIN[3] = VILLA;
                HOUSE_CHAIN[4] = PALACE;
                HOUSE_CHAIN[5] = SKYSCRAPER;

                ALL_TYPES.add(ROAD);
                ALL_TYPES.add(PIPELINE);
                ALL_TYPES.add(DOCK);
                ALL_TYPES.add(OIL_DOCK);
                ALL_TYPES.add(BOILER);
                ALL_TYPES.add(POWERPLANT);
                ALL_TYPES.add(WAREHOUSE_CARRIAGE);
                ALL_TYPES.add(WAREHOUSE_TRUCK);

                ALL_TYPES.add(CABIN);
                ALL_TYPES.add(HOUSE);
                ALL_TYPES.add(DOMUS);
                ALL_TYPES.add(VILLA);
                ALL_TYPES.add(PALACE);
                ALL_TYPES.add(SKYSCRAPER);

                ALL_TYPES.add(SAWMILL);
                ALL_TYPES.add(GRAIN_FARM);
                ALL_TYPES.add(GRAIN_FIELD);
                ALL_TYPES.add(GRAPES_FIELD);
                ALL_TYPES.add(HOPS_FIELD);
                ALL_TYPES.add(LAVANDER_FIELD);
                ALL_TYPES.add(OLIVE_FIELD);
                ALL_TYPES.add(FLAX_FIELD);
                ALL_TYPES.add(RUBBER_FIELD);
                ALL_TYPES.add(COFFEE_FIELD);
                ALL_TYPES.add(TOBACCO_FIELD);
                ALL_TYPES.add(SUGAR_FIELD);
                ALL_TYPES.add(COTTON_FIELD);
                ALL_TYPES.add(MILL);
                ALL_TYPES.add(BAKERY);
                ALL_TYPES.add(SHEEP_RANCH);
                ALL_TYPES.add(WOOLEN_MILL);
                ALL_TYPES.add(IRONMINE);
                ALL_TYPES.add(SMITHY);
                ALL_TYPES.add(CLAYPIT);
                ALL_TYPES.add(BRICKFACTORY);
                ALL_TYPES.add(GOAT_RANCH);
                ALL_TYPES.add(CHEESE_MAKER);
                ALL_TYPES.add(OLIVE_GROVE);
                ALL_TYPES.add(OIL_PREES);
                ALL_TYPES.add(KILN);
                ALL_TYPES.add(TAILORING_WORKSHOP);
                ALL_TYPES.add(SANDPIT);
                ALL_TYPES.add(GLASSWORKS);
                ALL_TYPES.add(WINDOWMAKER);
                ALL_TYPES.add(COALMINE);
                ALL_TYPES.add(STEELWORK);
                ALL_TYPES.add(LAVANDER_FARM);
                ALL_TYPES.add(NITRATE_EXTRACTOR);
                ALL_TYPES.add(SOAP_MAKER);
                ALL_TYPES.add(FISHERY);
                ALL_TYPES.add(CANNERY);
                ALL_TYPES.add(HOPS_FARM);
                ALL_TYPES.add(BREWERY);
                ALL_TYPES.add(MARBLE_QUARRY);
                ALL_TYPES.add(OILRIG);
                ALL_TYPES.add(PERFUME_MIXER);
                ALL_TYPES.add(COW_RANCH);
                ALL_TYPES.add(PURSE_FACTORY);
                ALL_TYPES.add(COFFEE_PLANTATION);
                ALL_TYPES.add(COFFEE_ROASTER);
                ALL_TYPES.add(FLAX_FARM);
                ALL_TYPES.add(FABRIC_FACTORY);
                ALL_TYPES.add(CEMENTMIXER);
                ALL_TYPES.add(SUGAR_PLANTATION);
                ALL_TYPES.add(RUM_DISTILLERY);
                ALL_TYPES.add(ZINCMINE);
                ALL_TYPES.add(COPPERMINE);
                ALL_TYPES.add(BRASS_FOUNDRY);
                ALL_TYPES.add(SPECTACLES_FACTORY);
                ALL_TYPES.add(RUBBER_PLANTATION);
                ALL_TYPES.add(CABLE_FACTORY);
                ALL_TYPES.add(LIGHTBULB_FACTORY);
                ALL_TYPES.add(MOTORFACTORY);
                ALL_TYPES.add(STEAM_CARRIAGES_FACTORY);
                ALL_TYPES.add(PLASTIC_REFINERY);
                ALL_TYPES.add(ELECTRICMOTOR_FACTORY);
                ALL_TYPES.add(FUEL_REFINERY);
                ALL_TYPES.add(TRUCK_FACTORY);
                ALL_TYPES.add(TOBACCO_PLANTATION);
                ALL_TYPES.add(CIGAR_FACTORY);
                ALL_TYPES.add(RADIO_FACTORY);
                ALL_TYPES.add(COTTON_FARM);
                ALL_TYPES.add(VINEYARD);
                ALL_TYPES.add(WINE_CELLAR);
                ALL_TYPES.add(FAN_FACTORY);
                ALL_TYPES.add(FUR_DEALER);
                ALL_TYPES.add(SEWING_MACHINE_FACTORY);
                ALL_TYPES.add(WOOD_CAMP);

                ALL_TYPES.add(PAPER_MAKER);
                ALL_TYPES.add(WELL);
                ALL_TYPES.add(AQUEDUCT);
                ALL_TYPES.add(DUMP);
                ALL_TYPES.add(INCINERATOR);
                ALL_TYPES.add(SMALL_FIRE_STATION);
                ALL_TYPES.add(BIG_FIRE_STATION);
                ALL_TYPES.add(SMALL_PARK);
                ALL_TYPES.add(BIG_PARK);
                ALL_TYPES.add(THEATER);
                ALL_TYPES.add(CINEMA);
                ALL_TYPES.add(SCHOOL);
                ALL_TYPES.add(UNIVERSITY);
                ALL_TYPES.add(POST_OFFICE);
                ALL_TYPES.add(RADIO_TOWER);
                ALL_TYPES.add(THERMAL_BATHS);
                ALL_TYPES.add(HOSPITAL);
                ALL_TYPES.add(TEMPLE);
                ALL_TYPES.add(CHURCH);

                ALL_TYPES.add(SHEEP_PEN_1);
                ALL_TYPES.add(SHEEP_PEN_2);
                ALL_TYPES.add(SHEEP_PEN_3);
                ALL_TYPES.add(COW_PEN_1);
                ALL_TYPES.add(COW_PEN_2);
                ALL_TYPES.add(COW_PEN_3);
                ALL_TYPES.add(GOAT_PEN_1);
                ALL_TYPES.add(GOAT_PEN_2);
                ALL_TYPES.add(GOAT_PEN_3);

                ALL_TYPES.add(FORUM_FOUNDATIONS);
                ALL_TYPES.add(FORUM_MAIN_STRUCTRE);
                ALL_TYPES.add(FORUM_INFRASTUCTURE);
                ALL_TYPES.add(FORUM_FINISHING);

                ALL_TYPES.add(CL_SITE_PREPARATION);
                ALL_TYPES.add(CL_FOUNDATION);
                ALL_TYPES.add(CL_EXTERIOR);
                ALL_TYPES.add(CL_INTERIOR);
                ALL_TYPES.add(CL_FINISHING);

                ALL_TYPES.add(UE_FOUNDATION);
                ALL_TYPES.add(UE_STRUCTURE);
                ALL_TYPES.add(UE_INTERIOR);
                ALL_TYPES.add(UE_FINISHING);

                ALL_TYPES.add(ST_FOUNATION);
                ALL_TYPES.add(ST_MAIN_FRAME);
                ALL_TYPES.add(ST_FINISHING);

                ALL_TYPES.add(AC_FOUNDATION);
                ALL_TYPES.add(AC_STRUCTURE);
                ALL_TYPES.add(AC_INTERIOR);
                ALL_TYPES.add(AC_EXTERIOR);
                ALL_TYPES.add(AC_FINISHING);

                MONUMENT_CHAINS.put("FORUM", new BuildingType[] {
                                FORUM_FOUNDATIONS,
                                FORUM_MAIN_STRUCTRE,
                                FORUM_INFRASTUCTURE,
                                FORUM_FINISHING
                });
                MONUMENT_CHAINS.put("COLOSSEUM", new BuildingType[] {
                                CL_SITE_PREPARATION,
                                CL_FOUNDATION,
                                CL_EXTERIOR,
                                CL_INTERIOR,
                                CL_FINISHING
                });
                MONUMENT_CHAINS.put("UNIVERSAL_EXPOSITION", new BuildingType[] {
                                UE_FOUNDATION,
                                UE_STRUCTURE,
                                UE_INTERIOR,
                                UE_FINISHING
                });
                MONUMENT_CHAINS.put("STEEL_TOWER", new BuildingType[] {
                                ST_FOUNATION,
                                ST_MAIN_FRAME,
                                ST_FINISHING
                });
                MONUMENT_CHAINS.put("ACCADEMY", new BuildingType[] {
                                AC_FOUNDATION,
                                AC_STRUCTURE,
                                AC_INTERIOR,
                                AC_EXTERIOR,
                                AC_FINISHING
                });
        }
}
