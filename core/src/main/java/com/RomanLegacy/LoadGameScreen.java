package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.RomanLegacy.UITheme;

/**
 * Spinner di caricamento: carica lo specifico file di salvataggio passato,
 * poi crea la GameScreen. Usato solo dal menu principale (dove non esiste
 * ancora una GameScreen viva da riutilizzare) — dal menu in-game si usa
 * invece GameScreen.loadFromSaveData() direttamente, senza passare da qui.
 */
public class LoadGameScreen implements Screen {

    private final Main game;
    private final String filename;
    private final OrthographicCamera camera;
    private final ShapeRenderer sr;
    private final SpriteBatch batch;
    private final GlyphLayout layout = new GlyphLayout();

    private SaveData saveData;
    private boolean loaded = false;
    private float elapsed = 0f;
    private static final float MIN_DISPLAY_TIME = 1.0f;

    private static final float PANEL_W = 460f;
    private static final float PANEL_H = 140f;

    public LoadGameScreen(Main game, String filename) {
        this.game = game;
        this.filename = filename;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        sr = new ShapeRenderer();
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        elapsed += delta;

        ScreenUtils.clear(UITheme.BG_DARK.r, UITheme.BG_DARK.g, UITheme.BG_DARK.b, 1);
        camera.update();
        sr.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawBackground();
        drawLoadingPanel();

        if (elapsed >= MIN_DISPLAY_TIME && !loaded) {
            loaded = true;
            GameScreen gameScreen = new GameScreen(game, saveData);
            game.setScreen(gameScreen);
        }
    }

    private void drawBackground() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_DARK);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);

        sr.setColor(UITheme.BG_DECORATIVE);
        sr.rect(0, Main.SCREEN_H - 200, Main.SCREEN_W, 200);
        sr.end();
    }

    private void drawLoadingPanel() {
        float px = Main.SCREEN_W / 2f - PANEL_W / 2f;
        float py = Main.SCREEN_H / 2f - PANEL_H / 2f;

        UITheme.drawPanel(sr, px, py, PANEL_W, PANEL_H);

        int dots = ((int) (elapsed * 3) % 4);
        String title = "LOADING" + ".".repeat(dots);

        batch.begin();

        layout.setText(game.fonts.large, title);
        game.fonts.large.setColor(UITheme.TEXT_GOLD);
        game.fonts.large.draw(batch, title,
                Main.SCREEN_W / 2f - layout.width / 2f, py + PANEL_H - 32);

        if (saveData != null) {
            String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                    .format(new java.util.Date(saveData.timestamp));
            String saveLine = "Save: " + dateStr;
            layout.setText(game.fonts.small, saveLine);
            game.fonts.small.setColor(UITheme.TEXT_GRAY);
            game.fonts.small.draw(batch, saveLine,
                    Main.SCREEN_W / 2f - layout.width / 2f, py + PANEL_H - 72);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        sr.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int w, int h) {
        camera.update();
    }

    @Override
    public void show() {
        saveData = SaveManager.getInstance().loadSave(filename);
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
}