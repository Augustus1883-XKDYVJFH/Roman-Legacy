package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class GameSettings {

    public static class KeyBindings {
        public int moveUp = Input.Keys.W;
        public int moveDown = Input.Keys.S;
        public int moveLeft = Input.Keys.A;
        public int moveRight = Input.Keys.D;
        public int zoomIn = Input.Keys.EQUALS;
        public int zoomOut = Input.Keys.MINUS;
        public int toggleFactions = Input.Keys.F;
        public int openConsumption = Input.Keys.C;
        public int cancel = Input.Keys.ESCAPE;
        public int openTrade = Input.Keys.T;
        public int pause = Input.Keys.P;
        public int quickSave = Input.Keys.F5;
        public int quickLoad = Input.Keys.F9;
        public int switchMap = Input.Keys.M;
        public int openSettings = Input.Keys.O;
        public boolean cancelMouseRight = false;
    }

    // Impostazioni esistenti
    public float musicVolume = 0.6f;
    public float sfxVolume = 0.8f;
    public boolean musicEnabled = true;
    public boolean sfxEnabled = true;
    public KeyBindings keys = new KeyBindings();

    // NUOVE IMPOSTAZIONI DA AGGIUNGERE
    public boolean fullscreen = true;
    public int windowWidth = 1280;
    public int windowHeight = 720;
    public float brightness = 1.0f;
    public int autoSaveInterval = 60;
    private static GameSettings instance;

    public boolean firstLaunchAchievementGranted = false;

    // ── Singleton ────────────────────────────────────────────────────────────────
    private GameSettings() {
    }

    public static GameSettings get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    // ── Persistenza ──────────────────────────────────────────────────────────────
    private static final String SETTINGS_PATH = "saves/settings.json";

    private static GameSettings load() {
        try {
            FileHandle dir = Gdx.files.local("saves");
            if (!dir.exists())
                dir.mkdirs();

            FileHandle file = Gdx.files.local(SETTINGS_PATH);
            if (!file.exists()) {
                return new GameSettings();
            }
            Json json = makeJson();

            GameSettings loaded = json.fromJson(GameSettings.class, file.readString());
            if (loaded.keys == null)
                loaded.keys = new KeyBindings();
            if (loaded.keys.switchMap == 0)
                loaded.keys.switchMap = Input.Keys.M;
            if (loaded.keys.openSettings == 0)
                loaded.keys.openSettings = Input.Keys.O;

            return loaded;
        } catch (Exception e) {
            Gdx.app.error("GameSettings", "Load failed, using defaults", e);
            return new GameSettings();
        }
    }

    public void save() {
        try {
            FileHandle file = Gdx.files.local(SETTINGS_PATH);
            file.writeString(makeJson().prettyPrint(this), false);
        } catch (Exception e) {
            return;
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────────

    /** Volume effettivo musica (0 se disabilitata). */
    public float effectiveMusicVolume() {
        return musicEnabled ? musicVolume : 0f;
    }

    /** Volume effettivo SFX (0 se disabilitato). */
    public float effectiveSfxVolume() {
        return sfxEnabled ? sfxVolume : 0f;
    }

    /** Nome leggibile di un tasto (es. Input.Keys.W → "W"). */
    public static String keyName(int keycode) {
        String name = Input.Keys.toString(keycode);
        return name != null ? name : "?";
    }

    private static Json makeJson() {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        json.setUsePrototypes(false);
        return json;
    }
}