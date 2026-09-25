package com.RomanLegacy;

/**
 * Difficoltà scelta all'evento iniziale THE_NEW_CITY.
 * Non tocca costi/tempi dei singoli edifici produttivi: agisce solo su
 * poche leve globali (reddito, manutenzione, monumenti), così da non dover
 * mantenere 3 varianti di ogni BuildingType.
 */
public enum Difficulty {

    EASY(1.15f, 0.85f, 0.85f, 0.85f),
    MEDIUM(1.00f, 1.00f, 1.00f, 1.00f),
    HARD(0.85f, 1.15f, 1.15f, 1.15f);

    /** Moltiplicatore tasse case (>1 = più reddito). */
    public final float incomeMultiplier;
    /** Moltiplicatore manutenzione edifici/flotta (>1 = costa di più). */
    public final float maintenanceMultiplier;
    /** Moltiplicatore costo upgrade monumenti (>1 = costa di più). */
    public final float monumentCostMultiplier;
    /** Moltiplicatore tempo upgrade monumenti (>1 = ci mette di più). */
    public final float monumentTimeMultiplier;

    Difficulty(float incomeMultiplier, float maintenanceMultiplier,
            float monumentCostMultiplier, float monumentTimeMultiplier) {
        this.incomeMultiplier = incomeMultiplier;
        this.maintenanceMultiplier = maintenanceMultiplier;
        this.monumentCostMultiplier = monumentCostMultiplier;
        this.monumentTimeMultiplier = monumentTimeMultiplier;
    }
}