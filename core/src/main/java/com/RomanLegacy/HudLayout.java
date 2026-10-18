package com.RomanLegacy;

import com.badlogic.gdx.Gdx;

/**
 * Rettangolo della mappa tra le barre dell'HUD (coordinate schermo, origine in
 * basso).
 */
public final class HudLayout {

    /** Larghezza colonna pulsanti a sinistra: salirà al passo 2. */
    public static final float LEFT_W = 0f;
    /** Larghezza barra ideologia a destra: salirà al passo 3. */
    public static final float RIGHT_W = 0f;

    private HudLayout() {
    }

    public static int mapX() {
        return (int) LEFT_W;
    }

    public static int mapY() {
        return HudRenderer.BOTBAR_H + HudRenderer.TAB_H;
    }

    public static int mapW() {
        return Math.max(1, Gdx.graphics.getWidth() - (int) (LEFT_W + RIGHT_W));
    }

    public static int mapH() {
        return Math.max(1, Gdx.graphics.getHeight() - HudRenderer.TOPBAR_H - mapY());
    }

    /**
     * mx, my con origine in basso: my = Gdx.graphics.getHeight() -
     * Gdx.input.getY().
     */
    public static boolean isOverMap(float mx, float my) {
        return mx >= mapX() && mx < mapX() + mapW()
                && my >= mapY() && my < mapY() + mapH();
    }
}