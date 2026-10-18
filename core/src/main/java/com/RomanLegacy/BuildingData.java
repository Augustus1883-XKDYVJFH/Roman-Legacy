package com.RomanLegacy;

public class BuildingData {
    public float x, y;
    public String id;
    public float productionTimer = 0f;
    public boolean paused = false;
    public boolean rotated = false;
    public boolean connected = false;
    public float housePop = 0f;
    public boolean monumentUpgrading = false;
    public float monumentUpgradeProgress = 0f;
    public int ownerX = -1;
    public int ownerY = -1;

    public BuildingData() {
        // Costruttore vuoto per serializzazione
    }

}