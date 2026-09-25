package com.RomanLegacy;

import java.util.UUID;

public class BuildingData {
    public float x, y, width, height;
    public String id;
    public String uuid;
    public float productionTimer = 0f;
    public boolean paused = false;
    public boolean rotated = false;
    public boolean connected = false;
    public boolean isHouse = false;
    public float housePop = 0f;
    public boolean monumentUpgrading = false;
    public float monumentUpgradeProgress = 0f;
    public int ownerX = -1;
    public int ownerY = -1;

    public BuildingData() {
        // Costruttore vuoto per serializzazione
    }

    public BuildingData(String id, float x, float y, float width, float height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.uuid = UUID.randomUUID().toString();
    }
}