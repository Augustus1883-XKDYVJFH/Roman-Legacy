package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.HashMap;
import java.util.Map;

/**
 * Carica data/buildings.json in una mappa id -> BuildingDef, una sola
 * volta all'avvio. Usato da BuildingType per costruire i campi statici
 * final tramite fromDef(id).
 */
public class BuildingDefLoader {

    public static Map<String, BuildingDef> load(String path) {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);

        BuildingDef[] defs = json.fromJson(BuildingDef[].class, Gdx.files.internal(path));

        Map<String, BuildingDef> map = new HashMap<>();
        for (BuildingDef d : defs) {
            if (map.containsKey(d.id)) {
                Gdx.app.error("BuildingDefLoader", "Duplicate building id in JSON: " + d.id);
            }
            map.put(d.id, d);
        }
        return map;
    }
}