package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce l'intera flotta commerciale.
 * Viene aggiornato ogni frame da GameScreen via tickTrade(delta).
 *
 * Responsabilità:
 * - Acquisto navi
 * - Avvio/gestione viaggi (carico, scarico, timer)
 * - Manutenzione (dedotta ogni minuto come gli edifici)
 * - Verifica presenza porto nel mondo corretto
 */
public class TradeManager {

    // ── Flotta
    // ────────────────────────────────────────────────────────────────────
    private final List<Ship> fleet = new ArrayList<>();
    private int nextId = 1;
    private static final float MAINTENANCE_INTERVAL = 60f;
    private final GameState state;

    public TradeManager(GameState state) {
        this.state = state;
    }

    // ── API pubblica
    // ──────────────────────────────────────────────────────────────

    /** Acquista una nuova nave. Restituisce false se non si possono permettere. */
    public boolean buyShip(ShipType type) {
        if (state.resources.get("coin") < type.purchaseCost) {
            return false;
        }
        state.resources.add("coin", -type.purchaseCost);
        String name = type.label + " " + nextId;
        Ship ship = new Ship(type, name, nextId++);
        fleet.add(ship);
        return true;
    }

    /** Restituisce la lista navi (non modificabile dall'esterno). */
    public List<Ship> getFleet() {
        return fleet;
    }

    /**
     * Invia una nave in viaggio se è docked e ha almeno una rotta configurata.
     * Carica le risorse dal mondo di origine.
     */
    public boolean dispatch(Ship ship) {
        if (ship.state != Ship.State.DOCKED)
            return false;
        if (!ship.hasRoute())
            return false;
        // Verifica porto nel mondo di origine
        List<BuildingInstance> srcBuildings = ship.toNewWorld ? state.buildingsOld : state.buildingsNew;
        if (!hasPortInList(srcBuildings, ship.type.requiredPort())) {
            return false;
        }

        for (int i = 0; i < ship.type.slots; i++) {
            String res = ship.assignedResources[i];
            if (res == null || res.isEmpty()) {
                ship.cargo[i] = 0f;
                continue;
            }
            float available = state.resources.get(res);
            float toLoad = Math.min(available, ship.type.stackSize);
            state.resources.add(res, -toLoad);
            ship.cargo[i] = toLoad;
        }

        ship.state = Ship.State.TRAVELING;
        ship.travelTimer = 0f;
        return true;
    }

    // ── Tick
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Chiamato ogni frame da GameScreen.
     * Gestisce avanzamento viaggi, scarico e manutenzione.
     */
    public void tick(float delta) {
        for (Ship ship : fleet) {
            if (ship.state == Ship.State.TRAVELING) {
                ship.travelTimer += delta;
                if (ship.travelTimer >= ship.type.travelSecs) {
                    unload(ship);
                }
            }
        }
        // manutenzione al secondo, coerente con tickPopulation
        float maintenancePerSec = totalMaintenancePerMin() / 60f;
        if (maintenancePerSec > 0f) {
            state.resources.add("coin", -maintenancePerSec * delta);
        }
    }

    // ── Tick manutenzione (per HudRenderer — bilancio al minuto) ─────────────────

    /** Costo manutenzione totale flotta al minuto. */
    public float totalMaintenancePerMin() {
        float total = 0f;
        for (Ship ship : fleet)
            total += ship.type.maintenancePerMin;
        return total;
    }

    // ── Privati
    // ───────────────────────────────────────────────────────────────────

    private void unload(Ship ship) {
        // Scarica nel mondo di destinazione
        for (int i = 0; i < ship.type.slots; i++) {
            if (ship.cargo[i] > 0f && ship.assignedResources[i] != null) {
                state.resources.add(ship.assignedResources[i], ship.cargo[i]);
                ship.cargo[i] = 0f;
            }
        }

        // Inverte direzione e torna docked
        // (il giocatore può cambiare direzione/rotta mentre è docked)
        ship.state = Ship.State.DOCKED;
        ship.travelTimer = 0f;
    }

    private boolean hasPortInList(List<BuildingInstance> buildings, BuildingType portType) {
        for (BuildingInstance b : buildings)
            if (b.type == portType)
                return true;
        return false;
    }

    // ── Save / Load
    // ───────────────────────────────────────────────────────────────

    /** Serializza la flotta per il salvataggio. */
    public List<ShipSaveData> serialize() {
        List<ShipSaveData> list = new ArrayList<>();
        for (Ship ship : fleet) {
            ShipSaveData d = new ShipSaveData();
            d.id = ship.id;
            d.name = ship.name;
            d.typeName = ship.type.name();
            d.toNewWorld = ship.toNewWorld;
            d.stateName = ship.state.name();
            d.travelTimer = ship.travelTimer;
            d.assignedResources = ship.assignedResources.clone();
            d.cargo = ship.cargo.clone();
            list.add(d);
        }
        return list;
    }

    /** Ripristina la flotta da un salvataggio. */
    public void deserialize(List<ShipSaveData> data) {
        fleet.clear();
        if (data == null)
            return;
        for (ShipSaveData d : data) {
            try {
                ShipType type = ShipType.valueOf(d.typeName);
                Ship ship = new Ship(type, d.name, d.id);
                ship.toNewWorld = d.toNewWorld;
                ship.state = Ship.State.valueOf(d.stateName);
                ship.travelTimer = d.travelTimer;
                if (d.assignedResources != null)
                    System.arraycopy(d.assignedResources, 0, ship.assignedResources, 0,
                            Math.min(d.assignedResources.length, ship.assignedResources.length));
                if (d.cargo != null)
                    System.arraycopy(d.cargo, 0, ship.cargo, 0,
                            Math.min(d.cargo.length, ship.cargo.length));
                fleet.add(ship);
                if (ship.id >= nextId)
                    nextId = ship.id + 1;
            } catch (Exception e) {
                Gdx.app.error("TradeManager", "Failed to load ship: " + d.name, e);
            }
        }
    }

    // ── Inner class dati salvataggio
    // ──────────────────────────────────────────────

    public static class ShipSaveData {
        public int id;
        public String name;
        public String typeName;
        public boolean toNewWorld;
        public String stateName;
        public float travelTimer;
        public String[] assignedResources;
        public float[] cargo;
    }
}