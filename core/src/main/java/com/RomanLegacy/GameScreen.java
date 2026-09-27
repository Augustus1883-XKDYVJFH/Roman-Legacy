package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.RomanLegacy.UITheme;

public class GameScreen implements Screen {

    private final Main game;
    private final GameState state;
    private final GameInputHandler input;
    private final MapRenderer mapRenderer;
    private final HudRenderer hud;
    private final ShapeRenderer sr;
    private final SpriteBatch batch;
    private final OrthographicCamera mapCamera;
    private final OrthographicCamera uiCamera;
    private final SaveManager saveManager;

    private float autoSaveTimer = 0f;
    private boolean isDisposing = false;
    private boolean paused = false;
    private boolean inGameMenu = false;
    private int inGameMenuIndex = 0;
    private int inGameHoveredIndex = -1;
    private boolean fadingToMenu = false;
    private float fadeTimer = 0f;
    private static final float FADE_TO_MENU_DURATION = 1f;

    private final String[] inGameMenuItems = {
            "Continue",
            "Load game",
            "Save game",
            "Options",
            "Exit to menu",
            "Exit game"
    };

    private final Vector3 menuMousePos = new Vector3();
    private final GlyphLayout menuLayout = new GlyphLayout();

    private static final float MENU_BUTTON_H = 46f;
    private static final float MENU_BUTTON_W = UITheme.MENU_ITEM_WIDTH;

    private int selectedIndex = 0;

    public GameScreen(Main game) {
        this(game, null);
    }

    public GameScreen(Main game, SaveData preloadedSaveData) {
        this.game = game;

        mapCamera = new OrthographicCamera();
        mapCamera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        mapCamera.zoom = 1.2f;

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);

        sr = new ShapeRenderer();
        batch = new SpriteBatch();

        saveManager = SaveManager.getInstance();
        SaveData saveData = preloadedSaveData;

        long factionSeed;
        if (saveData != null && saveData.factionSeed != 0) {
            factionSeed = saveData.factionSeed;
        } else {
            factionSeed = System.currentTimeMillis();
        }

        state = new GameState(factionSeed);
        input = new GameInputHandler(game, state, mapCamera);
        mapRenderer = new MapRenderer(sr, mapCamera);
        hud = new HudRenderer(game, sr, batch, uiCamera);

        if (saveData != null) {
            state.loadFromSave(saveData);
            if (saveData.cameraX != 0 || saveData.cameraY != 0) {
                mapCamera.position.set(saveData.cameraX, saveData.cameraY, 0);
                mapCamera.zoom = saveData.cameraZoom;
                input.restoreCamera(saveData.cameraX, saveData.cameraY, saveData.cameraZoom);
            }
        } else {
            mapCamera.position.set(input.getCameraX(), input.getCameraY(), 0);
        }
        mapCamera.update();
    }

    public void toggleInGameMenu() {
        inGameMenu = !inGameMenu;
        paused = inGameMenu;
    }

    private void updateInGameMenuHover() {
        inGameHoveredIndex = -1;
        menuMousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiCamera.unproject(menuMousePos);

        float sh = Gdx.graphics.getHeight();
        int startY = (int) (sh / 2f) + 50;
        float x = Gdx.graphics.getWidth() / 2f - MENU_BUTTON_W / 2f;

        for (int i = 0; i < inGameMenuItems.length; i++) {
            float y = startY - i * UITheme.MENU_ITEM_SPACING;
            float by = y - MENU_BUTTON_H / 2f;
            if (menuMousePos.x >= x && menuMousePos.x <= x + MENU_BUTTON_W &&
                    menuMousePos.y >= by && menuMousePos.y <= by + MENU_BUTTON_H) {
                inGameHoveredIndex = i;
                break;
            }
        }
    }

    private void handleInGameMenuInput() {
        updateInGameMenuHover();

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            inGameMenuIndex = (inGameMenuIndex - 1 + inGameMenuItems.length) % inGameMenuItems.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            inGameMenuIndex = (inGameMenuIndex + 1) % inGameMenuItems.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            selectInGameMenuItem();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            inGameMenu = true;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && inGameHoveredIndex >= 0) {
            inGameMenuIndex = inGameHoveredIndex;
            selectInGameMenuItem();
        }
    }

    private void selectInGameMenuItem() {
        switch (inGameMenuIndex) {
            case 0: // Continue
                inGameMenu = false;
                paused = false;
                break;
            case 1: // Load game
            case 2: // Save game — stessa schermata, gestisce entrambe le azioni
                game.setScreen(new SaveSlotScreen(game, this, this, true));
                break;
            case 3: // Options
                game.setScreen(new SettingsScreen(game, this));
                break;
            case 4: // Exit to menu
                autosaveNow();
                fadingToMenu = true;
                fadeTimer = 0f;
                break;
            case 5: // Exit game
                autosaveNow();
                Gdx.app.exit();
                break;
        }
    }

    private void autosaveNow() {
        if (!isDisposing) {
            saveManager.autosave(state, mapCamera.position.x, mapCamera.position.y, mapCamera.zoom);
        }
    }

    public void saveManualAs(String name) {
        saveManager.saveManual(state, mapCamera.position.x, mapCamera.position.y, mapCamera.zoom, name);
    }

    public void quickSave() {
        saveManager.quickSave(state, mapCamera.position.x, mapCamera.position.y, mapCamera.zoom);
    }

    public void quickLoad() {
        SaveData sd = saveManager.quickLoad();
        if (sd != null) {
            loadFromSaveData(sd);
        }
    }

    public void loadFromSaveData(SaveData saveData) {
        state.loadFromSave(saveData);
        mapCamera.position.set(saveData.cameraX, saveData.cameraY, 0);
        mapCamera.zoom = saveData.cameraZoom;
        input.restoreCamera(saveData.cameraX, saveData.cameraY, saveData.cameraZoom);
        inGameMenu = false;
        paused = false;
    }

    private void drawInGameMenu() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.BG_DARK);
        sr.rect(0, 0, sw, sh);
        sr.setColor(UITheme.BG_DECORATIVE);
        sr.rect(0, sh - 200, sw, 200);
        sr.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        String title = "GAME MENU";
        menuLayout.setText(game.fonts.huge, title);
        game.fonts.huge.setColor(UITheme.TEXT_GOLD);
        game.fonts.huge.draw(batch, title, sw / 2f - menuLayout.width / 2f, sh - 130);
        batch.end();

        int startY = (int) (sh / 2f) + 50;
        for (int i = 0; i < inGameMenuItems.length; i++) {
            float y = startY - i * UITheme.MENU_ITEM_SPACING;
            UITheme.drawMenuRow(sr, batch, game.fonts.large, menuLayout,
                    inGameMenuItems[i], sw / 2f, y, MENU_BUTTON_W, MENU_BUTTON_H,
                    i == inGameMenuIndex, i == inGameHoveredIndex);
        }

        batch.begin();
        String footer = "UP/DOWN = Select    •    ENTER = Confirm    •    ESC = Exit";
        menuLayout.setText(game.fonts.small, footer);
        game.fonts.small.setColor(UITheme.TEXT_DARK_GRAY);
        game.fonts.small.draw(batch, footer, sw / 2f - menuLayout.width / 2f, 70);
        batch.end();
    }

    private void drawFadeOverlay(float alpha) {
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, alpha);
        sr.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        sr.end();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                input.onScroll(amountY);
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        if (fadingToMenu) {
            fadeTimer += delta;
            uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            uiCamera.update();
            drawInGameMenu();

            float alpha = Math.min(1f, fadeTimer / FADE_TO_MENU_DURATION);
            drawFadeOverlay(alpha);

            if (fadeTimer >= FADE_TO_MENU_DURATION) {
                fadingToMenu = false;
                game.setScreen(new MenuScreen(game));
            }
            return;
        }

        GameSettings.KeyBindings keys = GameSettings.get().keys;

        if (Gdx.input.isKeyJustPressed(keys.pause)) {
            paused = !paused;
        }

        if (Gdx.input.isKeyJustPressed(keys.quickSave)) {
            quickSave();
        }

        if (Gdx.input.isKeyJustPressed(keys.quickLoad)) {
            quickLoad();
            return;
        }

        if (inGameMenu) {
            handleInGameMenuInput();
            drawInGameMenu();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
            state.gameSpeed = 1f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
            state.gameSpeed = 2f;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3))
            state.gameSpeed = 3f;
        if (Gdx.input.isKeyJustPressed(keys.quickSave)) {
            quickSave();
        }
        if (Gdx.input.isKeyJustPressed(keys.quickLoad)) {
            quickLoad();
            return;
        }

        updateTimers(delta * state.gameSpeed);
        state.tickFloaters(delta); // delta reale, non scalato: animazione sempre alla stessa velocità
        if (state.eventManager.pendingEvent == null)
            input.handleInput(delta);

        autoSaveTimer += delta;
        if (autoSaveTimer >= GameSettings.get().autoSaveInterval) {
            autosaveNow();
            autoSaveTimer = 0f;
        }

        ScreenUtils.clear(0, 0, 0, 1);
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        mapRenderer.drawMap(state);
        mapRenderer.drawDeposits(state);
        mapRenderer.drawBuildings(state, input);

        if (input.selectedBuilding != null && input.previewCellX >= 0
                && input.previewCellY >= 0)
            mapRenderer.drawBuildingPreview(input, state);

        if (input.selectedTool != GameInputHandler.ToolAction.NONE && input.previewCellX >= 0
                && input.previewCellY >= 0)
            mapRenderer.drawToolPreview(input, state);

        mapRenderer.drawFloaters(state, game, batch);

        hud.drawTopBar(input, state);
        hud.drawBottomBar(input);
        hud.drawHoverTooltip(input, state);

        if (input.showBuildingPanel && input.inspectedBuilding != null)
            hud.drawBuildingPanel(input, state);
        if (input.showFactionPanel)
            hud.drawFactionPanel(input, state);
        if (state.showNewWorldOverlay)
            hud.drawNewWorldOverlay(state);
        if (state.eventManager.pendingEvent != null)
            hud.drawNarrativeEventPanel(state);
    }

    @Override
    public void resize(int w, int h) {
        mapCamera.viewportWidth = w;
        mapCamera.viewportHeight = h;
        mapCamera.update();
        uiCamera.setToOrtho(false, w, h);
        uiCamera.update();
    }

    @Override
    public void hide() {
        autosaveNow();
    }

    @Override
    public void pause() {
        autosaveNow();
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        isDisposing = true;
        autosaveNow();
        sr.dispose();
        batch.dispose();
    }

    private void updateTimers(float delta) {
        state.tickProduction(delta);

        state.populationTimer += delta;
        if (state.populationTimer >= GameState.POP_INTERVAL) {
            state.populationTimer = 0;
            state.tickPopulation();
            input.loadTab(input.activeTab);
            if (!state.onNewWorld)
                state.checkExpedition();
        }

        if (state.expeditionActive) {
            state.expeditionTimer += delta;
            if (state.expeditionTimer >= GameState.EXPEDITION_SECS)
                state.completeExpedition();
        }

        state.tradeManager.tick(delta);
        state.eventManager.tick(delta, state);
        state.tickMonumentUpgrades(delta);
        game.audio.setUnlockedEvents(new java.util.HashSet<>(state.eventManager.serializeFiredEvents()));
    }
}