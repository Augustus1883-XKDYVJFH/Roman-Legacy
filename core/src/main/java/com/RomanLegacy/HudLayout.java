package com.RomanLegacy;

import com.badlogic.gdx.Gdx;

/**
 * Rettangolo della mappa tra le barre dell'HUD (coordinate schermo, origine in
 * basso).
 */
public final class HudLayout {

    /** Larghezza colonna pulsanti a sinistra: salirà al passo 2. */
    public static final float LEFT_W = 64f;
    /** Larghezza barra ideologia a destra: salirà al passo 3. */
    public static final float RIGHT_W = 28f;

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

    private static float boxX, boxY, boxW, boxH;

    /**
     * Rettangolo di un riquadro HUD sovrapposto alla mappa (escluso dai click sulla
     * mappa).
     */
    public static void setOverlayBox(float x, float y, float w, float h) {
        boxX = x;
        boxY = y;
        boxW = w;
        boxH = h;
    }

    /**
     * mx, my con origine in basso: my = Gdx.graphics.getHeight() -
     * Gdx.input.getY().
     */
    public static boolean isOverMap(float mx, float my) {
        boolean inMap = mx >= mapX() && mx < mapX() + mapW()
                && my >= mapY() && my < mapY() + mapH();
        boolean inBox = boxW > 0 && mx >= boxX && mx <= boxX + boxW
                && my >= boxY && my <= boxY + boxH;
        return inMap && !inBox;
    }
}