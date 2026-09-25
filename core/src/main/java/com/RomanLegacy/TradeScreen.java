package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.RomanLegacy.UITheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Schermata commercio navale.
 * Layout:
 * [Acquista nave] | [Lista flotta] | [Dettaglio nave selezionata]
 *
 * Navigazione:
 * T / ESC = chiudi
 * Click su nave = seleziona
 * Click su slot risorsa = cicla tra le risorse disponibili
 * Click direzione = inverte OW↔NW
 * Click Salpa = dispatch
 */
public class TradeScreen implements Screen {

    // ── Layout
    // ────────────────────────────────────────────────────────────────────
    private static final float COL1_X = 40f; // colonna acquisto
    private static final float COL1_W = 340f;
    private static final float COL2_X = 400f; // colonna lista flotta
    private static final float COL2_W = 400f;
    private static final float COL3_X = 820f; // colonna dettaglio
    private static final float COL3_W = 560f;
    private static final float TOP_Y = 958f;
    private static final float ROW_H = 72f;
    private static final float SHIP_ROW_H = 64f;

    // ── Colori
    // ────────────────────────────────────────────────────────────────────
    private static final Color COL_BG = UITheme.BG_DARK;
    private static final Color COL_PANEL = UITheme.BG_PANEL;
    private static final Color COL_GOLD = UITheme.GOLD_LIGHT;
    private static final Color COL_GRAY = UITheme.TEXT_GRAY;
    private static final Color COL_ACCENT = UITheme.GOLD_PRIMARY;
    private static final Color COL_GREEN = UITheme.STATUS_GREEN;
    private static final Color COL_RED = UITheme.STATUS_RED;
    private static final Color COL_BLUE = UITheme.STATUS_BLUE;
    private static final Color COL_SEL = UITheme.SELECTION_BG;

    // ── Stato UI
    // ──────────────────────────────────────────────────────────────────
    private int selectedShipIndex = -1;
    /** Indice dello slot in attesa di selezione risorsa (-1 = nessuno). */
    private int selectingSlot = -1;
    private float listScrollOffset = 0f;
    private float resourceScrollOffset = 0f;
    private static final float RESOURCE_PICKER_Y = 700f;
    private static final float RESOURCE_PICKER_ROW_H = 28f;
    private static final float RESOURCE_PICKER_MAX_H = 420f;

    /** Lista piatta delle risorse selezionabili (no coin, no population). */
    private final List<String> selectableResources = new ArrayList<>();

    // ── Riferimenti
    // ───────────────────────────────────────────────────────────────
    private final Main game;
    private final GameState state;
    private final GameScreen previousScreen;
    private final OrthographicCamera camera;
    private final ShapeRenderer sr;
    private final SpriteBatch batch;

    public TradeScreen(Main game, GameState state, GameScreen previousScreen) {
        this.game = game;
        this.state = state;
        this.previousScreen = previousScreen;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        sr = new ShapeRenderer();
        batch = new SpriteBatch();
        buildSelectableResources();
    }

    // ── Screen lifecycle
    // ──────────────────────────────────────────────────────────

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (selectingSlot >= 0) {
                    resourceScrollOffset = clamp(resourceScrollOffset + amountY * RESOURCE_PICKER_ROW_H,
                            0f, maxResourceScrollOffset());
                } else {
                    listScrollOffset = Math.max(0,
                            listScrollOffset + amountY * SHIP_ROW_H);
                }
                return true;
            }
        });
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
    public void resize(int w, int h) {
        camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        camera.update();
    }

    @Override
    public void dispose() {
        sr.dispose();
        batch.dispose();
    }

    // ── Render
    // ────────────────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        if (handleInput())
            return;

        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1);
        camera.update();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawBackground();
        drawTitle();
        drawBuyColumn();
        drawFleetColumn();
        drawDetailColumn();
        drawFooter();

        if (selectingSlot >= 0)
            drawResourcePicker();
    }

    // ── Input
    // ─────────────────────────────────────────────────────────────────────

    private boolean handleInput() {
        GameSettings.KeyBindings keys = GameSettings.get().keys;

        if (Gdx.input.isKeyJustPressed(keys.cancel) ||
                Gdx.input.isKeyJustPressed(GameSettings.get().keys.openTrade)) {
            if (selectingSlot >= 0) {
                selectingSlot = -1;
                return false;
            }
            closeToPreviousScreen();
            return true;
        }

        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
            return false;
        float mx = mx(), my = my();

        // Resource picker aperto — intercetta click
        if (selectingSlot >= 0) {
            handleResourcePickerClick(mx, my);
            return false;
        }

        // Torna al gioco (footer)
        if (mx >= 40 && mx <= 240 && my >= 28 && my <= 60) {
            closeToPreviousScreen();
            return true;
        }

        // Colonna acquisto — click su pulsante buy
        handleBuyClick(mx, my);

        // Colonna lista — selezione nave
        handleFleetListClick(mx, my);

        // Colonna dettaglio — interazione con nave selezionata
        if (selectedShipIndex >= 0 && selectedShipIndex < state.tradeManager.getFleet().size())
            handleDetailClick(mx, my, state.tradeManager.getFleet().get(selectedShipIndex));
        return false;
    }

    private void handleBuyClick(float mx, float my) {
        ShipType[] types = ShipType.values();
        for (int i = 0; i < types.length; i++) {
            float btnY = TOP_Y - 80 - i * (ROW_H + 16);
            float btnX = COL1_X + 4;
            float btnW = COL1_W - 8;
            float btnH = ROW_H - 4;
            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                boolean bought = state.tradeManager.buyShip(types[i]);
                if (bought)
                    game.audio.playSound(AudioManager.SFX_UI_CLICK);
                return;
            }
        }
    }

    private void handleFleetListClick(float mx, float my) {
        List<Ship> fleet = state.tradeManager.getFleet();
        float startY = TOP_Y - 80;
        for (int i = 0; i < fleet.size(); i++) {
            float ry = startY - i * SHIP_ROW_H + listScrollOffset;
            if (ry < 80 || ry > TOP_Y)
                continue;
            if (mx >= COL2_X && mx <= COL2_X + COL2_W
                    && my >= ry - SHIP_ROW_H && my <= ry) {
                selectedShipIndex = i;
                selectingSlot = -1;
                game.audio.playSound(AudioManager.SFX_UI_CLICK);
                return;
            }
        }
    }

    private void handleDetailClick(float mx, float my, Ship ship) {
        // Solo se docked
        if (ship.state != Ship.State.DOCKED)
            return;

        float detailStartY = TOP_Y - 80;

        // Pulsante direzione
        float dirBtnY = detailStartY - 10;
        if (mx >= COL3_X + 4 && mx <= COL3_X + 200 && my >= dirBtnY - 28 && my <= dirBtnY) {
            ship.toNewWorld = !ship.toNewWorld;
            game.audio.playSound(AudioManager.SFX_UI_CLICK);
            return;
        }

        // Slot risorse
        for (int i = 0; i < ship.type.slots; i++) {
            float slotY = detailStartY - 60 - i * 52f;
            if (mx >= COL3_X + 4 && mx <= COL3_X + COL3_W - 8
                    && my >= slotY - 36 && my <= slotY) {
                selectingSlot = i;
                resourceScrollOffset = 0f;
                return;
            }
        }

        // Pulsante Salpa
        float salpaBtnY = detailStartY - 60 - ship.type.slots * 52f - 20;
        if (mx >= COL3_X + 4 && mx <= COL3_X + 200 && my >= salpaBtnY - 30 && my <= salpaBtnY) {
            boolean ok = state.tradeManager.dispatch(ship);
            if (ok)
                game.audio.playSound(AudioManager.SFX_EXPEDITION_START);
            return;
        }
    }

    private void handleResourcePickerClick(float mx, float my) {
        float pickerX = COL3_X + 4;
        float pickerW = COL3_W - 8;
        float pickerY = RESOURCE_PICKER_Y;
        float rowH = RESOURCE_PICKER_ROW_H;
        float pickerH = resourcePickerHeight();

        // Click fuori = chiudi
        if (mx < pickerX || mx > pickerX + pickerW
                || my < pickerY - pickerH
                || my > pickerY + 30) {
            selectingSlot = -1;
            return;
        }

        // Click su "Nessuna" (prima riga sotto al titolo)
        if (my >= pickerY - rowH && my <= pickerY) {
            Ship ship = state.tradeManager.getFleet().get(selectedShipIndex);
            ship.assignedResources[selectingSlot] = null;
            selectingSlot = -1;
            return;
        }

        int idx = (int) ((pickerY - rowH - my + resourceScrollOffset) / rowH);
        if (idx >= 0 && idx < selectableResources.size()) {
            Ship ship = state.tradeManager.getFleet().get(selectedShipIndex);
            ship.assignedResources[selectingSlot] = selectableResources.get(idx);
            selectingSlot = -1;
            game.audio.playSound(AudioManager.SFX_UI_CLICK);
        }
    }

    // ── Disegno
    // ───────────────────────────────────────────────────────────────────

    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_DARK);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);

        // Decorative top band
        sr.setColor(UITheme.BG_DECORATIVE);
        sr.rect(0, Main.SCREEN_H - 200, Main.SCREEN_W, 200);

        sr.end();

        // Pannelli colonne con cornice bronzo/oro condivisa (lasciano spazio in
        // alto al titolo e in basso al footer).
        float colH = Main.SCREEN_H - 180;
        UITheme.drawPanel(sr, COL1_X, 80, COL1_W, colH);
        UITheme.drawPanel(sr, COL2_X, 80, COL2_W, colH);
        UITheme.drawPanel(sr, COL3_X, 80, COL3_W, colH);
    }

    private void drawTitle() {
        batch.begin();
        game.fonts.title.setColor(UITheme.TEXT_GOLD);
        game.fonts.title.draw(batch, "ROTTE COMMERCIALI", 40, Main.SCREEN_H - 30);

        game.fonts.normal.setColor(UITheme.TEXT_GRAY);
        game.fonts.normal.draw(batch,
                "Flotta: " + state.tradeManager.getFleet().size() + " navi  |  " +
                        "Manutenzione: " + (int) state.tradeManager.totalMaintenancePerMin() + "/min",
                44, Main.SCREEN_H - 68);
        batch.end();
    }

    private void drawBuyColumn() {
        batch.begin();
        game.fonts.large.setColor(COL_GOLD);
        game.fonts.large.draw(batch, "Acquista nave", COL1_X + 8, TOP_Y - 10);
        batch.end();

        ShipType[] types = ShipType.values();
        for (int i = 0; i < types.length; i++) {
            ShipType t = types[i];
            float btnY = TOP_Y - 80 - i * (ROW_H + 16);
            boolean canAfford = state.resources.get("coin") >= t.purchaseCost;

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(canAfford ? Color.valueOf("1a2a1a") : Color.valueOf("2a1a1a"));
            sr.rect(COL1_X + 4, btnY, COL1_W - 8, ROW_H - 4);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(canAfford ? COL_GREEN : COL_RED);
            sr.rect(COL1_X + 4, btnY, COL1_W - 8, ROW_H - 4);
            sr.end();

            batch.begin();
            game.fonts.normal.setColor(canAfford ? Color.WHITE : COL_GRAY);
            game.fonts.normal.draw(batch, t.label, COL1_X + 12, btnY + ROW_H - 10);
            game.fonts.small.setColor(COL_GRAY);
            game.fonts.small.draw(batch,
                    "Slot: " + t.slots + "  Stack: " + t.stackSize +
                            "  Viaggio: " + (int) t.travelSecs + "s",
                    COL1_X + 12, btnY + ROW_H - 28);
            game.fonts.small.setColor(canAfford ? COL_GOLD : COL_RED);
            game.fonts.small.draw(batch,
                    "Costo: " + (int) t.purchaseCost + "  Man: " + (int) t.maintenancePerMin + "/min",
                    COL1_X + 12, btnY + ROW_H - 44);
            batch.end();
        }
    }

    private void drawFleetColumn() {
        batch.begin();
        game.fonts.large.setColor(COL_GOLD);
        game.fonts.large.draw(batch, "Flotta", COL2_X + 8, TOP_Y - 10);
        batch.end();

        List<Ship> fleet = state.tradeManager.getFleet();
        if (fleet.isEmpty()) {
            batch.begin();
            game.fonts.normal.setColor(COL_GRAY);
            game.fonts.normal.draw(batch, "Nessuna nave acquistata.", COL2_X + 12, TOP_Y - 80);
            batch.end();
            return;
        }

        float startY = TOP_Y - 80;
        for (int i = 0; i < fleet.size(); i++) {
            Ship ship = fleet.get(i);
            float ry = startY - i * SHIP_ROW_H + listScrollOffset;
            if (ry < 80 || ry > TOP_Y)
                continue;

            boolean selected = (i == selectedShipIndex);

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(selected ? COL_SEL : Color.valueOf("0e0e1a"));
            sr.rect(COL2_X + 4, ry - SHIP_ROW_H + 4, COL2_W - 8, SHIP_ROW_H - 6);
            sr.end();
            if (selected) {
                sr.begin(ShapeRenderer.ShapeType.Line);
                sr.setColor(COL_ACCENT);
                sr.rect(COL2_X + 4, ry - SHIP_ROW_H + 4, COL2_W - 8, SHIP_ROW_H - 6);
                sr.end();
            }

            // Barra progresso viaggio
            if (ship.state == Ship.State.TRAVELING) {
                float barW = (COL2_W - 16) * ship.travelProgress();
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(COL_BLUE);
                sr.rect(COL2_X + 4, ry - SHIP_ROW_H + 4, barW, 4);
                sr.end();
            }

            Color stateColor = ship.state == Ship.State.DOCKED ? COL_GREEN
                    : ship.state == Ship.State.TRAVELING ? COL_BLUE
                            : COL_GOLD;

            batch.begin();
            game.fonts.normal.setColor(Color.WHITE);
            game.fonts.normal.draw(batch, ship.name, COL2_X + 10, ry - 10);
            game.fonts.small.setColor(COL_GRAY);
            game.fonts.small.draw(batch, ship.directionLabel(), COL2_X + 10, ry - 28);
            game.fonts.small.setColor(stateColor);
            game.fonts.small.draw(batch, stateLabel(ship), COL2_X + 180, ry - 28);
            batch.end();
        }
    }

    private void drawDetailColumn() {
        batch.begin();
        game.fonts.large.setColor(COL_GOLD);
        game.fonts.large.draw(batch, "Dettaglio", COL3_X + 8, TOP_Y - 10);
        batch.end();

        if (selectedShipIndex < 0 || selectedShipIndex >= state.tradeManager.getFleet().size()) {
            batch.begin();
            game.fonts.normal.setColor(COL_GRAY);
            game.fonts.normal.draw(batch, "Seleziona una nave dalla lista.", COL3_X + 12, TOP_Y - 80);
            batch.end();
            return;
        }

        Ship ship = state.tradeManager.getFleet().get(selectedShipIndex);
        float detailStartY = TOP_Y - 80;
        boolean docked = ship.state == Ship.State.DOCKED;

        // Info nave
        batch.begin();
        game.fonts.normal.setColor(Color.WHITE);
        game.fonts.normal.draw(batch, ship.type.label + " — " + ship.name, COL3_X + 8, detailStartY);
        game.fonts.small.setColor(COL_GRAY);
        game.fonts.small.draw(batch, ship.type.description, COL3_X + 8, detailStartY - 20);
        batch.end();

        // Pulsante direzione
        float dirBtnY = detailStartY - 38;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(docked ? Color.valueOf("1a2a3a") : Color.valueOf("1a1a1a"));
        sr.rect(COL3_X + 4, dirBtnY - 26, 220, 26);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(docked ? COL_BLUE : COL_GRAY);
        sr.rect(COL3_X + 4, dirBtnY - 26, 220, 26);
        sr.end();
        batch.begin();
        game.fonts.normal.setColor(docked ? COL_BLUE : COL_GRAY);
        game.fonts.normal.draw(batch, "Direzione: " + ship.directionLabel(), COL3_X + 10, dirBtnY - 6);
        batch.end();

        // Slot cargo
        for (int i = 0; i < ship.type.slots; i++) {
            float slotY = detailStartY - 78 - i * 52f;
            String resId = ship.assignedResources[i];
            String resLabel = resId != null ? getResourceLabel(resId) : "— vuoto —";
            float cargoAmt = ship.cargo[i];

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(docked ? Color.valueOf("1e1e2e") : Color.valueOf("141418"));
            sr.rect(COL3_X + 4, slotY - 36, COL3_W - 8, 38);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(docked ? COL_ACCENT : COL_GRAY);
            sr.rect(COL3_X + 4, slotY - 36, COL3_W - 8, 38);
            sr.end();

            batch.begin();
            game.fonts.normal.setColor(resId != null ? Color.WHITE : COL_GRAY);
            game.fonts.normal.draw(batch, "Slot " + (i + 1) + ": " + resLabel, COL3_X + 12, slotY - 8);
            if (cargoAmt > 0) {
                game.fonts.small.setColor(COL_GOLD);
                game.fonts.small.draw(batch, "A bordo: " + (int) cargoAmt, COL3_X + 12, slotY - 26);
            } else if (resId != null && docked) {
                float avail = state.resources.get(resId);
                game.fonts.small.setColor(avail > 0 ? COL_GREEN : COL_RED);
                game.fonts.small.draw(batch,
                        "Disponibile: " + (int) avail + "/" + ship.type.stackSize,
                        COL3_X + 12, slotY - 26);
            }
            if (docked) {
                game.fonts.small.setColor(COL_GRAY);
                game.fonts.small.draw(batch, "[clicca per cambiare]", COL3_X + COL3_W - 160, slotY - 8);
            }
            batch.end();
        }

        // Pulsante Salpa / stato viaggio
        float salpaBtnY = detailStartY - 78 - ship.type.slots * 52f - 10;
        if (docked) {
            boolean canDispatch = ship.hasRoute();
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(canDispatch ? Color.valueOf("1a3a1a") : Color.valueOf("2a1a1a"));
            sr.rect(COL3_X + 4, salpaBtnY - 30, 220, 30);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(canDispatch ? COL_GREEN : COL_RED);
            sr.rect(COL3_X + 4, salpaBtnY - 30, 220, 30);
            sr.end();
            batch.begin();
            game.fonts.normal.setColor(canDispatch ? COL_GREEN : COL_GRAY);
            game.fonts.normal.draw(batch,
                    canDispatch ? "Salpa!" : "Configura rotta prima",
                    COL3_X + 12, salpaBtnY - 8);
            batch.end();
        } else {
            // Barra progresso
            float barW = (COL3_W - 8) * ship.travelProgress();
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(Color.valueOf("111128"));
            sr.rect(COL3_X + 4, salpaBtnY - 16, COL3_W - 8, 16);
            sr.setColor(COL_BLUE);
            sr.rect(COL3_X + 4, salpaBtnY - 16, barW, 16);
            sr.end();
            batch.begin();
            game.fonts.small.setColor(Color.WHITE);
            game.fonts.small.draw(batch,
                    "In viaggio " + ship.directionLabel() +
                            "  " + (int) (ship.travelProgress() * 100) + "%",
                    COL3_X + 8, salpaBtnY - 2);
            batch.end();
        }
    }

    private void drawResourcePicker() {
        if (selectedShipIndex < 0)
            return;

        float pickerX = COL3_X + 4;
        float pickerW = COL3_W - 8;
        float rowH = RESOURCE_PICKER_ROW_H;
        float pickerH = resourcePickerHeight();
        float pickerY = RESOURCE_PICKER_Y;

        // Overlay scuro
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.6f);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);
        sr.end();
        // Pannello picker con cornice retro condivisa
        UITheme.drawPanel(sr, pickerX, pickerY - pickerH, pickerW, pickerH);

        batch.begin();
        game.fonts.normal.setColor(COL_GOLD);
        game.fonts.normal.draw(batch,
                "Seleziona risorsa per slot " + (selectingSlot + 1),
                pickerX + 8, pickerY - 6);

        // Riga "Nessuna"
        game.fonts.normal.setColor(COL_GRAY);
        game.fonts.normal.draw(batch, "— Nessuna —", pickerX + 8, pickerY - rowH - 6);

        int firstVisible = Math.max(0, (int) (resourceScrollOffset / rowH));
        int visibleRows = Math.min(selectableResources.size() - firstVisible,
                (int) ((pickerH - rowH - 10) / rowH) + 1);
        for (int i = firstVisible; i < firstVisible + visibleRows; i++) {
            String id = selectableResources.get(i);
            float amt = state.resources.get(id);
            float ry = pickerY - ((i - firstVisible) + 2) * rowH - 6;
            if (ry < pickerY - pickerH)
                break;

            game.fonts.normal.setColor(amt > 0 ? Color.WHITE : COL_GRAY);
            game.fonts.normal.draw(batch, getResourceLabel(id), pickerX + 8, ry);
            game.fonts.small.setColor(COL_GOLD);
            game.fonts.small.draw(batch, (int) amt + "", pickerX + pickerW - 70, ry);
        }
        batch.end();
    }

    private void drawFooter() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.valueOf("0e0e14"));
        sr.rect(0, 0, Main.SCREEN_W, 76);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.valueOf("1a1a2a"));
        sr.rect(40, 20, 200, 36);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(COL_ACCENT);
        sr.rect(40, 20, 200, 36);
        sr.end();

        batch.begin();
        game.fonts.normal.setColor(COL_GOLD);
        game.fonts.normal.draw(batch, "← Torna al gioco", 56, 44);
        game.fonts.small.setColor(COL_GRAY);
        game.fonts.small.draw(batch, "[T] o [ESC] per chiudere", 280, 38);
        batch.end();
    }

    // ── Helper
    // ────────────────────────────────────────────────────────────────────

    private void buildSelectableResources() {
        selectableResources.clear();
        for (ResourceManager.Resource r : state.resources.getAll().values()) {
            if (r.id.equals("coin") || r.id.equals("population"))
                continue;
            selectableResources.add(r.id);
        }
    }

    private String getResourceLabel(String id) {
        ResourceManager.Resource r = state.resources.getAll().get(id);
        return r != null ? r.label : id;
    }

    private String stateLabel(Ship ship) {
        switch (ship.state) {
            case DOCKED:
                return "In porto";
            case TRAVELING:
                return "In viaggio " + (int) (ship.travelProgress() * 100) + "%";
            default:
                return "?";
        }
    }

    private float mx() {
        return Gdx.input.getX() * ((float) Main.SCREEN_W / Gdx.graphics.getWidth());
    }

    private float my() {
        return (Gdx.graphics.getHeight() - Gdx.input.getY())
                * ((float) Main.SCREEN_H / Gdx.graphics.getHeight());
    }

    private void closeToPreviousScreen() {
        game.setScreen(previousScreen);
        dispose();
    }

    private float resourcePickerHeight() {
        return Math.min((selectableResources.size() + 1) * RESOURCE_PICKER_ROW_H + 10,
                RESOURCE_PICKER_MAX_H);
    }

    private float maxResourceScrollOffset() {
        float contentH = selectableResources.size() * RESOURCE_PICKER_ROW_H;
        float visibleH = Math.max(0f, resourcePickerHeight() - RESOURCE_PICKER_ROW_H - 10);
        return Math.max(0f, contentH - visibleH);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}