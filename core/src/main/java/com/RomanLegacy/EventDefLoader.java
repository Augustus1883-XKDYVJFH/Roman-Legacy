package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Carica data/events.json mantenendo l'ordine del file. */
public class EventDefLoader {

    public static List<EventDef> load(String path) {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);

        EventDef[] defs = json.fromJson(EventDef[].class, Gdx.files.internal(path));

        List<EventDef> list = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (EventDef d : defs) {
            if (d.id == null || d.id.isEmpty()) {
                Gdx.app.error("EventDefLoader", "Event without id, skipped");
                continue;
            }
            if (!seen.add(d.id)) {
                Gdx.app.error("EventDefLoader", "Duplicate event id in JSON: " + d.id);
                continue;
            }
            list.add(d);
        }
        return list;
    }
}