package com.RomanLegacy;

/**
 * Definisce i tipi di nave disponibili.
 * slots = numero di slot cargo (risorse trasportabili per viaggio)
 * stackSize = unità per slot
 * travelSecs = secondi per un viaggio OW→NW (o NW→OW)
 * cost = costo acquisto in monete
 * maintenance = costo in monete al minuto
 * oilOnly = true = usa Porto Petrolifero, false = usa Porto merci
 */
public enum ShipType {

    MERCANTILE(
            "Mercantile",
            "Nave da carico generica. Trasporta qualsiasi risorsa non petrolifera.",
            2, 100, 120f,
            2000f, 5f,
            false),
    PETROLIERA(
            "Petroliera",
            "Nave cisterna per il petrolio grezzo e i suoi derivati.",
            1, 100, 150f,
            3000f, 8f,
            true);

    // ── Campi
    // ─────────────────────────────────────────────────────────────────────
    public final String label;
    public final String description;
    public final int slots;
    public final int stackSize;
    public final float travelSecs;
    public final float purchaseCost;
    public final float maintenancePerMin;
    public final boolean oilOnly;

    ShipType(String label, String description,
            int slots, int stackSize, float travelSecs,
            float purchaseCost, float maintenancePerMin,
            boolean oilOnly) {
        this.label = label;
        this.description = description;
        this.slots = slots;
        this.stackSize = stackSize;
        this.travelSecs = travelSecs;
        this.purchaseCost = purchaseCost;
        this.maintenancePerMin = maintenancePerMin;
        this.oilOnly = oilOnly;
    }

    /** Porto richiesto per questa nave. */
    public BuildingType requiredPort() {
        return oilOnly ? BuildingType.OIL_DOCK : BuildingType.DOCK;
    }
}