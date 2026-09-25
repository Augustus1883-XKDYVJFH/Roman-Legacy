package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private static SaveManager instance;
    private final Json json;

    public static final int SAVE_VERSION = 9;

    public static class SaveSlotInfo {
        public String filename;
        public String displayName;
        public long timestamp;
        public boolean isAutosave;
    }

    private SaveManager() {
        json = new Json();
        json.setIgnoreUnknownFields(true);
        json.setUsePrototypes(false);

        json.setElementType(SaveData.class, "buildingsOld", BuildingData.class);
        json.setElementType(SaveData.class, "buildingsNew", BuildingData.class);
        json.setElementType(SaveData.class, "fleet", TradeManager.ShipSaveData.class);

        FileHandle dir = Gdx.files.local("saves");
        if (!dir.exists())
            dir.mkdirs();
    }

    public static SaveManager getInstance() {
        if (instance == null)
            instance = new SaveManager();
        return instance;
    }

    // ── API pubblica ─────────────────────────────────────────────────────────

    /** Autosave a rotazione su 3 slot: sovrascrive sempre il più vecchio. */
    public void autosave(GameState state, float camX, float camY, float camZoom) {
        String targetSlot = "autosave1.json";
        long oldestTime = Long.MAX_VALUE;
        for (int i = 1; i <= 3; i++) {
            String fname = "autosave" + i + ".json";
            FileHandle fh = Gdx.files.local("saves/" + fname);
            if (!fh.exists()) {
                targetSlot = fname;
                oldestTime = -1;
                break;
            }
            try {
                SaveData sd = json.fromJson(SaveData.class, fh.readString());
                if (sd.timestamp < oldestTime) {
                    oldestTime = sd.timestamp;
                    targetSlot = fname;
                }
            } catch (Exception e) {
                targetSlot = fname; // slot corrotto, sovrascrivilo
                break;
            }
        }
        writeSaveData(buildSaveData(state, camX, camY, camZoom), targetSlot);
    }

    /** Salvataggio manuale con nome custom. Stesso nome = sovrascrive. */
    public void saveManual(GameState state, float camX, float camY, float camZoom, String name) {
        String filename = "manual_" + sanitizeName(name) + ".json";
        writeSaveData(buildSaveData(state, camX, camY, camZoom), filename);
    }

    /** Scorciatoia F5: sempre lo stesso slot manuale "Quicksave". */
    public void quickSave(GameState state, float camX, float camY, float camZoom) {
        saveManual(state, camX, camY, camZoom, "Quicksave");
    }

    public SaveData quickLoad() {
        return loadSave("manual_Quicksave.json");
    }

    /** Lista tutti i salvataggi (autosave + manuali), più recenti prima. */
    public List<SaveSlotInfo> listSaves() {
        List<SaveSlotInfo> result = new ArrayList<>();
        FileHandle dir = Gdx.files.local("saves");
        if (!dir.exists())
            return result;

        for (FileHandle f : dir.list(".json")) {
            String name = f.name();
            if (!name.startsWith("autosave") && !name.startsWith("manual_"))
                continue;
            try {
                SaveData sd = json.fromJson(SaveData.class, f.readString());
                SaveSlotInfo info = new SaveSlotInfo();
                info.filename = name;
                info.isAutosave = name.startsWith("autosave");
                info.timestamp = sd.timestamp;
                info.displayName = info.isAutosave
                        ? "Autosave " + name.replace("autosave", "").replace(".json", "")
                        : name.substring("manual_".length(), name.length() - 5);
                result.add(info);
            } catch (Exception e) {
                // salvataggio corrotto, ignoralo nella lista
            }
        }
        result.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        return result;
    }

    public SaveData loadSave(String filename) {
        FileHandle fh = Gdx.files.local("saves/" + filename);
        if (!fh.exists())
            return null;
        try {
            return json.fromJson(SaveData.class, fh.readString());
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Load failed: " + filename, e);
            return null;
        }
    }

    public void deleteSave(String filename) {
        FileHandle fh = Gdx.files.local("saves/" + filename);
        if (fh.exists())
            fh.delete();
    }

    /** Elimina tutti i salvataggi, autosave inclusi. */
    public void deleteAllSaves() {
        FileHandle dir = Gdx.files.local("saves");
        if (!dir.exists())
            return;
        for (FileHandle f : dir.list(".json")) {
            if (f.name().startsWith("manual_") || f.name().startsWith("autosave"))
                f.delete();
        }
    }

    public boolean hasAnySave() {
        return !listSaves().isEmpty();
    }

    // ── Privati ──────────────────────────────────────────────────────────────

    private SaveData buildSaveData(GameState state, float camX, float camY, float camZoom) {
        SaveData saveData = new SaveData();
        saveData.version = SAVE_VERSION;
        saveData.timestamp = System.currentTimeMillis();

        saveData.buildingsOld = serializeBuildings(state.buildingsOld);
        saveData.buildingsNew = serializeBuildings(state.buildingsNew);

        saveData.gold = (int) state.resources.get("coin");
        saveData.population = state.resources.get("population");
        saveData.onNewWorld = state.onNewWorld;
        saveData.newWorldUnlocked = state.newWorldUnlocked;

        saveData.cameraX = camX;
        saveData.cameraY = camY;
        saveData.cameraZoom = camZoom;

        saveData.terrainMapOld = state.terrainMapOld;
        saveData.terrainMapNew = state.terrainMapNew;

        saveData.resources = new java.util.HashMap<>();
        for (var entry : state.resources.getAll().entrySet()) {
            saveData.resources.put(entry.getKey(), entry.getValue().amount);
        }

        saveData.factionSeed = state.factionManager.getSeed();
        FactionManager.FactionState[] slots = state.factionManager.getAllSlots();
        saveData.factionRelations = new int[] {
                slots[0].relation, slots[1].relation, slots[2].relation
        };
        saveData.factionArrived = new boolean[] {
                slots[0].arrived, slots[1].arrived, slots[2].arrived
        };

        saveData.fleet = state.tradeManager.serialize();
        saveData.ideologyValue = state.ideologyManager.getValue();
        saveData.firedEvents = state.eventManager.serializeFiredEvents();
        saveData.pendingEventId = state.eventManager.getPendingEventId();
        saveData.gameSpeed = state.gameSpeed;
        saveData.difficulty = state.difficulty.name();

        return saveData;
    }

    private void writeSaveData(SaveData saveData, String filename) {
        try {
            FileHandle dir = Gdx.files.local("saves");
            if (!dir.exists())
                dir.mkdirs();
            Gdx.files.local("saves/" + filename).writeString(json.prettyPrint(saveData), false);
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Save failed: " + filename, e);
        }
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank())
            name = "Save";
        String cleaned = name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        return cleaned.length() > 60 ? cleaned.substring(0, 60) : cleaned;
    }

    private Array<BuildingData> serializeBuildings(List<BuildingInstance> list) {
        Array<BuildingData> result = new Array<>();
        for (BuildingInstance b : list) {
            BuildingData data = new BuildingData();
            data.x = b.rootX * GameState.CELL_SIZE;
            data.y = b.rootY * GameState.CELL_SIZE;
            data.width = b.effectiveW() * GameState.CELL_SIZE;
            data.height = b.effectiveH() * GameState.CELL_SIZE;
            data.id = b.type.id;
            data.uuid = b.rootX + "_" + b.rootY + "_" + System.currentTimeMillis();
            data.productionTimer = b.progress;
            data.paused = b.paused;
            data.rotated = b.rotated;
            data.connected = b.connected;
            data.housePop = b.housePop;
            data.isHouse = b.type.isHouse;
            data.monumentUpgrading = b.monumentUpgrading;
            data.monumentUpgradeProgress = b.monumentUpgradeProgress;
            data.ownerX = b.ownerX;
            data.ownerY = b.ownerY;
            result.add(data);
        }
        return result;
    }
}