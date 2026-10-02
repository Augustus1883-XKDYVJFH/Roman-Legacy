package com.RomanLegacy;

import com.RomanLegacy.BuildingType.ServiceCategory;
import com.badlogic.gdx.Gdx;
import com.RomanLegacy.EventManager;
import com.RomanLegacy.IdeologyManager;
import com.RomanLegacy.BuildingType.ConsumptionServiceCategory;

import java.util.*;

/**
 * Contiene tutto lo stato della partita: mappe, edifici, risorse, fazioni,
 * timer.
 * Non sa nulla di rendering né di input — è puro dato + logica di gioco.
 */
public class GameState {

    // ── Costanti mappa ───────────────────────────────────────────────────────────
    public static final int MAP_WIDTH = 400;
    public static final int MAP_HEIGHT = 400;
    public static final int CELL_SIZE = 25;
    private static final int DEPOSITS_PER_TYPE = 5;

    // ── Costanti spedizione ──────────────────────────────────────────────────────
    public static final int NEW_WORLD_POP_REQ = 1;
    public static final float EXPEDITION_SECS = 120f;

    // ── Tick interval ────────────────────────────────────────────────────────────
    public static final float TICK_INTERVAL = 0.5f;
    public static final float POP_INTERVAL = 1.0f;
    private static final float CAPACITY_SERVICE_BONUS_PER_CAT = 0.15f;
    private static final float CONSUMPTION_SERVICE_REDUCTION_PER_CAT = 0.10f;

    // ── Risorse, fazioni e commercio ────────────────────────────────────────────
    public final ResourceManager resources;
    public final FactionManager factionManager;
    public final TradeManager tradeManager;
    public final IdeologyManager ideologyManager;
    public final EventManager eventManager;

    // ── Mappe ────────────────────────────────────────────────────────────────────
    public int[][] terrainMapOld;
    public int[][] terrainMapNew;
    public int[][] terrainMap;

    public List<BuildingInstance> buildingsOld = new ArrayList<>();
    public List<BuildingInstance> buildingsNew = new ArrayList<>();
    public List<BuildingInstance> buildings;
    public List<BuildingInstance> warehouseCarriageList = new ArrayList<>();
    public List<BuildingInstance> warehouseTruckList = new ArrayList<>();
    public List<ResourceFloater> floaters = new ArrayList<>();
    public Map<String, List<int[]>> depositsOld = new HashMap<>();
    public Map<String, List<int[]>> depositsNew = new HashMap<>();

    private Map<ServiceCategory, Float> totalCapacity = new HashMap<>();
    private Map<ServiceCategory, Float> totalConsumption = new HashMap<>();
    private Map<ServiceCategory, Float> serviceSatisfaction = new HashMap<>();
    private Map<ConsumptionServiceCategory, Float> consumptionServiceSatisfaction = new HashMap<>();
    private float[] classPopulation = new float[BuildingType.SOCIAL_CLASS_NAMES.length];
    private float[] classWorkforceSatisfaction = new float[BuildingType.SOCIAL_CLASS_NAMES.length];
    private boolean[] classHasDemand = new boolean[BuildingType.SOCIAL_CLASS_NAMES.length];
    private float[] classWorkforceDemand = new float[BuildingType.SOCIAL_CLASS_NAMES.length];
    private static final BuildingType[] DEPOSIT_BUILDINGS = {
            BuildingType.CLAYPIT, BuildingType.SANDPIT, BuildingType.MARBLE_QUARRY, BuildingType.OILRIG
    };

    // ── Flags mappa ──────────────────────────────────────────────────────────────
    public boolean onNewWorld = false;
    public boolean expeditionActive = false;
    public boolean newWorldUnlocked = false;
    public float expeditionTimer = 0f;
    public boolean showNewWorldOverlay = false;

    // ── Timer produzione / popolazione ──────────────────────────────────────────
    public float productionTimer = 0f;
    public float populationTimer = 0f;

    // Moltiplicatore velocità di gioco (1x/2x/3x). Non influisce sul tempo reale
    // di autosalvataggio, solo sulla simulazione.
    public float gameSpeed = 1f;

    /**
     * Difficoltà scelta all'evento THE_NEW_CITY. Default MEDIUM finché non risolto.
     */
    public Difficulty difficulty = Difficulty.MEDIUM;

    // ── Costruttori ──────────────────────────────────────────────────────────────

    /**
     * Costruttore con seed esplicito — usato al caricamento del salvataggio
     * in modo che le fazioni vengano create subito con il seed corretto,
     * evitando che cambino ad ogni load.
     */
    public GameState(long factionSeed) {
        resources = new ResourceManager();
        factionManager = new FactionManager(factionSeed);
        ideologyManager = new IdeologyManager();
        tradeManager = new TradeManager(this);
        eventManager = new EventManager(factionSeed + 1);

        terrainMapOld = new MapGenerator().generate(MAP_WIDTH, MAP_HEIGHT, 3.5);
        generateDepositsForWorld(false);
        terrainMapNew = null;
        terrainMap = terrainMapOld;
        buildings = buildingsOld;
    }

    /**
     * Carica lo stato da un salvataggio.
     */
    public void loadFromSave(SaveData save) {
        // 1. Ripristina le mappe
        if (save.terrainMapOld != null) {
            terrainMapOld = save.terrainMapOld;
        } else {
            terrainMapOld = new MapGenerator().generate(MAP_WIDTH, MAP_HEIGHT, 3.5);
        }

        if (save.terrainMapNew != null) {
            terrainMapNew = save.terrainMapNew;
        } else if (save.newWorldUnlocked) {
            terrainMapNew = new MapGenerator().generate(MAP_WIDTH, MAP_HEIGHT, 5.0);
        } else {
            terrainMapNew = null;
        }

        if (save.onNewWorld) {
            if (terrainMapNew == null)
                terrainMapNew = new MapGenerator().generate(MAP_WIDTH, MAP_HEIGHT, 5.0);
            terrainMap = terrainMapNew;
            buildings = buildingsNew;
        } else {
            terrainMap = terrainMapOld;
            buildings = buildingsOld;
        }

        // I depositi non sono salvati: si rigenerano dallo stesso seed, quindi tornano
        // identici.
        generateDepositsForWorld(false);
        if (terrainMapNew != null)
            generateDepositsForWorld(true);

        // 3. Carica edifici di ENTRAMBI i mondi
        buildingsOld.clear();
        buildingsNew.clear();

        if (save.buildingsOld != null) {
            for (BuildingData data : save.buildingsOld) {
                BuildingInstance b = deserializeBuilding(data);
                if (b != null)
                    buildingsOld.add(b);
            }
        }

        if (save.buildingsNew != null) {
            for (BuildingData data : save.buildingsNew) {
                BuildingInstance b = deserializeBuilding(data);
                if (b != null)
                    buildingsNew.add(b);
            }
        }

        if (save.resources != null && !save.resources.isEmpty()) {
            for (var entry : save.resources.entrySet()) {
                resources.set(entry.getKey(), entry.getValue());
            }
        } else {
            if (save.gold > 0)
                resources.set("coin", save.gold);
            if (save.population > 0)
                resources.set("population", save.population);
        }

        onNewWorld = save.onNewWorld;
        newWorldUnlocked = save.newWorldUnlocked;

        // 7. Ripristina stato fazioni
        if (save.factionRelations != null && save.factionArrived != null) {
            FactionManager.FactionState[] slots = factionManager.getAllSlots();
            for (int i = 0; i < slots.length && i < save.factionRelations.length; i++) {
                slots[i].relation = save.factionRelations[i];
                slots[i].arrived = save.factionArrived[i];
            }
        }

        // 12. Ripristina difficoltà
        if (save.difficulty != null) {
            try {
                difficulty = Difficulty.valueOf(save.difficulty);
            } catch (IllegalArgumentException e) {
                difficulty = Difficulty.MEDIUM;
            }
        }

        // 8. Ripristina ideologie
        ideologyManager.setValue(save.ideologyValue);

        // 9. Ripristina flotta
        tradeManager.deserialize(save.fleet);

        // 10. Ripristina eventi
        eventManager.deserializeFiredEvents(save.firedEvents);
        eventManager.restorePendingEvent(save.pendingEventId);

        // 11. Ricalcola connessioni
        updateAllConnections();
    }

    // ── Tick ─────────────────────────────────────────────────────────────────────
    public void tickProduction(float delta) {
        for (BuildingInstance b : buildings) {
            if (b.type.prod == null && !b.type.consumableOnly)
                continue;
            boolean produced = b.tick(resources, delta);
            if (produced && b.type.prod != null && !b.type.consumableOnly) {
                float worldX = (b.rootX + b.effectiveW() / 2f) * CELL_SIZE;
                float worldY = (b.rootY + b.effectiveH()) * CELL_SIZE;
                spawnResourceFloater(b.type.prod, b.type.rate, worldX, worldY);
            }
        }
    }

    /**
     * Aggiunge un floater "+N risorsa"; cap di sicurezza per non accumulare
     * all'infinito.
     */
    private void spawnResourceFloater(String resourceId, int amount, float worldX, float worldY) {
        if (floaters.size() > 300)
            floaters.remove(0);
        floaters.add(new ResourceFloater(resourceId, amount, worldX, worldY));
    }

    /**
     * Da chiamare ogni frame con delta reale (non scalato), aggiorna e ripulisce i
     * floater.
     */
    public void tickFloaters(float delta) {
        Iterator<ResourceFloater> it = floaters.iterator();
        while (it.hasNext()) {
            ResourceFloater f = it.next();
            f.update(delta);
            if (f.isDead())
                it.remove();
        }
    }

    public void tickMonumentUpgrades(float delta) {
        for (BuildingInstance b : buildings) {
            if (b.monumentUpgrading) {
                if (b.tickMonumentUpgrade(delta, difficulty.monumentTimeMultiplier)) {
                    ideologyManager.applyShift(b.type.axisShift); // b.type è già il nuovo livello
                }
            }
        }
    }

    public void tickPopulation() {
        updateServiceCapacity();
        updateWorkforce();

        float totalPop = 0f;
        float totalTaxes = 0f;
        float totalMaintenance = 0f;

        // Moltiplicatori servizi (globali, non per-casa) e di difficoltà —
        // calcolati una sola volta per tick.
        float incomeMultiplier = getIncomeMultiplier() * difficulty.incomeMultiplier;

        for (BuildingInstance b : buildings) {
            if (b.type.isHouse) {
                BuildingType.ConnectionType req = b.type.requiredConnection;
                boolean canGrow = (req == null || req == BuildingType.ConnectionType.NONE) || b.connected;

                if (canGrow) {
                    b.tickHouse(resources, this);
                }
                totalPop += b.housePop;

                float baseTax = b.calculateTaxesPerMinute() / 60f;
                totalTaxes += baseTax * incomeMultiplier;
            }

            // Manutenzione
            if (!b.type.isHouse && b.type.input != null && !b.type.input.isEmpty()) {
                if (!b.paused && b.connected) {
                    totalMaintenance += b.getMaintenancePerMinute() / 60f;
                }
            }
        }

        totalMaintenance *= difficulty.maintenanceMultiplier;

        if (totalTaxes > 0)
            resources.add("coin", totalTaxes);
        if (totalMaintenance > 0)
            resources.add("coin", -totalMaintenance);

        resources.setTotalPopulation(totalPop);
    }

    public void updateServiceCapacity() {
        for (ServiceCategory cat : ServiceCategory.values()) {
            totalCapacity.put(cat, 0f);
            totalConsumption.put(cat, 0f);
        }

        // 1. Capacità totale
        for (BuildingInstance b : buildings) {
            if (b.type.serviceCategory != null) {
                float capacity = b.type.serviceCapacity;
                if (b.paused || !b.connected) {
                    capacity *= 0.3f;
                }
                totalCapacity.put(b.type.serviceCategory,
                        totalCapacity.get(b.type.serviceCategory) + capacity);
            }
        }

        // 2. Consumo totale case
        for (BuildingInstance b : buildings) {
            if (b.type.isHouse && b.connected) {
                for (ServiceCategory cat : ServiceCategory.values()) {
                    float cap = b.type.houseCap();
                    float consumption = cat.consumptionPerHouse * (cap > 0 ? b.housePop / cap : 0f);
                    totalConsumption.put(cat, totalConsumption.get(cat) + consumption);
                }
            }
        }

        // 3. Soddisfazione
        for (ServiceCategory cat : ServiceCategory.values()) {
            float capacity = totalCapacity.getOrDefault(cat, 0f);
            float consumption = totalConsumption.getOrDefault(cat, 0f);

            if (capacity > 0 && consumption > 0) {
                serviceSatisfaction.put(cat, Math.min(1.0f, capacity / consumption));
            } else if (capacity > 0 && consumption == 0) {
                serviceSatisfaction.put(cat, 1.0f);
            } else {
                serviceSatisfaction.put(cat, 0f);
            }
        }

        updateConsumptionServices();
    }

    /**
     * Servizi a consumo: ogni edificio ha un raggio di influenza.
     * Una casa è servita se si trova nel raggio di almeno un edificio attivo della
     * categoria.
     * Soddisfazione = % di case servite / totale case per quella categoria.
     */
    private void updateConsumptionServices() {
        // Inizializza mappe per case servite e totali per categoria
        Map<ConsumptionServiceCategory, Integer> housesServed = new HashMap<>();
        Map<ConsumptionServiceCategory, Integer> housesTotal = new HashMap<>();

        for (ConsumptionServiceCategory cat : ConsumptionServiceCategory.values()) {
            housesServed.put(cat, 0);
            housesTotal.put(cat, 0);
        }

        // Raccogli tutti i servizi attivi per categoria
        Map<ConsumptionServiceCategory, List<BuildingInstance>> activeServices = new HashMap<>();
        for (ConsumptionServiceCategory cat : ConsumptionServiceCategory.values()) {
            activeServices.put(cat, new ArrayList<>());
        }

        for (BuildingInstance b : buildings) {
            ConsumptionServiceCategory cat = b.type.consumptionServiceCategory;
            if (cat == null)
                continue;

            // Controlla se il servizio è attivo
            boolean isActive = b.connected && !b.paused;
            if (isActive && b.type.input != null) {
                for (Map.Entry<String, Float> e : b.type.input.entrySet()) {
                    if (resources.get(e.getKey()) < e.getValue()) {
                        isActive = false;
                        break;
                    }
                }
            }

            if (isActive) {
                activeServices.get(cat).add(b);
            }
        }

        // Per ogni casa, controlla se ha accesso ai servizi
        for (BuildingInstance house : buildings) {
            if (!house.type.isHouse || house.type.houseLevel == null)
                continue;

            int centerX = house.rootX + house.effectiveW() / 2;
            int centerY = house.rootY + house.effectiveH() / 2;

            for (ConsumptionServiceCategory cat : ConsumptionServiceCategory.values()) {
                housesTotal.put(cat, housesTotal.get(cat) + 1);

                boolean hasService = false;
                for (BuildingInstance service : activeServices.get(cat)) {
                    int radius = service.type.influenceRadius;
                    if (radius <= 0) {
                        // Se non ha raggio, influenza globale (fallback)
                        hasService = true;
                        break;
                    }

                    int serviceCenterX = service.rootX + service.effectiveW() / 2;
                    int serviceCenterY = service.rootY + service.effectiveH() / 2;

                    int dx = centerX - serviceCenterX;
                    int dy = centerY - serviceCenterY;
                    int distSq = dx * dx + dy * dy;
                    int radiusSq = radius * radius;

                    if (distSq <= radiusSq) {
                        hasService = true;
                        break;
                    }
                }

                if (hasService) {
                    housesServed.put(cat, housesServed.get(cat) + 1);
                }
            }
        }

        // Calcola soddisfazione = case servite / case totali per categoria
        for (ConsumptionServiceCategory cat : ConsumptionServiceCategory.values()) {
            int total = housesTotal.get(cat);
            int served = housesServed.get(cat);

            if (total == 0) {
                consumptionServiceSatisfaction.put(cat, 0f);
            } else {
                consumptionServiceSatisfaction.put(cat, (float) served / total);
            }
        }
    }

    public void updateWorkforce() {
        float[] supply = new float[BuildingType.SOCIAL_CLASS_NAMES.length];
        float[] demand = new float[BuildingType.SOCIAL_CLASS_NAMES.length];

        for (BuildingInstance b : buildings) {
            if (b.type.isHouse && b.type.houseLevel != null) {
                supply[b.type.houseLevel] += (b.housePop * 2);
            }
        }

        for (BuildingInstance b : buildings) {
            if (b.type.workforceClass != null && b.connected && !b.paused) {
                demand[b.type.workforceClass] += b.type.workforceAmount;
            }
        }

        classPopulation = supply;
        classWorkforceDemand = demand;

        for (int i = 0; i < demand.length; i++) {
            classHasDemand[i] = demand[i] > 0f;
            classWorkforceSatisfaction[i] = demand[i] > 0f ? Math.min(1f, supply[i] / demand[i]) : 1f;
        }

        for (BuildingInstance b : buildings) {
            if (b.type.workforceClass == null) {
                b.workforceSatisfaction = 1f;
                continue;
            }
            int idx = b.type.workforceClass;
            b.workforceSatisfaction = classWorkforceSatisfaction[idx];
        }
    }

    public float getIncomeMultiplier() {
        float bonus = 0f;
        for (ServiceCategory cat : ServiceCategory.values()) {
            bonus += CAPACITY_SERVICE_BONUS_PER_CAT * getServiceSatisfaction(cat);
        }
        return 1f + bonus;
    }

    /**
     * Moltiplicatore consumo case: 1.0 - somma(0.10 * soddisfazione) per ogni
     * ConsumptionServiceCategory.
     */
    public float getConsumptionMultiplier() {
        float reduction = 0f;
        for (ConsumptionServiceCategory cat : ConsumptionServiceCategory.values()) {
            reduction += CONSUMPTION_SERVICE_REDUCTION_PER_CAT * getConsumptionServiceSatisfaction(cat);
        }
        return Math.max(0f, 1f - reduction);
    }

    public float getClassPopulation(int classIndex) {
        if (classIndex < 0 || classIndex >= classPopulation.length)
            return 0f;
        return classPopulation[classIndex];
    }

    public float getClassWorkforceSatisfaction(int classIndex) {
        if (classIndex < 0 || classIndex >= classWorkforceSatisfaction.length)
            return 1f;
        return classWorkforceSatisfaction[classIndex];
    }

    public boolean classHasWorkforceDemand(int classIndex) {
        if (classIndex < 0 || classIndex >= classHasDemand.length)
            return false;
        return classHasDemand[classIndex];
    }

    public float getClassWorkforceDemand(int classIndex) {
        if (classIndex < 0 || classIndex >= classWorkforceDemand.length)
            return 0f;
        return classWorkforceDemand[classIndex];
    }

    public float getConsumptionServiceSatisfaction(ConsumptionServiceCategory cat) {
        return consumptionServiceSatisfaction.getOrDefault(cat, 0f);
    }

    public float getServiceSatisfaction(ServiceCategory cat) {
        return serviceSatisfaction.getOrDefault(cat, 0f);
    }

    public float getServiceCapacity(ServiceCategory cat) {
        return totalCapacity.getOrDefault(cat, 0f);
    }

    public float getServiceConsumption(ServiceCategory cat) {
        return totalConsumption.getOrDefault(cat, 0f);
    }

    // ── Spedizione / Nuovo Mondo ─────────────────────────────────────────────────

    public void checkExpedition() {
        if (newWorldUnlocked || expeditionActive)
            return;
        float patriziPop = 0f;
        for (BuildingInstance b : buildingsOld) {
            if (b.type == BuildingType.VILLA)
                patriziPop += b.housePop;
        }
        if ((int) patriziPop >= NEW_WORLD_POP_REQ)
            startExpedition();
    }

    public void startExpedition() {
        expeditionActive = true;
        expeditionTimer = 0f;
        terrainMapNew = new MapGenerator().generate(MAP_WIDTH, MAP_HEIGHT, 5.0);
        generateDepositsForWorld(true);
    }

    public void completeExpedition() {
        newWorldUnlocked = true;
        expeditionActive = false;
        expeditionTimer = 0f;
        showNewWorldOverlay = true;
    }

    public void switchMap() {
        onNewWorld = !onNewWorld;
        terrainMap = onNewWorld ? terrainMapNew : terrainMapOld;
        buildings = onNewWorld ? buildingsNew : buildingsOld;
    }

    public BuildingType.Region getCurrentRegion() {
        return onNewWorld ? BuildingType.Region.NEW_WORLD : BuildingType.Region.OLD_WORLD;
    }

    // ── Connessioni ──────────────────────────────────────────────────────────────

    public void updateAllConnections() {
        for (BuildingInstance b : buildings)
            b.connected = false;

        // Flood fill per ROAD (porto merci)
        Set<String> roadConnected = new HashSet<>();
        for (BuildingInstance b : buildings) {
            if (b.type == BuildingType.DOCK) {
                for (int dy = 0; dy < b.effectiveH(); dy++)
                    for (int dx = 0; dx < b.effectiveW(); dx++)
                        roadConnected.add((b.rootX + dx) + "," + (b.rootY + dy));
            }
        }
        roadConnected = floodFillFromSet(roadConnected, BuildingType.ROAD);

        // Flood fill per PIPE (porto petrolio)
        Set<String> pipeConnected = new HashSet<>();
        for (BuildingInstance b : buildings) {
            if (b.type == BuildingType.OIL_DOCK) {
                for (int dy = 0; dy < b.effectiveH(); dy++)
                    for (int dx = 0; dx < b.effectiveW(); dx++)
                        pipeConnected.add((b.rootX + dx) + "," + (b.rootY + dy));
            }
        }
        pipeConnected = floodFillFromSet(pipeConnected, BuildingType.PIPELINE);

        for (BuildingInstance b : buildings) {
            BuildingType.ConnectionType req = b.type.requiredConnection;
            if (req == BuildingType.ConnectionType.ROAD) {
                b.connected = buildingTouchesNetwork(b, roadConnected);
            } else if (req == BuildingType.ConnectionType.PIPE) {
                b.connected = buildingTouchesNetwork(b, pipeConnected);
            } else if (req == BuildingType.ConnectionType.BOTH) {
                b.connected = buildingTouchesNetwork(b, roadConnected)
                        && buildingTouchesNetwork(b, pipeConnected);
            } else {
                b.connected = true;
            }
        }

        updateSteamAndElectricityInfluence();
        updateLogisticsInfluence();
        updatePenEfficiency();
    }

    private Set<String> floodFillFromSet(Set<String> seeds, BuildingType pathType) {
        return floodFillFromList(buildings, seeds, pathType);
    }

    /** Rete porto → strade/PIPELINE raggiungibili (per anteprima piazzamento). */
    public Set<String> buildPathNetwork(List<BuildingInstance> buildingsList,
            BuildingType portType, BuildingType pathType) {
        Set<String> seeds = new HashSet<>();
        for (BuildingInstance b : buildingsList) {
            if (b.type == portType) {
                for (int dy = 0; dy < b.effectiveH(); dy++)
                    for (int dx = 0; dx < b.effectiveW(); dx++)
                        seeds.add((b.rootX + dx) + "," + (b.rootY + dy));
            }
        }
        return floodFillFromList(buildingsList, seeds, pathType);
    }

    private Set<String> floodFillFromList(List<BuildingInstance> buildingsList,
            Set<String> seeds, BuildingType pathType) {
        Set<String> visited = new HashSet<>(seeds);
        Queue<int[]> queue = new LinkedList<>();

        for (String seed : seeds) {
            String[] parts = seed.split(",");
            queue.add(new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) });
        }

        int[] ddx = { 0, 1, 0, -1 };
        int[] ddy = { 1, 0, -1, 0 };

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int cx = cell[0], cy = cell[1];

            for (int i = 0; i < 4; i++) {
                int nx = cx + ddx[i];
                int ny = cy + ddy[i];
                String nkey = nx + "," + ny;

                if (nx < 0 || ny < 0 || nx >= MAP_WIDTH || ny >= MAP_HEIGHT)
                    continue;
                if (visited.contains(nkey))
                    continue;

                BuildingInstance building = getBuildingAtFromList(buildingsList, nx, ny);
                if (building != null && building.type == pathType) {
                    visited.add(nkey);
                    queue.add(new int[] { nx, ny });
                }
            }
        }
        return visited;
    }

    /**
     * True se una cella dell'edificio coincide con la rete o è adiacente ad essa.
     */
    public boolean buildingTouchesNetwork(BuildingInstance b, Set<String> network) {
        if (network.isEmpty())
            return false;

        int[] ddx = { 0, 1, 0, -1 };
        int[] ddy = { 1, 0, -1, 0 };

        for (int dy = 0; dy < b.effectiveH(); dy++) {
            for (int dx = 0; dx < b.effectiveW(); dx++) {
                int cx = b.rootX + dx;
                int cy = b.rootY + dy;
                String key = cx + "," + cy;
                if (network.contains(key))
                    return true;
                for (int i = 0; i < 4; i++) {
                    if (network.contains((cx + ddx[i]) + "," + (cy + ddy[i])))
                        return true;
                }
            }
        }
        return false;
    }

    public void updateSteamAndElectricityInfluence() {
        // Reset di tutti i flag
        for (BuildingInstance b : buildings) {
            b.hasSteam = false;
            b.hasElectricity = false;
        }

        // Calcola influenza vapore (da ogni BOILER)
        for (BuildingInstance source : buildings) {
            if (source.type == BuildingType.BOILER && source.type.influenceRadius > 0) {
                applyInfluence(source, source.type.influenceRadius, true);
            }
        }

        // Calcola influenza elettricità (da ogni POWERPLANT)
        for (BuildingInstance source : buildings) {
            if (source.type == BuildingType.POWERPLANT && source.type.influenceRadius > 0) {
                applyInfluence(source, source.type.influenceRadius, false);
            }
        }
    }

    private void applyInfluence(BuildingInstance source, int radius, boolean isSteam) {
        // Calcola centro dell'edificio sorgente
        int centerX = source.rootX + source.effectiveW() / 2;
        int centerY = source.rootY + source.effectiveH() / 2;

        int radiusSq = radius * radius;

        // Scansiona tutti gli edifici e controlla se sono nel raggio
        for (BuildingInstance target : buildings) {
            if (target == source)
                continue;

            // Calcola centro edificio target
            int targetCenterX = target.rootX + target.effectiveW() / 2;
            int targetCenterY = target.rootY + target.effectiveH() / 2;

            int dx = targetCenterX - centerX;
            int dy = targetCenterY - centerY;
            int distSq = dx * dx + dy * dy;

            if (distSq <= radiusSq) {
                if (isSteam) {
                    target.hasSteam = true;
                } else {
                    target.hasElectricity = true;
                }
            }
        }
    }

    public void updateLogisticsInfluence() {
        // Reset flag
        for (BuildingInstance b : buildings) {
            b.hasCarriageLogistics = false;
            b.hasTruckLogistics = false;
        }

        // Calcola influenza magazzini carrozze
        warehouseCarriageList.clear();
        for (BuildingInstance b : buildings) {
            if (b.type == BuildingType.WAREHOUSE_CARRIAGE && b.type.influenceRadius > 0) {
                warehouseCarriageList.add(b);
            }
        }
        for (BuildingInstance source : warehouseCarriageList) {
            applyLogisticsInfluence(source, source.type.influenceRadius, true);
        }

        // Calcola influenza magazzini autocarri
        warehouseTruckList.clear();
        for (BuildingInstance b : buildings) {
            if (b.type == BuildingType.WAREHOUSE_TRUCK && b.type.influenceRadius > 0) {
                warehouseTruckList.add(b);
            }
        }
        for (BuildingInstance source : warehouseTruckList) {
            applyLogisticsInfluence(source, source.type.influenceRadius, false);
        }
    }

    private void applyLogisticsInfluence(BuildingInstance source, int radius, boolean isCarriage) {
        int centerX = source.rootX + source.effectiveW() / 2;
        int centerY = source.rootY + source.effectiveH() / 2;
        int radiusSq = radius * radius;

        for (BuildingInstance target : buildings) {
            if (target == source)
                continue;
            if (target.type.prod == null)
                continue; // Solo edifici produttivi

            int targetCenterX = target.rootX + target.effectiveW() / 2;
            int targetCenterY = target.rootY + target.effectiveH() / 2;

            int dx = targetCenterX - centerX;
            int dy = targetCenterY - centerY;
            int distSq = dx * dx + dy * dy;

            if (distSq <= radiusSq) {
                if (isCarriage) {
                    target.hasCarriageLogistics = true;
                } else {
                    target.hasTruckLogistics = true;
                }
            }
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────────────

    public BuildingInstance getBuildingAt(int cx, int cy) {
        for (BuildingInstance b : buildings) {
            if (b.occupies(cx, cy))
                return b;
        }
        return null;
    }

    public BuildingInstance getBuildingAtFromList(List<BuildingInstance> list, int cx, int cy) {
        for (BuildingInstance b : list) {
            if (b.occupies(cx, cy))
                return b;
        }
        return null;
    }

    public boolean isOccupied(int cx, int cy, int w, int h) {
        // Controlla edifici
        for (BuildingInstance bi : buildings) {
            for (int dy = 0; dy < h; dy++)
                for (int dx = 0; dx < w; dx++)
                    if (bi.occupies(cx + dx, cy + dy))
                        return true;
        }
        return false;
    }

    public boolean hasDepositAt(BuildingType bt, int x, int y) {
        Map<String, List<int[]>> deposits = onNewWorld ? depositsNew : depositsOld;
        List<int[]> spots = deposits.get(bt.id);
        if (spots == null)
            return false;
        for (int[] s : spots)
            if (s[0] == x && s[1] == y)
                return true;
        return false;
    }

    private void generateDepositsForWorld(boolean newWorld) {
        int[][] terrain = newWorld ? terrainMapNew : terrainMapOld;
        if (terrain == null)
            return;

        long seed = factionManager.getSeed() + (newWorld ? 3 : 2); // +1 è già usato da EventManager
        Random rng = new Random(seed);
        BuildingType.Region region = newWorld ? BuildingType.Region.NEW_WORLD : BuildingType.Region.OLD_WORLD;
        Map<String, List<int[]>> deposits = new HashMap<>();

        for (BuildingType bt : DEPOSIT_BUILDINGS) {
            if (!bt.isAllowedInRegion(region))
                continue;

            List<int[]> spots = new ArrayList<>();
            int attempts = 0;
            while (spots.size() < DEPOSITS_PER_TYPE && attempts < 2000) {
                attempts++;
                int x = rng.nextInt(MAP_WIDTH - bt.w);
                int y = rng.nextInt(MAP_HEIGHT - bt.h);
                if (!terrainOkForDeposit(bt, terrain, x, y))
                    continue;
                if (overlapsAnyDeposit(deposits, x, y, bt.w, bt.h))
                    continue;
                spots.add(new int[] { x, y });
            }
            deposits.put(bt.id, spots);
        }

        if (newWorld)
            depositsNew = deposits;
        else
            depositsOld = deposits;
    }

    private boolean terrainOkForDeposit(BuildingType bt, int[][] terrain, int x, int y) {
        for (int dy = 0; dy < bt.h; dy++) {
            for (int dx = 0; dx < bt.w; dx++) {
                int t = terrain[y + dy][x + dx];
                if (t == MapGenerator.WATER) {
                    boolean waterOk = bt.onlyTerrain != null
                            && containsTerrainValue(bt.onlyTerrain, MapGenerator.WATER);
                    if (!waterOk)
                        return false;
                }
                if (t == MapGenerator.MOUNTAIN || t == MapGenerator.PEAK) {
                    boolean mtnOk = bt.onlyTerrain != null
                            && (containsTerrainValue(bt.onlyTerrain, MapGenerator.MOUNTAIN)
                                    || containsTerrainValue(bt.onlyTerrain, MapGenerator.PEAK));
                    if (!mtnOk)
                        return false;
                }
                if (!bt.allowedOnTerrain(t))
                    return false;
            }
        }
        return true;
    }

    private static boolean containsTerrainValue(int[] arr, int val) {
        if (arr == null)
            return false;
        for (int v : arr)
            if (v == val)
                return true;
        return false;
    }

    private boolean overlapsAnyDeposit(Map<String, List<int[]>> deposits, int x, int y, int w, int h) {
        for (Map.Entry<String, List<int[]>> e : deposits.entrySet()) {
            BuildingType other = BuildingType.valueOf(e.getKey());
            for (int[] s : e.getValue()) {
                if (x < s[0] + other.w && s[0] < x + w && y < s[1] + other.h && s[1] < y + h)
                    return true;
            }
        }
        return false;
    }

    /**
     * True se il livello successivo del monumento entra senza sovrapporsi a
     * terreno non edificabile o ad altri edifici.
     */
    public boolean canMonumentGrow(BuildingInstance monument) {
        BuildingType next = monument.type.nextMonumentLevel();
        if (next == null)
            return false;

        int w = monument.rotated ? next.h : next.w;
        int h = monument.rotated ? next.w : next.h;
        if (monument.rootX < 0 || monument.rootY < 0
                || monument.rootX + w > MAP_WIDTH || monument.rootY + h > MAP_HEIGHT)
            return false;

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int terrain = terrainMap[monument.rootY + dy][monument.rootX + dx];
                if (terrain == MapGenerator.WATER || terrain == MapGenerator.MOUNTAIN
                        || terrain == MapGenerator.PEAK)
                    return false;
            }
        }
        for (BuildingInstance other : buildings) {
            if (other == monument)
                continue;
            for (int dy = 0; dy < h; dy++) {
                for (int dx = 0; dx < w; dx++) {
                    if (other.occupies(monument.rootX + dx, monument.rootY + dy))
                        return false;
                }
            }
        }
        return true;
    }

    public boolean monumentExists(String chainId) {
        for (BuildingInstance b : buildingsOld) {
            if (b.type.isMonument && b.type.monumentChainId.equals(chainId))
                return true;
        }
        for (BuildingInstance b : buildingsNew) {
            if (b.type.isMonument && b.type.monumentChainId.equals(chainId))
                return true;
        }
        return false;
    }

    public int countBuildingsOfType(BuildingType type) {
        int count = 0;
        for (BuildingInstance b : buildingsOld)
            if (b.type == type)
                count++;
        for (BuildingInstance b : buildingsNew)
            if (b.type == type)
                count++;
        return count;
    }

    public boolean hasPen(BuildingInstance ranch, String penId) {
        for (BuildingInstance p : buildings)
            if (p.type.id.equals(penId) && p.ownerX == ranch.rootX && p.ownerY == ranch.rootY)
                return true;
        return false;
    }

    /**
     * Rimuove un edificio; se è un allevamento o una fattoria toglie anche
     * recinti/campi.
     */
    public List<BuildingInstance> removeBuilding(BuildingInstance target) {
        List<BuildingInstance> removed = new ArrayList<>();
        if (buildings.remove(target))
            removed.add(target);

        Iterator<BuildingInstance> it = buildings.iterator();
        while (it.hasNext()) {
            BuildingInstance child = it.next();
            boolean belongsToRemovedBuilding = target.type.hasPens()
                    && child.type.isPen()
                    && target.type.id.equals(child.type.penOwnerId)
                    && child.ownerX == target.rootX && child.ownerY == target.rootY;
            belongsToRemovedBuilding |= target.type.hasFields()
                    && child.type.isField()
                    && target.type.id.equals(child.type.fieldOwnerId)
                    && child.ownerX == target.rootX && child.ownerY == target.rootY;
            if (belongsToRemovedBuilding) {
                removed.add(child);
                it.remove();
            }
        }
        return removed;
    }

    /** Efficienza allevamenti (recinti) e fattorie (campi), 0..1. */
    public void updatePenEfficiency() {
        for (BuildingInstance b : buildings) {
            if (b.type.hasPens()) {
                int built = 0;
                for (String penId : b.type.penIds)
                    if (hasPen(b, penId))
                        built++;
                b.penEfficiency = (float) built / b.type.penIds.length;
            } else if (b.type.hasFields()) {
                int count = countFields(b);
                b.penEfficiency = Math.min(1f, (float) count / b.type.fieldCap);
            } else {
                b.penEfficiency = 1f;
            }
        }
    }

    public int countFields(BuildingInstance farm) {
        int count = 0;
        for (BuildingInstance b : buildings)
            if (b.type.isField() && b.type.fieldOwnerId.equals(farm.type.id)
                    && b.ownerX == farm.rootX && b.ownerY == farm.rootY)
                count++;
        return count;
    }

    // ── Privati
    // ───────────────────────────────────────────────────────────────────

    private BuildingInstance deserializeBuilding(BuildingData data) {
        try {
            BuildingType type = BuildingType.valueOf(data.id);
            int rootX = (int) (data.x / CELL_SIZE);
            int rootY = (int) (data.y / CELL_SIZE);
            BuildingInstance b = new BuildingInstance(type, rootX, rootY);
            b.progress = data.productionTimer;
            b.paused = data.paused;
            b.rotated = data.rotated;
            b.connected = data.connected;
            b.housePop = data.housePop;
            b.monumentUpgrading = data.monumentUpgrading;
            b.monumentUpgradeProgress = data.monumentUpgradeProgress;
            b.ownerX = data.ownerX;
            b.ownerY = data.ownerY;
            return b;
        } catch (IllegalArgumentException e) {
            Gdx.app.error("GameState", "Unknown building type: " + data.id);
            return null;
        }
    }
}
