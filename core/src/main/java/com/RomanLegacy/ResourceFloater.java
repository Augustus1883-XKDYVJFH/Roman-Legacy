package com.RomanLegacy;

/**
 * Testo fluttuante "+N risorsa" che appare sopra un edificio quando produce,
 * sale lentamente e sfuma. Puramente visivo, nessun impatto sulla logica.
 */
public class ResourceFloater {

    public static final float DURATION = 1.4f;
    public static final float RISE_SPEED = 18f; // unità mondo/secondo

    public final String resourceId;
    public final int amount;
    public final float worldX;
    public float worldY;
    public float life = 0f;

    public ResourceFloater(String resourceId, int amount, float worldX, float worldY) {
        this.resourceId = resourceId;
        this.amount = amount;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    /** Da chiamare ogni frame con delta reale (non scalato da gameSpeed/pausa). */
    public void update(float delta) {
        life += delta;
        worldY += RISE_SPEED * delta;
    }

    public boolean isDead() {
        return life >= DURATION;
    }

    /** Alpha 1.0 finché non inizia la fase di dissolvenza, poi scende a 0. */
    public float alpha() {
        float fadeStart = DURATION * 0.55f;
        if (life < fadeStart)
            return 1f;
        return Math.max(0f, 1f - (life - fadeStart) / (DURATION - fadeStart));
    }
}