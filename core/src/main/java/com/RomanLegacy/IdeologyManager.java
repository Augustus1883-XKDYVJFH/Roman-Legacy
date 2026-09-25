package com.RomanLegacy;

/**
 * Gestisce il singolo asse ideologico Tradizione (-100) ↔ Evoluzione (+100).
 * Il valore viene modificato con shift FISSI e IRREVERSIBILI:
 * - al piazzamento di un edificio (axisShift di BuildingType)
 * - all'upgrade di una casa (axisShift del nuovo livello)
 * - in futuro da decreti ed eventi narrativi
 *
 * La demolizione di un edificio NON reverte lo shift: l'ideologia
 * rappresenta un cambiamento culturale permanente, non un bonus locale.
 */
public class IdeologyManager {

    public static final float MIN_VALUE = -100f;
    public static final float MAX_VALUE = 100f;

    private float value = 0f;

    public IdeologyManager() {
    }

    public IdeologyManager(float initialValue) {
        this.value = clamp(initialValue);
    }

    /** Applica uno shift fisso e irreversibile all'asse. */
    public void applyShift(float delta) {
        if (delta == 0f)
            return;
        float old = value;
        value = clamp(value + delta);
        if (old != value) {
            System.out.println("[IdeologyManager] Ideology: " + old + " -> " + value
                    + " (delta " + delta + ")");
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float v) {
        this.value = clamp(v);
    }

    /**
     * Etichetta puramente visiva.
     * Gli scaglioni numerici verranno riutilizzati dal sistema eventi.
     */
    public String label() {
        if (value >= 60)
            return "Revolutionary";
        if (value >= 20)
            return "Progressive";
        if (value >= -20)
            return "Balanced";
        if (value >= -60)
            return "Conservative";
        return "Traditionalist";
    }

    private static float clamp(float v) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, v));
    }
}