package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Texture;
import com.RomanLegacy.UITheme;
import com.RomanLegacy.Main;
import com.RomanLegacy.BuildingInstance;
import com.RomanLegacy.BuildingType;
import com.RomanLegacy.ResourceManager;

import java.util.*;

public class ConsumptionScreen implements Screen {

    private final Main game;
    private final GameState gameState;
    private final GameScreen previousGameScreen;
    private final OrthographicCamera camera;
    private final ShapeRenderer sr;
    private final SpriteBatch batch;

    private float scrollOffset = 0f;
    private float maxScroll = 0f;
    private float scrollDelta = 0f;
    private float statsUpdateTimer = 0f;

    private static final float ROW_HEIGHT = 44f;
    private static final float ROWS_VISIBLE = 19f;
    private static final float ROW_ICON_SIZE = 32f;
    private static final float COL_LABEL_X = 110f;
    private static final float COL_QTY_R = 760f;
    private static final float COL_PROD_R = 1160f;
    private static final float COL_CONS_R = 1520f;
    private static final float COL_BAL_R = 1840f;
    private static final float CLOSE_BTN_X = 20f;
    private static final float CLOSE_BTN_Y = 6f;
    private static final float CLOSE_BTN_W = 180f;
    private static final float CLOSE_BTN_H = 32f;
    private static final float STATS_UPDATE_INTERVAL = 1.0f;

    private final com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

    private void drawRight(com.badlogic.gdx.graphics.g2d.BitmapFont font, String text, float rightX, float y) {
        layout.setText(font, text);
        font.draw(batch, text, rightX - layout.width, y);
    }

    private List<ResourceStats> resourceStats = new ArrayList<>();

    private static class ResourceStats {
        String id, label;
        float production, consumption, balance;
        float amount;
        boolean isDeficit;

        ResourceStats(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    public ConsumptionScreen(Main game, GameState gameState, GameScreen previousGameScreen) {
        this.game = game;
        this.gameState = gameState;
        this.previousGameScreen = previousGameScreen;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        this.sr = new ShapeRenderer();
        this.batch = new SpriteBatch();
    }

    @Override
    public void show() {
        calculateStats();
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                scrollDelta += amountY * ROW_HEIGHT * 3f;
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {

        statsUpdateTimer += delta;
        if (statsUpdateTimer >= STATS_UPDATE_INTERVAL || resourceStats.isEmpty()) {
            calculateStats();
            statsUpdateTimer = 0f;
        }
        // Gestione input SEMPLIFICATA - controlli diretti
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            game.setScreen(previousGameScreen);
            return;
        }

        // Scroll con rotellina (accumulato dall'InputProcessor in show())
        if (scrollDelta != 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + scrollDelta));
            scrollDelta = 0f;
        }

        // Scroll con frecce
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            scrollOffset += 200 * delta;
            scrollOffset = Math.min(maxScroll, scrollOffset);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            scrollOffset -= 200 * delta;
            scrollOffset = Math.max(0, scrollOffset);
        }

        ScreenUtils.clear(0.05f, 0.08f, 0.12f, 1f);
        camera.update();

        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawBackground();
        drawHeader();
        drawResourceList();
        drawFooter();
        handleFooterClick();
    }

    private void drawFooter() {
        float sw = Main.SCREEN_W;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_TOP_BAND);
        sr.rect(0, 0, sw, 44);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK);
        sr.line(0, 44, sw, 44);
        sr.end();

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hover = mx >= CLOSE_BTN_X && mx <= CLOSE_BTN_X + CLOSE_BTN_W
                && my >= CLOSE_BTN_Y && my <= CLOSE_BTN_Y + CLOSE_BTN_H;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hover ? UITheme.HOVER_BG : UITheme.BG_PANEL);
        sr.rect(CLOSE_BTN_X, CLOSE_BTN_Y, CLOSE_BTN_W, CLOSE_BTN_H);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(hover ? UITheme.GOLD_PRIMARY : UITheme.GOLD_DARK);
        sr.rect(CLOSE_BTN_X, CLOSE_BTN_Y, CLOSE_BTN_W, CLOSE_BTN_H);
        sr.end();

        batch.begin();
        game.fonts.normal.setColor(UITheme.TEXT_GOLD);
        game.fonts.normal.draw(batch, "<- Back to game", CLOSE_BTN_X + 16, CLOSE_BTN_Y + CLOSE_BTN_H - 9);
        game.fonts.small.setColor(UITheme.TEXT_GRAY);
        game.fonts.small.draw(batch, "Green = surplus | Red = deficit", CLOSE_BTN_X + CLOSE_BTN_W + 24, 30);
        batch.end();
    }

    private void handleFooterClick() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
            return;
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        if (mx >= CLOSE_BTN_X && mx <= CLOSE_BTN_X + CLOSE_BTN_W
                && my >= CLOSE_BTN_Y && my <= CLOSE_BTN_Y + CLOSE_BTN_H) {
            game.setScreen(previousGameScreen);
        }
    }

    private void calculateStats() {
        Map<String, ResourceStats> statsMap = new HashMap<>();

        for (ResourceManager.Resource r : gameState.resources.getAll().values()) {
            if (r.id.equals("coin") || r.id.equals("population"))
                continue; // le monete non servono in questa schermata
            ResourceStats rs = new ResourceStats(r.id, r.label);
            rs.amount = r.amount;
            statsMap.put(r.id, rs);
        }

        for (BuildingInstance b : gameState.buildings) {
            if (b.type.prod != null && !b.paused && b.connected) {
                float prod = b.type.rate * (60f / b.type.cycleTime) * b.penEfficiency;
                ResourceStats rs = statsMap.get(b.type.prod);
                if (rs != null)
                    rs.production += prod;
            }

            if (b.type.input != null && !b.paused && b.connected) {
                for (Map.Entry<String, Float> e : b.type.input.entrySet()) {
                    float cons = e.getValue() * (60f / b.type.cycleTime);
                    ResourceStats rs = statsMap.get(e.getKey());
                    if (rs != null)
                        rs.consumption += cons;
                }
            }

            if (b.type.isHouse && b.type.houseNeeds != null && b.type.houseNeeds.length > 0) {
                int cap = b.type.houseCap();
                if (cap > 0) {
                    boolean needsSatisfied = true;
                    for (String need : b.type.houseNeeds) {
                        if (gameState.resources.get(need) < 0.05f) {
                            needsSatisfied = false;
                            break;
                        }
                    }

                    if (needsSatisfied) {
                        float consumptionMultiplier = gameState.getConsumptionMultiplier();
                        float perTick = (0.006f * (((b.type.houseLevel + 1) * 0.1f))) * consumptionMultiplier;
                        float perMinute = perTick * 60f;
                        for (String need : b.type.houseNeeds) {
                            ResourceStats rs = statsMap.get(need);
                            if (rs != null)
                                rs.consumption += perMinute;
                        }
                    }
                }
            }
        }

        resourceStats = new ArrayList<>(statsMap.values());
        for (ResourceStats rs : resourceStats) {
            rs.balance = rs.production - rs.consumption;
            rs.isDeficit = rs.balance < -0.1f && rs.consumption > 0.1f;
        }

        resourceStats.removeIf(rs -> rs.production < 0.01f && rs.consumption < 0.01f && rs.amount < 0.01f);

        resourceStats.sort((a, b) -> {
            if (a.isDeficit != b.isDeficit)
                return a.isDeficit ? -1 : 1;

            boolean aSignificant = Math.abs(a.balance) > 0.01f;
            boolean bSignificant = Math.abs(b.balance) > 0.01f;

            if (aSignificant != bSignificant)
                return aSignificant ? -1 : 1; // le significative vengono prima, sempre

            if (aSignificant) {
                return Float.compare(b.balance, a.balance);
            }
            return a.label.compareTo(b.label);
        });

        maxScroll = Math.max(0, resourceStats.size() * ROW_HEIGHT - ROWS_VISIBLE * ROW_HEIGHT);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    // ── Layout verticale (cornice retro condivisa) ────────────────────────────
    private static final float FRAME_X = 16f;
    private static final float FRAME_BOTTOM = 48f;
    private static final float TITLE_Y = Main.SCREEN_H - 45f;
    private static final float SUBTITLE_Y = Main.SCREEN_H - 80f;
    private static final float FRAME_TOP = Main.SCREEN_H - 96f;
    private static final float HEADER_BAND_H = 40f;
    private static final float HEADER_BAND_Y = FRAME_TOP - 6f - HEADER_BAND_H;
    private static final float ROWS_TOP = HEADER_BAND_Y - 8f;

    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_DARK);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);

        // Decorative top band
        sr.setColor(UITheme.BG_DECORATIVE);
        sr.rect(0, Main.SCREEN_H - 200, Main.SCREEN_W, 200);
        sr.end();

        // Pannello contenuto con cornice bronzo/oro condivisa
        UITheme.drawPanel(sr, FRAME_X, FRAME_BOTTOM,
                Main.SCREEN_W - FRAME_X * 2, FRAME_TOP - FRAME_BOTTOM);
    }

    private void drawHeader() {
        float sw = Main.SCREEN_W;

        batch.begin();
        game.fonts.title.setColor(UITheme.TEXT_GOLD);
        game.fonts.title.draw(batch, "RESOURCE BALANCE", sw / 2f - 180, TITLE_Y);
        game.fonts.normal.setColor(UITheme.TEXT_GRAY);
        game.fonts.normal.draw(batch, "[ESC] / [C]: close | [↑↓] or scroll to navigate", sw / 2f - 200,
                SUBTITLE_Y);
        batch.end();

        float bandX = FRAME_X + 8, bandW = sw - bandX * 2;

        // Banda intestazione colonne
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_TOP_BAND);
        sr.rect(bandX, HEADER_BAND_Y, bandW, HEADER_BAND_H);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK);
        sr.rect(bandX, HEADER_BAND_Y, bandW, HEADER_BAND_H);
        sr.end();

        float textY = HEADER_BAND_Y + HEADER_BAND_H - 8;
        batch.begin();
        game.fonts.large.setColor(UITheme.TEXT_GOLD);
        game.fonts.large.draw(batch, "Resource", COL_LABEL_X, textY);
        drawRight(game.fonts.large, "Owned", COL_QTY_R, textY);
        drawRight(game.fonts.large, "Production/min", COL_PROD_R, textY);
        drawRight(game.fonts.large, "Consumption/min", COL_CONS_R, textY);
        drawRight(game.fonts.large, "Balance/min", COL_BAL_R, textY);
        batch.end();
    }

    private void drawResourceList() {
        float sw = Main.SCREEN_W;
        float startY = ROWS_TOP;

        int firstVisible = (int) (scrollOffset / ROW_HEIGHT);
        int visibleCount = Math.min((int) ROWS_VISIBLE + 2, resourceStats.size() - firstVisible);

        for (int i = 0; i < visibleCount; i++) {
            int idx = firstVisible + i;
            if (idx >= resourceStats.size())
                break;

            ResourceStats rs = resourceStats.get(idx);
            float y = startY - (idx * ROW_HEIGHT) + scrollOffset;
            if (y < FRAME_BOTTOM + ROW_HEIGHT || y > startY + 2)
                continue;

            float rowX = FRAME_X + 8, rowW = sw - rowX * 2;
            Color rowColor = (idx % 2 == 0) ? new Color(0.10f, 0.10f, 0.15f, 1f) : new Color(0.06f, 0.06f, 0.10f, 1f);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(rowColor);
            sr.rect(rowX, y - ROW_HEIGHT + 6, rowW, ROW_HEIGHT - 2);
            sr.end();

            Texture icon = game.icons != null ? game.icons.getResourceIcon(rs.id) : null;
            float iconX = FRAME_X + 8 + 18;
            float iconY = y - ROW_HEIGHT / 2f - ROW_ICON_SIZE / 2f + 6f;

            if (icon == null) {
                ResourceManager.Resource res = gameState.resources.getAll().get(rs.id);
                float[] c = res != null ? res.color : new float[] { 0.6f, 0.6f, 0.6f };
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(c[0] * 0.85f, c[1] * 0.85f, c[2] * 0.85f, 1f);
                sr.rect(iconX, iconY, ROW_ICON_SIZE, ROW_ICON_SIZE);
                sr.end();
            }

            float textY = y - 12f;

            batch.begin();
            if (icon != null) {
                batch.setColor(1f, 1f, 1f, 1f);
                batch.draw(icon, iconX, iconY, ROW_ICON_SIZE, ROW_ICON_SIZE);
            }
            game.fonts.normal.setColor(rs.isDeficit ? new Color(1f, 0.5f, 0.4f, 1f) : Color.WHITE);
            game.fonts.normal.draw(batch, rs.label, COL_LABEL_X, textY);

            game.fonts.large.setColor(0.4f, 0.9f, 0.4f, 1f);
            drawRight(game.fonts.large, formatRate(rs.production), COL_PROD_R, textY);
            game.fonts.large.setColor(Color.WHITE);
            drawRight(game.fonts.large, formatRate(rs.amount), COL_QTY_R, textY);
            game.fonts.large.setColor(0.9f, 0.4f, 0.4f, 1f);
            drawRight(game.fonts.large, formatRate(rs.consumption), COL_CONS_R, textY);

            String balanceStr = formatBalance(rs.balance);
            Color balanceColor = rs.balance >= 1 ? new Color(0.3f, 0.9f, 0.3f, 1f)
                    : (rs.balance <= -1 ? new Color(0.9f, 0.3f, 0.3f, 1f) : new Color(0.7f, 0.7f, 0.4f, 1f));
            game.fonts.large.setColor(balanceColor);
            drawRight(game.fonts.large, balanceStr, COL_BAL_R, textY);
            batch.end();
        }

        if (maxScroll > 0) {
            float trackTop = ROWS_TOP;
            float trackBottom = FRAME_BOTTOM + 4;
            float trackH = trackTop - trackBottom;
            float barH = Math.max(24f, (ROWS_VISIBLE / resourceStats.size()) * trackH);
            float t = scrollOffset / maxScroll;
            float barY = trackTop - barH - t * (trackH - barH);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0.3f, 0.3f, 0.4f, 0.8f);
            sr.rect(sw - FRAME_X - 14, barY, 8, barH);
            sr.end();
        }
    }

    private String formatRate(float rate) {
        if (rate < 0.0005f)
            return "0";
        if (rate < 0.1f)
            return String.format("%.3f", rate); // consumi minuscoli (es. cabin): sempre visibili
        if (rate < 10f)
            return String.format("%.1f", rate);
        return String.valueOf((int) rate);
    }

    private String formatBalance(float balance) {
        if (Math.abs(balance) < 0.0005f)
            return "0";
        String sign = balance > 0 ? "+" : "-";
        return sign + formatRate(Math.abs(balance));
    }

    @Override
    public void resize(int w, int h) {
        camera.update();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        sr.dispose();
        batch.dispose();
    }
}