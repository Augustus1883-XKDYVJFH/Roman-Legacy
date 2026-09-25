package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

/**
 * Carica e gestisce le icone 8bit delle risorse/edifici.
 * Se un'icona non esiste su disco, getResourceIcon() ritorna null e il
 * chiamante deve fare fallback al quadrato colorato.
 */
public class IconManager {

    private final Map<String, Texture> resourceIcons = new HashMap<>();
    private final Map<String, Texture> buildingIcons = new HashMap<>();
    private final Map<String, Texture> factionIcons = new HashMap<>();

    private static final String RESOURCE_PATH = "icons/resources/";
    private static final String BUILDING_PATH = "icons/buildings/";
    private static final String FACTION_PATH = "icons/factions/";

    public Texture getResourceIcon(String resourceId) {
        return loadIfNeeded(resourceIcons, RESOURCE_PATH, resourceId);
    }

    public Texture getBuildingIcon(String buildingId) {
        return loadIfNeeded(buildingIcons, BUILDING_PATH, buildingId);
    }

    public Texture getFactionIcon(Faction faction) {
        if (faction == null)
            return null;
        return loadIfNeeded(factionIcons, FACTION_PATH, faction.name().toLowerCase());
    }

    private Texture loadIfNeeded(Map<String, Texture> cache, String basePath, String id) {
        if (id == null)
            return null;
        if (cache.containsKey(id))
            return cache.get(id); // può essere null se già verificato assente

        String path = basePath + id + ".png";
        Texture tex = null;
        try {
            if (Gdx.files.internal(path).exists()) {
                tex = new Texture(Gdx.files.internal(path));
                tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest); // pixel art nitida
            }
        } catch (Exception e) {
            tex = null;
        }
        cache.put(id, tex);
        return tex;
    }

    public void dispose() {
        for (Texture t : resourceIcons.values())
            if (t != null)
                t.dispose();
        for (Texture t : buildingIcons.values())
            if (t != null)
                t.dispose();
        for (Texture t : factionIcons.values())
            if (t != null)
                t.dispose();
        factionIcons.clear();
        resourceIcons.clear();
        buildingIcons.clear();
    }
}