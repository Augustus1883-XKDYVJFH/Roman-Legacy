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

                public String getLabel() {
                        return Localization.getOr("service." + name(), label);
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

                public String getLabel() {
                        return Localization.getOr("consumption_service." + name(), label);
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

        public static BuildingType valueOf(String id) {
                for (BuildingType bt : ALL_TYPES) {
                        if (bt.id.equals(id))
                                return bt;
                }
                throw new IllegalArgumentException("Unknown building type: " + id);
        }

        public static String socialClassName(int index) {
                String fallback = (index >= 0 && index < SOCIAL_CLASS_NAMES.length)
                                ? SOCIAL_CLASS_NAMES[index]
                                : "?";
                return Localization.getOr("social_class." + index, fallback);
        }

        public static List<BuildingType> getAllBuildable() {
                List<BuildingType> list = new ArrayList<>();
                for (BuildingType bt : ALL_TYPES) {
                        if (bt.buildable)
                                list.add(bt);
                }
                return list;
        }

        public String getLabel() {
                return Localization.getOr("building." + id + ".name", label);
        }

        // ── Info lines per tooltip hover ─────────────────────────────────────────────
        public List<String> getInfoLines(ResourceManager rm) {
                return getInfoLines(rm, null);
        }

        /**
         * costOverride: costo dinamico da mostrare al posto di quello fisso (es. il
         * porto).
         */
        public List<String> getInfoLines(ResourceManager rm, Map<String, Float> costOverride) {
                List<String> lines = new ArrayList<>();
                lines.add(getLabel());

                Map<String, Float> shownCost = costOverride != null ? costOverride : cost;
                if (shownCost != null && !shownCost.isEmpty())
                        lines.add(Localization.format("info.cost", joinAmounts(shownCost, rm)));

                if (requiredConnection != null && requiredConnection != ConnectionType.NONE) {
                        String key = switch (requiredConnection) {
                                case ROAD -> "info.conn.road";
                                case PIPE -> "info.conn.pipe";
                                default -> "info.conn.both";
                        };
                        lines.add(Localization.format("info.requires", Localization.get(key)));
                }

                if (requiredPower != null && requiredPower != PowerType.NONE) {
                        String key = switch (requiredPower) {
                                case STEAM -> "info.power.steam";
                                case ELECTRICITY -> "info.power.electricity";
                                default -> "info.power.both";
                        };
                        lines.add(Localization.format("info.power", Localization.get(key)));
                }

                if (input != null && !input.isEmpty())
                        lines.add(Localization.format(consumableOnly ? "info.consumes" : "info.input",
                                        joinAmounts(input, rm)));

                if (!consumableOnly && prod != null)
                        lines.add(Localization.format("info.output", String.valueOf(rate), resourceLabel(rm, prod),
                                        String.valueOf(cycleTime)));

                if (isHouse && houseNeeds != null && houseNeeds.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < houseNeeds.length; i++) {
                                if (i > 0)
                                        sb.append(", ");
                                sb.append(resourceLabel(rm, houseNeeds[i]));
                        }
                        lines.add(Localization.format("info.needs", sb.toString()));
                }

                if (isMonument)
                        lines.add(Localization.format("info.monument", String.valueOf(monumentLevel + 1),
                                        String.valueOf(getMonumentChainLength())));
                if (requiresDeposit)
                        lines.add(Localization.get("info.deposit"));
                if (influenceRadius > 0)
                        lines.add(Localization.format("info.radius", String.valueOf(influenceRadius)));
                if (maintenanceCost > 0)
                        lines.add(Localization.format("info.maintenance", String.valueOf((int) maintenanceCost)));
                if (axisShift != 0)
                        lines.add(axisShift > 0
                                        ? Localization.format("info.evolution", String.valueOf((int) axisShift))
                                        : Localization.format("info.tradition", String.valueOf((int) axisShift)));
                if (workforceClass != null && workforceAmount > 0)
                        lines.add(Localization.format("info.workforce", String.valueOf(workforceAmount),
                                        socialClassName(workforceClass)));
                if (unlockClass != null && unlockAmount > 0)
                        lines.add(Localization.format("info.unlock", String.valueOf(unlockAmount),
                                        socialClassName(unlockClass)));

                return lines;
        }

        private String joinAmounts(Map<String, Float> map, ResourceManager rm) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, Float> e : map.entrySet()) {
                        if (!first)
                                sb.append(", ");
                        sb.append((int) (float) e.getValue()).append("x ").append(resourceLabel(rm, e.getKey()));
                        first = false;
                }
                return sb.toString();
        }

        private String resourceLabel(ResourceManager rm, String resId) {
                if (rm != null && rm.getAll().containsKey(resId))
                        return rm.getAll().get(resId).getLabel();
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
                if (d.prod != null || d.consumableOnly)
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
        public static final BuildingType FISHERY = fromDef("FISHERY");
        public static final BuildingType NITRATE_EXTRACTOR = fromDef("NITRATE_EXTRACTOR");
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

        // ── INDUSTRIA BASE ───────────────────────────────────────────────────────────
        public static final BuildingType SAWMILL = fromDef("SAWMILL");
        public static final BuildingType MILL = fromDef("MILL");
        public static final BuildingType BAKERY = fromDef("BAKERY");
        public static final BuildingType BREWERY = fromDef("BREWERY");
        public static final BuildingType OIL_PREES = fromDef("OIL_PREES");
        public static final BuildingType CHEESE_MAKER = fromDef("CHEESE_MAKER");
        public static final BuildingType SOAP_MAKER = fromDef("SOAP_MAKER");
        public static final BuildingType PERFUME_MIXER = fromDef("PERFUME_MIXER");
        public static final BuildingType SMITHY = fromDef("SMITHY");
        public static final BuildingType FABRIC_FACTORY = fromDef("FABRIC_FACTORY");
        public static final BuildingType WOOLEN_MILL = fromDef("WOOLEN_MILL");
        public static final BuildingType KILN = fromDef("KILN");
        public static final BuildingType TAILORING_WORKSHOP = fromDef("TAILORING_WORKSHOP");
        public static final BuildingType GLASSWORKS = fromDef("GLASSWORKS");
        public static final BuildingType STEELWORK = fromDef("STEELWORK");
        public static final BuildingType BRASS_FOUNDRY = fromDef("BRASS_FOUNDRY");
        public static final BuildingType BRICKFACTORY = fromDef("BRICKFACTORY");
        public static final BuildingType WINDOWMAKER = fromDef("WINDOWMAKER");
        public static final BuildingType PAPER_MAKER = fromDef("PAPER_MAKER");
        public static final BuildingType SPECTACLES_FACTORY = fromDef("SPECTACLES_FACTORY");
        public static final BuildingType CEMENTMIXER = fromDef("CEMENTMIXER");
        public static final BuildingType CANNERY = fromDef("CANNERY");
        public static final BuildingType PURSE_FACTORY = fromDef("PURSE_FACTORY");
        public static final BuildingType MOTORFACTORY = fromDef("MOTORFACTORY");
        public static final BuildingType WINE_CELLAR = fromDef("WINE_CELLAR");
        public static final BuildingType FUEL_REFINERY = fromDef("FUEL_REFINERY");
        public static final BuildingType PLASTIC_REFINERY = fromDef("PLASTIC_REFINERY");
        public static final BuildingType CABLE_FACTORY = fromDef("CABLE_FACTORY");
        public static final BuildingType ELECTRICMOTOR_FACTORY = fromDef("ELECTRICMOTOR_FACTORY");
        public static final BuildingType LIGHTBULB_FACTORY = fromDef("LIGHTBULB_FACTORY");
        public static final BuildingType COFFEE_ROASTER = fromDef("COFFEE_ROASTER");
        public static final BuildingType CIGAR_FACTORY = fromDef("CIGAR_FACTORY");
        public static final BuildingType RUM_DISTILLERY = fromDef("RUM_DISTILLERY");
        public static final BuildingType RADIO_FACTORY = fromDef("RADIO_FACTORY");
        public static final BuildingType STEAM_CARRIAGES_FACTORY = fromDef("STEAM_CARRIAGES_FACTORY");
        public static final BuildingType TRUCK_FACTORY = fromDef("TRUCK_FACTORY");
        public static final BuildingType FAN_FACTORY = fromDef("FAN_FACTORY");
        public static final BuildingType FUR_DEALER = fromDef("FUR_DEALER");
        public static final BuildingType SEWING_MACHINE_FACTORY = fromDef("SEWING_MACHINE_FACTORY");
        public static final BuildingType WAREHOUSE_CARRIAGE = fromDef("WAREHOUSE_CARRIAGE");
        public static final BuildingType WAREHOUSE_TRUCK = fromDef("WAREHOUSE_TRUCK");
        public static final BuildingType POWERPLANT = fromDef("POWERPLANT");
        public static final BuildingType BOILER = fromDef("BOILER");

        public static final BuildingType WELL = fromDef("WELL");
        public static final BuildingType AQUEDUCT = fromDef("AQUEDUCT");
        public static final BuildingType DUMP = fromDef("DUMP");
        public static final BuildingType INCINERATOR = fromDef("INCINERATOR");
        public static final BuildingType SMALL_FIRE_STATION = fromDef("SMALL_FIRE_STATION");
        public static final BuildingType BIG_FIRE_STATION = fromDef("BIG_FIRE_STATION");
        public static final BuildingType THERMAL_BATHS = fromDef("THERMAL_BATHS");
        public static final BuildingType HOSPITAL = fromDef("HOSPITAL");
        public static final BuildingType TEMPLE = fromDef("TEMPLE");
        public static final BuildingType CHURCH = fromDef("CHURCH");
        public static final BuildingType THEATER = fromDef("THEATER");
        public static final BuildingType CINEMA = fromDef("CINEMA");
        public static final BuildingType POST_OFFICE = fromDef("POST_OFFICE");
        public static final BuildingType RADIO_TOWER = fromDef("RADIO_TOWER");
        public static final BuildingType SCHOOL = fromDef("SCHOOL");
        public static final BuildingType UNIVERSITY = fromDef("UNIVERSITY");
        public static final BuildingType SMALL_PARK = fromDef("SMALL_PARK");
        public static final BuildingType BIG_PARK = fromDef("BIG_PARK");

        // FORO
        public static final BuildingType FORUM_FOUNDATIONS = fromDef("FORUM_FOUNDATIONS");
        public static final BuildingType FORUM_MAIN_STRUCTRE = fromDef("FORUM_MAIN_STRUCTRE");
        public static final BuildingType FORUM_INFRASTUCTURE = fromDef("FORUM_INFRASTUCTURE");
        public static final BuildingType FORUM_FINISHING = fromDef("FORUM_FINISHING");

        // COLOSSEO
        public static final BuildingType CL_SITE_PREPARATION = fromDef("CL_SITE_PREPARATION");
        public static final BuildingType CL_FOUNDATION = fromDef("CL_FOUNDATION");
        public static final BuildingType CL_EXTERIOR = fromDef("CL_EXTERIOR");
        public static final BuildingType CL_INTERIOR = fromDef("CL_INTERIOR");
        public static final BuildingType CL_FINISHING = fromDef("CL_FINISHING");

        // ESPOSIZIONE UNIVERSALE
        public static final BuildingType UE_FOUNDATION = fromDef("UE_FOUNDATION");
        public static final BuildingType UE_STRUCTURE = fromDef("UE_STRUCTURE");
        public static final BuildingType UE_INTERIOR = fromDef("UE_INTERIOR");
        public static final BuildingType UE_FINISHING = fromDef("UE_FINISHING");

        // TORRE D'ACCIAIO
        public static final BuildingType ST_FOUNATION = fromDef("ST_FOUNATION");
        public static final BuildingType ST_MAIN_FRAME = fromDef("ST_MAIN_FRAME");
        public static final BuildingType ST_FINISHING = fromDef("ST_FINISHING");

        // ACCADEMIA
        public static final BuildingType AC_FOUNDATION = fromDef("AC_FOUNDATION");
        public static final BuildingType AC_STRUCTURE = fromDef("AC_STRUCTURE");
        public static final BuildingType AC_INTERIOR = fromDef("AC_INTERIOR");
        public static final BuildingType AC_EXTERIOR = fromDef("AC_EXTERIOR");
        public static final BuildingType AC_FINISHING = fromDef("AC_FINISHING");

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

                ALL_TYPES.add(WOOD_CAMP);
                ALL_TYPES.add(SAWMILL);
                ALL_TYPES.add(CLAYPIT);
                ALL_TYPES.add(BRICKFACTORY);
                ALL_TYPES.add(MARBLE_QUARRY);
                ALL_TYPES.add(IRONMINE);
                ALL_TYPES.add(COALMINE);
                ALL_TYPES.add(STEELWORK);
                ALL_TYPES.add(SANDPIT);
                ALL_TYPES.add(GLASSWORKS);
                ALL_TYPES.add(WINDOWMAKER);
                ALL_TYPES.add(CEMENTMIXER);
                ALL_TYPES.add(PAPER_MAKER);

                ALL_TYPES.add(GRAIN_FARM);
                ALL_TYPES.add(GRAIN_FIELD);
                ALL_TYPES.add(MILL);
                ALL_TYPES.add(BAKERY);

                ALL_TYPES.add(SHEEP_RANCH);
                ALL_TYPES.add(WOOLEN_MILL);
                ALL_TYPES.add(FLAX_FARM);
                ALL_TYPES.add(FLAX_FIELD);
                ALL_TYPES.add(FABRIC_FACTORY);
                ALL_TYPES.add(TAILORING_WORKSHOP);
                ALL_TYPES.add(COTTON_FARM);
                ALL_TYPES.add(COTTON_FIELD);
                ALL_TYPES.add(COW_RANCH);
                ALL_TYPES.add(PURSE_FACTORY);
                ALL_TYPES.add(FUR_DEALER);

                ALL_TYPES.add(SMITHY);

                ALL_TYPES.add(GOAT_RANCH);
                ALL_TYPES.add(CHEESE_MAKER);

                ALL_TYPES.add(OLIVE_GROVE);
                ALL_TYPES.add(OLIVE_FIELD);
                ALL_TYPES.add(OIL_PREES);

                ALL_TYPES.add(KILN);

                ALL_TYPES.add(LAVANDER_FARM);
                ALL_TYPES.add(LAVANDER_FIELD);
                ALL_TYPES.add(NITRATE_EXTRACTOR);
                ALL_TYPES.add(SOAP_MAKER);
                ALL_TYPES.add(PERFUME_MIXER);

                ALL_TYPES.add(FISHERY);
                ALL_TYPES.add(CANNERY);

                ALL_TYPES.add(HOPS_FARM);
                ALL_TYPES.add(HOPS_FIELD);
                ALL_TYPES.add(BREWERY);

                ALL_TYPES.add(ZINCMINE);
                ALL_TYPES.add(COPPERMINE);
                ALL_TYPES.add(BRASS_FOUNDRY);
                ALL_TYPES.add(SPECTACLES_FACTORY);

                ALL_TYPES.add(SUGAR_PLANTATION);
                ALL_TYPES.add(SUGAR_FIELD);
                ALL_TYPES.add(RUM_DISTILLERY);

                ALL_TYPES.add(SEWING_MACHINE_FACTORY);

                ALL_TYPES.add(COFFEE_PLANTATION);
                ALL_TYPES.add(COFFEE_FIELD);
                ALL_TYPES.add(COFFEE_ROASTER);

                ALL_TYPES.add(RUBBER_PLANTATION);
                ALL_TYPES.add(RUBBER_FIELD);
                ALL_TYPES.add(CABLE_FACTORY);
                ALL_TYPES.add(LIGHTBULB_FACTORY);
                ALL_TYPES.add(RADIO_FACTORY);

                ALL_TYPES.add(MOTORFACTORY);
                ALL_TYPES.add(STEAM_CARRIAGES_FACTORY);

                ALL_TYPES.add(OILRIG);
                ALL_TYPES.add(PLASTIC_REFINERY);
                ALL_TYPES.add(ELECTRICMOTOR_FACTORY);
                ALL_TYPES.add(FAN_FACTORY);

                ALL_TYPES.add(VINEYARD);
                ALL_TYPES.add(GRAPES_FIELD);
                ALL_TYPES.add(WINE_CELLAR);

                ALL_TYPES.add(TOBACCO_PLANTATION);
                ALL_TYPES.add(TOBACCO_FIELD);
                ALL_TYPES.add(CIGAR_FACTORY);

                ALL_TYPES.add(FUEL_REFINERY);
                ALL_TYPES.add(TRUCK_FACTORY);

                ALL_TYPES.add(WELL);
                ALL_TYPES.add(AQUEDUCT);
                ALL_TYPES.add(DUMP);
                ALL_TYPES.add(INCINERATOR);
                ALL_TYPES.add(SMALL_FIRE_STATION);
                ALL_TYPES.add(BIG_FIRE_STATION);
                ALL_TYPES.add(SMALL_PARK);
                ALL_TYPES.add(BIG_PARK);
                ALL_TYPES.add(SCHOOL);
                ALL_TYPES.add(UNIVERSITY);

                ALL_TYPES.add(THEATER);
                ALL_TYPES.add(CINEMA);
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
