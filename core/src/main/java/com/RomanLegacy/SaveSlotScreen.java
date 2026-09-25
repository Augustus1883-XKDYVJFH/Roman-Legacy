package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.List;

/**
 * Schermata unica per salvare/caricare/eliminare partite — multi-slot.
 * Interamente utilizzabile solo col mouse, con l'unica eccezione della
 * digitazione del nome di un nuovo salvataggio (richiede necessariamente
 * la tastiera per scrivere lettere) — ma conferma/annulla restano pulsanti.
 *
 * NON usa Gdx.input.getTextInput(): su backend LWJGL3 è inaffidabile
 * (spesso non apre alcun dialog). Il campo nome è disegnato e gestito
 * interamente da questa classe tramite keyTyped/keyDown.
 */
public class SaveSlotScreen implements Screen {

    private final Main game;
    private final Screen previousScreen;
    private final GameScreen activeGameScreen; // non-null solo se allowSave
    private final boolean allowSave;

    private final OrthographicCamera camera;
    private final ShapeRenderer sr;
    private final SpriteBatch batch;
    private final GlyphLayout layout = new GlyphLayout();

    private List<SaveManager.SaveSlotInfo> slots;
    private float scrollOffset = 0f;
    private float maxScroll = 0f;
    private float scrollDelta = 0f;

    private static final float FRAME_X = 16f;
    private static final float FRAME_BOTTOM = 90f;
    private static final float FRAME_TOP = Main.SCREEN_H - 180f;
    private static final float ROW_H = 64f;
    private static final float ROW_GAP = 6f;
    private static final float BTN_H = 40f;
    private static final float DELETE_BTN_W = 40f;
    private static final float LOAD_BTN_W = 100f;

    // ── Overlay inserimento nome ─────────────────────────────────────────────
    private boolean namingMode = false;
    private final StringBuilder nameBuffer = new StringBuilder();
    private float cursorBlink = 0f;

    private static final float NAME_BOX_W = 500f;
    private static final float NAME_BOX_H = 190f;
    private static final int NAME_MAX_LEN = 40;

    public SaveSlotScreen(Main game, Screen previousScreen, GameScreen activeGameScreen, boolean allowSave) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.activeGameScreen = activeGameScreen;
        this.allowSave = allowSave;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        sr = new ShapeRenderer();
        batch = new SpriteBatch();
    }

    private void refreshList() {
        slots = SaveManager.getInstance().listSaves();
        maxScroll = Math.max(0, slots.size() * (ROW_H + ROW_GAP) - (FRAME_TOP - FRAME_BOTTOM));
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    @Override
    public void show() {
        refreshList();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (namingMode)
                    return false;
                scrollDelta += amountY * ROW_H * 2f;
                return true;
            }

            @Override
            public boolean keyTyped(char character) {
                if (!namingMode)
                    return false;
                if (character == '\b') {
                    if (nameBuffer.length() > 0)
                        nameBuffer.deleteCharAt(nameBuffer.length() - 1);
                    return true;
                }
                if (character == '\r' || character == '\n') {
                    confirmNaming();
                    return true;
                }
                if (character >= 32 && character != 127 && nameBuffer.length() < NAME_MAX_LEN) {
                    nameBuffer.append(character);
                }
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (namingMode && keycode == Input.Keys.ESCAPE) {
                    cancelNaming();
                    return true;
                }
                return false;
            }
        });
    }

    private void openNaming() {
        namingMode = true;
        nameBuffer.setLength(0);
    }

    private void confirmNaming() {
        if (nameBuffer.length() > 0 && activeGameScreen != null) {
            activeGameScreen.saveManualAs(nameBuffer.toString());
            refreshList();
        }
        namingMode = false;
    }

    private void cancelNaming() {
        namingMode = false;
    }

    @Override
    public void render(float delta) {
        cursorBlink += delta;
        if (scrollDelta != 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + scrollDelta));
            scrollDelta = 0f;
        }

        ScreenUtils.clear(UITheme.BG_DARK.r, UITheme.BG_DARK.g, UITheme.BG_DARK.b, 1);
        camera.update();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawBackground();
        drawTitle();
        drawTopButtons();
        drawList();
        drawFooter();

        if (namingMode) {
            drawNamingOverlay();
        }

        handleClicks();
    }

    // ── Disegno ──────────────────────────────────────────────────────────────

    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_DARK);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);
        sr.setColor(UITheme.BG_DECORATIVE);
        sr.rect(0, Main.SCREEN_H - 200, Main.SCREEN_W, 200);
        sr.end();

        UITheme.drawPanel(sr, FRAME_X, FRAME_BOTTOM, Main.SCREEN_W - FRAME_X * 2, FRAME_TOP - FRAME_BOTTOM);
    }

    private void drawTitle() {
        batch.begin();
        String title = allowSave ? "SAVE / LOAD" : "LOAD GAME";
        layout.setText(game.fonts.title, title);
        game.fonts.title.setColor(UITheme.TEXT_GOLD);
        game.fonts.title.draw(batch, title, Main.SCREEN_W / 2f - layout.width / 2f, Main.SCREEN_H - 45f);
        batch.end();
    }

    private float newSaveBtnX, quickSaveBtnX, deleteAllBtnX, topBtnY;

    private void drawTopButtons() {
        topBtnY = Main.SCREEN_H - 130f;
        float btnW = 220f;
        float gap = 16f;
        float x = Main.SCREEN_W - FRAME_X - 16f;

        deleteAllBtnX = x - btnW;
        drawButton(deleteAllBtnX, topBtnY, btnW, BTN_H, "Delete all saves", UITheme.STATUS_RED);
        x = deleteAllBtnX - gap;

        if (allowSave) {
            quickSaveBtnX = x - btnW;
            drawButton(quickSaveBtnX, topBtnY, btnW, BTN_H, "Quicksave", UITheme.STATUS_BLUE);
            x = quickSaveBtnX - gap;

            newSaveBtnX = x - btnW;
            drawButton(newSaveBtnX, topBtnY, btnW, BTN_H, "+ New save", UITheme.STATUS_GREEN);
        }
    }

    private void drawButton(float x, float y, float w, float h, String label, Color accent) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hover = !namingMode && mx >= x && mx <= x + w && my >= y && my <= y + h;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hover ? UITheme.HOVER_BG : UITheme.BG_PANEL);
        sr.rect(x, y, w, h);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(accent);
        sr.rect(x, y, w, h);
        sr.end();

        batch.begin();
        layout.setText(game.fonts.normal, label);
        game.fonts.normal.setColor(accent);
        game.fonts.normal.draw(batch, label, x + w / 2f - layout.width / 2f, y + h / 2f + layout.height / 2f);
        batch.end();
    }

    private void drawList() {
        if (slots.isEmpty()) {
            batch.begin();
            String msg = "No save found";
            layout.setText(game.fonts.large, msg);
            game.fonts.large.setColor(UITheme.TEXT_GRAY);
            game.fonts.large.draw(batch, msg, Main.SCREEN_W / 2f - layout.width / 2f, FRAME_TOP - 100f);
            batch.end();
            return;
        }

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        for (int i = 0; i < slots.size(); i++) {
            SaveManager.SaveSlotInfo info = slots.get(i);
            float y = FRAME_TOP - i * (ROW_H + ROW_GAP) + scrollOffset;
            if (y < FRAME_BOTTOM + ROW_H || y > FRAME_TOP + ROW_H)
                continue;

            float rowX = FRAME_X + 16f;
            float rowW = Main.SCREEN_W - rowX * 2f;
            float rowY = y - ROW_H;

            boolean rowHover = !namingMode && mx >= rowX && mx <= rowX + rowW && my >= rowY && my <= y;

            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(rowHover ? UITheme.HOVER_BG : (i % 2 == 0 ? Color.valueOf("101018") : Color.valueOf("0c0c12")));
            sr.rect(rowX, rowY, rowW, ROW_H);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(UITheme.GOLD_DARK);
            sr.rect(rowX, rowY, rowW, ROW_H);
            sr.end();

            batch.begin();
            String name = (info.isAutosave ? "[Auto] " : "") + info.displayName;
            game.fonts.large.setColor(UITheme.TEXT_GOLD);
            game.fonts.large.draw(batch, name, rowX + 16f, rowY + ROW_H - 14f);

            String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm")
                    .format(new java.util.Date(info.timestamp));
            game.fonts.small.setColor(UITheme.TEXT_GRAY);
            game.fonts.small.draw(batch, dateStr, rowX + 16f, rowY + ROW_H - 38f);
            batch.end();

            float loadX = rowX + rowW - DELETE_BTN_W - 12f - LOAD_BTN_W;
            drawButton(loadX, rowY + (ROW_H - BTN_H) / 2f, LOAD_BTN_W, BTN_H, "Load", UITheme.STATUS_GREEN);

            float delX = rowX + rowW - DELETE_BTN_W - 6f;
            drawButton(delX, rowY + (ROW_H - BTN_H) / 2f, DELETE_BTN_W, BTN_H, "X", UITheme.STATUS_RED);
        }

        if (maxScroll > 0) {
            float trackTop = FRAME_TOP;
            float trackBottom = FRAME_BOTTOM;
            float trackH = trackTop - trackBottom;
            float barH = Math.max(24f, (trackH / (slots.size() * (ROW_H + ROW_GAP))) * trackH);
            float t = scrollOffset / maxScroll;
            float barY = trackTop - barH - t * (trackH - barH);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0.3f, 0.3f, 0.4f, 0.8f);
            sr.rect(Main.SCREEN_W - FRAME_X - 10, barY, 6, barH);
            sr.end();
        }
    }

    private void drawFooter() {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hover = !namingMode && mx >= 40 && mx <= 240 && my >= 28 && my <= 60;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_TOP_BAND);
        sr.rect(0, 0, Main.SCREEN_W, 80);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hover ? UITheme.HOVER_BG : UITheme.BG_PANEL);
        sr.rect(40, 28, 200, 32);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(hover ? UITheme.GOLD_PRIMARY : UITheme.GOLD_DARK);
        sr.rect(40, 28, 200, 32);
        sr.end();

        batch.begin();
        game.fonts.normal.setColor(UITheme.TEXT_GOLD);
        game.fonts.normal.draw(batch, "<- Back", 56, 52);
        batch.end();
    }

    // ── Overlay nome salvataggio ─────────────────────────────────────────────

    private float nameConfirmBtnX, nameCancelBtnX, nameBtnY, nameBoxX, nameBoxY;

    private void drawNamingOverlay() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.75f);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);
        sr.end();

        nameBoxX = Main.SCREEN_W / 2f - NAME_BOX_W / 2f;
        nameBoxY = Main.SCREEN_H / 2f - NAME_BOX_H / 2f;

        UITheme.drawPanel(sr, nameBoxX, nameBoxY, NAME_BOX_W, NAME_BOX_H);

        batch.begin();
        String title = "Save name";
        layout.setText(game.fonts.large, title);
        game.fonts.large.setColor(UITheme.TEXT_GOLD);
        game.fonts.large.draw(batch, title, Main.SCREEN_W / 2f - layout.width / 2f, nameBoxY + NAME_BOX_H - 24);
        batch.end();

        // Campo testo
        float fieldX = nameBoxX + 30f;
        float fieldW = NAME_BOX_W - 60f;
        float fieldY = nameBoxY + NAME_BOX_H - 100f;
        float fieldH = 40f;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_DARK);
        sr.rect(fieldX, fieldY, fieldW, fieldH);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.GOLD_PRIMARY);
        sr.rect(fieldX, fieldY, fieldW, fieldH);
        sr.end();

        String display = nameBuffer.toString();
        boolean showCursor = (cursorBlink % 1.0f) < 0.5f;
        String cursorChar = showCursor ? "|" : "";

        batch.begin();
        game.fonts.normal.setColor(UITheme.TEXT_WHITE);
        game.fonts.normal.draw(batch, display + cursorChar, fieldX + 10f, fieldY + fieldH - 12f);
        batch.end();

        // Pulsanti Confirm / Cancel
        nameBtnY = nameBoxY + 24f;
        float btnW = 150f;
        nameConfirmBtnX = Main.SCREEN_W / 2f - btnW - 8f;
        nameCancelBtnX = Main.SCREEN_W / 2f + 8f;

        drawOverlayButton(nameConfirmBtnX, nameBtnY, btnW, BTN_H, "Confirm", UITheme.STATUS_GREEN);
        drawOverlayButton(nameCancelBtnX, nameBtnY, btnW, BTN_H, "Cancel", UITheme.STATUS_RED);
    }

    private void drawOverlayButton(float x, float y, float w, float h, String label, Color accent) {
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hover ? UITheme.HOVER_BG : UITheme.BG_PANEL);
        sr.rect(x, y, w, h);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(accent);
        sr.rect(x, y, w, h);
        sr.end();

        batch.begin();
        layout.setText(game.fonts.normal, label);
        game.fonts.normal.setColor(accent);
        game.fonts.normal.draw(batch, label, x + w / 2f - layout.width / 2f, y + h / 2f + layout.height / 2f);
        batch.end();
    }

    // ── Click handling ───────────────────────────────────────────────────────

    private void handleClicks() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
            return;

        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Overlay nome ha priorità assoluta: se attivo, blocca tutto il resto
        if (namingMode) {
            if (mx >= nameConfirmBtnX && mx <= nameConfirmBtnX + 150f
                    && my >= nameBtnY && my <= nameBtnY + BTN_H) {
                confirmNaming();
                return;
            }
            if (mx >= nameCancelBtnX && mx <= nameCancelBtnX + 150f
                    && my >= nameBtnY && my <= nameBtnY + BTN_H) {
                cancelNaming();
                return;
            }
            return; // click fuori dai pulsanti dell'overlay = ignorato
        }

        // Footer back
        if (mx >= 40 && mx <= 240 && my >= 28 && my <= 60) {
            game.setScreen(previousScreen);
            return;
        }

        // Top buttons
        if (my >= topBtnY && my <= topBtnY + BTN_H) {
            if (mx >= deleteAllBtnX && mx <= deleteAllBtnX + 220f) {
                SaveManager.getInstance().deleteAllSaves();
                refreshList();
                return;
            }
            if (allowSave) {
                if (mx >= quickSaveBtnX && mx <= quickSaveBtnX + 220f) {
                    activeGameScreen.quickSave();
                    refreshList();
                    return;
                }
                if (mx >= newSaveBtnX && mx <= newSaveBtnX + 220f) {
                    openNaming();
                    return;
                }
            }
        }

        // Righe
        for (int i = 0; i < slots.size(); i++) {
            SaveManager.SaveSlotInfo info = slots.get(i);
            float y = FRAME_TOP - i * (ROW_H + ROW_GAP) + scrollOffset;
            if (y < FRAME_BOTTOM + ROW_H || y > FRAME_TOP + ROW_H)
                continue;

            float rowX = FRAME_X + 16f;
            float rowW = Main.SCREEN_W - rowX * 2f;
            float rowY = y - ROW_H;
            float btnY = rowY + (ROW_H - BTN_H) / 2f;

            float delX = rowX + rowW - DELETE_BTN_W - 6f;
            if (mx >= delX && mx <= delX + DELETE_BTN_W && my >= btnY && my <= btnY + BTN_H) {
                SaveManager.getInstance().deleteSave(info.filename);
                refreshList();
                return;
            }

            float loadX = rowX + rowW - DELETE_BTN_W - 12f - LOAD_BTN_W;
            boolean onLoadBtn = mx >= loadX && mx <= loadX + LOAD_BTN_W && my >= btnY && my <= btnY + BTN_H;
            boolean onRow = mx >= rowX && mx <= rowX + rowW && my >= rowY && my <= y;

            if (onLoadBtn || onRow) {
                loadSlot(info.filename);
                return;
            }
        }
    }

    private void loadSlot(String filename) {
        if (allowSave && activeGameScreen != null) {
            SaveData sd = SaveManager.getInstance().loadSave(filename);
            if (sd != null) {
                activeGameScreen.loadFromSaveData(sd);
                game.setScreen(activeGameScreen);
            }
        } else {
            if (game.audio != null)
                game.audio.playMusic(AudioManager.MUSIC_GAME);
            game.setScreen(new LoadGameScreen(game, filename));
        }
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