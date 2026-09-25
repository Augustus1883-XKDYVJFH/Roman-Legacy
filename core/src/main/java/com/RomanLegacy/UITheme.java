package com.RomanLegacy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

/**
 * Centralized UI theme constants for consistent design across all screens.
 * Follows the Main Menu design language.
 */
public class UITheme {

    // ── Colors ──────────────────────────────────────────────────────────────
    public static final Color BG_DARK = Color.valueOf("0d0d14");
    public static final Color BG_PANEL = Color.valueOf("12121e");
    public static final Color BG_TOP_BAND = Color.valueOf("0a0a0f");
    public static final Color BG_DECORATIVE = Color.valueOf("0a0907");

    // Gold accent colors
    public static final Color GOLD_PRIMARY = Color.valueOf("c8a820");
    public static final Color GOLD_LIGHT = Color.valueOf("e8d080");
    public static final Color GOLD_DARK = Color.valueOf("8a6e15");

    // Text colors
    public static final Color TEXT_GOLD = Color.valueOf("e8d080");
    public static final Color TEXT_WHITE = Color.WHITE;
    public static final Color TEXT_GRAY = Color.valueOf("888888");
    public static final Color TEXT_DARK_GRAY = Color.valueOf("555555");

    // Status colors
    public static final Color STATUS_GREEN = Color.valueOf("55cc55");
    public static final Color STATUS_RED = Color.valueOf("cc5555");
    public static final Color STATUS_BLUE = Color.valueOf("4488cc");
    public static final Color STATUS_YELLOW = Color.valueOf("ccaa44");

    // Selection colors
    public static final Color SELECTION_BG = Color.valueOf("1e1e30");
    public static final Color SELECTION_BORDER = GOLD_PRIMARY;
    public static final Color HOVER_BG = Color.valueOf("333344");

    // ── Layout constants ────────────────────────────────────────────────────
    public static final float DECO_LINE_Y_OFFSET = -25f;
    public static final float TITLE_Y_OFFSET = -130f;
    public static final float MENU_START_Y_OFFSET = 75f;
    public static final float MENU_ITEM_SPACING = 58f;
    public static final float MENU_ITEM_WIDTH = 400f;

    // ── Animation ───────────────────────────────────────────────────────────
    public static final float FADE_DURATION = 0.3f;
    public static final float HOVER_SCALE = 1.02f;

    // ── Cornice retro condivisa ───────────────────────────────────────────────
    // Stesso look del HUD (HudRenderer.drawPanel): sfondo scuro del tema +
    // doppio bordo bronzo/oro. Ogni metodo gestisce il proprio begin/end, quindi
    // non chiamarli mentre un altro batch ShapeRenderer è già aperto.

    /** Riempie il pannello con lo sfondo del tema. */
    public static void fillPanel(ShapeRenderer sr, float x, float y, float w, float h) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(BG_PANEL.r, BG_PANEL.g, BG_PANEL.b, 0.97f);
        sr.rect(x, y, w, h);
        sr.end();
    }

    /** Disegna il doppio bordo bronzo/oro attorno al pannello. */
    public static void framePanel(ShapeRenderer sr, float x, float y, float w, float h) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(GOLD_DARK);
        sr.rect(x, y, w, h);
        sr.setColor(GOLD_PRIMARY);
        sr.rect(x + 3, y + 3, w - 6, h - 6);
        sr.end();
    }

    /** Sfondo + doppio bordo in un'unica chiamata. */
    public static void drawPanel(ShapeRenderer sr, float x, float y, float w, float h) {
        fillPanel(sr, x, y, w, h);
        framePanel(sr, x, y, w, h);
    }

    /**
     * Disegna una riga di menu in stile "card": sfondo, bordo, barra dorata se
     * selezionata. Condivisa da BaseMenuScreen e dal menu in-game di GameScreen.
     * Gestisce da sé begin()/end() di sr e batch: va chiamata quando nessun
     * altro batch/ShapeRenderer è già aperto.
     */
    public static void drawMenuRow(ShapeRenderer sr, SpriteBatch batch, BitmapFont font, GlyphLayout layout,
            String label, float centerX, float y, float width, float height,
            boolean selected, boolean hovered) {
        float x = centerX - width / 2f;
        float by = y - height / 2f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        if (selected) {
            sr.setColor(SELECTION_BG);
        } else if (hovered) {
            sr.setColor(HOVER_BG);
        } else {
            sr.setColor(BG_PANEL);
        }
        sr.rect(x, by, width, height);
        if (selected) {
            sr.setColor(GOLD_PRIMARY);
            sr.rect(x, by, 4f, height);
        }
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(selected ? SELECTION_BORDER : GOLD_DARK);
        sr.rect(x, by, width, height);
        sr.end();

        batch.begin();
        layout.setText(font, label);
        font.setColor(selected || hovered ? TEXT_GOLD : TEXT_GRAY);
        font.draw(batch, label, centerX - layout.width / 2f, y + 8f);
        batch.end();
    }
}