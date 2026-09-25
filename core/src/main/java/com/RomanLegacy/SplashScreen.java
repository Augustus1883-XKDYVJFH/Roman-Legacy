package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class SplashScreen implements Screen {

    private final Main game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final ShapeRenderer sr;
    private final GlyphLayout layout = new GlyphLayout();

    private Texture logoTexture;

    // ── Sequenza di schermate ────────────────────────────────────────────────
    private static class Stage {
        final String line1;
        final String line2;
        final boolean useLogo;
        final float duration;
        final float fadeTime;

        Stage(String line1, String line2, boolean useLogo, float duration, float fadeTime) {
            this.line1 = line1;
            this.line2 = line2;
            this.useLogo = useLogo;
            this.duration = duration;
            this.fadeTime = fadeTime;
        }
    }

    private final Stage[] stages = {
            new Stage("Made by augustus1883", null, false, 3.6f, 0.9f),
            new Stage("EARLY ACCESS", null, false, 3.6f, 0.9f),
            new Stage(null, null, true, 3.6f, 0.9f) // logo / fallback Roman Legacy
    };

    private int stageIndex = 0;
    private long stageStartTime;
    private float alpha = 0f;

    public SplashScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        batch = new SpriteBatch();
        sr = new ShapeRenderer();

        try {
            logoTexture = new Texture(Gdx.files.internal("splash/logo.png"));
        } catch (Exception e) {
            logoTexture = null;
        }
    }

    @Override
    public void show() {
        stageStartTime = TimeUtils.millis();
    }

    @Override
    public void render(float delta) {
        Stage stage = stages[stageIndex];
        float elapsed = (TimeUtils.millis() - stageStartTime) / 1000f;

        if (elapsed < stage.fadeTime) {
            alpha = elapsed / stage.fadeTime;
        } else if (elapsed > stage.duration - stage.fadeTime) {
            alpha = (stage.duration - elapsed) / stage.fadeTime;
        } else {
            alpha = 1f;
        }
        alpha = Math.max(0f, Math.min(1f, alpha));

        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();

        if (stage.useLogo) {
            drawLogoStage();
        } else {
            drawTextStage(stage);
        }

        if (elapsed >= stage.duration) {
            advanceStage();
        }
    }

    private void advanceStage() {
        stageIndex++;
        if (stageIndex >= stages.length) {
            game.setScreen(new MenuScreen(game));
            return;
        }
        stageStartTime = TimeUtils.millis();
    }

    private void drawTextStage(Stage stage) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        layout.setText(game.fonts.huge, stage.line1);
        game.fonts.huge.setColor(0.9f, 0.8f, 0.5f, alpha);
        game.fonts.huge.draw(batch, stage.line1,
                Main.SCREEN_W / 2f - layout.width / 2f,
                Main.SCREEN_H / 2f + 20);

        if (stage.line2 != null) {
            layout.setText(game.fonts.normal, stage.line2);
            game.fonts.normal.setColor(0.7f, 0.7f, 0.7f, alpha);
            game.fonts.normal.draw(batch, stage.line2,
                    Main.SCREEN_W / 2f - layout.width / 2f,
                    Main.SCREEN_H / 2f - 30);
        }

        batch.end();
    }

    private void drawLogoStage() {
        if (logoTexture != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.setColor(1, 1, 1, alpha);
            float w = logoTexture.getWidth();
            float h = logoTexture.getHeight();
            float scale = Math.min(Main.SCREEN_W / w, Main.SCREEN_H / h) * 0.7f;
            batch.draw(logoTexture,
                    (Main.SCREEN_W - w * scale) / 2,
                    (Main.SCREEN_H - h * scale) / 2,
                    w * scale, h * scale);
            batch.setColor(1, 1, 1, 1);
            batch.end();
        } else {
            sr.setProjectionMatrix(camera.combined);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0, 0, 0, 1);
            sr.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);
            sr.end();

            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            String title = "ROMAN LEGACY";
            layout.setText(game.fonts.huge, title);
            game.fonts.huge.setColor(0.9f, 0.8f, 0.5f, alpha);
            game.fonts.huge.draw(batch, title,
                    Main.SCREEN_W / 2f - layout.width / 2f,
                    Main.SCREEN_H / 2f + 20);
            batch.end();
        }
    }

    @Override
    public void dispose() {
        if (logoTexture != null)
            logoTexture.dispose();
        batch.dispose();
        sr.dispose();
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