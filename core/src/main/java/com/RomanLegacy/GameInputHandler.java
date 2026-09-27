package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.util.*;

public class GameInputHandler {

    private static final int BOTBAR_H = HudRenderer.BOTBAR_H;
    private static final int TOPBAR_H = HudRenderer.TOPBAR_H;
    private static final int SLOT_W = HudRenderer.SLOT_W;
    private static final String[] TAB_LABELS = {
            "Logistics", "Houses", "Servicies", "Extraction", "Industry",
            "Monuments"
    };
    private static final BuildingType.Category[] TAB_CATS = {
            BuildingType.Category.LOGISTICA,
            BuildingType.Category.CASE,
            BuildingType.Category.SERVIZI,
            BuildingType.Category.ESTRAZIONE,
            BuildingType.Category.INDUSTRIA,
            BuildingType.Category.MONUMENTI
    };

    private final Main game;
    private final GameState state;
    private final OrthographicCamera mapCamera;
    private float cameraX, cameraY;
    private boolean dragging = false;
    private float dragStartX, dragStartY;
    public float zoomDelta = 0f;
    private float botScrollAccum = 0f;
    public int previewCellX = -1, previewCellY = -1;
    public BuildingType[] bottomBarBuildings = new BuildingType[0];
    public int selectedSlot = -1;
    public int hoveredSlot = -1;
    public boolean buildingRotated = false;
    public int activeTab = 2;
    public int botScrollOffset = 0;
    public BuildingType selectedBuilding = null;

    public enum ToolAction {
        NONE, DEMOLISH, UPGRADE_HOUSE
    }

    public ToolAction selectedTool = ToolAction.NONE;

    public boolean roadDragging = false;
    public int roadStartX = -1, roadStartY = -1;
    public boolean pipeDragging = false;
    public int pipeStartX = -1, pipeStartY = -1;

    public BuildingInstance inspectedBuilding = null;
    public BuildingInstance penOwner = null; // allevamento per cui si sta piazzando un recinto
    public boolean showBuildingPanel = false;
    public boolean showFactionPanel = false;
    public boolean panelJustOpened = false;
    public BuildingInstance fieldOwner = null;
    public boolean fieldDragging = false;
    public int fieldStartX = -1, fieldStartY = -1;

    public float panelX, panelY, panelW, panelH; // rettangolo del pannello edificio, aggiornato da HudRenderer

    private final Vector3 tmpVec = new Vector3();

    public GameInputHandler(Main game, GameState state, OrthographicCamera mapCamera) {
        this.game = game;
        this.state = state;
        this.mapCamera = mapCamera;

        cameraX = GameState.MAP_WIDTH * GameState.CELL_SIZE / 2f;
        cameraY = GameState.MAP_HEIGHT * GameState.CELL_SIZE / 2f;

        loadTab(activeTab);
    }

    public boolean isOverBuildingPanel(float mx, float my) {
        return showBuildingPanel
                && mx >= panelX && mx <= panelX + panelW
                && my >= panelY && my <= panelY + panelH;
    }

    public void handleInput(float delta) {
        handleKeys(delta);
        if (zoomDelta != 0) {
            zoomAround(Gdx.input.getX(), Gdx.input.getY(), zoomDelta * 0.12f);
            zoomDelta = 0;
        }

        int steps = (int) botScrollAccum;
        if (steps != 0) {
            botScrollAccum -= steps;
            int maxScroll = Math.max(0, getBottomBarSlotCount() - (int) (Gdx.graphics.getWidth() / SLOT_W));
            botScrollOffset = Math.max(0, Math.min(maxScroll, botScrollOffset + steps));
        }

        updatePreviewCell();
        updateHoveredSlot();
        handleMouseClick();
        handleCameraDrag(delta);
        syncCamera();
    }

    // Camera position (acceduto direttamente da GameScreen via mapCamera)
    void setCameraPosition() {
        mapCamera.position.set(cameraX, cameraY, 0);
        mapCamera.update();
    }

    public void restoreCamera(float x, float y, float zoom) {
        this.cameraX = x;
        this.cameraY = y;
        mapCamera.zoom = zoom;
        clampCamera();
        syncCamera();
    }

    /** Rotella: sopra la bottom bar scorre gli slot, altrove zooma la mappa. */
    public void onScroll(float amountY) {
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        if (my < BOTBAR_H + HudRenderer.TAB_H) {
            botScrollAccum += amountY;
        } else {
            zoomDelta += amountY;
        }
    }

    private void handleKeys(float delta) {
        GameSettings.KeyBindings keys = GameSettings.get().keys;

        if (Gdx.input.isKeyJustPressed(keys.toggleFactions)) {
            showFactionPanel = !showFactionPanel;
            if (showFactionPanel)
                panelJustOpened = true;
        }

        if (Gdx.input.isKeyJustPressed(keys.openConsumption)) {
            GameScreen currentGameScreen = (GameScreen) game.getScreen();
            game.setScreen(new ConsumptionScreen(game, state, currentGameScreen));
            return;
        }

        if (Gdx.input.isKeyJustPressed(keys.openTrade)) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            if (selectedBuilding != null) {
                buildingRotated = !buildingRotated;
            }
        }

        boolean escPressed = Gdx.input.isKeyJustPressed(keys.cancel);
        boolean rightClickCancel = keys.cancelMouseRight && Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);

        if (escPressed || rightClickCancel) {
            if (cancelActiveSelection()) {
                return;
            }
            if (escPressed) {
                ((GameScreen) game.getScreen()).toggleInGameMenu();
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(keys.zoomIn))
            zoomAround(Gdx.input.getX(), Gdx.input.getY(), -0.15f);
        if (Gdx.input.isKeyJustPressed(keys.zoomOut))
            zoomAround(Gdx.input.getX(), Gdx.input.getY(), 0.15f);

        float speed = 400 * delta * mapCamera.zoom;
        if (Gdx.input.isKeyPressed(keys.moveUp))
            cameraY += speed;
        if (Gdx.input.isKeyPressed(keys.moveDown))
            cameraY -= speed;
        if (Gdx.input.isKeyPressed(keys.moveLeft))
            cameraX -= speed;
        if (Gdx.input.isKeyPressed(keys.moveRight))
            cameraX += speed;
        clampCamera();

        if (Gdx.input.isKeyJustPressed(keys.switchMap)) {
            if (state.newWorldUnlocked) {
                state.switchMap();
                selectedBuilding = null;
                selectedSlot = -1;
                buildingRotated = false;
                roadDragging = false;
                pipeDragging = false;
                cameraX = (GameState.MAP_WIDTH * GameState.CELL_SIZE) / 2f;
                cameraY = (GameState.MAP_HEIGHT * GameState.CELL_SIZE) / 2f;
                loadTab(activeTab);
                state.updateAllConnections();

                if (game.audio != null) {
                    if (state.onNewWorld) {
                        game.audio.playMusic(AudioManager.MUSIC_NEWWORLD);
                    } else {
                        game.audio.playMusic(AudioManager.MUSIC_GAME);
                    }
                }
            }
        }
    }

    /**
     * Annulla lo stato di selezione/interazione attivo, in ordine di
     * priorità. Restituisce true se ha effettivamente annullato qualcosa,
     * false se non c'era nulla da annullare.
     */
    private boolean cancelActiveSelection() {
        if (showBuildingPanel) {
            showBuildingPanel = false;
            inspectedBuilding = null;
            return true;
        }
        if (showFactionPanel) {
            showFactionPanel = false;
            return true;
        }
        if (roadDragging) {
            roadDragging = false;
            roadStartX = roadStartY = -1;
            return true;
        }
        if (pipeDragging) {
            pipeDragging = false;
            pipeStartX = pipeStartY = -1;
            return true;
        }
        if (fieldDragging) {
            fieldDragging = false;
            fieldStartX = fieldStartY = -1;
            return true;
        }
        if (selectedTool != ToolAction.NONE) {
            selectedTool = ToolAction.NONE;
            selectedSlot = -1;
            return true;
        }
        if (selectedBuilding != null) {
            BuildingInstance owner = penOwner != null ? penOwner : fieldOwner;
            selectedBuilding = null;
            selectedSlot = -1;
            penOwner = null;
            fieldOwner = null;
            if (owner != null) {
                inspectedBuilding = owner;
                showBuildingPanel = true;
                panelJustOpened = true;
            }
            return true;
        }
        return false;
    }

    private void handleMouseClick() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
            return;

        float mx = Gdx.input.getX();
        float screenH = Gdx.graphics.getHeight();
        float my = screenH - Gdx.input.getY();
        float screenW = Gdx.graphics.getWidth();

        if (handleTopBarButtonClick(mx, my, screenW, screenH))
            return;

        if (handleRotateButtonClick(mx, my))
            return;

        // Il click appartiene al pannello: lo gestisce HudRenderer.handlePanelClicks()
        if (isOverBuildingPanel(mx, my))
            return;

        float tabH = HudRenderer.TAB_H;

        // Click sulla bottom bar (inclusi tabs)
        if (my < BOTBAR_H + tabH) {
            if (roadDragging) {
                roadDragging = false;
                roadStartX = roadStartY = -1;
            }
            if (pipeDragging) {
                pipeDragging = false;
                pipeStartX = pipeStartY = -1;
            }
            handleBottomBarClick(mx, my);
            return;
        }

        // Click sulla mappa
        if (selectedBuilding == BuildingType.ROAD) {
            if (!roadDragging) {
                if (previewCellX >= 0 && previewCellY >= 0) {
                    roadDragging = true;
                    roadStartX = previewCellX;
                    roadStartY = previewCellY;
                }
            } else {
                placeRoadLine(roadStartX, roadStartY, previewCellX, previewCellY);
                roadDragging = false;
                roadStartX = roadStartY = -1;
            }
            return;
        }

        if (selectedBuilding == BuildingType.PIPELINE) {
            if (!pipeDragging) {
                if (previewCellX >= 0 && previewCellY >= 0) {
                    pipeDragging = true;
                    pipeStartX = previewCellX;
                    pipeStartY = previewCellY;
                }
            } else {
                placePipeLine(pipeStartX, pipeStartY, previewCellX, previewCellY);
                pipeDragging = false;
                pipeStartX = pipeStartY = -1;
            }
            return;
        }

        if (selectedBuilding != null && selectedBuilding.isField()) {
            if (!fieldDragging) {
                if (previewCellX >= 0 && previewCellY >= 0) {
                    fieldDragging = true;
                    fieldStartX = previewCellX;
                    fieldStartY = previewCellY;
                }
            } else {
                placeFieldArea(fieldStartX, fieldStartY, previewCellX, previewCellY);
                fieldDragging = false;
                finishFieldPlacement();
            }
            return;
        }

        if (selectedBuilding != null) {
            tryPlaceBuilding(previewCellX, previewCellY);
            return;
        }

        if (selectedTool == ToolAction.DEMOLISH) {
            demolishBuildingAt(previewCellX, previewCellY);
            return;
        }

        if (selectedTool == ToolAction.UPGRADE_HOUSE) {
            upgradeHouseAt(previewCellX, previewCellY);
            return;
        }

        // Ispezione
        BuildingInstance clicked = state.getBuildingAt(previewCellX, previewCellY);
        if (clicked != null && clicked.type != BuildingType.ROAD && clicked.type != BuildingType.PIPELINE) {
            inspectedBuilding = clicked;
            showBuildingPanel = true;
            panelJustOpened = true;
            return;
        }
    }

    private boolean handleTopBarButtonClick(float mx, float my, float sw, float sh) {
        // indice da destra: 0 = MAP, 1 = BALANCE, 2 = FACTIONS
        for (int i = 0; i < 3; i++) {
            float[] pos = HudRenderer.topBarButtonPos(i, sw, sh);
            float bx = pos[0], by = pos[1];
            if (mx < bx || mx > bx + HudRenderer.TOPBTN_W || my < by || my > by + HudRenderer.TOPBTN_H)
                continue;

            switch (i) {
                case 0: // MAP
                    if (state.newWorldUnlocked) {
                        state.switchMap();
                        selectedBuilding = null;
                        selectedSlot = -1;
                        buildingRotated = false;
                        roadDragging = false;
                        pipeDragging = false;
                        cameraX = (GameState.MAP_WIDTH * GameState.CELL_SIZE) / 2f;
                        cameraY = (GameState.MAP_HEIGHT * GameState.CELL_SIZE) / 2f;
                        loadTab(activeTab);
                        state.updateAllConnections();
                        if (game.audio != null) {
                            game.audio.playMusic(
                                    state.onNewWorld ? AudioManager.MUSIC_NEWWORLD : AudioManager.MUSIC_GAME);
                        }
                    }
                    return true;
                case 1: // BALANCE
                    GameScreen currentGameScreen = (GameScreen) game.getScreen();
                    game.setScreen(new ConsumptionScreen(game, state, currentGameScreen));
                    return true;
                case 2: // FACTIONS
                    showFactionPanel = !showFactionPanel;
                    if (showFactionPanel)
                        panelJustOpened = true;
                    return true;
            }
        }
        return false;
    }

    private boolean handleRotateButtonClick(float mx, float my) {
        if (selectedBuilding == null || selectedBuilding == BuildingType.ROAD
                || selectedBuilding == BuildingType.PIPELINE)
            return false;

        float tabH = HudRenderer.TAB_H;
        float rbx = HudRenderer.ROTATE_BTN_X;
        float rby = BOTBAR_H + tabH + 4;
        if (mx >= rbx && mx <= rbx + HudRenderer.ROTATE_BTN_W && my >= rby && my <= rby + HudRenderer.ROTATE_BTN_H) {
            buildingRotated = !buildingRotated;
            return true;
        }
        return false;
    }

    private void handleBottomBarClick(float mx, float my) {
        penOwner = null;
        fieldOwner = null;
        fieldDragging = false;
        float screenW = Gdx.graphics.getWidth();
        float tabH = HudRenderer.TAB_H;
        float tabY = BOTBAR_H;

        // Click sui tabs
        if (my >= tabY && my < tabY + tabH) {
            float tabW = screenW / TAB_LABELS.length;
            int t = (int) (mx / tabW);
            if (t >= 0 && t < TAB_LABELS.length) {
                activeTab = t;
                selectedSlot = -1;
                selectedBuilding = null;
                selectedTool = ToolAction.NONE;
                botScrollOffset = 0;
                loadTab(activeTab);
            }
            return;
        }

        // Click sugli slot
        if (my >= 0 && my < tabY) {
            int slotsVisible = (int) (screenW / SLOT_W) + 1;
            int totalSlots = getBottomBarSlotCount();
            for (int i = 0; i < slotsVisible && i + botScrollOffset < totalSlots; i++) {
                float sx = i * SLOT_W;
                if (mx >= sx && mx < sx + SLOT_W) {
                    int idx = i + botScrollOffset;
                    if (selectedSlot == idx) {
                        selectedSlot = -1;
                        selectedBuilding = null;
                        selectedTool = ToolAction.NONE;
                    } else {
                        selectedSlot = idx;
                        selectedTool = getToolForSlot(idx);
                        if (selectedTool != ToolAction.NONE) {
                            selectedBuilding = null;
                        } else {
                            selectedBuilding = bottomBarBuildings[getBuildingIndexForSlot(idx)];
                        }
                    }
                    return;
                }
            }
        }
    }

    /**
     * Avvia il piazzamento di un recinto per l'allevamento dato (chiamato dal
     * pannello).
     */
    public void startPenPlacement(BuildingInstance ranch, BuildingType pen) {
        penOwner = ranch;
        selectedBuilding = pen;
        selectedSlot = -1;
        selectedTool = ToolAction.NONE;
        buildingRotated = false;
        showBuildingPanel = false;
        inspectedBuilding = null;
    }

    private void finishPenPlacement() {
        BuildingInstance owner = penOwner;
        penOwner = null;
        selectedBuilding = null;
        selectedSlot = -1;
        buildingRotated = false;
        if (owner != null) {
            inspectedBuilding = owner;
            showBuildingPanel = true;
            panelJustOpened = true;
        }
    }

    public void startFieldPlacement(BuildingInstance farm, BuildingType field) {
        fieldOwner = farm;
        selectedBuilding = field;
        selectedSlot = -1;
        selectedTool = ToolAction.NONE;
        buildingRotated = false;
        showBuildingPanel = false;
        inspectedBuilding = null;
    }

    private void finishFieldPlacement() {
        BuildingInstance owner = fieldOwner;
        fieldOwner = null;
        selectedBuilding = null;
        selectedSlot = -1;
        if (owner != null) {
            inspectedBuilding = owner;
            showBuildingPanel = true;
            panelJustOpened = true;
        }
    }

    /**
     * True se il rettangolo tocca (con un lato) la fattoria o uno dei suoi campi
     * già piazzati.
     */
    private boolean touchesFarmOrItsFields(BuildingInstance farm, int cx, int cy, int w, int h) {
        if (edgeAdjacent(farm.rootX, farm.rootY, farm.effectiveW(), farm.effectiveH(), cx, cy, w, h))
            return true;
        for (BuildingInstance f : state.buildings) {
            if (f.type.isField() && f.ownerX == farm.rootX && f.ownerY == farm.rootY
                    && edgeAdjacent(f.rootX, f.rootY, f.effectiveW(), f.effectiveH(), cx, cy, w, h))
                return true;
        }
        return false;
    }

    /**
     * Piazza tutti i campi validi nel rettangolo tra i due angoli, propagando
     * l'adiacenza (fattoria o campo già piazzato in questo stesso trascinamento),
     * finché c'è spazio libero, tetto non superato e risorse sufficienti.
     */
    private void placeFieldArea(int x1, int y1, int x2, int y2) {
        if (fieldOwner == null || selectedBuilding == null || !selectedBuilding.isField())
            return;

        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);

        List<int[]> pending = new ArrayList<>();
        for (int y = minY; y <= maxY; y++)
            for (int x = minX; x <= maxX; x++)
                pending.add(new int[] { x, y });

        boolean placedAny;
        do {
            placedAny = false;
            Iterator<int[]> it = pending.iterator();
            while (it.hasNext()) {
                if (state.countFields(fieldOwner) >= fieldOwner.type.fieldCap)
                    break;

                int[] cell = it.next();
                if (canPlace(selectedBuilding, cell[0], cell[1])) {
                    state.resources.spend(costFor(selectedBuilding));
                    BuildingInstance nb = new BuildingInstance(selectedBuilding, cell[0], cell[1]);
                    nb.ownerX = fieldOwner.rootX;
                    nb.ownerY = fieldOwner.rootY;
                    state.buildings.add(nb);
                    it.remove();
                    placedAny = true;
                }
            }
        } while (placedAny);

        state.updateAllConnections();
        SaveManager.getInstance().autosave(state, mapCamera.position.x, mapCamera.position.y, mapCamera.zoom);
    }

    /**
     * True se i due rettangoli condividono un lato (l'angolo da solo non basta).
     */
    private static boolean edgeAdjacent(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        boolean touchX = (bx == ax + aw || bx + bw == ax) && by < ay + ah && ay < by + bh;
        boolean touchY = (by == ay + ah || by + bh == ay) && bx < ax + aw && ax < bx + bw;
        return touchX || touchY;
    }

    /**
     * True se il rettangolo tocca (con un lato) l'allevamento o uno dei SUOI
     * recinti già costruiti.
     */
    private boolean touchesRanchOrItsPens(BuildingInstance ranch, int cx, int cy, int w, int h) {
        if (edgeAdjacent(ranch.rootX, ranch.rootY, ranch.effectiveW(), ranch.effectiveH(), cx, cy, w, h))
            return true;
        for (BuildingInstance p : state.buildings) {
            if (p.type.isPen() && p.ownerX == ranch.rootX && p.ownerY == ranch.rootY
                    && edgeAdjacent(p.rootX, p.rootY, p.effectiveW(), p.effectiveH(), cx, cy, w, h))
                return true;
        }
        return false;
    }

    public void loadTab(int tab) {
        BuildingType previousSelected = selectedBuilding;

        List<BuildingType> list = new ArrayList<>();
        BuildingType.Category cat = TAB_CATS[tab];
        for (BuildingType bt : BuildingType.getAllBuildable()) {
            if (!bt.buildable)
                continue;
            if (bt.getCategory() != cat)
                continue;
            if (!bt.isAllowedInRegion(state.getCurrentRegion()))
                continue;
            if (bt.unlockClass != null && state.getClassPopulation(bt.unlockClass) < bt.unlockAmount)
                continue;
            if (bt.requiredEventId != null && !state.eventManager.hasFired(bt.requiredEventId)) // ← NUOVO
                continue;

            if (cat == BuildingType.Category.MONUMENTI) {
                if (!bt.isFirstMonumentLevel())
                    continue;
                if (state.monumentExists(bt.monumentChainId))
                    continue;
            }

            list.add(bt);
        }
        bottomBarBuildings = list.toArray(new BuildingType[0]);

        // Mantiene la selezione se l'edificio scelto è ancora presente
        if (previousSelected != null && penOwner == null && fieldOwner == null) {
            int newIndex = -1;
            for (int i = 0; i < bottomBarBuildings.length; i++) {
                if (bottomBarBuildings[i] == previousSelected) {
                    newIndex = i;
                    break;
                }
            }
            if (newIndex >= 0) {
                selectedSlot = getBottomBarToolCount() + newIndex;
            } else {
                // l'edificio non c'è più nella lista (es. popolazione scesa sotto soglia)
                selectedBuilding = null;
                selectedSlot = -1;
            }
        }
    }

    public int getBottomBarToolCount() {
        return activeTab == 0 ? 2 : 0;
    }

    public int getBottomBarSlotCount() {
        return getBottomBarToolCount() + bottomBarBuildings.length;
    }

    public int getBuildingIndexForSlot(int slot) {
        return slot - getBottomBarToolCount();
    }

    public void updateHoveredSlot() {
        hoveredSlot = -1;
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        if (my < 0 || my >= BOTBAR_H)
            return;

        float screenW = Gdx.graphics.getWidth();
        int slotsVisible = (int) (screenW / SLOT_W) + 1;
        int totalSlots = getBottomBarSlotCount();

        for (int i = 0; i < slotsVisible && i + botScrollOffset < totalSlots; i++) {
            float sx = i * SLOT_W;
            if (mx >= sx && mx < sx + SLOT_W) {
                hoveredSlot = i + botScrollOffset;
                return;
            }
        }
    }

    public String getToolLabel(ToolAction tool) {
        switch (tool) {
            case DEMOLISH:
                return "Demolish";
            case UPGRADE_HOUSE:
                return "Upgrade";
            default:
                return "";
        }
    }

    public ToolAction getToolForSlot(int slot) {
        if (activeTab != 0)
            return ToolAction.NONE;
        if (slot == 0)
            return ToolAction.DEMOLISH;
        if (slot == 1)
            return ToolAction.UPGRADE_HOUSE;
        return ToolAction.NONE;
    }

    public String getToolColorHex(ToolAction tool) {
        switch (tool) {
            case DEMOLISH:
                return "cc4444";
            case UPGRADE_HOUSE:
                return "55cc55";
            default:
                return "888888";
        }
    }

    private void handleCameraDrag(float delta) {
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            if (!dragging) {
                dragging = true;
                dragStartX = Gdx.input.getX();
                dragStartY = Gdx.input.getY();
            } else {
                float dx = (Gdx.input.getX() - dragStartX) * mapCamera.zoom;
                float dy = (Gdx.input.getY() - dragStartY) * mapCamera.zoom;
                cameraX -= dx;
                cameraY += dy;
                dragStartX = Gdx.input.getX();
                dragStartY = Gdx.input.getY();
            }
        } else {
            dragging = false;
        }
        clampCamera();
    }

    private void syncCamera() {
        mapCamera.position.set(cameraX, cameraY, 0);
        mapCamera.update();
    }

    public void zoomAround(float sx, float sy, float delta) {
        float oldZoom = mapCamera.zoom;
        float newZoom = Math.max(0.3f, Math.min(maxZoom(), oldZoom + delta));
        if (newZoom == oldZoom)
            return;

        tmpVec.set(sx, sy, 0);
        mapCamera.unproject(tmpVec);
        float beforeX = tmpVec.x, beforeY = tmpVec.y;

        mapCamera.zoom = newZoom;
        mapCamera.position.set(cameraX, cameraY, 0);
        mapCamera.update();

        tmpVec.set(sx, sy, 0);
        mapCamera.unproject(tmpVec);
        cameraX -= (tmpVec.x - beforeX);
        cameraY -= (tmpVec.y - beforeY);
        clampCamera();
        mapCamera.position.set(cameraX, cameraY, 0);
        mapCamera.update();
    }

    private float maxZoom() {
        float zw = (GameState.MAP_WIDTH * GameState.CELL_SIZE) / mapCamera.viewportWidth;
        float zh = (GameState.MAP_HEIGHT * GameState.CELL_SIZE) / mapCamera.viewportHeight;
        return Math.min(zw, zh);
    }

    private void clampCamera() {
        float halfW = mapCamera.viewportWidth / 2f * mapCamera.zoom;
        float halfH = mapCamera.viewportHeight / 2f * mapCamera.zoom;
        cameraX = Math.max(halfW, Math.min(GameState.MAP_WIDTH * GameState.CELL_SIZE - halfW, cameraX));
        cameraY = Math.max(halfH, Math.min(GameState.MAP_HEIGHT * GameState.CELL_SIZE - halfH, cameraY));
    }

    public float getCameraX() {
        return cameraX;
    }

    public float getCameraY() {
        return cameraY;
    }

    private void updatePreviewCell() {
        float screenH = Gdx.graphics.getHeight();
        float my = Gdx.input.getY();
        // Non mostrare preview se il mouse è sopra la UI
        if (my > screenH - BOTBAR_H || my < TOPBAR_H) {
            previewCellX = previewCellY = -1;
            return;
        }
        tmpVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        mapCamera.unproject(tmpVec);
        previewCellX = (int) (tmpVec.x / GameState.CELL_SIZE);
        previewCellY = (int) (tmpVec.y / GameState.CELL_SIZE);

        // Clamp ai limiti della mappa
        previewCellX = Math.max(0, Math.min(GameState.MAP_WIDTH - 1, previewCellX));
        previewCellY = Math.max(0, Math.min(GameState.MAP_HEIGHT - 1, previewCellY));
    }

    private void demolishBuildingAt(int cx, int cy) {
        BuildingInstance target = state.getBuildingAt(cx, cy);
        if (target == null) {
            return;
        }

        state.removeBuilding(target); // se è un allevamento toglie anche i suoi recinti
        if (inspectedBuilding == target) {
            inspectedBuilding = null;
            showBuildingPanel = false;
        }
        state.updateAllConnections();
        if (game.audio != null)
            game.audio.playSound(AudioManager.SFX_DEMOLISH);
        SaveManager.getInstance().autosave(state, mapCamera.position.x, mapCamera.position.y, mapCamera.zoom);
    }

    private void upgradeHouseAt(int cx, int cy) {
        BuildingInstance target = state.getBuildingAt(cx, cy);
        if (target == null) {
            return;
        }

        if (target.type.isHouse) {
            BuildingType before = target.type;
            if (!target.upgradeHouse(state.resources)) {
                return;
            }
            state.ideologyManager.applyShift(target.type.axisShift);
            if (game.audio != null)
                game.audio.playSound(AudioManager.SFX_UPGRADE_HOUSE);
        } else if (target.type.isMonument) {
            // Avvia l'upgrade del monumento
            if (!target.startMonumentUpgrade(state.resources, state.difficulty.monumentCostMultiplier)) {
                return;
            }
            // Opzionale: suono specifico per monumento
            if (game.audio != null)
                game.audio.playSound(AudioManager.SFX_UI_CLICK);
        } else {
            return;
        }

        SaveManager.getInstance().autosave(state, mapCamera.position.x, mapCamera.position.y, mapCamera.zoom);
    }

    public void tryPlaceBuilding(int cx, int cy) {
        if (selectedBuilding == null)
            return;
        if (!canPlace(selectedBuilding, cx, cy))
            return;

        state.resources.spend(costFor(selectedBuilding));
        BuildingInstance newBuilding = new BuildingInstance(selectedBuilding, cx, cy);
        newBuilding.rotated = buildingRotated;
        if (selectedBuilding.isPen() && penOwner != null) {
            newBuilding.ownerX = penOwner.rootX;
            newBuilding.ownerY = penOwner.rootY;
        }
        state.buildings.add(newBuilding);
        state.ideologyManager.applyShift(selectedBuilding.axisShift);
        state.updateAllConnections();
        SaveManager.getInstance().autosave(state, mapCamera.position.x, mapCamera.position.y, mapCamera.zoom);

        if (selectedBuilding.isPen())
            finishPenPlacement();
    }

    public boolean canPlace(BuildingType bt, int cx, int cy) {
        int w = buildingRotated ? bt.h : bt.w;
        int h = buildingRotated ? bt.w : bt.h;

        if (cx < 0 || cy < 0 || cx + w > GameState.MAP_WIDTH || cy + h > GameState.MAP_HEIGHT)
            return false;

        if (bt.isPen()) {
            if (penOwner == null || !bt.penOwnerId.equals(penOwner.type.id))
                return false;
            if (state.hasPen(penOwner, bt.id))
                return false;
            if (!touchesRanchOrItsPens(penOwner, cx, cy, w, h))
                return false;
        }

        if (bt.isField()) {
            if (fieldOwner == null || !bt.fieldOwnerId.equals(fieldOwner.type.id))
                return false;
            if (state.countFields(fieldOwner) >= fieldOwner.type.fieldCap)
                return false;
            if (!touchesFarmOrItsFields(fieldOwner, cx, cy, w, h))
                return false;
        }

        if (bt.requiresDeposit && !state.hasDepositAt(bt, cx, cy))
            return false;

        if (bt == BuildingType.DOCK || bt == BuildingType.OIL_DOCK) {
            for (BuildingInstance b : state.buildings)
                if (b.type == bt)
                    return false;
        }

        if (isWaterBuilding(bt)) {
            if (!isWaterBuildingValid(cx, cy, w, h))
                return false;
        } else if (!canPlaceOnTerrain(bt, cx, cy, w, h)) {
            return false;
        }

        if (state.isOccupied(cx, cy, w, h))
            return false;

        if (bt.isMonument && bt.isFirstMonumentLevel() && state.monumentExists(bt.monumentChainId))
            return false;

        return state.resources.canAfford(costFor(bt));
    }

    /** Costo effettivo: i monumenti (fase 0) scalano con la difficoltà. */
    private Map<String, Float> costFor(BuildingType bt) {
        if (!bt.isMonument)
            return bt.cost;
        float mult = state.difficulty.monumentCostMultiplier;
        Map<String, Float> scaled = new HashMap<>();
        for (Map.Entry<String, Float> e : bt.cost.entrySet())
            scaled.put(e.getKey(), e.getValue() * mult);
        return scaled;
    }

    private boolean canPlaceOnTerrain(BuildingType bt, int cx, int cy, int w, int h) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int t = state.terrainMap[cy + dy][cx + dx];
                if (t == MapGenerator.WATER) {
                    boolean waterOk = bt.onlyTerrain != null && containsTerrain(bt.onlyTerrain, MapGenerator.WATER);
                    if (!waterOk)
                        return false;
                }
                if (t == MapGenerator.MOUNTAIN || t == MapGenerator.PEAK) {
                    boolean mtnOk = bt.onlyTerrain != null
                            && (containsTerrain(bt.onlyTerrain, MapGenerator.MOUNTAIN)
                                    || containsTerrain(bt.onlyTerrain, MapGenerator.PEAK));
                    if (!mtnOk)
                        return false;
                }
                if (!bt.allowedOnTerrain(t))
                    return false;
            }
        }
        return true;
    }

    private boolean isWaterBuilding(BuildingType bt) {
        return bt == BuildingType.DOCK || bt == BuildingType.OIL_DOCK
                || bt == BuildingType.FISHERY || bt == BuildingType.NITRATE_EXTRACTOR;
    }

    private boolean isWaterBuildingValid(int cx, int cy, int w, int h) {
        boolean touchesLand = false;
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int x = cx + dx, y = cy + dy;
                if (x < 0 || y < 0 || x >= GameState.MAP_WIDTH || y >= GameState.MAP_HEIGHT)
                    return false;
                if (state.terrainMap[y][x] != MapGenerator.WATER)
                    return false;
                // Controlla se tocca terra (adiacente a cella non acqua)
                if ((x > 0 && state.terrainMap[y][x - 1] != MapGenerator.WATER) ||
                        (x < GameState.MAP_WIDTH - 1 && state.terrainMap[y][x + 1] != MapGenerator.WATER) ||
                        (y > 0 && state.terrainMap[y - 1][x] != MapGenerator.WATER) ||
                        (y < GameState.MAP_HEIGHT - 1 && state.terrainMap[y + 1][x] != MapGenerator.WATER))
                    touchesLand = true;
            }
        }
        return touchesLand;
    }

    private boolean canPlaceOnTerrain(BuildingType bt, int cx, int cy) {
        for (int dy = 0; dy < bt.h; dy++) {
            for (int dx = 0; dx < bt.w; dx++) {
                int t = state.terrainMap[cy + dy][cx + dx];
                if (t == MapGenerator.WATER) {
                    boolean waterOk = bt.onlyTerrain != null && containsTerrain(bt.onlyTerrain, MapGenerator.WATER);
                    if (!waterOk)
                        return false;
                }
                if (t == MapGenerator.MOUNTAIN || t == MapGenerator.PEAK) {
                    boolean mtnOk = bt.onlyTerrain != null
                            && (containsTerrain(bt.onlyTerrain, MapGenerator.MOUNTAIN)
                                    || containsTerrain(bt.onlyTerrain, MapGenerator.PEAK));
                    if (!mtnOk)
                        return false;
                }
                if (!bt.allowedOnTerrain(t))
                    return false;
            }
        }
        return true;
    }

    private static boolean containsTerrain(int[] arr, int val) {
        if (arr == null)
            return false;
        for (int v : arr)
            if (v == val)
                return true;
        return false;
    }

    public List<int[]> getRoadLine(int x1, int y1, int x2, int y2) {
        List<int[]> cells = new ArrayList<>();
        int dx = x2 - x1, dy = y2 - y1;
        int ax = Math.abs(dx), ay = Math.abs(dy);
        int sx = dx >= 0 ? 1 : -1, sy = dy >= 0 ? 1 : -1;
        if (ax >= ay) {
            for (int i = 0; i <= ax; i++)
                cells.add(new int[] { x1 + i * sx, y1 });
            for (int i = 1; i <= ay; i++)
                cells.add(new int[] { x2, y1 + i * sy });
        } else {
            for (int i = 0; i <= ay; i++)
                cells.add(new int[] { x1, y1 + i * sy });
            for (int i = 1; i <= ax; i++)
                cells.add(new int[] { x1 + i * sx, y2 });
        }
        return cells;
    }

    private void placeRoadLine(int x1, int y1, int x2, int y2) {
        int placed = 0;
        for (int[] cell : getRoadLine(x1, y1, x2, y2)) {
            int cx = cell[0], cy = cell[1];
            if (cx < 0 || cy < 0 || cx >= GameState.MAP_WIDTH || cy >= GameState.MAP_HEIGHT)
                continue;
            if (state.isOccupied(cx, cy, 1, 1))
                continue;
            int t = state.terrainMap[cy][cx];
            if (t == MapGenerator.WATER || t == MapGenerator.MOUNTAIN || t == MapGenerator.PEAK)
                continue;
            if (!state.resources.canAfford(BuildingType.ROAD.cost))
                break;
            state.resources.spend(BuildingType.ROAD.cost);
            state.buildings.add(new BuildingInstance(BuildingType.ROAD, cx, cy));
            placed++;
        }
        if (placed > 0) {
            state.updateAllConnections();
        }
    }

    private void placePipeLine(int x1, int y1, int x2, int y2) {
        int placed = 0;
        for (int[] cell : getRoadLine(x1, y1, x2, y2)) {
            int cx = cell[0], cy = cell[1];
            if (cx < 0 || cy < 0 || cx >= GameState.MAP_WIDTH || cy >= GameState.MAP_HEIGHT)
                continue;
            if (state.isOccupied(cx, cy, 1, 1))
                continue;
            int t = state.terrainMap[cy][cx];
            if (t == MapGenerator.WATER || t == MapGenerator.MOUNTAIN || t == MapGenerator.PEAK)
                continue;
            if (!state.resources.canAfford(BuildingType.PIPELINE.cost))
                break;
            state.resources.spend(BuildingType.PIPELINE.cost);
            state.buildings.add(new BuildingInstance(BuildingType.PIPELINE, cx, cy));
            placed++;
        }
        if (placed > 0) {
            state.updateAllConnections();
        }
    }

    public boolean simulateConnectionAfterPlace(BuildingType bt, int cx, int cy) {
        if (bt.requiredConnection == null || bt.requiredConnection == BuildingType.ConnectionType.NONE)
            return true;

        BuildingInstance phantom = new BuildingInstance(bt, cx, cy);
        phantom.rotated = buildingRotated;

        List<BuildingInstance> temp = new ArrayList<>(state.buildings);
        temp.add(phantom);

        Set<String> road = state.buildPathNetwork(temp, BuildingType.DOCK, BuildingType.ROAD);
        Set<String> pipe = state.buildPathNetwork(temp, BuildingType.OIL_DOCK, BuildingType.PIPELINE);

        if (bt.requiredConnection == BuildingType.ConnectionType.ROAD)
            return state.buildingTouchesNetwork(phantom, road);
        if (bt.requiredConnection == BuildingType.ConnectionType.PIPE)
            return state.buildingTouchesNetwork(phantom, pipe);
        if (bt.requiredConnection == BuildingType.ConnectionType.BOTH)
            return state.buildingTouchesNetwork(phantom, road)
                    && state.buildingTouchesNetwork(phantom, pipe);
        return false;
    }
}