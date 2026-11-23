package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

/**
 * Sprite pixel art degli edifici da assets/sprites/buildings/<ID>.png
 * (e <ID>_r.png per la versione ruotata). Se manca il file ritorna null e il
 * chiamante usa il rettangolo colorato di riserva, come fa IconManager.
 */
public class SpriteManager {

    /**
     * Pixel d'arte per cella: un edificio w x h celle misura (w*16) x (h*16) px.
     */
    public static final int ART_PX_PER_CELL = 16;
    private static final String BUILDING_PATH = "sprites/buildings/";

    private final Map<String, Texture> buildingSprites = new HashMap<>();

    public Texture getBuildingSprite(BuildingType type, boolean rotated) {
        if (type.w == type.h)
            rotated = false; // edificio quadrato: la rotazione non cambia nulla
        String key = rotated ? type.id + "_r" : type.id;

        if (!buildingSprites.containsKey(key)) {
            Texture tex = tryLoad(BUILDING_PATH + key + ".png");
            buildingSprites.put(key, tex);
            if (tex != null) {
                int ew = (rotated ? type.h : type.w) * ART_PX_PER_CELL;
                int eh = (rotated ? type.w : type.h) * ART_PX_PER_CELL;
                if (tex.getWidth() != ew || tex.getHeight() != eh) {
                    Gdx.app.error("SpriteManager", key + ".png misura " + tex.getWidth() + "x"
                            + tex.getHeight() + ", attesi " + ew + "x" + eh);
                }
            }
        }
        return buildingSprites.get(key);
    }

    private final Map<Vehicle.Kind, Texture> vehicleSprites = new HashMap<>();

    /**
     * Sprite 16x16 del veicolo, rivolto verso destra; null se manca (si usa un
     * rettangolino).
     */
    public Texture getVehicleSprite(Vehicle.Kind kind) {
        if (!vehicleSprites.containsKey(kind)) {
            String name = kind.name().toLowerCase();
            Texture tex = tryLoad("sprites/vehicles/" + name + ".png");
            vehicleSprites.put(kind, tex);
            if (tex != null && (tex.getWidth() != ART_PX_PER_CELL || tex.getHeight() != ART_PX_PER_CELL))
                Gdx.app.error("SpriteManager", name + ".png misura " + tex.getWidth() + "x"
                        + tex.getHeight() + ", attesi " + ART_PX_PER_CELL + "x" + ART_PX_PER_CELL);
        }
        return vehicleSprites.get(kind);
    }

    private Texture tryLoad(String path) {
        try {
            if (!Gdx.files.internal(path).exists())
                return null;
            Texture tex = new Texture(Gdx.files.internal(path));
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest); // pixel art nitida
            return tex;
        } catch (Exception e) {
            Gdx.app.error("SpriteManager", "Cannot load " + path, e);
            return null;
        }
    }

    public void dispose() {
        for (Texture t : buildingSprites.values())
            if (t != null)
                t.dispose();
        buildingSprites.clear();
        for (Texture t : vehicleSprites.values())
            if (t != null)
                t.dispose();
        vehicleSprites.clear();
    }
}