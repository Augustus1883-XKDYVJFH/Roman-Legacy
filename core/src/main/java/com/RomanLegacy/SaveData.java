package com.RomanLegacy;

import java.util.*;
import com.badlogic.gdx.utils.Array;

public class SaveData {
    public Array<BuildingData> buildingsOld;
    public Array<BuildingData> buildingsNew;
    public int version;
    public long timestamp;
    public boolean onNewWorld;
    public boolean newWorldUnlocked;
    public int gold;
    public float population;
    public String difficulty;

    // Camera position
    public float cameraX;
    public float cameraY;
    public float cameraZoom;

    // Mappe
    public int[][] terrainMapOld;
    public int[][] terrainMapNew;

    // Risorse
    public Map<String, Float> resources;
    public List<String> firedEvents;

    // Fazioni (fix: seed e stato non venivano salvati, le fazioni cambiavano ad
    // ogni load)
    public long factionSeed;
    public int[] factionRelations; // relazioni [-100, 100] per ciascuno dei 3 slot
    public boolean[] factionArrived; // quali fazioni sono già arrivate
    public float ideologyValue;
    public String pendingEventId;
    public float gameSpeed;

    // Flotta commerciale
    public List<TradeManager.ShipSaveData> fleet;
    public Map<String, Float> serviceSatisfaction;

    public SaveData() {
        buildingsOld = new Array<>();
        buildingsNew = new Array<>();
    }
}