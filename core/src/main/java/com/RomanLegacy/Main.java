package com.RomanLegacy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.RomanLegacy.IconManager;

public class Main extends Game {

    public static final int SCREEN_W = 1920;
    public static final int SCREEN_H = 1080;

    public FontManager fonts;
    public AudioManager audio;
    public IconManager icons;
    public SteamManager steam;
    public AchievementManager achievements;

    private Cursor customCursor;

    @Override
    public void create() {
        GameSettings settings = GameSettings.get();
        if (settings.fullscreen) {
            Gdx.app.postRunnable(() -> {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            });
        } else {
            int maxW = Gdx.graphics.getDisplayMode().width;
            int maxH = Gdx.graphics.getDisplayMode().height;
            int w = Math.min(settings.windowWidth, maxW);
            int h = Math.min(settings.windowHeight, maxH);
            Gdx.graphics.setWindowedMode(w, h);
        }

        fonts = new FontManager();
        audio = new AudioManager();
        icons = new IconManager();
        steam = new SteamManager();
        achievements = new AchievementManager(steam);
        achievements.checkFirstLaunch(GameSettings.get());

        loadCustomCursor();

        audio.playMusic(AudioManager.MUSIC_MENU);
        setScreen(new SplashScreen(this));
    }

    private void loadCustomCursor() {
        try {
            Pixmap pixmap = new Pixmap(Gdx.files.internal("icons/cursor/cursor.png"));
            customCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
            Gdx.graphics.setCursor(customCursor);
            pixmap.dispose();
        } catch (Exception e) {
            Gdx.app.error("Main", "Custom cursor load failed, using system default", e);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (audio != null)
            audio.pauseMusic();
    }

    @Override
    public void resume() {
        super.resume();
        if (audio != null)
            audio.resumeMusic();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fonts != null)
            fonts.dispose();
        if (audio != null)
            audio.dispose();
        if (icons != null)
            icons.dispose();
        if (steam != null)
            steam.dispose();
        if (customCursor != null)
            customCursor.dispose();
    }
}