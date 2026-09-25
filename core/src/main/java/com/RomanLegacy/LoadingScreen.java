package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.RomanLegacy.UITheme;

/**
 * Schermata di caricamento che carica tutti gli assets prima di iniziare.
 */
public class LoadingScreen implements Screen {

    private final Main game;
    private final OrthographicCamera camera;
    private final ShapeRenderer sr;
    private final SpriteBatch batch;

    private AssetManager assetManager;
    private float progress = 0f;
    private boolean loaded = false;
    private float elapsed = 0f;
    private static final float MIN_DISPLAY_TIME = 1.0f;

    // Lista degli assets da caricare
    private static final String[] MUSIC_FILES = {
            "audio/menu.ogg", "audio/game.ogg", "audio/newworld.ogg"
    };

    private static final String[] SFX_FILES = {
            "audio/place_building.ogg", "audio/demolish.ogg", "audio/ui_click.ogg",
            "audio/expedition_start.ogg", "audio/expedition_complete.ogg",
            "audio/upgrade_house.ogg", "audio/faction_arrive.ogg"
    };

    private final Screen targetScreen;

    // Costruttore con destinazione (quello principale)
    public LoadingScreen(Main game, Screen targetScreen) {
        this.game = game;
        this.targetScreen = targetScreen;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        sr = new ShapeRenderer();
        batch = new SpriteBatch();

        assetManager = new AssetManager();

        // Carica musiche
        for (String path : MUSIC_FILES) {
            if (Gdx.files.internal(path).exists()) {
                assetManager.load(path, Music.class);
            }
        }

        // Carica SFX
        for (String path : SFX_FILES) {
            if (Gdx.files.internal(path).exists()) {
                assetManager.load(path, Sound.class);
            }
        }
    }

    // Costruttore senza parametri (chiama l'altro con MenuScreen di default)
    public LoadingScreen(Main game) {
        this(game, new MenuScreen(game)); // ← CORRETTO: chiama l'altro costruttore
    }

    @Override
    public void render(float delta) {
        elapsed += delta;

        // Aggiorna il caricamento
        if (!loaded) {
            if (assetManager.update()) {
                loaded = true;
                onLoadingComplete();
            }
            progress = assetManager.getProgress();
        }

        // Disegna
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1);
        camera.update();

        drawBackground();
        drawLoadingBar();
        drawTips();

        // Passa alla schermata target SOLO dopo il tempo minimo E caricamento
        // completato
        if (loaded && elapsed >= MIN_DISPLAY_TIME) {
            game.setScreen(targetScreen);
        }
    }

    private void drawBackground() {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_DARK);
        sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);

        // Decorative top band
        sr.setColor(UITheme.BG_DECORATIVE);
        sr.rect(0, Main.SCREEN_H - 200, Main.SCREEN_W, 200);
        sr.end();
    }

    private void drawLoadingBar() {
        float barW = 400f;
        float barH = 24f;
        float barX = (Main.SCREEN_W - barW) / 2;
        float barY = Main.SCREEN_H / 2f - 50;

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_PANEL);
        sr.rect(barX, barY, barW, barH);
        sr.setColor(UITheme.GOLD_PRIMARY);
        sr.rect(barX, barY, barW * progress, barH);
        sr.end();

        batch.begin();
        game.fonts.large.setColor(UITheme.TEXT_GOLD);
        String percent = String.format("%d%%", (int) (progress * 100));
        game.fonts.large.draw(batch, percent, barX + barW / 2 - 30, barY + barH + 30);
        batch.end();
    }

    private void drawTips() {
        String[] tips = {
                ""
        };

        int tipIndex = (int) (System.currentTimeMillis() / 5000) % tips.length;

        batch.begin();
        game.fonts.small.setColor(0.6f, 0.6f, 0.6f, 1f);
        game.fonts.small.draw(batch, tips[tipIndex],
                Main.SCREEN_W / 2f - 300, Main.SCREEN_H / 2f - 100);
        game.fonts.small.draw(batch, "Caricamento assets in corso...",
                Main.SCREEN_W / 2f - 150, Main.SCREEN_H / 2f - 130);
        batch.end();
    }

    private void onLoadingComplete() {
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        sr.dispose();
        batch.dispose();
    }

    @Override
    public void show() {
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
}