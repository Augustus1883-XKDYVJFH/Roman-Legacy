package com.RomanLegacy;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una singola nave della flotta del giocatore.
 *
 * Stati possibili:
 * DOCKED — ferma al porto, in attesa di partire
 * TRAVELING — in viaggio verso la destinazione
 *
 * Direzione:
 * toNewWorld = true → carica da OW, scarica in NW
 * toNewWorld = false → carica da NW, scarica in OW
 */
public class Ship {

    // ── Stato
    // ─────────────────────────────────────────────────────────────────────
    public enum State {
        DOCKED, TRAVELING
    }

    // ── Campi
    // ─────────────────────────────────────────────────────────────────────
    public final ShipType type;
    public String name;
    public boolean toNewWorld = true; // direzione del viaggio
    public State state = State.DOCKED;

    /**
     * Slot cargo: ogni elemento è l'id della risorsa selezionata (null = vuoto).
     */
    public final String[] assignedResources;

    /** Quantità attualmente caricata per ogni slot (0 se vuoto o in docking). */
    public final float[] cargo;

    /** Timer del viaggio corrente [0 … type.travelSecs]. */
    public float travelTimer = 0f;

    /** ID progressivo assegnato da TradeManager (usato per save/load). */
    public int id;

    // ── Costruttore
    // ───────────────────────────────────────────────────────────────

    public Ship(ShipType type, String name, int id) {
        this.type = type;
        this.name = name;
        this.id = id;
        this.assignedResources = new String[type.slots];
        this.cargo = new float[type.slots];
    }

    // ── Utilità
    // ───────────────────────────────────────────────────────────────────

    /** True se almeno uno slot ha una risorsa assegnata. */
    public boolean hasRoute() {
        for (String r : assignedResources)
            if (r != null && !r.isEmpty())
                return true;
        return false;
    }

    /** Progresso viaggio [0.0 … 1.0]. */
    public float travelProgress() {
        if (type.travelSecs <= 0)
            return 1f;
        return Math.min(1f, travelTimer / type.travelSecs);
    }

    /** Etichetta direzione leggibile. */
    public String directionLabel() {
        return toNewWorld ? "OW → NW" : "NW → OW";
    }

    /** Carico totale attualmente a bordo. */
    public float totalCargo() {
        float total = 0f;
        for (float c : cargo)
            total += c;
        return total;
    }
}