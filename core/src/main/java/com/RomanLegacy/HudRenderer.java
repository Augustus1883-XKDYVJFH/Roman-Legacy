package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.RomanLegacy.NarrativeEvent;
import com.RomanLegacy.Main;
import com.RomanLegacy.BuildingInstance;
import com.RomanLegacy.BuildingType;
import com.RomanLegacy.BuildingType.ServiceCategory;
import com.RomanLegacy.Faction;
import com.RomanLegacy.FactionManager;
import com.RomanLegacy.GameInputHandler.ToolAction;
import com.RomanLegacy.ResourceManager;
import com.RomanLegacy.BuildingType.ConsumptionServiceCategory;

import java.util.*;

/**
 * Disegna tutta l'interfaccia utente: topbar, bottombar, pannelli, overlay.
 * Non gestisce input né logica di gioco.
 */
public class HudRenderer {
    public static final int TOPBAR_ROW_H = 35;
    public static final int TOPBAR_ROW1_H = 64;
    public static final int TOPBAR_H = TOPBAR_ROW1_H + TOPBAR_ROW_H * 3;
    public static final int TAB_H = 30;
    public static final int SLOT_W = 200;
    public static final int BOTBAR_H = 92;
    public static final int TOPBAR_RES = 8;
    public static final float TOPBTN_W = 84f;
    public static final float TOPBTN_H = 24f;
    public static final float ROTATE_BTN_X = 280f;
    public static final float ROTATE_BTN_W = 90f;
    public static final float ROTATE_BTN_H = 22f;

    private static final int GRID_COLS = 4;
    private static final int SERVICE_COLS = ServiceCategory.values().length
            + ConsumptionServiceCategory.values().length;
    private static final int WORKFORCE_COLS = BuildingType.SOCIAL_CLASS_NAMES.length;
    private static final int GRID_LABEL_MAXCHARS = 11;

    private static final float HEADER_H = 48f;
    private static final float MONUMENT_BOTTOM_RESERVE = 96f;
    private static final float PANEL_FOOTER_H = 34f;
    private static final float PANEL_FOOTER_GAP = 8f;
    private static final float PRODUCTION_BUTTON_ZONE_H = 54f;
    private static final float MONUMENT_BUTTON_ZONE_H = 32f;

    private static final float TOPBTN_GAP = 8f;
    private static final float TOPBTN_MARGIN_RIGHT = 20f;
    private static final float TOPBTN_MARGIN_TOP = 4f;

    private static final float HOUSE_BOTTOM_RESERVE = PANEL_FOOTER_H + PANEL_FOOTER_GAP + 8f;
    private static final float PRODUCTION_BOTTOM_RESERVE = PANEL_FOOTER_H + PANEL_FOOTER_GAP + PRODUCTION_BUTTON_ZONE_H;
    private static final float MONUMENT_BOTTOM_RESERVE_BTN = PANEL_FOOTER_H + PANEL_FOOTER_GAP + MONUMENT_BUTTON_ZONE_H;
    private static final float MONUMENT_BOTTOM_RESERVE_NOBTN = PANEL_FOOTER_H + PANEL_FOOTER_GAP + 8f;

    private static final float ICON_SIZE = 46f;
    private static final float CELL_W = 90f;
    private static final float CELL_H = 76f;
    private static final float SECTION_LABEL_H = 22f;
    private static final float HAIRLINE_GAP = 16f;
    private static final float BIG_STAT_H = 56f;
    private static final float CONTENT_TOP_PAD = 14f;
    private static final float BIG_STAT_VALUE_LABEL_GAP = 6f;
    private static final float BIG_STAT_BOTTOM_GAP = 10f;
    private static final float BOTBAR_ICON_SIZE = 34f;
    private static final float BOTBAR_ICON_TOP_PAD = 10f;
    private static final float BOTBAR_LABEL_Y = 16f;
    private static final float RES_ICON_SIZE = 36f;
    private static final float SATISFACTION_DOT_SIZE = 6f;
    private static final float FIELD_BUTTON_ZONE_H = 34f;

    private static final Color COL_TOPBAR_BG = UITheme.BG_TOP_BAND;
    private static final Color COL_BOTBAR_BG = UITheme.BG_DARK;
    private static final Color COL_SLOT_BG = UITheme.BG_PANEL;
    private static final Color COL_SLOT_SEL = UITheme.GOLD_PRIMARY;
    private static final Color COL_SLOT_HOVER = UITheme.HOVER_BG;
    private static final Color COL_TAB_ACTIVE = UITheme.GOLD_PRIMARY;
    private static final Color COL_TAB_BG = UITheme.BG_TOP_BAND;
    private static final Color COL_TEXT_GOLD = UITheme.TEXT_GOLD;
    private static final Color COL_TEXT_GRAY = UITheme.TEXT_GRAY;
    private static final Color COL_TEXT_GREEN = UITheme.STATUS_GREEN;
    private static final Color COL_TEXT_WHITE = Color.WHITE;

    // Celle cliccabili della riga recinti (ricalcolate a ogni frame in
    // drawProductionBlock)
    private final List<float[]> penHitRects = new ArrayList<>();
    private final List<BuildingType> penHitTypes = new ArrayList<>();
    private float[] fieldButtonRect = null;

    private static final String[] TAB_LABELS = { "Logistics", "Houses", "Services", "Extraction", "Industry",
            "Monumenti" };

    private final Main game;
    private final ShapeRenderer sr;
    private final SpriteBatch batch;
    private final OrthographicCamera uiCamera;

    public static float[] topBarButtonPos(int indexFromRight, float sw, float sh) {
        float x = sw - TOPBTN_MARGIN_RIGHT - (indexFromRight + 1) * TOPBTN_W - indexFromRight * TOPBTN_GAP;
        float y = sh - TOPBTN_MARGIN_TOP - TOPBTN_H;
        return new float[] { x, y };
    }

    public HudRenderer(Main game, ShapeRenderer sr, SpriteBatch batch, OrthographicCamera uiCamera) {
        this.game = game;
        this.sr = sr;
        this.batch = batch;
        this.uiCamera = uiCamera;
    }

    private void drawPanel(float x, float y, float w, float h) {
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_PANEL.r, UITheme.BG_PANEL.g, UITheme.BG_PANEL.b, 0.97f);
        sr.rect(x, y, w, h);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK);
        sr.rect(x, y, w, h);
        sr.setColor(UITheme.GOLD_PRIMARY);
        sr.rect(x + 3, y + 3, w - 6, h - 6);
        sr.end();
    }

    private void drawPanelHeaderFooter(BuildingInstance h, float px, float py, float pw, float panH) {
        // Header band
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_TOP_BAND.r, UITheme.BG_TOP_BAND.g, UITheme.BG_TOP_BAND.b, 0.9f);
        sr.rect(px + 3, py + panH - HEADER_H, pw - 6, HEADER_H - 3);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK);
        sr.line(px + 3, py + panH - HEADER_H, px + pw - 3, py + panH - HEADER_H);
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        game.fonts.large.setColor(COL_TEXT_GOLD);
        game.fonts.large.draw(batch, h.type.label, px + 16, py + panH - 15);
        game.fonts.normal.setColor(COL_TEXT_GRAY);
        game.fonts.normal.draw(batch, h.type.w + "x" + h.type.h + " cells", px + pw - 100, py + panH - 15);
        batch.end();

        // Footer band
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_TOP_BAND.r, UITheme.BG_TOP_BAND.g, UITheme.BG_TOP_BAND.b, 0.9f);
        sr.rect(px + 3, py + 3, pw - 6, PANEL_FOOTER_H);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK);
        sr.line(px + 3, py + 3 + PANEL_FOOTER_H, px + pw - 3, py + 3 + PANEL_FOOTER_H);
        sr.end();

        String hint = "[ESC] or click outside to close";
        batch.begin();
        game.fonts.small.setColor(COL_TEXT_GRAY);
        com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.fonts.small,
                hint);
        game.fonts.small.draw(batch, hint, px + pw / 2f - gl.width / 2f, py + 3 + PANEL_FOOTER_H / 2f + gl.height / 2f);
        batch.end();
    }

    private static class PanelLine {
        final com.badlogic.gdx.graphics.g2d.BitmapFont font;
        final String text;
        final Color color;
        final float lineHeight;

        PanelLine(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, Color color, float lineHeight) {
            this.font = font;
            this.text = text;
            this.color = color;
            this.lineHeight = lineHeight;
        }
    }

    private static class GridItem {
        float[] iconColor;
        String amountText;
        Color borderColor;
        String resourceId; // usato per cercare l'icona risorsa, null = nessuna
        String buildingId; // icona edificio (recinti): se non null ha la precedenza su resourceId
        Color textColor; // null = bianco

        GridItem(float[] iconColor, String amountText, Color borderColor, String resourceId) {
            this.iconColor = iconColor;
            this.amountText = amountText;
            this.borderColor = borderColor;
            this.resourceId = resourceId;
        }
    }

    private String spacedCaps(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            sb.append(Character.toUpperCase(s.charAt(i)));
            if (i < s.length() - 1)
                sb.append(' ');
        }
        return sb.toString();
    }

    private int rowsForCount(int count, int cols) {
        if (count <= 0)
            return 0;
        return (count + cols - 1) / cols;
    }

    private void drawTopBarButtons(GameInputHandler input, GameState state, float sw, float sh) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        String[] labels = { "MAP", "BALANCE", "FACTIONS" };
        boolean[] enabled = { state.newWorldUnlocked, true, true };
        boolean[] active = { false, false, input.showFactionPanel };

        for (int i = 0; i < labels.length; i++) {
            float[] pos = topBarButtonPos(i, sw, sh);
            float bx = pos[0], by = pos[1];
            boolean hover = enabled[i] && mx >= bx && mx <= bx + TOPBTN_W && my >= by && my <= by + TOPBTN_H;

            sr.setProjectionMatrix(uiCamera.combined);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            if (active[i])
                sr.setColor(0.22f, 0.18f, 0.05f, 1f);
            else if (hover)
                sr.setColor(COL_SLOT_HOVER);
            else
                sr.setColor(COL_SLOT_BG);
            sr.rect(bx, by, TOPBTN_W, TOPBTN_H);
            sr.end();

            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(enabled[i] ? (active[i] ? UITheme.GOLD_PRIMARY : UITheme.GOLD_DARK) : Color.valueOf("333333"));
            sr.rect(bx, by, TOPBTN_W, TOPBTN_H);
            sr.end();

            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                    game.fonts.small, labels[i]);
            Color textColor = !enabled[i] ? COL_TEXT_GRAY : (active[i] || hover ? COL_TEXT_GOLD : COL_TEXT_WHITE);
            game.fonts.small.setColor(textColor);
            game.fonts.small.draw(batch, labels[i], bx + TOPBTN_W / 2f - gl.width / 2f,
                    by + TOPBTN_H / 2f + gl.height / 2f);
            batch.end();
        }
    }

    /** Etichetta di sezione piccola, maiuscola, spaziata — stile minimal. */
    private float drawSectionLabel(String text, float px, float y) {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        game.fonts.small.setColor(UITheme.GOLD_DARK);
        game.fonts.small.draw(batch, spacedCaps(text), px + 16, y);
        batch.end();
        return y - SECTION_LABEL_H;
    }

    /** Linea sottile bronzo, usata per separare le sezioni. */
    private float drawHairline(float px, float pw, float y) {
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK.r, UITheme.GOLD_DARK.g, UITheme.GOLD_DARK.b, 0.5f);
        sr.line(px + 16, y, px + pw - 16, y);
        sr.end();
        return y - HAIRLINE_GAP;
    }

    private float measureBigStatHeight(String value, String label) {
        com.badlogic.gdx.graphics.g2d.GlyphLayout valLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                game.fonts.title, value);
        com.badlogic.gdx.graphics.g2d.GlyphLayout lblLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                game.fonts.small, spacedCaps(label));
        return valLayout.height + BIG_STAT_VALUE_LABEL_GAP + lblLayout.height + BIG_STAT_BOTTOM_GAP;
    }

    /**
     * Numero grande + etichetta piccola sotto, con spaziatura calcolata
     * sull'altezza reale del testo.
     */
    private float drawBigStat(String value, String label, float px, float pw, float topY) {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        com.badlogic.gdx.graphics.g2d.GlyphLayout valLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                game.fonts.title, value);
        game.fonts.title.setColor(COL_TEXT_GOLD);
        game.fonts.title.draw(batch, value, px + pw / 2f - valLayout.width / 2f, topY);

        String lbl = spacedCaps(label);
        float labelY = topY - valLayout.height - BIG_STAT_VALUE_LABEL_GAP;
        com.badlogic.gdx.graphics.g2d.GlyphLayout lblLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                game.fonts.small, lbl);
        game.fonts.small.setColor(COL_TEXT_GRAY);
        game.fonts.small.draw(batch, lbl, px + pw / 2f - lblLayout.width / 2f, labelY);

        batch.end();
        return labelY - lblLayout.height - BIG_STAT_BOTTOM_GAP;
    }

    private float measureLines(List<PanelLine> lines) {
        float h = 0f;
        for (PanelLine l : lines)
            h += l.lineHeight;
        return h;
    }

    private float drawLines(List<PanelLine> lines, float px, float startY) {
        float y = startY;
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        for (PanelLine l : lines) {
            l.font.setColor(l.color);
            l.font.draw(batch, l.text, px + 16, y);
            y -= l.lineHeight;
        }
        batch.end();
        return y;
    }

    private Texture iconFor(GridItem item) {
        if (game.icons == null)
            return null;
        if (item.buildingId != null)
            return game.icons.getBuildingIcon(item.buildingId);
        return game.icons.getResourceIcon(item.resourceId);
    }

    /** Rettangolo cliccabile (cella intera) dell'item i di una griglia. */
    private float[] gridCellRect(int i, float px, float pw, float topY) {
        float startX = px + (pw - GRID_COLS * CELL_W) / 2f;
        int col = i % GRID_COLS;
        int row = i / GRID_COLS;
        return new float[] { startX + col * CELL_W, topY - (row + 1) * CELL_H, CELL_W, CELL_H };
    }

    private float drawResourceGrid(List<GridItem> items, float px, float pw, float topY) {
        return drawResourceGrid(items, px, pw, topY, false);
    }

    private float drawResourceGrid(List<GridItem> items, float px, float pw, float topY, boolean thickBorder) {
        if (items.isEmpty())
            return topY;

        int rows = rowsForCount(items.size(), GRID_COLS);
        float gridW = GRID_COLS * CELL_W;
        float startX = px + (pw - gridW) / 2f;

        sr.setProjectionMatrix(uiCamera.combined);
        for (int i = 0; i < items.size(); i++) {
            GridItem item = items.get(i);
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            float cx = startX + col * CELL_W + (CELL_W - ICON_SIZE) / 2f;
            float cy = topY - row * CELL_H - ICON_SIZE;

            Texture icon = iconFor(item);

            if (icon != null) {
                batch.setProjectionMatrix(uiCamera.combined);
                batch.begin();
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(icon, cx, cy, ICON_SIZE, ICON_SIZE);
                batch.end();
            } else {
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(item.iconColor[0] * 0.85f, item.iconColor[1] * 0.85f, item.iconColor[2] * 0.85f, 1f);
                sr.rect(cx, cy, ICON_SIZE, ICON_SIZE);
                sr.end();
            }

            if (thickBorder) {
                float t = 3f;
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(item.borderColor);
                sr.rect(cx - t, cy + ICON_SIZE, ICON_SIZE + t * 2, t); // sopra
                sr.rect(cx - t, cy - t, ICON_SIZE + t * 2, t); // sotto
                sr.rect(cx - t, cy, t, ICON_SIZE); // sinistra
                sr.rect(cx + ICON_SIZE, cy, t, ICON_SIZE); // destra
                sr.end();
            } else {
                sr.begin(ShapeRenderer.ShapeType.Line);
                sr.setColor(item.borderColor);
                sr.rect(cx, cy, ICON_SIZE, ICON_SIZE);
                sr.end();
            }
        }

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        for (int i = 0; i < items.size(); i++) {
            GridItem item = items.get(i);
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            float cx = startX + col * CELL_W + CELL_W / 2f;
            float cy = topY - row * CELL_H - ICON_SIZE - 8f;

            gl.setText(game.fonts.small, item.amountText);
            game.fonts.small.setColor(item.textColor != null ? item.textColor : COL_TEXT_WHITE);
            game.fonts.small.draw(batch, item.amountText, cx - gl.width / 2f, cy);
        }
        batch.end();

        return topY - rows * CELL_H;
    }

    /** Lista compatta a due colonne: puntino colore + etichetta + percentuale. */
    private float drawCompactList(List<String> labels, List<Float> values, List<Color> colors,
            float px, float pw, float topY) {
        if (labels.isEmpty())
            return topY;

        int cols = 2;
        float colW = (pw - 32f) / cols;
        int rows = rowsForCount(labels.size(), cols);
        float rowH = 26f;

        sr.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        for (int i = 0; i < labels.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            float x = px + 16 + col * colW;
            float y = topY - row * rowH;

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(colors.get(i));
            sr.rect(x, y - 8f, 7f, 7f);
            sr.end();

            batch.begin();
            game.fonts.normal.setColor(COL_TEXT_GRAY);
            game.fonts.normal.draw(batch, labels.get(i) + "  " + (int) (values.get(i) * 100) + "%", x + 14f, y);
            batch.end();
        }

        return topY - rows * rowH;
    }

    private float buttonZoneBottom(float py) {
        return py + PANEL_FOOTER_H + PANEL_FOOTER_GAP;
    }

    private List<GridItem> buildResourceGridItems(Map<String, Float> resMap, GameState state,
            boolean checkAvailability) {
        List<GridItem> items = new ArrayList<>();
        if (resMap == null)
            return items;
        for (Map.Entry<String, Float> e : resMap.entrySet()) {
            ResourceManager.Resource res = state.resources.getAll().get(e.getKey());
            float[] color = res != null ? res.color : new float[] { 0.6f, 0.6f, 0.6f };
            boolean ok = !checkAvailability || state.resources.get(e.getKey()) >= e.getValue();
            Color border = checkAvailability
                    ? (ok ? Color.valueOf("55cc55") : Color.valueOf("cc4444"))
                    : UITheme.GOLD_DARK;
            String amount = (int) (float) e.getValue() + "x";
            items.add(new GridItem(color, amount, border, e.getKey()));
        }
        return items;
    }

    private List<GridItem> buildHouseNeedsGridItems(BuildingInstance h, GameState state) {
        List<GridItem> items = new ArrayList<>();
        if (h.type.houseNeeds == null)
            return items;
        for (String need : h.type.houseNeeds) {
            ResourceManager.Resource res = state.resources.getAll().get(need);
            float[] color = res != null ? res.color : new float[] { 0.6f, 0.6f, 0.6f };
            boolean ok = state.resources.get(need) >= 0.05f;
            Color border = ok ? Color.valueOf("55cc55") : Color.valueOf("cc4444");
            items.add(new GridItem(color, "", border, need));
        }
        return items;
    }

    // ── CASA ─────────────────────────────────────────────────────────────────
    private static class HouseBlock {
        List<GridItem> needsGrid;
        List<String> serviceLabels = new ArrayList<>();
        List<Float> serviceValues = new ArrayList<>();
        List<Color> serviceColors = new ArrayList<>();
        PanelLine graceLine;
        PanelLine statusLine;
        int cap, pop;
    }

    private HouseBlock buildHouseBlock(BuildingInstance h, GameState state) {
        HouseBlock b = new HouseBlock();
        b.cap = h.type.houseCap();
        float housePopClamped = Math.min(b.cap, Math.max(0f, h.housePop));
        b.pop = (int) housePopClamped;
        float graceCap = b.cap * 0.30f;
        boolean inGrace = (int) housePopClamped < (int) graceCap;

        if (inGrace) {
            b.graceLine = new PanelLine(game.fonts.normal, "Minimum population: " + (int) graceCap,
                    Color.valueOf("88cc88"), 24f);
        }

        for (ServiceCategory cat : ServiceCategory.values()) {
            float s = state.getServiceSatisfaction(cat);
            b.serviceLabels.add(cat.label); // prima: cat.label + " " + cap + "/" + cons
            b.serviceValues.add(s);
            b.serviceColors
                    .add(s >= 0.9f ? Color.valueOf("55cc55") : s >= 0.7f ? UITheme.STATUS_YELLOW : UITheme.STATUS_RED);
        }
        for (ConsumptionServiceCategory cat : ConsumptionServiceCategory.values()) {
            float s = state.getConsumptionServiceSatisfaction(cat);
            b.serviceLabels.add(cat.label);
            b.serviceValues.add(s);
            b.serviceColors
                    .add(s >= 0.9f ? Color.valueOf("55cc55") : s >= 0.7f ? UITheme.STATUS_YELLOW : UITheme.STATUS_RED);
        }

        b.needsGrid = buildHouseNeedsGridItems(h, state);

        boolean canUp = h.canUpgrade(state.resources);
        String statusText;
        Color statusColor;
        if (h.type.nextHouseLevel() == null) {
            statusText = "Maximum level";
            statusColor = COL_TEXT_GRAY;
        } else if (canUp) {
            statusText = "Ready for upgrade";
            statusColor = Color.valueOf("55cc55");
        } else {
            statusText = b.pop < b.cap ? "Population required (" + b.pop + "/" + b.cap + ")" : "Needs unfulfilled";
            statusColor = COL_TEXT_GRAY;
        }
        b.statusLine = new PanelLine(game.fonts.large, statusText, statusColor, 30f);
        return b;
    }

    private float measureHouseBlock(HouseBlock b) {
        float h = measureBigStatHeight("0 / 0", "Inhabitants") + 20f;
        if (b.graceLine != null)
            h += b.graceLine.lineHeight;
        h += HAIRLINE_GAP + SECTION_LABEL_H;
        h += rowsForCount(b.needsGrid.size(), GRID_COLS) * CELL_H;
        h += HAIRLINE_GAP + SECTION_LABEL_H;
        h += rowsForCount(b.serviceLabels.size(), 2) * 26f;
        h += HAIRLINE_GAP;
        h += b.statusLine.lineHeight;
        return h;
    }

    private void drawCenteredButtonLabel(String text, com.badlogic.gdx.graphics.g2d.BitmapFont font,
            Color color, float boxX, float boxBottom, float boxW, float boxH) {
        com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, text);
        font.setColor(color);
        float tx = boxX + boxW / 2f - gl.width / 2f;
        float ty = boxBottom + (boxH + gl.height) / 2f;
        font.draw(batch, text, tx, ty);
    }

    private void drawHouseBlock(BuildingInstance h, HouseBlock b, float px, float pw, float topY) {
        float y = topY;
        y = drawBigStat(b.pop + " / " + b.cap, "Inhabitants", px, pw, y);

        float barY = y - 4f;
        float barW = pw - 32f;
        float fill = b.cap > 0 ? (h.housePop / (float) b.cap) : 0f;
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.2f, 0.2f, 0.2f, 1f);
        sr.rect(px + 16, barY, barW, 8);
        sr.setColor(0.3f, 0.8f, 0.3f, 1f);
        sr.rect(px + 16, barY, barW * fill, 8);
        sr.setColor(1f, 0.8f, 0.2f, 0.8f);
        sr.rect(px + 16 + barW * 0.30f - 1, barY, 2, 8);
        sr.end();
        y = barY - 16f;

        if (b.graceLine != null) {
            y = drawLines(List.of(b.graceLine), px, y);
        }

        y = drawHairline(px, pw, y);
        y = drawSectionLabel("Needs", px, y);
        y = drawResourceGrid(b.needsGrid, px, pw, y, true);

        y = drawHairline(px, pw, y);
        y = drawSectionLabel("Services", px, y);
        y = drawCompactList(b.serviceLabels, b.serviceValues, b.serviceColors, px, pw, y);

        y = drawHairline(px, pw, y);
        drawLines(List.of(b.statusLine), px, y);
    }

    // ── SERVIZIO A CAPACITÀ ──────────────────────────────────────────────────
    private static class ServiceBlock {
        ServiceCategory cat;
        PanelLine statusLine;
        String capacityText;
        List<PanelLine> cityLines = new ArrayList<>();
    }

    private ServiceBlock buildServiceBlock(BuildingInstance h, GameState state) {
        ServiceBlock b = new ServiceBlock();
        b.cat = h.type.serviceCategory;

        // Stessa regola di GameState.updateServiceCapacity(): 30% se in pausa o
        // scollegato
        boolean reduced = h.paused || !h.connected;
        float effective = h.type.serviceCapacity * (reduced ? 0.3f : 1f);
        b.capacityText = String.valueOf((int) effective);

        String statusText;
        Color statusColor;
        if (!h.connected) {
            statusText = "Not connected: capacity at 30%";
            statusColor = Color.valueOf("cc8844");
        } else if (h.paused) {
            statusText = "Paused: capacity at 30%";
            statusColor = Color.valueOf("cc8844");
        } else {
            statusText = "Active";
            statusColor = Color.valueOf("55cc55");
        }
        b.statusLine = new PanelLine(game.fonts.normal, statusText, statusColor, 26f);

        float cap = state.getServiceCapacity(b.cat);
        float cons = state.getServiceConsumption(b.cat);
        float s = state.getServiceSatisfaction(b.cat);
        Color sColor = s >= 0.9f ? Color.valueOf("55cc55") : s >= 0.7f ? UITheme.STATUS_YELLOW : UITheme.STATUS_RED;

        b.cityLines.add(new PanelLine(game.fonts.normal, "City capacity: " + (int) cap, COL_TEXT_WHITE, 24f));
        b.cityLines.add(new PanelLine(game.fonts.normal, "City demand: " + (int) cons, COL_TEXT_WHITE, 24f));
        b.cityLines.add(new PanelLine(game.fonts.normal, "Coverage: " + (int) (s * 100) + "%", sColor, 24f));
        return b;
    }

    private float measureServiceBlock(ServiceBlock b) {
        float h = b.statusLine.lineHeight;
        h += measureBigStatHeight(b.capacityText, "Capacity");
        h += HAIRLINE_GAP + SECTION_LABEL_H;
        h += measureLines(b.cityLines);
        return h;
    }

    private void drawServiceBlock(ServiceBlock b, float px, float pw, float topY) {
        float y = drawLines(List.of(b.statusLine), px, topY);
        y = drawBigStat(b.capacityText, "Capacity", px, pw, y);
        y = drawHairline(px, pw, y);
        y = drawSectionLabel(b.cat.label, px, y);
        drawLines(b.cityLines, px, y);
    }

    // ── PRODUZIONE ───────────────────────────────────────────────────────────
    private static class ProductionBlock {
        PanelLine statusLine;
        List<GridItem> penGrid; // null se l'edificio non ha recinti
        List<BuildingType> penTypes;
        List<GridItem> inputGrid;
        List<GridItem> outputGrid; // null se consumableOnly
        PanelLine outputLine; // solo consumableOnly: "Cycle every Xs"
        List<PanelLine> powerLines = new ArrayList<>();
        PanelLine cycleLine;
        boolean hasInput;
        boolean hasFieldButton;
    }

    private ProductionBlock buildProductionBlock(BuildingInstance h, GameState state) {
        ProductionBlock b = new ProductionBlock();

        String statusText = h.paused ? "Paused" : "Producing";
        Color statusColor = h.paused ? Color.valueOf("cc8844")
                : (h.type.consumableOnly ? Color.valueOf("55aacc") : Color.valueOf("55cc55"));
        b.statusLine = new PanelLine(game.fonts.normal, statusText, statusColor, 26f);

        // Recinti: una cella per tipo, con stato (costruito / costruibile / non
        // permesso)
        if (h.type.hasPens()) {
            b.penGrid = new ArrayList<>();
            b.penTypes = new ArrayList<>();
            for (String penId : h.type.penIds) {
                BuildingType pen = BuildingType.valueOf(penId);
                boolean built = state.hasPen(h, penId);
                boolean affordable = state.resources.canAfford(pen.cost);
                Color border = built ? Color.valueOf("55cc55")
                        : (affordable ? UITheme.GOLD_PRIMARY : Color.valueOf("cc4444"));
                Color textColor = built ? Color.valueOf("55cc55")
                        : (affordable ? COL_TEXT_GOLD : Color.valueOf("cc4444"));
                Color c = Color.valueOf(pen.colorHex);
                GridItem item = new GridItem(new float[] { c.r, c.g, c.b }, built ? "Built" : "Build", border, null);
                item.buildingId = pen.id;
                item.textColor = textColor;
                b.penGrid.add(item);
                b.penTypes.add(pen);
            }
        }

        if (h.type.hasFields()) {
            int count = state.countFields(h);
            int cap = h.type.fieldCap;
            b.powerLines.add(new PanelLine(game.fonts.small,
                    "Fields: " + count + "/" + cap + (count < cap ? "  (click below to plant)" : ""),
                    count >= cap ? Color.valueOf("55cc55") : Color.valueOf("cc8844"), 18f));
            b.hasFieldButton = count < cap;
        }

        b.hasInput = h.type.input != null && !h.type.input.isEmpty();
        if (b.hasInput) {
            b.inputGrid = buildResourceGridItems(h.type.input, state, true);
        }

        boolean producesResource = !h.type.consumableOnly && h.type.prod != null;
        if (producesResource) {
            b.outputGrid = buildResourceGridItems(
                    Map.of(h.type.prod, (float) h.type.rate), state, false);
        } else if (h.type.consumableOnly) {
            b.outputLine = new PanelLine(game.fonts.normal, "Cycle every " + h.type.cycleTime + "s",
                    COL_TEXT_GRAY, 22f);
        }

        if (h.type.requiredPower == BuildingType.PowerType.STEAM
                || h.type.requiredPower == BuildingType.PowerType.BOTH) {
            b.powerLines.add(new PanelLine(game.fonts.small, h.hasSteam ? "Steam: active" : "Steam: missing",
                    h.hasSteam ? Color.valueOf("88aaff") : Color.valueOf("ff8888"), 18f));
        }
        if (h.type.requiredPower == BuildingType.PowerType.ELECTRICITY
                || h.type.requiredPower == BuildingType.PowerType.BOTH) {
            b.powerLines.add(
                    new PanelLine(game.fonts.small, h.hasElectricity ? "Electricity: active" : "Electricity: missing",
                            h.hasElectricity ? Color.valueOf("ffff88") : Color.valueOf("ff8888"), 18f));
        }

        if (h.type.hasPens()) {
            int pct = (int) (h.penEfficiency * 100);
            b.powerLines.add(new PanelLine(game.fonts.small, "Efficiency: " + pct + "%",
                    pct >= 100 ? Color.valueOf("55cc55") : Color.valueOf("cc8844"), 18f));
        }

        String cycleText = "Cycle: " + (int) (h.progress * 100) + "%";
        if (producesResource)
            cycleText += "  •  " + h.type.cycleTime + "s";
        b.cycleLine = new PanelLine(game.fonts.small, cycleText,
                h.paused ? Color.valueOf("cc8844") : COL_TEXT_GRAY, 20f);

        return b;
    }

    private float measureProductionBlock(ProductionBlock b) {
        float h = b.statusLine.lineHeight;
        if (b.penGrid != null) {
            h += HAIRLINE_GAP + SECTION_LABEL_H;
            h += rowsForCount(b.penGrid.size(), GRID_COLS) * CELL_H;
        }
        if (b.hasInput) {
            h += HAIRLINE_GAP + SECTION_LABEL_H;
            h += rowsForCount(b.inputGrid.size(), GRID_COLS) * CELL_H;
        }
        if (b.outputGrid != null) {
            h += HAIRLINE_GAP + SECTION_LABEL_H;
            h += rowsForCount(b.outputGrid.size(), GRID_COLS) * CELL_H;
        } else if (b.outputLine != null) {
            h += 8f + b.outputLine.lineHeight;
        }
        for (PanelLine p : b.powerLines)
            h += p.lineHeight;
        h += 8f + b.cycleLine.lineHeight;
        return h;
    }

    private void drawProductionBlock(ProductionBlock b, float px, float pw, float topY) {
        float y = topY;
        y = drawLines(List.of(b.statusLine), px, y);

        if (b.penGrid != null) {
            y = drawHairline(px, pw, y);
            y = drawSectionLabel("Pens", px, y);
            penHitRects.clear();
            penHitTypes.clear();
            for (int i = 0; i < b.penGrid.size(); i++) {
                penHitRects.add(gridCellRect(i, px, pw, y));
                penHitTypes.add(b.penTypes.get(i));
            }
            y = drawResourceGrid(b.penGrid, px, pw, y, true);
        }

        if (b.hasInput) {
            y = drawHairline(px, pw, y);
            y = drawSectionLabel("Input", px, y);
            y = drawResourceGrid(b.inputGrid, px, pw, y);
        }

        if (b.outputGrid != null) {
            y = drawHairline(px, pw, y);
            y = drawSectionLabel("Output", px, y);
            y = drawResourceGrid(b.outputGrid, px, pw, y);
            y -= 8f;
        } else if (b.outputLine != null) {
            y -= 8f;
            y = drawLines(List.of(b.outputLine), px, y);
        }

        List<PanelLine> tail = new ArrayList<>(b.powerLines);
        tail.add(b.cycleLine);
        drawLines(tail, px, y);
    }

    private float drawIconLine(String resourceId, String text, Color color, float px, float y) {
        float iconSize = 20f;
        Texture icon = game.icons != null ? game.icons.getResourceIcon(resourceId) : null;

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        float textX = px + 16;
        if (icon != null) {
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(icon, px + 16, y - iconSize + 4f, iconSize, iconSize);
            textX = px + 16 + iconSize + 6f;
        }
        game.fonts.normal.setColor(color);
        game.fonts.normal.draw(batch, text, textX, y);
        batch.end();

        return y - 26f;
    }

    private void drawProductionBarAndButton(BuildingInstance h, float px, float py, float pw) {
        float base = buttonZoneBottom(py);
        float barW = pw - 32f;
        float bby = base + 26f + 6f;

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.15f, 0.15f, 0.15f, 1f);
        sr.rect(px + 16, bby, barW, 8);
        if (h.type.consumableOnly && !h.paused) {
            sr.setColor(0.35f, 0.55f, 0.75f, 1f);
        } else if (h.paused) {
            sr.setColor(0.70f, 0.45f, 0.10f, 1f);
        } else {
            sr.setColor(0.2f, 0.75f, 0.2f, 1f);
        }
        sr.rect(px + 16, bby, barW * h.progress, 8);
        sr.end();

        if (h.type.input != null || h.type.prod != null) {
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(UITheme.GOLD_DARK);
            sr.rect(px + 16, base, pw - 32, 26);
            sr.end();

            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            String btnLabel = h.paused ? "Resume" : "Pause";
            drawCenteredButtonLabel(btnLabel, game.fonts.normal, COL_TEXT_GOLD, px + 16, base, pw - 32, 26);
            batch.end();
        }
    }

    // ── MONUMENTO ────────────────────────────────────────────────────────────
    private static class MonumentBlock {
        PanelLine statusLine;
        PanelLine progressLine;
        List<GridItem> costGrid;
        boolean hasButton;
    }

    private MonumentBlock buildMonumentBlock(BuildingInstance h, GameState state) {
        MonumentBlock b = new MonumentBlock();

        if (h.type.isLastMonumentLevel()) {
            b.statusLine = new PanelLine(game.fonts.normal, "Completed", Color.valueOf("55cc55"), 24f);
            if (h.type.influenceRadius > 0) {
                b.progressLine = new PanelLine(game.fonts.small,
                        "Influence radius: " + h.type.influenceRadius + " cells",
                        COL_TEXT_GRAY, 20f);
            }
        } else if (h.monumentUpgrading) {
            b.statusLine = new PanelLine(game.fonts.normal, "Under construction", Color.valueOf("4488cc"), 24f);
            float base = h.type.upgradeTime > 0 ? h.type.upgradeTime : 60f;
            float required = base * state.difficulty.monumentTimeMultiplier;
            int percent = required > 0 ? (int) (h.monumentUpgradeProgress / required * 100) : 0;
            b.progressLine = new PanelLine(game.fonts.small, "Progress: " + percent + "%", COL_TEXT_GRAY, 20f);
        } else {
            b.statusLine = new PanelLine(game.fonts.normal,
                    "Phase " + (h.type.monumentLevel + 1) + " / " + h.type.getMonumentChainLength(),
                    Color.valueOf("e8d080"), 24f);
            BuildingType next = h.type.nextMonumentLevel();
            if (next != null && next.upgradeCost != null && !next.upgradeCost.isEmpty()) {
                Map<String, Float> scaledCost = new HashMap<>();
                float mult = state.difficulty.monumentCostMultiplier;
                for (Map.Entry<String, Float> e : next.upgradeCost.entrySet()) {
                    scaledCost.put(e.getKey(), e.getValue() * mult);
                }
                b.costGrid = buildResourceGridItems(scaledCost, state, true);
            }
            b.hasButton = true;
        }
        return b;
    }

    private float measureMonumentBlock(MonumentBlock b) {
        float h = b.statusLine.lineHeight;
        if (b.progressLine != null)
            h += b.progressLine.lineHeight;
        if (b.costGrid != null && !b.costGrid.isEmpty()) {
            h += HAIRLINE_GAP + SECTION_LABEL_H;
            h += rowsForCount(b.costGrid.size(), GRID_COLS) * CELL_H;
        }
        return h;
    }

    private void drawMonumentBlock(MonumentBlock b, float px, float pw, float topY) {
        List<PanelLine> head = new ArrayList<>();
        head.add(b.statusLine);
        if (b.progressLine != null)
            head.add(b.progressLine);
        float y = drawLines(head, px, topY);

        if (b.costGrid != null && !b.costGrid.isEmpty()) {
            y = drawHairline(px, pw, y);
            y = drawSectionLabel("Next phase cost", px, y);
            drawResourceGrid(b.costGrid, px, pw, y);
        }
    }

    private void drawMonumentButton(float px, float py, float pw) {
        float btnY = buttonZoneBottom(py);
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.valueOf("55cc55"));
        sr.rect(px + 16, btnY, pw - 32, 24);
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        drawCenteredButtonLabel("Build next phase", game.fonts.normal, Color.valueOf("55cc55"),
                px + 16, btnY, pw - 32, 24);
        batch.end();
    }

    private void drawFieldButton(float px, float py, float pw) {
        float base = buttonZoneBottom(py) + PRODUCTION_BUTTON_ZONE_H;
        float btnY = base + 4f;
        float btnH = 24f;
        fieldButtonRect = new float[] { px + 16, btnY, pw - 32, btnH };

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_PRIMARY);
        sr.rect(fieldButtonRect[0], fieldButtonRect[1], fieldButtonRect[2], fieldButtonRect[3]);
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        drawCenteredButtonLabel("Plant fields", game.fonts.normal, UITheme.GOLD_PRIMARY,
                fieldButtonRect[0], fieldButtonRect[1], fieldButtonRect[2], fieldButtonRect[3]);
        batch.end();
    }

    // ── ORCHESTRAZIONE ───────────────────────────────────────────────────────
    public void drawBuildingPanel(GameInputHandler input, GameState state) {
        fieldButtonRect = null;
        penHitRects.clear();
        penHitTypes.clear();
        if (!input.showBuildingPanel || input.inspectedBuilding == null)
            return;

        BuildingInstance h = input.inspectedBuilding;
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float pw = h.type.isHouse ? 480f : 420f;

        float panH;
        HouseBlock houseBlock = null;
        ProductionBlock prodBlock = null;
        MonumentBlock monBlock = null;
        ServiceBlock svcBlock = null;

        if (h.type.isHouse) {
            houseBlock = buildHouseBlock(h, state);
            panH = HEADER_H + CONTENT_TOP_PAD + measureHouseBlock(houseBlock)
                    + PANEL_FOOTER_H + PANEL_FOOTER_GAP + 8f;
        } else if (h.type.prod != null || h.type.consumableOnly) {
            prodBlock = buildProductionBlock(h, state);
            float btnZone = PRODUCTION_BUTTON_ZONE_H + (prodBlock.hasFieldButton ? FIELD_BUTTON_ZONE_H : 0f);
            panH = HEADER_H + CONTENT_TOP_PAD + measureProductionBlock(prodBlock)
                    + PANEL_FOOTER_H + PANEL_FOOTER_GAP + btnZone;
        } else if (h.type.isMonument) {
            monBlock = buildMonumentBlock(h, state);
            float btnZone = monBlock.hasButton ? MONUMENT_BUTTON_ZONE_H : 8f;
            panH = HEADER_H + CONTENT_TOP_PAD + measureMonumentBlock(monBlock)
                    + PANEL_FOOTER_H + PANEL_FOOTER_GAP + btnZone;
        } else if (h.type.serviceCategory != null) {
            svcBlock = buildServiceBlock(h, state);
            panH = HEADER_H + CONTENT_TOP_PAD + measureServiceBlock(svcBlock)
                    + PANEL_FOOTER_H + PANEL_FOOTER_GAP + 8f;
        } else {
            panH = HEADER_H + CONTENT_TOP_PAD + 40f + PANEL_FOOTER_H + PANEL_FOOTER_GAP;
        }

        float px = (sw - pw) / 2f;
        float py = (sh - panH) / 2f;

        input.panelX = px;
        input.panelY = py;
        input.panelW = pw;
        input.panelH = panH;

        drawPanel(px, py, pw, panH);
        drawPanelHeaderFooter(h, px, py, pw, panH);

        float topY = py + panH - HEADER_H - CONTENT_TOP_PAD;

        if (houseBlock != null) {
            drawHouseBlock(h, houseBlock, px, pw, topY);
        } else if (prodBlock != null) {
            drawProductionBlock(prodBlock, px, pw, topY);
            drawProductionBarAndButton(h, px, py, pw);
            if (prodBlock.hasFieldButton)
                drawFieldButton(px, py, pw);
            drawPenTooltip(state);
        } else if (monBlock != null) {
            drawMonumentBlock(monBlock, px, pw, topY);
            if (monBlock.hasButton)
                drawMonumentButton(px, py, pw);
        } else if (svcBlock != null) {
            drawServiceBlock(svcBlock, px, pw, topY);
        } else {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            game.fonts.normal.setColor(COL_TEXT_GRAY);
            game.fonts.normal.draw(batch, "No production", px + 16, topY);
            batch.end();
        }

        handlePanelClicks(input, state, px, py, pw, panH, h);
    }

    private void handlePanelClicks(GameInputHandler input, GameState state,
            float px, float py, float pw, float panH, BuildingInstance h) {
        if (input.panelJustOpened) {
            input.panelJustOpened = false;
            return;
        }
        if (!Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT))
            return;

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Recinti: click su una cella libera e permessa -> avvia il piazzamento
        if (h.type.hasPens()) {
            for (int i = 0; i < penHitRects.size(); i++) {
                float[] r = penHitRects.get(i);
                if (mx >= r[0] && mx <= r[0] + r[2] && my >= r[1] && my <= r[1] + r[3]) {
                    BuildingType pen = penHitTypes.get(i);
                    if (!state.hasPen(h, pen.id) && state.resources.canAfford(pen.cost))
                        input.startPenPlacement(h, pen);
                    return;
                }
            }
        }

        if (h.type.hasFields() && fieldButtonRect != null
                && mx >= fieldButtonRect[0] && mx <= fieldButtonRect[0] + fieldButtonRect[2]
                && my >= fieldButtonRect[1] && my <= fieldButtonRect[1] + fieldButtonRect[3]) {
            if (state.countFields(h) < h.type.fieldCap)
                input.startFieldPlacement(h, BuildingType.valueOf(h.type.fieldTypeId));
            return;
        }

        if (h.type.prod != null || h.type.consumableOnly) {
            float btnY = buttonZoneBottom(py);
            if (mx >= px + 16 && mx <= px + pw - 16 && my >= btnY && my <= btnY + 26) {
                h.paused = !h.paused;
                return;
            }
        }

        if (h.type.isMonument && !h.type.isLastMonumentLevel() && !h.monumentUpgrading) {
            float btnY = buttonZoneBottom(py);
            if (mx >= px + 16 && mx <= px + pw - 16 && my >= btnY && my <= btnY + 24) {
                if (state.canMonumentGrow(h))
                    h.startMonumentUpgrade(state.resources, state.difficulty.monumentCostMultiplier);
                return;
            }
        }

        if (mx < px || mx > px + pw || my < py || my > py + panH) {
            input.showBuildingPanel = false;
            input.inspectedBuilding = null;
        }
    }

    public void drawTopBar(GameInputHandler input, GameState state) {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_TOP_BAND);
        sr.rect(0, sh - TOPBAR_H, sw, TOPBAR_H);
        sr.end();

        float row2Top = sh - TOPBAR_ROW1_H;

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK.r, UITheme.GOLD_DARK.g, UITheme.GOLD_DARK.b, 0.5f);
        for (int i = 0; i < 3; i++) {
            float y = row2Top - TOPBAR_ROW_H * i;
            sr.line(0, y, sw, y);
        }
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_PRIMARY);
        sr.line(0, sh - TOPBAR_H, sw, sh - TOPBAR_H);
        sr.end();

        drawResourceRow(state, sw, sh);
        drawExpeditionBar(state, sw, sh);
        drawServiceGridRow(state, sw, row2Top - TOPBAR_ROW_H);
        drawWorkforceGridRow(state, sw, row2Top - TOPBAR_ROW_H * 2);
        drawIdeologyRow(state, sw, sh - TOPBAR_H);
        drawTopBarButtons(input, state, sw, sh);
    }

    private void drawResourceRow(GameState state, float sw, float sh) {
        float row1Y = sh - TOPBAR_ROW1_H;
        ResourceManager.Resource[] topRes = state.resources.getTopBarResources(TOPBAR_RES);

        float rightReserve = 300f;
        float resStartX = 140f;
        float resSlotW = (sw - resStartX - rightReserve) / Math.max(1, topRes.length);
        float iconY = row1Y + (TOPBAR_ROW1_H - RES_ICON_SIZE) / 2f;

        com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < topRes.length; i++) {
            ResourceManager.Resource r = topRes[i];
            Texture icon = game.icons != null ? game.icons.getResourceIcon(r.id) : null;
            if (icon != null)
                continue;
            gl.setText(game.fonts.title, formatResource(r.amount));
            float blockW = RES_ICON_SIZE + 8f + gl.width;
            float slotCenterX = resStartX + i * resSlotW + resSlotW / 2f;
            float rx = slotCenterX - blockW / 2f;
            sr.setColor(r.color[0] * 0.85f, r.color[1] * 0.85f, r.color[2] * 0.85f, 1f);
            sr.rect(rx, iconY, RES_ICON_SIZE, RES_ICON_SIZE);
        }
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        for (int i = 0; i < topRes.length; i++) {
            ResourceManager.Resource r = topRes[i];
            String amountText = formatResource(r.amount);
            gl.setText(game.fonts.title, amountText);
            float blockW = RES_ICON_SIZE + 8f + gl.width;
            float slotCenterX = resStartX + i * resSlotW + resSlotW / 2f;
            float rx = slotCenterX - blockW / 2f;

            Texture icon = game.icons != null ? game.icons.getResourceIcon(r.id) : null;
            if (icon != null) {
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(icon, rx, iconY, RES_ICON_SIZE, RES_ICON_SIZE);
            }
            game.fonts.title.setColor(new Color(r.color[0], r.color[1], r.color[2], 1f));
            game.fonts.title.draw(batch, amountText, rx + RES_ICON_SIZE + 8, row1Y + TOPBAR_ROW1_H - 16);
        }

        float totalTaxes = 0f;
        float totalMaintenance = 0f;
        for (BuildingInstance b : state.buildings) {
            if (b.type.isHouse) {
                totalTaxes += b.calculateTaxesPerMinute();
            } else if (b.type.input != null && !b.type.input.isEmpty()) {
                if (!b.paused && b.connected)
                    totalMaintenance += b.getMaintenancePerMinute();
            }
        }
        float balance = totalTaxes - totalMaintenance;
        String balanceText = String.format("%+.0f/min", balance);
        Color balanceColor = balance >= 0 ? Color.valueOf("55cc55") : Color.valueOf("cc5555");
        game.fonts.large.setColor(balanceColor);
        game.fonts.large.draw(batch, balanceText, sw - 250, sh - 40);

        String speedPopText = "Speed " + (int) state.gameSpeed + "x   •   Pop. "
                + (int) state.resources.get("population");
        game.fonts.normal.setColor(COL_TEXT_GRAY);
        game.fonts.normal.draw(batch, speedPopText, sw - 250, sh - 62);

        batch.end();
    }

    private void drawExpeditionBar(GameState state, float sw, float sh) {
        if (!state.expeditionActive)
            return;
        float barW = 120f;
        float bx = sw - (state.newWorldUnlocked ? 150f : 10f);
        float by = sh - TOPBAR_ROW_H + 10;

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.15f, 0.15f, 0.15f, 1f);
        sr.rect(bx, by, barW, 5);
        sr.setColor(0.2f, 0.65f, 0.9f, 1f);
        sr.rect(bx, by, barW * Math.min(1f, state.expeditionTimer / GameState.EXPEDITION_SECS), 5);
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        game.fonts.small.setColor(COL_TEXT_GRAY);
        game.fonts.small.draw(batch,
                "Expedition " + (int) (state.expeditionTimer / GameState.EXPEDITION_SECS * 100) + "%",
                bx, by + 18);
        batch.end();
    }

    private void drawSatisfactionRow(List<String> labels, List<Color> colors, float sw, float rowY) {
        if (labels.isEmpty())
            return;
        float itemW = sw / labels.size();
        float y = rowY + TOPBAR_ROW_H / 2f + 5f;

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < labels.size(); i++) {
            float x = i * itemW + 8;
            sr.setColor(colors.get(i));
            sr.rect(x, y - 4f, SATISFACTION_DOT_SIZE, SATISFACTION_DOT_SIZE);
        }
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        for (int i = 0; i < labels.size(); i++) {
            float x = i * itemW + 8 + SATISFACTION_DOT_SIZE + 6f;
            game.fonts.small.setColor(COL_TEXT_GRAY);
            game.fonts.small.draw(batch, labels.get(i), x, y);
        }
        batch.end();
    }

    public void drawBottomBar(GameInputHandler input) {
        float sw = Gdx.graphics.getWidth();
        float tabY = BOTBAR_H;
        float slotW = SLOT_W;
        int slotsVisible = (int) (sw / slotW) + 1;
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        int totalSlots = input.getBottomBarSlotCount();
        float tabW = sw / TAB_LABELS.length;

        int hoveredTab = -1;
        if (my >= tabY && my < tabY + TAB_H) {
            int t = (int) (mx / tabW);
            if (t >= 0 && t < TAB_LABELS.length)
                hoveredTab = t;
        }

        // ── Precalcolo icone/colori fallback per gli slot visibili ──────────────
        Texture[] slotIcons = new Texture[slotsVisible];
        Color[] slotFallbackColor = new Color[slotsVisible];
        for (int i = 0; i < slotsVisible; i++) {
            int idx = i + input.botScrollOffset;
            if (idx >= totalSlots)
                continue;
            GameInputHandler.ToolAction tool = input.getToolForSlot(idx);
            if (tool == GameInputHandler.ToolAction.NONE) {
                BuildingType bt = input.bottomBarBuildings[input.getBuildingIndexForSlot(idx)];
                slotFallbackColor[i] = Color.valueOf(bt.colorHex);

                if (bt.prod != null && !bt.consumableOnly) {
                    slotIcons[i] = game.icons != null ? game.icons.getResourceIcon(bt.prod) : null;
                } else {
                    slotIcons[i] = game.icons != null ? game.icons.getBuildingIcon(bt.id) : null;
                }
            } else {
                slotIcons[i] = null;
                slotFallbackColor[i] = Color.valueOf(input.getToolColorHex(tool));
            }
        }

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(COL_BOTBAR_BG);
        sr.rect(0, 0, sw, BOTBAR_H);
        sr.setColor(COL_TAB_BG);
        sr.rect(0, tabY, sw, TAB_H);

        for (int i = 0; i < TAB_LABELS.length; i++) {
            if (i == input.activeTab) {
                sr.setColor(0.18f, 0.14f, 0.04f, 1f);
                sr.rect(i * tabW + 1, tabY, tabW - 2, TAB_H);
            } else if (i == hoveredTab) {
                sr.setColor(COL_SLOT_HOVER);
                sr.rect(i * tabW + 1, tabY, tabW - 2, TAB_H);
            }
        }

        for (int i = 0; i < slotsVisible; i++) {
            int idx = i + input.botScrollOffset;
            if (idx >= totalSlots)
                break;
            float sx = i * slotW + 5;
            boolean hover = mx >= sx && mx < sx + slotW && my >= 0 && my < BOTBAR_H;
            boolean sel = (idx == input.selectedSlot);

            if (sel)
                sr.setColor(0.22f, 0.18f, 0.05f, 1f);
            else if (hover)
                sr.setColor(COL_SLOT_HOVER);
            else
                sr.setColor(COL_SLOT_BG);
            sr.rect(sx + 1, 1, slotW - 2, BOTBAR_H - 2);
        }
        sr.end();

        // ── Quadrati colorati di fallback (solo dove manca la texture icona) ────
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < slotsVisible; i++) {
            int idx = i + input.botScrollOffset;
            if (idx >= totalSlots)
                break;
            if (slotIcons[i] != null)
                continue;
            float sx = i * slotW + 5;
            float iconX = sx + (slotW - BOTBAR_ICON_SIZE) / 2f;
            float iconY = BOTBAR_H - BOTBAR_ICON_TOP_PAD - BOTBAR_ICON_SIZE;
            Color c = slotFallbackColor[i];
            sr.setColor(c.r * 0.85f, c.g * 0.85f, c.b * 0.85f, 1f);
            sr.rect(iconX, iconY, BOTBAR_ICON_SIZE, BOTBAR_ICON_SIZE);
        }
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < slotsVisible; i++) {
            int idx = i + input.botScrollOffset;
            if (idx >= totalSlots)
                break;
            if (slotIcons[i] != null)
                continue;
            float sx = i * slotW + 5;
            float iconX = sx + (slotW - BOTBAR_ICON_SIZE) / 2f;
            float iconY = BOTBAR_H - BOTBAR_ICON_TOP_PAD - BOTBAR_ICON_SIZE;
            sr.setColor(UITheme.GOLD_DARK.r, UITheme.GOLD_DARK.g, UITheme.GOLD_DARK.b, 0.6f);
            sr.rect(iconX, iconY, BOTBAR_ICON_SIZE, BOTBAR_ICON_SIZE);
        }
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        com.badlogic.gdx.graphics.g2d.GlyphLayout tabLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        for (int i = 0; i < TAB_LABELS.length; i++) {
            boolean active = i == input.activeTab;
            game.fonts.small.setColor(active ? COL_TEXT_GOLD : (i == hoveredTab ? COL_TEXT_WHITE : COL_TEXT_GRAY));
            tabLayout.setText(game.fonts.small, TAB_LABELS[i]);
            game.fonts.small.draw(batch, TAB_LABELS[i],
                    i * tabW + tabW / 2f - tabLayout.width / 2f, tabY + TAB_H - 5);
        }

        com.badlogic.gdx.graphics.g2d.GlyphLayout labelLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        float labelTop = BOTBAR_H - BOTBAR_ICON_TOP_PAD - BOTBAR_ICON_SIZE - 6f;

        for (int i = 0; i < slotsVisible; i++) {
            int idx = i + input.botScrollOffset;
            if (idx >= totalSlots)
                break;
            float sx = i * slotW + 5;
            boolean sel = (idx == input.selectedSlot);

            if (slotIcons[i] != null) {
                float iconX = sx + (slotW - BOTBAR_ICON_SIZE) / 2f;
                float iconY = BOTBAR_H - BOTBAR_ICON_TOP_PAD - BOTBAR_ICON_SIZE;
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(slotIcons[i], iconX, iconY, BOTBAR_ICON_SIZE, BOTBAR_ICON_SIZE);
            }

            GameInputHandler.ToolAction tool = input.getToolForSlot(idx);
            BuildingType bt = (tool == GameInputHandler.ToolAction.NONE)
                    ? input.bottomBarBuildings[input.getBuildingIndexForSlot(idx)]
                    : null;
            String label = (tool == GameInputHandler.ToolAction.NONE) ? bt.label : input.getToolLabel(tool);

            Color nameColor = sel ? COL_TEXT_GOLD : COL_TEXT_WHITE;
            labelLayout.setText(game.fonts.small, label, nameColor, slotW - 10,
                    com.badlogic.gdx.utils.Align.center, true);
            game.fonts.small.draw(batch, labelLayout, sx + slotW / 2f - (slotW - 10) / 2f, labelTop);
        }

        if (input.selectedBuilding != null) {
            game.fonts.normal.setColor(COL_TEXT_GOLD);
            game.fonts.normal.draw(batch,
                    input.selectedBuilding.label + " — Click = place   ESC = cancel",
                    8, tabY + TAB_H + 16);
        } else if (input.selectedTool != GameInputHandler.ToolAction.NONE) {
            game.fonts.normal.setColor(COL_TEXT_GOLD);
            game.fonts.normal.draw(batch,
                    input.getToolLabel(input.selectedTool) + " — Click on a building   ESC = cancel",
                    8, tabY + TAB_H + 16);
        }

        batch.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.30f, 0.25f, 0.12f, 1f);
        sr.rect(0, tabY + TAB_H - 1, sw, 1);
        sr.setColor(0.20f, 0.20f, 0.20f, 1f);
        sr.rect(0, tabY - 1, sw, 1);
        sr.setColor(COL_TAB_ACTIVE);
        float atx = input.activeTab * tabW;
        sr.line(atx + 2, tabY, atx + tabW - 2, tabY);
        sr.setColor(0.22f, 0.22f, 0.22f, 1f);
        for (int i = 1; i < slotsVisible; i++)
            sr.line(i * slotW, 4, i * slotW, BOTBAR_H - 4);
        sr.end();

        if (input.selectedBuilding != null && input.selectedBuilding != BuildingType.ROAD
                && input.selectedBuilding != BuildingType.PIPELINE) {
            float rbx = ROTATE_BTN_X;
            float rby = tabY + TAB_H + 4;
            float mx2 = Gdx.input.getX();
            float my2 = Gdx.graphics.getHeight() - Gdx.input.getY();
            boolean hover = mx2 >= rbx && mx2 <= rbx + ROTATE_BTN_W && my2 >= rby && my2 <= rby + ROTATE_BTN_H;

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(hover ? COL_SLOT_HOVER : COL_SLOT_BG);
            sr.rect(rbx, rby, ROTATE_BTN_W, ROTATE_BTN_H);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(UITheme.GOLD_DARK);
            sr.rect(rbx, rby, ROTATE_BTN_W, ROTATE_BTN_H);
            sr.end();

            batch.begin();
            String rlabel = "Rotate";
            com.badlogic.gdx.graphics.g2d.GlyphLayout rgl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                    game.fonts.small, rlabel);
            game.fonts.small.setColor(hover ? COL_TEXT_GOLD : COL_TEXT_WHITE);
            game.fonts.small.draw(batch, rlabel, rbx + ROTATE_BTN_W / 2f - rgl.width / 2f,
                    rby + ROTATE_BTN_H / 2f + rgl.height / 2f);
            batch.end();
        }
    }

    public void drawHoverTooltip(GameInputHandler input, GameState state) {
        if (input.hoveredSlot < 0)
            return;
        if (input.getToolForSlot(input.hoveredSlot) != GameInputHandler.ToolAction.NONE)
            return;

        int buildingIdx = input.getBuildingIndexForSlot(input.hoveredSlot);
        if (buildingIdx < 0 || buildingIdx >= input.bottomBarBuildings.length)
            return;

        BuildingType bt = input.bottomBarBuildings[buildingIdx];
        drawTooltipBox(bt.getInfoLines(state.resources));
    }

    /**
     * Tooltip sui recinti del pannello di un allevamento: costo e regola di
     * piazzamento.
     */
    private void drawPenTooltip(GameState state) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        for (int i = 0; i < penHitRects.size(); i++) {
            float[] r = penHitRects.get(i);
            if (mx >= r[0] && mx <= r[0] + r[2] && my >= r[1] && my <= r[1] + r[3]) {
                List<String> lines = new ArrayList<>(penHitTypes.get(i).getInfoLines(state.resources));
                lines.add("Next to the ranch or its pens");
                drawTooltipBox(lines);
                return;
            }
        }
    }

    private void drawTooltipBox(List<String> lines) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        float tx = mx + 18f;
        float ty = my + 18f;

        float lineH = 22f;
        float padding = 14f;
        float maxWidth = 0f;
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        for (String line : lines) {
            layout.setText(game.fonts.small, line);
            maxWidth = Math.max(maxWidth, layout.width);
        }
        float boxW = maxWidth + padding * 2;
        float boxH = lines.size() * lineH + padding * 2;

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        if (tx + boxW > sw)
            tx = mx - boxW - 18f;
        if (ty + boxH > sh)
            ty = sh - boxH - 4f;

        drawPanel(tx, ty, boxW, boxH);

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        float textY = ty + boxH - padding;
        for (int i = 0; i < lines.size(); i++) {
            game.fonts.small.setColor(i == 0 ? COL_TEXT_GOLD : COL_TEXT_WHITE);
            game.fonts.small.draw(batch, lines.get(i), tx + padding, textY);
            textY -= lineH;
        }
        batch.end();
    }

    private static class HouseContent {
        List<PanelLine> beforeBar;
        List<PanelLine> afterBar;
    }

    /**
     * Cella di griglia: valore colorato in alto, etichetta troncata sotto — nessun
     * pallino.
     */
    private void drawStatCell(String label, String valueText, Color valueColor, float cellX, float cellW, float rowY) {
        com.badlogic.gdx.graphics.g2d.GlyphLayout glVal = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                game.fonts.small, valueText);
        com.badlogic.gdx.graphics.g2d.GlyphLayout glLbl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                game.fonts.small, label);

        float centerX = cellX + cellW / 2f;
        game.fonts.small.setColor(valueColor);
        game.fonts.small.draw(batch, valueText, centerX - glVal.width / 2f, rowY + TOPBAR_ROW_H - 8f);
        game.fonts.small.setColor(COL_TEXT_GRAY);
        game.fonts.small.draw(batch, label, centerX - glLbl.width / 2f, rowY + 13f);
    }

    private void drawServiceGridRow(GameState state, float sw, float rowY) {
        float cellW = sw / SERVICE_COLS;
        int col = 0;

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        for (ServiceCategory cat : ServiceCategory.values()) {
            float s = state.getServiceSatisfaction(cat);
            Color c = s >= 0.8f ? UITheme.STATUS_GREEN : (s < 0.5f ? UITheme.STATUS_RED : UITheme.STATUS_YELLOW);
            drawStatCell(cat.label, (int) (s * 100) + "%", c, col * cellW, cellW, rowY);
            col++;
        }
        for (ConsumptionServiceCategory cat : ConsumptionServiceCategory.values()) {
            float s = state.getConsumptionServiceSatisfaction(cat);
            Color c = s >= 0.8f ? UITheme.STATUS_GREEN : (s < 0.5f ? UITheme.STATUS_RED : UITheme.STATUS_YELLOW);
            drawStatCell(cat.label, (int) (s * 100) + "%", c, col * cellW, cellW, rowY);
            col++;
        }
        batch.end();
    }

    private void drawWorkforceGridRow(GameState state, float sw, float rowY) {
        float cellW = sw / WORKFORCE_COLS;

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        for (int i = 0; i < BuildingType.SOCIAL_CLASS_NAMES.length; i++) {
            if (!state.classHasWorkforceDemand(i))
                continue; // colonna resta vuota, ma la posizione i*cellW è comunque fissa
            float s = state.getClassWorkforceSatisfaction(i);
            int available = (int) state.getClassPopulation(i);
            int required = (int) state.getClassWorkforceDemand(i);
            Color c = s >= 0.8f ? UITheme.STATUS_GREEN : (s < 0.5f ? UITheme.STATUS_RED : UITheme.STATUS_YELLOW);
            String value = available + "/" + required + " " + (int) (s * 100) + "%";
            drawStatCell(BuildingType.SOCIAL_CLASS_NAMES[i], value, c, i * cellW, cellW, rowY);
        }
        batch.end();
    }

    private void drawIdeologyRow(GameState state, float sw, float row4Y) {
        float value = state.ideologyManager.getValue();
        float norm = (value - IdeologyManager.MIN_VALUE)
                / (IdeologyManager.MAX_VALUE - IdeologyManager.MIN_VALUE);

        float barMarginX = 24f;
        float barW = sw - barMarginX * 2f;
        float barH = 10f;
        float barY = row4Y + (TOPBAR_ROW_H - barH) / 2f + 6f;

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(0.16f, 0.16f, 0.16f, 1f);
        sr.rect(barMarginX, barY, barW, barH);

        float bronzeW = barW * (1f - norm);

        sr.setColor(0.55f, 0.32f, 0.14f, 1f);
        sr.rect(barMarginX, barY, bronzeW, barH);

        sr.setColor(0.18f, 0.42f, 0.62f, 1f);
        sr.rect(barMarginX + bronzeW, barY, barW - bronzeW, barH);

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK);
        sr.rect(barMarginX, barY, barW, barH);
        sr.setColor(0.5f, 0.5f, 0.5f, 0.8f);
        sr.rect(barMarginX + barW * 0.5f - 1f, barY, 2f, barH);
        sr.end();

        float markerX = barMarginX + bronzeW;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.WHITE);
        sr.rect(markerX - 2f, barY - 4f, 4f, barH + 8f);
        sr.end();
    }

    public void drawNarrativeEventPanel(GameState state) {
        NarrativeEvent event = state.eventManager.pendingEvent;
        if (event == null)
            return;

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        int choiceCount = event.choices.length;

        float pw = 600f;
        float ph = 230f + choiceCount * 56f;
        float px = (sw - pw) / 2f;
        float py = (sh - ph) / 2f;

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.8f);
        sr.rect(0, 0, sw, sh);
        sr.end();
        drawPanel(px, py, pw, ph);

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        game.fonts.large.setColor(COL_TEXT_GOLD);
        game.fonts.large.draw(batch, event.title, px + 24, py + ph - 28);

        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(game.fonts.normal, event.description, COL_TEXT_WHITE, pw - 48,
                com.badlogic.gdx.utils.Align.left, true);
        game.fonts.normal.setColor(COL_TEXT_WHITE);
        game.fonts.normal.draw(batch, layout, px + 24, py + ph - 70);
        batch.end();

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        int hoveredChoice = -1;

        for (int i = 0; i < choiceCount; i++) {
            float by = py + 24 + (choiceCount - 1 - i) * 56f;
            boolean hover = mx >= px + 20 && mx <= px + pw - 20 && my >= by && my <= by + 46;
            if (hover)
                hoveredChoice = i;

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(hover ? 0.22f : 0.14f, hover ? 0.18f : 0.14f, hover ? 0.05f : 0.18f, 1f);
            sr.rect(px + 20, by, pw - 40, 46);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(hover ? COL_SLOT_SEL : new Color(0.4f, 0.4f, 0.4f, 1f));
            sr.rect(px + 20, by, pw - 40, 46);
            sr.end();

            batch.begin();
            game.fonts.normal.setColor(hover ? COL_TEXT_GOLD : COL_TEXT_WHITE);
            game.fonts.normal.draw(batch, event.choices[i].label, px + 32, by + 29);
            batch.end();
        }

        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT) && hoveredChoice >= 0) {
            state.eventManager.resolveChoice(hoveredChoice, state);
        }
    }

    private String formatTime(int seconds) {
        if (seconds < 60)
            return seconds + "s";
        int mins = seconds / 60;
        int secs = seconds % 60;
        if (secs == 0)
            return mins + "m";
        return mins + "m " + secs + "s";
    }

    private HouseContent buildHouseLines(BuildingInstance h, GameState state) {
        HouseContent hc = new HouseContent();
        hc.beforeBar = new ArrayList<>();
        hc.afterBar = new ArrayList<>();

        int cap = h.type.houseCap();
        int pop = (int) h.housePop;
        float graceCap = cap * 0.30f;
        boolean inGrace = (int) h.housePop < (int) graceCap;

        hc.beforeBar.add(new PanelLine(game.fonts.normal, "Inhabitants: " + pop + " / " + cap, COL_TEXT_WHITE, 26f));

        if (inGrace) {
            hc.afterBar.add(new PanelLine(game.fonts.normal, "(minimum population: " + (int) graceCap + ")",
                    Color.valueOf("88cc88"), 26f));
        }

        hc.afterBar.add(new PanelLine(game.fonts.normal, "Servicies:", COL_TEXT_GRAY, 24f));
        for (ServiceCategory cat : ServiceCategory.values()) {
            float satisfaction = state.getServiceSatisfaction(cat);
            Color color = satisfaction >= 0.9f ? Color.valueOf("55cc55")
                    : satisfaction >= 0.7f ? UITheme.STATUS_YELLOW : UITheme.STATUS_RED;
            hc.afterBar.add(new PanelLine(game.fonts.small,
                    cat.label + " (" + (int) (satisfaction * 100) + "%)", color, 20f));
        }
        for (ConsumptionServiceCategory cat : ConsumptionServiceCategory.values()) {
            float satisfaction = state.getConsumptionServiceSatisfaction(cat);
            Color color = satisfaction >= 0.9f ? Color.valueOf("55cc55")
                    : satisfaction >= 0.7f ? UITheme.STATUS_YELLOW : UITheme.STATUS_RED;
            hc.afterBar.add(new PanelLine(game.fonts.small,
                    cat.label + " (" + (int) (satisfaction * 100) + "%)", color, 20f));
        }

        hc.afterBar.add(new PanelLine(game.fonts.normal, "Needs:", COL_TEXT_GRAY, 24f));
        if (h.type.houseNeeds != null) {
            for (String need : h.type.houseNeeds) {
                boolean ok = state.resources.get(need) >= 0.05f;
                String label = state.resources.getAll().containsKey(need)
                        ? state.resources.getAll().get(need).label
                        : need;
                hc.afterBar.add(new PanelLine(game.fonts.normal, label,
                        ok ? Color.valueOf("55cc55") : Color.valueOf("cc4444"), 22f));
            }
        }

        boolean canUp = h.canUpgrade(state.resources);
        String statusText;
        Color statusColor;
        if (h.type.nextHouseLevel() == null) {
            statusText = "Maximum level";
            statusColor = COL_TEXT_GRAY;
        } else if (canUp) {
            statusText = "Ready for the upgrade";
            statusColor = Color.valueOf("55cc55");
        } else {
            int popCap = h.type.houseCap();
            statusText = pop < popCap ? "Required population (" + pop + "/" + popCap + ")" : "Needs unfulfilled";
            statusColor = COL_TEXT_GRAY;
        }
        hc.afterBar.add(new PanelLine(game.fonts.normal, statusText, statusColor, 26f));

        return hc;
    }

    private void drawPopBar(BuildingInstance h, float px, float pw, float barY) {
        int cap = h.type.houseCap();
        float barW = pw - 32f;
        float fill = cap > 0 ? (h.housePop / cap) : 0f;
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.2f, 0.2f, 0.2f, 1f);
        sr.rect(px + 16, barY, barW, 12);
        sr.setColor(0.3f, 0.8f, 0.3f, 1f);
        sr.rect(px + 16, barY, barW * fill, 12);
        float graceX = px + 16 + barW * 0.30f;
        sr.setColor(1f, 0.8f, 0.2f, 0.8f);
        sr.rect(graceX - 1, barY, 2, 12);
        sr.end();
    }

    private List<PanelLine> buildProductionLines(BuildingInstance h, GameState state) {
        List<PanelLine> lines = new ArrayList<>();

        if (h.paused) {
            lines.add(new PanelLine(game.fonts.normal, "PAUSED", Color.valueOf("cc8844"), 26f));
        } else {
            lines.add(new PanelLine(game.fonts.normal, "PRODUCING",
                    h.type.consumableOnly ? Color.valueOf("55aacc") : Color.valueOf("55cc55"), 26f));
        }

        if (h.type.input != null && !h.type.input.isEmpty()) {
            lines.add(new PanelLine(game.fonts.normal, h.type.consumableOnly ? "Consumes:" : "Recipe:",
                    COL_TEXT_GRAY, 20f));
            for (Map.Entry<String, Float> e : h.type.input.entrySet()) {
                boolean ok = state.resources.get(e.getKey()) >= e.getValue();
                String lbl = state.resources.getAll().containsKey(e.getKey())
                        ? state.resources.getAll().get(e.getKey()).label
                        : e.getKey();
                lines.add(new PanelLine(game.fonts.normal,
                        (int) e.getValue().floatValue() + "x " + lbl + " (" + (int) state.resources.get(e.getKey())
                                + " available)",
                        ok ? Color.valueOf("55cc55") : Color.valueOf("cc4444"), 20f));
            }
            if (!h.type.consumableOnly && h.type.prod != null) {
                String prodLbl = state.resources.getAll().containsKey(h.type.prod)
                        ? state.resources.getAll().get(h.type.prod).label
                        : h.type.prod;
                lines.add(new PanelLine(game.fonts.normal,
                        " +" + h.type.rate + " x " + prodLbl + " every " + h.type.cycleTime + "s",
                        Color.valueOf("55cc55"), 20f));
            } else if (h.type.consumableOnly) {
                lines.add(new PanelLine(game.fonts.normal, " Consume every " + h.type.cycleTime + "s",
                        COL_TEXT_GRAY, 20f));
            }
        } else if (h.type.prod != null) {
            String prodLbl = state.resources.getAll().containsKey(h.type.prod)
                    ? state.resources.getAll().get(h.type.prod).label
                    : h.type.prod;
            lines.add(new PanelLine(game.fonts.normal,
                    "Produces: +" + h.type.rate + " " + prodLbl + " every " + h.type.cycleTime + " seconds ",
                    Color.valueOf("55cc55"), 20f));
        }

        if (h.type.requiredPower != BuildingType.PowerType.NONE) {
            if (h.type.requiredPower == BuildingType.PowerType.STEAM
                    || h.type.requiredPower == BuildingType.PowerType.BOTH) {
                lines.add(new PanelLine(game.fonts.small, h.hasSteam ? "Steam: ACTIVE" : "Steam: MISSING",
                        h.hasSteam ? Color.valueOf("88aaff") : Color.valueOf("ff8888"), 18f));
            }
            if (h.type.requiredPower == BuildingType.PowerType.ELECTRICITY
                    || h.type.requiredPower == BuildingType.PowerType.BOTH) {
                lines.add(new PanelLine(game.fonts.small,
                        h.hasElectricity ? "Electricity: ACTIVE" : "Electricity: MISSING",
                        h.hasElectricity ? Color.valueOf("ffff88") : Color.valueOf("ff8888"), 18f));
            }
        }

        lines.add(new PanelLine(game.fonts.normal,
                "Cicle: " + (int) (h.progress * 100) + "%" + (h.paused ? "  (PAUSED)" : ""),
                h.paused ? Color.valueOf("cc8844") : COL_TEXT_GRAY, 20f));

        if (h.type.consumptionServiceCategory != null && h.type.influenceRadius > 0) {
            lines.add(new PanelLine(game.fonts.small, "Radius: " + h.type.influenceRadius + " celle",
                    COL_TEXT_GRAY, 20f));
        }

        return lines;
    }

    private List<PanelLine> buildMonumentLines(BuildingInstance h, GameState state) {
        List<PanelLine> lines = new ArrayList<>();

        if (h.type.isLastMonumentLevel()) {
            lines.add(new PanelLine(game.fonts.normal, "COMPLETED", Color.valueOf("55cc55"), 22f));
            if (h.type.influenceRadius > 0) {
                lines.add(new PanelLine(game.fonts.normal, "Influence radius: " + h.type.influenceRadius + " celle",
                        COL_TEXT_GRAY, 20f));
            }
        } else if (h.monumentUpgrading) {
            lines.add(new PanelLine(game.fonts.normal, "UNDER CONSTRUCTION", Color.valueOf("4488cc"), 22f));
            int percent = (int) (h.monumentUpgradeProgress / h.type.upgradeTime * 100);
            lines.add(new PanelLine(game.fonts.normal, "Progress: " + percent + "%", COL_TEXT_GRAY, 20f));
        } else {
            lines.add(new PanelLine(game.fonts.normal,
                    "Phase " + (h.type.monumentLevel + 1) + "/" + h.type.getMonumentChainLength(),
                    Color.valueOf("e8d080"), 22f));

            BuildingType next = h.type.nextMonumentLevel();
            if (next != null && next.upgradeCost != null && !next.upgradeCost.isEmpty()) {
                lines.add(new PanelLine(game.fonts.normal, "Next phase cost:", COL_TEXT_GRAY, 20f));
                for (Map.Entry<String, Float> e : next.upgradeCost.entrySet()) {
                    boolean ok = state.resources.get(e.getKey()) >= e.getValue();
                    String lbl = state.resources.getAll().containsKey(e.getKey())
                            ? state.resources.getAll().get(e.getKey()).label
                            : e.getKey();
                    lines.add(new PanelLine(game.fonts.normal,
                            (int) e.getValue().floatValue() + "x " + lbl + " (" + (int) state.resources.get(e.getKey())
                                    + " available)",
                            ok ? Color.valueOf("55cc55") : Color.valueOf("cc4444"), 20f));
                }
            }
        }

        if (h.type.consumptionServiceCategory != null && h.type.influenceRadius > 0 && !h.type.isLastMonumentLevel()) {
            lines.add(new PanelLine(game.fonts.small, "Radius: " + h.type.influenceRadius + " celle",
                    COL_TEXT_GRAY, 20f));
        }

        return lines;
    }

    // ── Pannello fazioni ──────────────────────────────────────────────────────
    // ── Pannello fazioni ──────────────────────────────────────────────────────
    public void drawFactionPanel(GameInputHandler input, GameState state) {
        if (!input.showFactionPanel)
            return;

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        FactionManager.FactionState[] slots = state.factionManager.getAllSlots();

        float pw = Math.min(1500f, sw - 120f);
        float ph = Math.min(820f, sh - 140f);
        float px = (sw - pw) / 2f;
        float py = (sh - ph) / 2f;

        // Overlay scuro dietro al pannello
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.75f);
        sr.rect(0, 0, sw, sh);
        sr.end();

        drawPanel(px, py, pw, ph);

        float headerH = 70f;
        float footerH = 40f;

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        String title = "Factions";
        com.badlogic.gdx.graphics.g2d.GlyphLayout tgl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.fonts.title,
                title);
        game.fonts.title.setColor(COL_TEXT_GOLD);
        game.fonts.title.draw(batch, title, px + pw / 2f - tgl.width / 2f, py + ph - 24f);
        batch.end();

        float contentTop = py + ph - headerH;
        float contentBottom = py + footerH;
        float contentH = contentTop - contentBottom;

        float colGap = 24f;
        float colW = (pw - colGap * 4f) / 3f;

        // slot0 (prima fazione arrivata) → colonna centrale
        // slot1 → colonna destra, slot2 → colonna sinistra
        int[] colForSlot = { 1, 2, 0 };

        for (int i = 0; i < slots.length; i++) {
            int col = colForSlot[i];
            float colX = px + colGap + col * (colW + colGap);
            drawFactionColumn(slots[i], colX, contentBottom, colW, contentH);
        }

        batch.begin();
        String hint = "[F] / [ESC] / click outside to close";
        com.badlogic.gdx.graphics.g2d.GlyphLayout hgl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.fonts.small,
                hint);
        game.fonts.small.setColor(COL_TEXT_GRAY);
        game.fonts.small.draw(batch, hint, px + pw / 2f - hgl.width / 2f, py + footerH / 2f + hgl.height / 2f);
        batch.end();

        // Reset flag al primo frame
        if (input.panelJustOpened) {
            input.panelJustOpened = false;
            return;
        }

        // Chiudi pannello se si clicca fuori
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            float mx = Gdx.input.getX();
            float my = sh - Gdx.input.getY();
            if (mx < px || mx > px + pw || my < py || my > py + ph)
                input.showFactionPanel = false;
        }
    }

    /**
     * Disegna una singola colonna fazione: immagine in alto,
     * nome/keyword/descrizione sotto.
     */
    private void drawFactionColumn(FactionManager.FactionState fs, float colX, float colBottom,
            float colW, float colH) {
        Faction f = fs.faction;
        Color fc = Color.valueOf(f.colorHex);

        // Sfondo + bordo colonna
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(fc.r * 0.15f, fc.g * 0.15f, fc.b * 0.15f, 1f);
        sr.rect(colX, colBottom, colW, colH);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(fs.arrived ? fc : Color.valueOf("444444"));
        sr.rect(colX, colBottom, colW, colH);
        sr.end();

        if (!fs.arrived) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            String lockLbl = "???";
            com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                    game.fonts.huge,
                    lockLbl);
            game.fonts.huge.setColor(COL_TEXT_GRAY);
            game.fonts.huge.draw(batch, lockLbl, colX + colW / 2f - gl.width / 2f,
                    colBottom + colH / 2f + gl.height / 2f);
            batch.end();
            return;
        }

        // ── Zona immagine (in alto, ~75% della colonna) — rettangolo 3:2 ─────────
        float imgZoneH = colH * 0.75f;
        float imgZoneY = colBottom + colH - imgZoneH;

        float imgW = colW - 12f;
        float imgH = imgW * (3f / 2f);
        if (imgH > imgZoneH - 8f) {
            imgH = imgZoneH - 8f;
            imgW = imgH * (2f / 3f);
        }
        float imgX = colX + (colW - imgW) / 2f;
        float imgYCentered = imgZoneY + (imgZoneH - imgH) / 2f;

        Texture icon = game.icons != null ? game.icons.getFactionIcon(f) : null;

        if (icon == null) {
            sr.setProjectionMatrix(uiCamera.combined);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(fc.r * 0.85f, fc.g * 0.85f, fc.b * 0.85f, 1f);
            sr.rect(imgX, imgYCentered, imgW, imgH);
            sr.end();
        } else {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(icon, imgX, imgYCentered, imgW, imgH);
            batch.end();
        }
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(fc);
        sr.rect(imgX, imgYCentered, imgW, imgH);
        sr.end();

        // ── Zona testo (in basso) ────────────────────────────────────────────────
        float textW = colW - 24f;
        float y = imgZoneY - 16f;

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        // Nome fazione
        com.badlogic.gdx.graphics.g2d.GlyphLayout nameGl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                game.fonts.large, f.label, fc, textW, com.badlogic.gdx.utils.Align.center, false);
        game.fonts.large.setColor(fc);
        game.fonts.large.draw(batch, nameGl, colX + colW / 2f - textW / 2f, y);
        y -= nameGl.height + 10f;

        // Keyword ideologiche
        StringBuilder kwLine = new StringBuilder();
        Faction.Keyword[] kws = f.keywords();
        for (int i = 0; i < kws.length; i++) {
            kwLine.append("[").append(kws[i].label).append("]");
            if (i < kws.length - 1)
                kwLine.append("   ");
        }
        Color kwColor = Color.valueOf("c0b890");
        com.badlogic.gdx.graphics.g2d.GlyphLayout kwGl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.fonts.small,
                kwLine.toString(), kwColor, textW, com.badlogic.gdx.utils.Align.center, true);
        game.fonts.small.setColor(kwColor);
        game.fonts.small.draw(batch, kwGl, colX + colW / 2f - textW / 2f, y);
        y -= kwGl.height + 14f;

        // Descrizione — TODO: testo da inserire (placeholder vuoto per ora)
        String description = "";
        if (!description.isEmpty()) {
            com.badlogic.gdx.graphics.g2d.GlyphLayout descGl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(
                    game.fonts.normal, description, COL_TEXT_WHITE, textW, com.badlogic.gdx.utils.Align.center, true);
            game.fonts.normal.setColor(COL_TEXT_WHITE);
            game.fonts.normal.draw(batch, descGl, colX + colW / 2f - textW / 2f, y);
            y -= descGl.height + 10f;
        }

        batch.end();

        // ── Barra relazione in fondo alla colonna ───────────────────────────────
        float barW = colW - 24f;
        float barY = colBottom + 14f;
        float fill = (fs.relation + 100f) / 200f;
        float fr = 0.2f + (fs.relation >= 0 ? 0f : 0.6f) * (-fs.relation / 100f);
        float fg = 0.2f + (fs.relation >= 0 ? 0.6f : 0f) * (fs.relation / 100f);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.18f, 0.18f, 0.18f, 1f);
        sr.rect(colX + 12f, barY, barW, 10f);
        sr.setColor(fr, fg, 0.2f, 1f);
        sr.rect(colX + 12f, barY, barW * fill, 10f);
        sr.setColor(0.5f, 0.5f, 0.5f, 0.8f);
        sr.rect(colX + 12f + barW * 0.5f - 1f, barY, 2f, 10f);
        sr.end();
    }

    // ── Overlay Nuovo Mondo ───────────────────────────────────────────────────
    public void drawNewWorldOverlay(GameState state) {
        if (!state.showNewWorldOverlay)
            return;
        float sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();

        float pw = 540f, ph = 290f;
        float opx = (sw - pw) / 2f, opy = (sh - ph) / 2f;
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.78f);
        sr.rect(0, 0, sw, sh);
        sr.end();
        drawPanel(opx, opy, pw, ph);

        // Bottone (disegnato prima del testo così l'etichetta resta visibile sopra)
        float btnW = 260f, btnH = 36f;
        float btnX = opx + pw / 2f - btnW / 2f, btnY = opy + 26f;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.2f, 0.65f, 0.9f, 1f);
        sr.rect(btnX, btnY, btnW, btnH);
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        game.fonts.large.setColor(0.2f, 0.75f, 0.95f, 1f);
        game.fonts.large.draw(batch, "New World", opx + 24, opy + ph - 26);
        /*
         * game.fonts.normal.setColor(COL_TEXT_WHITE);
         * game.fonts.normal.draw(batch,
         * "Le navi imperiali hanno raggiunto nuove terre.", opx + 24, opy + ph - 78);
         * game.fonts.normal.draw(batch,
         * "Risorse sconosciute attendono di essere sfruttate.", opx + 24, opy + ph -
         * 110);
         * game.fonts.normal.draw(batch,
         * "Petrolio, gomma, seta e molto altro vi aspetta.", opx + 24, opy + ph - 142);
         */
        game.fonts.normal.setColor(0.05f, 0.05f, 0.05f, 1f);
        game.fonts.normal.draw(batch, "Now you can travel to the New World", btnX + 40, btnY + 24);
        batch.end();

        // Click handler per chiudere overlay
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            float mx = Gdx.input.getX(), my = sh - Gdx.input.getY();
            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                state.showNewWorldOverlay = false;
            }
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Color relationColor(int relation) {
        if (relation >= 60)
            return Color.valueOf("55cc55");
        if (relation >= 20)
            return Color.valueOf("aadd44");
        if (relation >= -20)
            return Color.valueOf("cccccc");
        if (relation >= -60)
            return Color.valueOf("dd8833");
        return Color.valueOf("cc3333");
    }

    private String formatResource(float v) {
        if (v >= 1000)
            return String.format("%.1fk", v / 1000f);
        return String.valueOf((int) v);
    }
}
