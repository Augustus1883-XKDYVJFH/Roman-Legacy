package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Vector3;
import com.RomanLegacy.UITheme;

public class SettingsScreen implements Screen {

    private final Main game;
    private final OrthographicCamera camera;
    private final ShapeRenderer sr;
    private final SpriteBatch batch;
    private final Screen previousScreen;
    private final GlyphLayout layout = new GlyphLayout();

    // ── Layout ─────────────────────────────────────────────────
    private static final float TAB_W = 200f;
    private static final float TAB_H = 44f;
    private static final float TAB_Y = 900f;
    private static final float PANEL_X = 200f;
    private static final float PANEL_Y = 150f;
    private static final float PANEL_W = 1520f;
    private static final float PANEL_H = 720f;

    private static final float ROW_H = 52f;
    private static final float COL_LABEL_X = 250f;
    private static final float COL_VALUE_X = 700f;
    private static final float SLIDER_W = 300f;
    // Le 16 keybinding non entrano in una sola colonna nell'altezza del
    // pannello: le dividiamo su due colonne da 8 righe ciascuna.
    private static final int CONTROLS_ROWS = 8;
    private static final float CONTROLS_COL2_OFFSET = 760f;

    // Range reale della brightness: NON è 0..1 come gli altri slider,
    // quindi va sempre convertita da/verso la posizione normalizzata dello
    // slider (0..1) invece di essere assegnata direttamente.
    private static final float BRIGHTNESS_MIN = 0.5f;
    private static final float BRIGHTNESS_MAX = 1.5f;

    // ── Tabs ───────────────────────────────────────────────────
    private enum Tab {
        CONTROLS, GRAPHICS, AUDIO, OTHER
    }

    private Tab currentTab = Tab.CONTROLS;

    // ── Keybinding remap ───────────────────────────────────────
    private int remapIndex = -1;
    private String[] controlLabels = {
            "Move Up", "Move Down", "Move Left", "Move Right",
            "Zoom +", "Zoom -", "Cancel (key)", "Cancel (right click)",
            "Factions [F]", "Balance [C]", "Trade [T]",
            "Pause [P]", "Quick save [F5]", "Quick load [F9]",
            "Switch map [M]", "Settings [O]"
    };

    // ── Slider drag ────────────────────────────────────────────
    private boolean draggingMusic = false;
    private boolean draggingSfx = false;
    private boolean draggingBrightness = false;
    private boolean draggingAutoSave = false;

    private final Vector3 mousePos = new Vector3();

    public SettingsScreen(Main game) {
        this(game, null);
    }

    public SettingsScreen(Main game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        sr = new ShapeRenderer();
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        updateMousePos();
        handleInput();

        ScreenUtils.clear(UITheme.BG_DARK.r, UITheme.BG_DARK.g, UITheme.BG_DARK.b, 1);
        camera.update();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawBackground();
        drawTitle();
        drawTabs();
        drawCurrentTab();
        drawFooter();

        if (remapIndex >= 0)
            drawRemapOverlay();
    }

    private void updateMousePos() {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
    }

    private void handleInput() {
        // Remap mode
        if (remapIndex >= 0) {
            for (int k = 0; k < 256; k++) {
                if (Gdx.input.isKeyJustPressed(k)) {
                    applyRemap(remapIndex, k);
                    remapIndex = -1;
                    return;
                }
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            closeSettings();
            return;
        }

        handleSliderDrag();
        handleMouseClick();
    }

    private void closeSettings() {
        GameSettings.get().save();
        applySettings();
        if (previousScreen != null) {
            game.setScreen(previousScreen);
        } else {
            game.setScreen(new MenuScreen(game));
        }
    }

    private void handleSliderDrag() {
        float mx = mousePos.x;
        boolean mouseDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        if (!mouseDown) {
            if (draggingMusic || draggingSfx || draggingBrightness || draggingAutoSave) {
                GameSettings.get().save();
                applyAudioSettings();
            }
            draggingMusic = draggingSfx = draggingBrightness = draggingAutoSave = false;
            return;
        }

        if (draggingMusic) {
            GameSettings.get().musicVolume = sliderValue(mx, sliderX(), SLIDER_W);
            applyAudioSettings();
        } else if (draggingSfx) {
            GameSettings.get().sfxVolume = sliderValue(mx, sliderX(), SLIDER_W);
        } else if (draggingBrightness) {
            GameSettings.get().brightness = BRIGHTNESS_MIN
                    + sliderValue(mx, sliderX(), SLIDER_W) * (BRIGHTNESS_MAX - BRIGHTNESS_MIN);
        } else if (draggingAutoSave) {
            GameSettings.get().autoSaveInterval = (int) (sliderValue(mx, sliderX(), SLIDER_W) * 180) + 30;
        }
    }

    private void handleMouseClick() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
            return;

        float mx = mousePos.x;
        float my = mousePos.y;

        // Tabs
        for (int i = 0; i < Tab.values().length; i++) {
            float tabX = (Main.SCREEN_W - Tab.values().length * TAB_W) / 2 + i * TAB_W;
            if (mx >= tabX && mx <= tabX + TAB_W && my >= TAB_Y && my <= TAB_Y + TAB_H) {
                currentTab = Tab.values()[i];
                game.audio.playSound(AudioManager.SFX_UI_CLICK);
                return;
            }
        }

        // Footer button
        if (mx >= 60 && mx <= 260 && my >= 30 && my <= 70) {
            game.audio.playSound(AudioManager.SFX_UI_CLICK);
            closeSettings();
            return;
        }

        if (currentTab == Tab.CONTROLS) {
            handleControlsClick(mx, my);
        } else if (currentTab == Tab.GRAPHICS) {
            handleGraphicsClick(mx, my);
        } else if (currentTab == Tab.AUDIO) {
            handleAudioClick(mx, my);
        } else if (currentTab == Tab.OTHER) {
            handleOtherClick(mx, my);
        }
    }

    private void handleControlsClick(float mx, float my) {
        GameSettings s = GameSettings.get();

        for (int i = 0; i < controlLabels.length; i++) {
            int col = i / CONTROLS_ROWS;
            int row = i % CONTROLS_ROWS;
            float valueX = COL_VALUE_X + col * CONTROLS_COL2_OFFSET;
            float y = rowY(row + 1);
            boolean isMouseToggle = controlLabels[i].equals("Cancel (right click)");
            float boxW = isMouseToggle ? 80 : 200;

            if (my >= y - 18 && my <= y + 10 && mx >= valueX - 10 && mx <= valueX + boxW + 10) {
                if (isMouseToggle) {
                    s.keys.cancelMouseRight = !s.keys.cancelMouseRight;
                    s.save();
                    game.audio.playSound(AudioManager.SFX_UI_CLICK);
                } else {
                    remapIndex = i;
                    game.audio.playSound(AudioManager.SFX_UI_CLICK);
                }
                return;
            }
        }
    }

    private void handleGraphicsClick(float mx, float my) {
        GameSettings s = GameSettings.get();

        float y = rowY(0);
        if (mx >= COL_VALUE_X - 10 && mx <= COL_VALUE_X + 80 && my >= y - 18 && my <= y + 10) {
            s.fullscreen = !s.fullscreen;
            applyGraphicsSettings();
            s.save();
            game.audio.playSound(AudioManager.SFX_UI_CLICK);
            return;
        }

        y = rowY(1);
        if (mx >= sliderX() - 10 && mx <= sliderX() + SLIDER_W + 10 && my >= y - 18 && my <= y + 10) {
            draggingBrightness = true;
        }
    }

    private void handleAudioClick(float mx, float my) {
        GameSettings s = GameSettings.get();

        // Music toggle
        float y = rowY(0);
        if (mx >= COL_VALUE_X - 10 && mx <= COL_VALUE_X + 80 && my >= y - 18 && my <= y + 10) {
            s.musicEnabled = !s.musicEnabled;
            applyAudioSettings();
            s.save();
            game.audio.playSound(AudioManager.SFX_UI_CLICK);
            return;
        }

        // Music slider
        y = rowY(1);
        if (mx >= sliderX() - 10 && mx <= sliderX() + SLIDER_W + 10 && my >= y - 18 && my <= y + 10) {
            draggingMusic = true;
            return;
        }

        // SFX toggle
        y = rowY(2);
        if (mx >= COL_VALUE_X - 10 && mx <= COL_VALUE_X + 80 && my >= y - 18 && my <= y + 10) {
            s.sfxEnabled = !s.sfxEnabled;
            s.save();
            game.audio.playSound(AudioManager.SFX_UI_CLICK);
            return;
        }

        // SFX slider
        y = rowY(3);
        if (mx >= sliderX() - 10 && mx <= sliderX() + SLIDER_W + 10 && my >= y - 18 && my <= y + 10) {
            draggingSfx = true;
        }
    }

    private void handleOtherClick(float mx, float my) {
        float y = rowY(0);
        if (mx >= sliderX() - 10 && mx <= sliderX() + SLIDER_W + 10 && my >= y - 18 && my <= y + 10) {
            draggingAutoSave = true;
        }
    }

    // ── Disegno ─────────────────────────────────────────────────
    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_DARK);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);

        sr.setColor(UITheme.BG_DECORATIVE);
        sr.rect(0, Main.SCREEN_H - 230, Main.SCREEN_W, 230);
        sr.end();

        UITheme.drawPanel(sr, PANEL_X, PANEL_Y, PANEL_W, PANEL_H);
    }

    private void drawTitle() {
        batch.begin();
        String title = "SETTINGS";
        layout.setText(game.fonts.huge, title);
        game.fonts.huge.setColor(UITheme.TEXT_GOLD);
        // Posizione dedicata (non UITheme.TITLE_Y_OFFSET, tarato per i menu
        // senza tab sotto): deve lasciare spazio alle tab che iniziano a TAB_Y.
        game.fonts.huge.draw(batch, title,
                Main.SCREEN_W / 2f - layout.width / 2f,
                Main.SCREEN_H - 55f);
        batch.end();
    }

    private void drawTabs() {
        float startX = (Main.SCREEN_W - Tab.values().length * TAB_W) / 2;
        String[] tabNames = { "CONTROLS", "GRAPHICS", "AUDIO", "OTHER" };

        for (int i = 0; i < tabNames.length; i++) {
            float tabX = startX + i * TAB_W;
            boolean active = currentTab.ordinal() == i;
            boolean hovered = !active && mousePos.x >= tabX && mousePos.x <= tabX + TAB_W
                    && mousePos.y >= TAB_Y && mousePos.y <= TAB_Y + TAB_H;

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(active ? UITheme.SELECTION_BG : (hovered ? UITheme.HOVER_BG : UITheme.BG_PANEL));
            sr.rect(tabX, TAB_Y, TAB_W, TAB_H);
            if (active) {
                sr.setColor(UITheme.GOLD_PRIMARY);
                sr.rect(tabX, TAB_Y, TAB_W, 4f);
            }
            sr.end();

            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(active ? UITheme.GOLD_PRIMARY : UITheme.GOLD_DARK);
            sr.rect(tabX, TAB_Y, TAB_W, TAB_H);
            sr.end();

            batch.begin();
            layout.setText(game.fonts.normal, tabNames[i]);
            game.fonts.normal.setColor(active || hovered ? UITheme.TEXT_GOLD : UITheme.TEXT_GRAY);
            game.fonts.normal.draw(batch, tabNames[i],
                    tabX + TAB_W / 2f - layout.width / 2f, TAB_Y + TAB_H / 2f + 7f);
            batch.end();
        }
    }

    private void drawCurrentTab() {
        switch (currentTab) {
            case CONTROLS:
                drawControlsTab();
                break;
            case GRAPHICS:
                drawGraphicsTab();
                break;
            case AUDIO:
                drawAudioTab();
                break;
            case OTHER:
                drawOtherTab();
                break;
        }
    }

    private void drawControlsTab() {
        GameSettings s = GameSettings.get();
        int[] keycodes = getKeycodes(s);

        batch.begin();
        game.fonts.large.setColor(UITheme.TEXT_GOLD);
        game.fonts.large.draw(batch, "Key bindings", COL_LABEL_X, rowY(0) + 30);
        batch.end();

        for (int i = 0; i < controlLabels.length; i++) {
            int col = i / CONTROLS_ROWS;
            int row = i % CONTROLS_ROWS;
            float labelX = COL_LABEL_X + col * CONTROLS_COL2_OFFSET;
            float valueX = COL_VALUE_X + col * CONTROLS_COL2_OFFSET;
            float y = rowY(row + 1);

            boolean waiting = (remapIndex == i);
            boolean isMouseToggle = controlLabels[i].equals("Cancel (right click)");
            float boxW = isMouseToggle ? 80 : 200;
            boolean hovered = !waiting && mousePos.x >= valueX - 10 && mousePos.x <= valueX + boxW + 10
                    && mousePos.y >= y - 18 && mousePos.y <= y + 10;

            batch.begin();
            game.fonts.normal.setColor(UITheme.TEXT_WHITE);
            game.fonts.normal.draw(batch, controlLabels[i], labelX, y);
            batch.end();

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(waiting ? UITheme.SELECTION_BG : (hovered ? UITheme.HOVER_BG : UITheme.BG_PANEL));
            sr.rect(valueX - 4, y - 20, boxW, 30);
            sr.end();

            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(waiting ? UITheme.GOLD_PRIMARY : UITheme.GOLD_DARK);
            sr.rect(valueX - 4, y - 20, boxW, 30);
            sr.end();

            batch.begin();
            String text;
            if (isMouseToggle) {
                text = s.keys.cancelMouseRight ? "ON" : "OFF";
            } else if (waiting) {
                text = "Press a key...";
            } else {
                text = GameSettings.keyName(keycodes[i]);
            }
            drawCenteredBoxText(text, valueX - 4, y - 20, boxW, 30, UITheme.TEXT_GOLD);
            batch.end();
        }
    }

    private void drawGraphicsTab() {
        GameSettings s = GameSettings.get();
        drawToggleRow(0, "Fullscreen", s.fullscreen);
        float brightness = Math.max(BRIGHTNESS_MIN, Math.min(BRIGHTNESS_MAX, s.brightness));
        drawSliderRow(1, "Brightness", brightness, BRIGHTNESS_MIN, BRIGHTNESS_MAX);
    }

    private void drawAudioTab() {
        GameSettings s = GameSettings.get();

        drawToggleRow(0, "Music", s.musicEnabled);
        drawSliderRow(1, "Music volume", s.musicVolume, 0f, 1f);
        drawToggleRow(2, "Sound effects", s.sfxEnabled);
        drawSliderRow(3, "SFX volume", s.sfxVolume, 0f, 1f);
    }

    private void drawOtherTab() {
        GameSettings s = GameSettings.get();
        float autoSavePercent = (s.autoSaveInterval - 30) / 180f;
        int minutes = s.autoSaveInterval / 60;
        int seconds = s.autoSaveInterval % 60;
        String timeStr = minutes > 0 ? minutes + " min" : seconds + " sec";
        if (minutes > 0 && seconds > 0)
            timeStr = minutes + "m " + seconds + "s";

        drawSliderRow(0, "Auto-save (every)", autoSavePercent, 0f, 1f, timeStr);
    }

    // ── Helper per disegno ──────────────────────────────────────

    private void drawToggleRow(int row, String label, boolean value) {
        float y = rowY(row);
        boolean hovered = mousePos.x >= COL_VALUE_X - 10 && mousePos.x <= COL_VALUE_X + 80
                && mousePos.y >= y - 18 && mousePos.y <= y + 10;

        batch.begin();
        game.fonts.normal.setColor(UITheme.TEXT_WHITE);
        game.fonts.normal.draw(batch, label, COL_LABEL_X, y);
        batch.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(value ? UITheme.STATUS_GREEN.r * 0.35f : UITheme.BG_PANEL.r,
                value ? UITheme.STATUS_GREEN.g * 0.35f : UITheme.BG_PANEL.g,
                value ? UITheme.STATUS_GREEN.b * 0.35f : UITheme.BG_PANEL.b, 1f);
        sr.rect(COL_VALUE_X - 4, y - 20, 80, 28);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(hovered ? UITheme.GOLD_PRIMARY : UITheme.GOLD_DARK);
        sr.rect(COL_VALUE_X - 4, y - 20, 80, 28);
        sr.end();

        batch.begin();
        drawCenteredBoxText(value ? "ON" : "OFF", COL_VALUE_X - 4, y - 20, 80, 28,
                value ? UITheme.STATUS_GREEN : UITheme.TEXT_GRAY);
        batch.end();
    }

    private void drawSliderRow(int row, String label, float value, float min, float max) {
        // Il testo deve riflettere la posizione del cursore sullo slider
        // (normalizzata su [min,max]), non il valore grezzo * 100: per la
        // brightness (range 0.5-1.5) questo mostrava percentuali sballate
        // di 50 punti rispetto alla posizione reale del cursore.
        float norm = (max > min) ? (value - min) / (max - min) : 0f;
        drawSliderRow(row, label, value, min, max, (int) (norm * 100) + "%");
    }

    private void drawSliderRow(int row, String label, float value, float min, float max, String valueText) {
        float y = rowY(row);
        float sliderX = sliderX();

        batch.begin();
        game.fonts.normal.setColor(UITheme.TEXT_WHITE);
        game.fonts.normal.draw(batch, label, COL_LABEL_X, y);
        game.fonts.small.setColor(UITheme.TEXT_GRAY);
        game.fonts.small.draw(batch, valueText, sliderX + SLIDER_W + 15, y);
        batch.end();

        float normValue = (value - min) / (max - min);
        float sliderY = y - 12;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_PANEL.r * 1.6f, UITheme.BG_PANEL.g * 1.6f, UITheme.BG_PANEL.b * 1.6f, 1f);
        sr.rect(sliderX, sliderY, SLIDER_W, 12);
        sr.setColor(UITheme.GOLD_PRIMARY);
        sr.rect(sliderX, sliderY, SLIDER_W * normValue, 12);
        sr.setColor(UITheme.TEXT_WHITE);
        sr.rect(sliderX + SLIDER_W * normValue - 5, sliderY - 4, 10, 20);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_DARK);
        sr.rect(sliderX, sliderY, SLIDER_W, 12);
        sr.end();
    }

    private void drawFooter() {
        boolean hovered = mousePos.x >= 60 && mousePos.x <= 260 && mousePos.y >= 30 && mousePos.y <= 70;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_TOP_BAND);
        sr.rect(0, 0, Main.SCREEN_W, 80);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hovered ? UITheme.HOVER_BG : UITheme.BG_PANEL);
        sr.rect(60, 30, 200, 40);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(hovered ? UITheme.GOLD_PRIMARY : UITheme.GOLD_DARK);
        sr.rect(60, 30, 200, 40);
        sr.end();

        batch.begin();
        game.fonts.normal.setColor(UITheme.TEXT_GOLD);
        game.fonts.normal.draw(batch, "<- Back to menu", 76, 55);
        batch.end();
    }

    private void drawRemapOverlay() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.8f);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);
        sr.end();

        String title = "Press the new key for: " + controlLabels[remapIndex];
        String hint = "(ESC to cancel)";

        layout.setText(game.fonts.large, title);
        float boxW = layout.width + 80f;
        float boxH = 140f;
        float boxX = Main.SCREEN_W / 2f - boxW / 2f;
        float boxY = Main.SCREEN_H / 2f - boxH / 2f;

        UITheme.drawPanel(sr, boxX, boxY, boxW, boxH);

        batch.begin();
        game.fonts.large.setColor(UITheme.TEXT_GOLD);
        game.fonts.large.draw(batch, title, Main.SCREEN_W / 2f - layout.width / 2f, boxY + boxH - 40);

        layout.setText(game.fonts.normal, hint);
        game.fonts.normal.setColor(UITheme.TEXT_GRAY);
        game.fonts.normal.draw(batch, hint, Main.SCREEN_W / 2f - layout.width / 2f, boxY + boxH - 90);
        batch.end();
    }

    // ── Helper ─────────────────────────────────────────────────

    private float rowY(int row) {
        return PANEL_Y + PANEL_H - 70 - row * ROW_H;
    }

    private float sliderX() {
        return COL_VALUE_X;
    }

    private float sliderValue(float mx, float sliderX, float width) {
        return Math.max(0f, Math.min(1f, (mx - sliderX) / width));
    }

    /** Disegna testo centrato orizzontalmente e verticalmente dentro un box. */
    private void drawCenteredBoxText(String text, float boxX, float boxY, float boxW, float boxH, Color color) {
        layout.setText(game.fonts.normal, text);
        game.fonts.normal.setColor(color);
        float tx = boxX + boxW / 2f - layout.width / 2f;
        float ty = boxY + (boxH + layout.height) / 2f;
        game.fonts.normal.draw(batch, text, tx, ty);
    }

    private int[] getKeycodes(GameSettings s) {
        return new int[] {
                s.keys.moveUp, s.keys.moveDown, s.keys.moveLeft, s.keys.moveRight,
                s.keys.zoomIn, s.keys.zoomOut, s.keys.cancel, 0,
                s.keys.toggleFactions, s.keys.openConsumption, s.keys.openTrade,
                s.keys.pause, s.keys.quickSave, s.keys.quickLoad,
                s.keys.switchMap, s.keys.openSettings
        };
    }

    private void applyRemap(int index, int keycode) {
        if (keycode == Input.Keys.ESCAPE)
            return;

        GameSettings s = GameSettings.get();
        switch (index) {
            case 0:
                s.keys.moveUp = keycode;
                break;
            case 1:
                s.keys.moveDown = keycode;
                break;
            case 2:
                s.keys.moveLeft = keycode;
                break;
            case 3:
                s.keys.moveRight = keycode;
                break;
            case 4:
                s.keys.zoomIn = keycode;
                break;
            case 5:
                s.keys.zoomOut = keycode;
                break;
            case 6:
                s.keys.cancel = keycode;
                break;
            case 7:
                break; // mouse toggle
            case 8:
                s.keys.toggleFactions = keycode;
                break;
            case 9:
                s.keys.openConsumption = keycode;
                break;
            case 10:
                s.keys.openTrade = keycode;
                break;
            case 11:
                s.keys.pause = keycode;
                break;
            case 12:
                s.keys.quickSave = keycode;
                break;
            case 13:
                s.keys.quickLoad = keycode;
                break;
            case 14:
                s.keys.switchMap = keycode;
                break;
            case 15:
                s.keys.openSettings = keycode;
                break;
        }
        s.save();
        game.audio.playSound(AudioManager.SFX_UI_CLICK);
    }

    private void applySettings() {
        // NOTA: applyGraphicsSettings() NON va richiamato qui. Il toggle
        // fullscreen la applica già immediatamente in handleGraphicsClick();
        // richiamarla anche alla chiusura forzava sempre setWindowedMode()
        // con le dimensioni di default (1280x720), rimpicciolendo la
        // finestra ogni volta che si usciva dalle Impostazioni, anche senza
        // aver toccato nulla.
        applyAudioSettings();
        applyControlsSettings();
    }

    private void applyGraphicsSettings() {
        GameSettings s = GameSettings.get();
        if (s.fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            int maxW = Gdx.graphics.getDisplayMode().width;
            int maxH = Gdx.graphics.getDisplayMode().height;
            int w = Math.min(s.windowWidth, maxW);
            int h = Math.min(s.windowHeight, maxH);
            Gdx.graphics.setWindowedMode(w, h);
        }
    }

    private void applyAudioSettings() {
        GameSettings s = GameSettings.get();
        if (game.audio != null) {
            game.audio.applyVolumeSettings();
            if (!s.musicEnabled) {
                game.audio.pauseMusic();
            } else {
                game.audio.resumeMusic();
            }
        }
    }

    private void applyControlsSettings() {
        // I tasti vengono letti direttamente da GameSettings.get() nei vari handler
    }

    // ── Screen lifecycle ────────────────────────────────────────

    @Override
    public void show() {
        game.audio.playSound(AudioManager.SFX_UI_CLICK);
    }

    @Override
    public void resize(int w, int h) {
        camera.update();
    }

    @Override
    public void hide() {
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