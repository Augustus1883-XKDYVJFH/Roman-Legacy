package com.RomanLegacy;

/**
 * Veicolo estetico che percorre una lista di celle di strada. Nessun effetto
 * sulla simulazione.
 */
public class Vehicle {

    public enum Kind {
        CART, STEAM_CARRIAGE, TRUCK
    }

    public final Kind kind;
    public final int[] pathX;
    public final int[] pathY;
    public final float speed; // celle al secondo
    public float dist = 0f; // celle percorse lungo il percorso
    public float x, y; // posizione in celle (centro del veicolo)
    public float angle = 0f; // gradi: 0 = destra, 90 = su, 180 = sinistra, 270 = giù

    public Vehicle(Kind kind, int[] pathX, int[] pathY, float speed) {
        this.kind = kind;
        this.pathX = pathX;
        this.pathY = pathY;
        this.speed = speed;
        this.x = pathX[0] + 0.5f;
        this.y = pathY[0] + 0.5f;
        updateAngle(0);
    }

    /**
     * Avanza lungo il percorso. Ritorna false se è arrivato o se la strada davanti
     * non esiste più.
     */
    public boolean advance(float delta, boolean[][] roadGrid) {
        dist += speed * delta;
        int last = pathX.length - 1;
        if (dist >= last)
            return false;

        int i = (int) dist;
        if (!roadGrid[pathY[i + 1]][pathX[i + 1]])
            return false; // strada demolita

        float t = dist - i;
        x = pathX[i] + 0.5f + (pathX[i + 1] - pathX[i]) * t;
        y = pathY[i] + 0.5f + (pathY[i + 1] - pathY[i]) * t;
        updateAngle(i);
        return true;
    }

    private void updateAngle(int i) {
        int dx = pathX[i + 1] - pathX[i];
        int dy = pathY[i + 1] - pathY[i];
        angle = dx > 0 ? 0f : (dy > 0 ? 90f : (dx < 0 ? 180f : 270f));
    }
}