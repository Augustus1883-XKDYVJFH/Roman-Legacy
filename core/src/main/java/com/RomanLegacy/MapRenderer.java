package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class MapRenderer {

    private static final Color[] TERRAIN_COLORS = {
            Color.valueOf("7ba7c4"), // WATER
            Color.valueOf("fdf063"), // SHORE
            Color.valueOf("B5F748"), // PLAIN
            Color.valueOf("6a8f55"), // FOREST
            Color.valueOf("9a8e6a"), // HILL
            Color.valueOf("A58E8C"), // MOUNTAIN
            Color.valueOf("808080"), // PEAK
    };

    private static final float[][] TERRAIN_COLORS_NEW = {
            { 0.29f, 0.56f, 0.77f }, // WATER
            { 0.96f, 0.88f, 0.43f }, // SHORE
            { 0.49f, 0.79f, 0.37f }, // PLAIN
            { 0.18f, 0.48f, 0.23f }, // FOREST
            { 0.54f, 0.48f, 0.31f }, // HILL
            { 0.48f, 0.42f, 0.42f }, // MOUNTAIN
            { 0.56f, 0.56f, 0.56f }, // PEAK
    };

    // Per-tile brightness multiplier by terrain type, so flat plains read
    // darker than rising hills/mountains/peaks (fake elevation shading).
    private static final float[] TERRAIN_SHADE = {
            1.00f, // WATER
            1.00f, // SHORE
            0.96f, // PLAIN
            0.94f, // FOREST
            1.02f, // HILL
            1.06f, // MOUNTAIN
            1.12f, // PEAK
    };

    // Amplitude of the deterministic per-tile color jitter (retro textured look).
    private static final float TILE_JITTER = 0.045f;
    // Below this zoom the map is close enough to show tile borders.
    private static final float GRID_ZOOM_THRESHOLD = 0.9f;

    private static final Color COL_CANPLACE = new Color(0, 1, 0, 0.35f);
    private static final Color COL_CANTPLACE = new Color(1, 0, 0, 0.35f);
    private static final Color COL_CANPLACE_CONNECTED = new Color(0, 1, 0, 0.45f);
    private static final Color COL_CANPLACE_NO_CONN = new Color(1, 1, 0, 0.45f);
    private final ShapeRenderer sr;
    private final OrthographicCamera mapCamera;

    public MapRenderer(ShapeRenderer sr, OrthographicCamera mapCamera) {
        this.sr = sr;
        this.mapCamera = mapCamera;
    }

    public void drawMap(GameState state) {
        int cs = GameState.CELL_SIZE;
        float cameraX = mapCamera.position.x;
        float cameraY = mapCamera.position.y;

        float left = cameraX - mapCamera.viewportWidth / 2f * mapCamera.zoom;
        float right = cameraX + mapCamera.viewportWidth / 2f * mapCamera.zoom;
        float bottom = cameraY - mapCamera.viewportHeight / 2f * mapCamera.zoom;
        float top = cameraY + mapCamera.viewportHeight / 2f * mapCamera.zoom;

        int startX = Math.max(0, (int) (left / cs) - 1);
        int endX = Math.min(GameState.MAP_WIDTH, (int) (right / cs) + 2);
        int startY = Math.max(0, (int) (bottom / cs) - 1);
        int endY = Math.min(GameState.MAP_HEIGHT, (int) (top / cs) + 2);

        sr.setProjectionMatrix(mapCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int t = state.terrainMap[y][x];
                // Controllo bounds per sicurezza
                if (t < 0 || t >= TERRAIN_COLORS.length) {
                    t = MapGenerator.PLAIN;
                }

                float r, g, b;
                if (state.onNewWorld) {
                    float[] nc = TERRAIN_COLORS_NEW[t];
                    r = nc[0];
                    g = nc[1];
                    b = nc[2];
                } else {
                    Color c = TERRAIN_COLORS[t];
                    r = c.r;
                    g = c.g;
                    b = c.b;
                }

                // Elevation shading + deterministic per-tile jitter.
                float shade = TERRAIN_SHADE[t] * (1f + (tileNoise(x, y) - 0.5f) * 2f * TILE_JITTER);
                sr.setColor(clamp01(r * shade), clamp01(g * shade), clamp01(b * shade), 1f);
                sr.rect(x * cs, y * cs, cs, cs);
            }
        }
        sr.end();

        // Sottile griglia tra le celle quando si è abbastanza vicini (look retro).
        if (mapCamera.zoom < GRID_ZOOM_THRESHOLD) {
            float alpha = 0.18f * (1f - mapCamera.zoom / GRID_ZOOM_THRESHOLD);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(0f, 0f, 0f, alpha);
            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    sr.rect(x * cs, y * cs, cs, cs);
                }
            }
            sr.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    /**
     * Rumore deterministico [0,1) per cella — stabile tra i frame, niente flicker.
     */
    private static float tileNoise(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        h = h ^ (h >>> 16);
        return (h & 0x7fffffff) / (float) 0x7fffffff;
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private final Color tmpCol = new Color();

    public void drawBuildings(GameState state, GameInputHandler input) {
        int cs = GameState.CELL_SIZE;

        sr.setProjectionMatrix(mapCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (BuildingInstance bi : state.buildings) {
            Color base = tmpCol.set(Color.valueOf(bi.type.colorHex));
            float r = base.r, g = base.g, b = base.b;
            for (int dy = 0; dy < bi.effectiveH(); dy++) {
                for (int dx = 0; dx < bi.effectiveW(); dx++) {
                    int gx = bi.rootX + dx, gy = bi.rootY + dy;
                    float shade = 1f + (tileNoise(gx, gy) - 0.5f) * 2f * TILE_JITTER;
                    sr.setColor(clamp01(r * shade), clamp01(g * shade), clamp01(b * shade), 1f);
                    sr.rect(gx * cs, gy * cs, cs, cs);
                }
            }
        }
        sr.end();

        // Contorno nero sottile standard
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0, 0, 0, 0.6f);
        for (BuildingInstance bi : state.buildings) {
            sr.rect(bi.rootX * cs, bi.rootY * cs, bi.effectiveW() * cs, bi.effectiveH() * cs);
        }
        sr.end();

        // Bordo rosso spesso: edifici scollegati, sempre visibile
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(1f, 0.2f, 0.2f, 0.9f);
        for (BuildingInstance bi : state.buildings) {
            if (bi.type.requiredConnection != null
                    && bi.type.requiredConnection != BuildingType.ConnectionType.NONE
                    && !bi.connected) {
                drawThickOutline(bi.rootX * cs, bi.rootY * cs, bi.effectiveW() * cs, bi.effectiveH() * cs, 3f);
            }
        }
        sr.end();

        // Bordo blu spesso: relazione tra edificio ispezionato e la sua influenza
        if (input != null && input.showBuildingPanel && input.inspectedBuilding != null) {
            BuildingInstance source = input.inspectedBuilding;

            if (source.type.influenceRadius > 0) {
                drawInfluenceRadiusForBuilding(source);
                int centerX = source.rootX + source.effectiveW() / 2;
                int centerY = source.rootY + source.effectiveH() / 2;
                int radiusSq = source.type.influenceRadius * source.type.influenceRadius;

                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(0.3f, 0.6f, 1f, 0.9f);
                for (BuildingInstance bi : state.buildings) {
                    if (bi == source)
                        continue;
                    if (!influenceApplies(source.type, bi.type))
                        continue;
                    int tcx = bi.rootX + bi.effectiveW() / 2;
                    int tcy = bi.rootY + bi.effectiveH() / 2;
                    int dx = tcx - centerX, dy = tcy - centerY;
                    if (dx * dx + dy * dy <= radiusSq) {
                        drawThickOutline(bi.rootX * cs, bi.rootY * cs, bi.effectiveW() * cs, bi.effectiveH() * cs, 3f);
                    }
                }
                sr.end();
            } else if (source.type.isHouse) {
                // Caso 2: si ispeziona una casa → evidenzia le sorgenti che la coprono
                int centerX = source.rootX + source.effectiveW() / 2;
                int centerY = source.rootY + source.effectiveH() / 2;

                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(0.3f, 0.6f, 1f, 0.9f);
                for (BuildingInstance bi : state.buildings) {
                    if (bi == source)
                        continue;
                    if (bi.type.influenceRadius <= 0)
                        continue;
                    if (!influenceApplies(bi.type, source.type))
                        continue;
                    int scx = bi.rootX + bi.effectiveW() / 2;
                    int scy = bi.rootY + bi.effectiveH() / 2;
                    int dx = scx - centerX, dy = scy - centerY;
                    int radiusSq = bi.type.influenceRadius * bi.type.influenceRadius;
                    if (dx * dx + dy * dy <= radiusSq) {
                        drawThickOutline(bi.rootX * cs, bi.rootY * cs, bi.effectiveW() * cs, bi.effectiveH() * cs, 3f);
                    }
                }
                sr.end();
            }
        }
    }

    /** True se targetType riceve davvero un beneficio dal raggio di sourceType. */
    private boolean influenceApplies(BuildingType sourceType, BuildingType targetType) {
        if (sourceType == BuildingType.BOILER) {
            return targetType.requiredPower == BuildingType.PowerType.STEAM
                    || targetType.requiredPower == BuildingType.PowerType.BOTH;
        }
        if (sourceType == BuildingType.POWERPLANT) {
            return targetType.requiredPower == BuildingType.PowerType.ELECTRICITY
                    || targetType.requiredPower == BuildingType.PowerType.BOTH;
        }
        if (sourceType == BuildingType.WAREHOUSE_CARRIAGE || sourceType == BuildingType.WAREHOUSE_TRUCK) {
            return targetType.prod != null;
        }
        if (sourceType.consumptionServiceCategory != null) {
            return targetType.isHouse;
        }
        return false;
    }

    /**
     * Disegna i floater "+N risorsa" (stile Anno 1800): icona + testo che sale
     * e sfuma, ancorati in coordinate mondo così seguono lo zoom/pan mappa.
     * batch va passato dal chiamante (GameScreen) — MapRenderer non ne possiede
     * uno proprio.
     */
    public void drawFloaters(GameState state, Main game, SpriteBatch batch) {
        if (state.floaters.isEmpty())
            return;

        GlyphLayout gl = new GlyphLayout();
        float iconSize = 20f;
        float gap = 4f;
        float padX = 6f;
        float padY = 4f;

        java.util.List<float[]> boxes = new java.util.ArrayList<>();

        for (ResourceFloater f : state.floaters) {
            float alpha = f.alpha();
            if (alpha <= 0f)
                continue;

            String text = "+" + f.amount;
            Texture icon = game.icons != null ? game.icons.getResourceIcon(f.resourceId) : null;

            gl.setText(game.fonts.normal, text);
            float totalW = (icon != null ? iconSize + gap : 0f) + gl.width;
            float startX = f.worldX - totalW / 2f;

            float boxX = startX - padX;
            float boxY = f.worldY - Math.max(iconSize, gl.height) / 2f - padY;
            float boxW = totalW + padX * 2f;
            float boxH = Math.max(iconSize, gl.height) + padY * 2f;

            boxes.add(new float[] { boxX, boxY, boxW, boxH, alpha });
        }

        if (boxes.isEmpty())
            return;

        sr.setProjectionMatrix(mapCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (float[] bx : boxes) {
            sr.setColor(0f, 0f, 0f, 0.45f * bx[4]);
            sr.rect(bx[0], bx[1], bx[2], bx[3]);
        }
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(mapCamera.combined);
        batch.begin();

        for (ResourceFloater f : state.floaters) {
            float alpha = f.alpha();
            if (alpha <= 0f)
                continue;

            ResourceManager.Resource res = state.resources.getAll().get(f.resourceId);
            Color textColor = res != null
                    ? new Color(res.color[0], res.color[1], res.color[2], alpha)
                    : new Color(1f, 1f, 1f, alpha);

            String text = "+" + f.amount;
            Texture icon = game.icons != null ? game.icons.getResourceIcon(f.resourceId) : null;

            gl.setText(game.fonts.normal, text);
            float totalW = (icon != null ? iconSize + gap : 0f) + gl.width;
            float startX = f.worldX - totalW / 2f;

            if (icon != null) {
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(icon, startX, f.worldY - iconSize / 2f, iconSize, iconSize);
            }

            game.fonts.normal.setColor(textColor);
            game.fonts.normal.draw(batch, text,
                    startX + (icon != null ? iconSize + gap : 0f),
                    f.worldY + gl.height / 2f);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    /**
     * Cornice piena (4 rettangoli), più affidabile di ShapeRenderer.Line su alcuni
     * driver.
     */
    private void drawThickOutline(float x, float y, float w, float h, float thickness) {
        sr.rect(x, y + h - thickness, w, thickness);
        sr.rect(x, y, w, thickness);
        sr.rect(x, y, thickness, h);
        sr.rect(x + w - thickness, y, thickness, h);
    }

    public void drawBuildingPreview(GameInputHandler input, GameState state) {
        int cx = input.previewCellX, cy = input.previewCellY;
        if (cx < 0 || cy < 0)
            return;
        int cs = GameState.CELL_SIZE;
        sr.setProjectionMatrix(mapCamera.combined);

        if (input.selectedBuilding == null)
            return;

        if (input.selectedBuilding == BuildingType.ROAD && input.roadDragging) {
            drawLineCells(input.getRoadLine(input.roadStartX, input.roadStartY, cx, cy), input);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(Color.WHITE);
            sr.rect(input.roadStartX * cs, input.roadStartY * cs, cs, cs);
            sr.end();
            return;
        }

        // PIPE dragging
        if (input.selectedBuilding == BuildingType.PIPELINE && input.pipeDragging) {
            drawLineCells(input.getRoadLine(input.pipeStartX, input.pipeStartY, cx, cy), input);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(Color.WHITE);
            sr.rect(input.pipeStartX * cs, input.pipeStartY * cs, cs, cs);
            sr.end();
            return;
        }

        // Campo (drag ad area)
        if (input.selectedBuilding.isField() && input.fieldDragging) {
            drawFieldAreaPreview(input, cx, cy);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(Color.WHITE);
            sr.rect(input.fieldStartX * cs, input.fieldStartY * cs, cs, cs);
            sr.end();
            return;
        }

        // ROAD/PIPE singole
        if (input.selectedBuilding == BuildingType.ROAD || input.selectedBuilding == BuildingType.PIPELINE) {
            boolean ok = input.canPlace(input.selectedBuilding, cx, cy);
            Color col = ok ? COL_CANPLACE : COL_CANTPLACE;
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(col);
            sr.rect(cx * cs, cy * cs, cs, cs);
            sr.end();
            return;
        }

        // Raggio di influenza
        if (input.selectedBuilding.influenceRadius > 0) {
            drawInfluenceRadiusPreview(input, cx, cy, input.selectedBuilding);
            drawInfluencePreviewHighlights(state, input, cx, cy, input.selectedBuilding);
        }

        Color col;
        boolean canPlace = input.canPlace(input.selectedBuilding, cx, cy);
        if (!canPlace) {
            col = COL_CANTPLACE;
        } else if (input.selectedBuilding.requiredConnection != BuildingType.ConnectionType.NONE) {
            boolean connected = input.simulateConnectionAfterPlace(input.selectedBuilding, cx, cy);
            col = connected ? COL_CANPLACE_CONNECTED : COL_CANPLACE_NO_CONN;
        } else {
            col = COL_CANPLACE_CONNECTED;
        }

        int previewW = input.buildingRotated ? input.selectedBuilding.h : input.selectedBuilding.w;
        int previewH = input.buildingRotated ? input.selectedBuilding.w : input.selectedBuilding.h;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(col);
        sr.rect(cx * cs, cy * cs, previewW * cs, previewH * cs);
        sr.end();
    }

    public void drawInfluenceRadiusPreview(GameInputHandler input, int cx, int cy, BuildingType bt) {
        if (bt.influenceRadius <= 0)
            return;

        int cs = GameState.CELL_SIZE;
        int radius = bt.influenceRadius;

        int w = input.buildingRotated ? bt.h : bt.w;
        int h = input.buildingRotated ? bt.w : bt.h;
        int centerX = cx + w / 2;
        int centerY = cy + h / 2;

        float worldCenterX = (centerX + 0.5f) * cs;
        float worldCenterY = (centerY + 0.5f) * cs;
        float worldRadius = radius * cs;

        sr.setProjectionMatrix(mapCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);

        // Colore diverso per tipo di servizio
        Color color;
        if (bt == BuildingType.BOILER) {
            color = new Color(0.6f, 0.6f, 0.7f, 0.6f);
        } else if (bt == BuildingType.POWERPLANT) {
            color = new Color(0.9f, 0.9f, 0.3f, 0.6f);
        } else if (bt.consumptionServiceCategory != null) {
            // Colori per i servizi a consumo
            switch (bt.consumptionServiceCategory) {
                case HEALTH:
                    color = new Color(0.8f, 0.2f, 0.2f, 0.5f); // Rosso
                    break;
                case RELIGION:
                    color = new Color(0.8f, 0.7f, 0.1f, 0.5f); // Oro
                    break;
                case ENTERTAINMENT:
                    color = new Color(0.8f, 0.2f, 0.8f, 0.5f); // Viola
                    break;
                case COMMUNICATION:
                    color = new Color(0.2f, 0.6f, 0.8f, 0.5f); // Azzurro
                    break;
                default:
                    color = new Color(0.5f, 0.5f, 0.8f, 0.4f);
            }
        } else {
            color = new Color(0.5f, 0.5f, 0.8f, 0.4f);
        }

        sr.setColor(color);

        // Disegna cerchio
        int segments = 64;
        float angleStep = 360f / segments;
        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;
            float x1 = worldCenterX + (float) Math.cos(Math.toRadians(angle1)) * worldRadius;
            float y1 = worldCenterY + (float) Math.sin(Math.toRadians(angle1)) * worldRadius;
            float x2 = worldCenterX + (float) Math.cos(Math.toRadians(angle2)) * worldRadius;
            float y2 = worldCenterY + (float) Math.sin(Math.toRadians(angle2)) * worldRadius;
            sr.line(x1, y1, x2, y2);
        }

        sr.end();
    }

    /**
     * Evidenzia in blu, durante l'anteprima di piazzamento:
     * - gli edifici esistenti che ricadrebbero nel raggio dell'edificio in
     * costruzione (se questo ha un raggio proprio);
     * - l'edificio esistente a raggio che coprirebbe l'edificio in costruzione
     * (se questo può beneficiarne: vapore/elettricità/logistica/servizi).
     */
    private void drawInfluencePreviewHighlights(GameState state, GameInputHandler input, int cx, int cy,
            BuildingType bt) {
        int w = input.buildingRotated ? bt.h : bt.w;
        int h = input.buildingRotated ? bt.w : bt.h;
        int centerX = cx + w / 2;
        int centerY = cy + h / 2;
        int cs = GameState.CELL_SIZE;

        sr.setProjectionMatrix(mapCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.3f, 0.6f, 1f, 0.9f);

        // Caso 1: l'edificio in costruzione ha un raggio proprio.
        if (bt.influenceRadius > 0) {
            int radiusSq = bt.influenceRadius * bt.influenceRadius;
            for (BuildingInstance target : state.buildings) {
                if (!influenceApplies(bt, target.type))
                    continue;
                int tcx = target.rootX + target.effectiveW() / 2;
                int tcy = target.rootY + target.effectiveH() / 2;
                int dx = tcx - centerX, dy = tcy - centerY;
                if (dx * dx + dy * dy <= radiusSq) {
                    drawThickOutline(target.rootX * cs, target.rootY * cs,
                            target.effectiveW() * cs, target.effectiveH() * cs, 3f);
                }
            }
        }
        // FINE Caso 1 — qui la graffa si chiude, il Caso 2 sotto è FUORI da questo if

        // Caso 2: l'edificio in costruzione può beneficiare di un raggio esistente.
        // Deve girare SEMPRE, indipendentemente da bt.influenceRadius.
        for (BuildingInstance source : state.buildings) {
            BuildingType st = source.type;
            if (st.influenceRadius <= 0)
                continue;
            if (!influenceApplies(st, bt))
                continue;

            int scx = source.rootX + source.effectiveW() / 2;
            int scy = source.rootY + source.effectiveH() / 2;
            int dx = scx - centerX, dy = scy - centerY;
            int radiusSq = st.influenceRadius * st.influenceRadius;
            if (dx * dx + dy * dy <= radiusSq) {
                drawThickOutline(source.rootX * cs, source.rootY * cs,
                        source.effectiveW() * cs, source.effectiveH() * cs, 3f);
            }
        }

        sr.end();
    }

    private void drawBuildingOutline(BuildingInstance b) {
        int cs = GameState.CELL_SIZE;
        sr.rect(b.rootX * cs, b.rootY * cs, b.effectiveW() * cs, b.effectiveH() * cs);
    }

    public void drawInfluenceRadiusForBuilding(BuildingInstance b) {
        if (b.type.influenceRadius <= 0)
            return;
        int cs = GameState.CELL_SIZE;
        int radius = b.type.influenceRadius;
        int centerX = b.rootX + b.effectiveW() / 2;
        int centerY = b.rootY + b.effectiveH() / 2;
        float worldCenterX = (centerX + 0.5f) * cs;
        float worldCenterY = (centerY + 0.5f) * cs;
        float worldRadius = radius * cs;

        sr.setProjectionMatrix(mapCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(colorForInfluenceSource(b.type));

        int segments = 64;
        float angleStep = 360f / segments;
        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;
            float x1 = worldCenterX + (float) Math.cos(Math.toRadians(angle1)) * worldRadius;
            float y1 = worldCenterY + (float) Math.sin(Math.toRadians(angle1)) * worldRadius;
            float x2 = worldCenterX + (float) Math.cos(Math.toRadians(angle2)) * worldRadius;
            float y2 = worldCenterY + (float) Math.sin(Math.toRadians(angle2)) * worldRadius;
            sr.line(x1, y1, x2, y2);
        }
        sr.end();
    }

    private void drawFieldAreaPreview(GameInputHandler input, int cx, int cy) {
        int minX = Math.min(input.fieldStartX, cx), maxX = Math.max(input.fieldStartX, cx);
        int minY = Math.min(input.fieldStartY, cy), maxY = Math.max(input.fieldStartY, cy);
        int cs = GameState.CELL_SIZE;

        sr.setProjectionMatrix(mapCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                boolean ok = input.canPlace(input.selectedBuilding, x, y);
                sr.setColor(ok ? COL_CANPLACE : COL_CANTPLACE);
                sr.rect(x * cs, y * cs, cs, cs);
            }
        }
        sr.end();
    }

    /**
     * Contorno colorato sui depositi del mondo corrente: doppio bordo, colore
     * dell'edificio che li usa.
     */
    public void drawDeposits(GameState state) {
        java.util.Map<String, java.util.List<int[]>> deposits = state.onNewWorld ? state.depositsNew
                : state.depositsOld;
        if (deposits.isEmpty())
            return;
        int cs = GameState.CELL_SIZE;

        sr.setProjectionMatrix(mapCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        for (java.util.Map.Entry<String, java.util.List<int[]>> e : deposits.entrySet()) {
            BuildingType bt = BuildingType.valueOf(e.getKey());
            Color c = Color.valueOf(bt.colorHex);
            sr.setColor(c.r, c.g, c.b, 0.9f);
            for (int[] s : e.getValue()) {
                sr.rect(s[0] * cs, s[1] * cs, bt.w * cs, bt.h * cs);
                sr.rect(s[0] * cs + 3, s[1] * cs + 3, bt.w * cs - 6, bt.h * cs - 6);
            }
        }
        sr.end();
    }

    private Color colorForInfluenceSource(BuildingType bt) {
        if (bt == BuildingType.BOILER)
            return new Color(0.6f, 0.6f, 0.7f, 0.6f);
        if (bt == BuildingType.POWERPLANT)
            return new Color(0.9f, 0.9f, 0.3f, 0.6f);
        if (bt.consumptionServiceCategory != null) {
            switch (bt.consumptionServiceCategory) {
                case HEALTH:
                    return new Color(0.8f, 0.2f, 0.2f, 0.5f);
                case RELIGION:
                    return new Color(0.8f, 0.7f, 0.1f, 0.5f);
                case ENTERTAINMENT:
                    return new Color(0.8f, 0.2f, 0.8f, 0.5f);
                case COMMUNICATION:
                    return new Color(0.2f, 0.6f, 0.8f, 0.5f);
                default:
                    return new Color(0.5f, 0.5f, 0.8f, 0.4f);
            }
        }
        return new Color(0.5f, 0.5f, 0.8f, 0.4f);
    }

    public void drawToolPreview(GameInputHandler input, GameState state) {
        int cx = input.previewCellX, cy = input.previewCellY;
        if (cx < 0 || cy < 0 || input.selectedTool == GameInputHandler.ToolAction.NONE)
            return;

        int cs = GameState.CELL_SIZE;
        BuildingInstance target = state.getBuildingAt(cx, cy);
        boolean valid = false;

        if (input.selectedTool == GameInputHandler.ToolAction.DEMOLISH) {
            valid = target != null;
        } else if (input.selectedTool == GameInputHandler.ToolAction.UPGRADE_HOUSE) {
            valid = target != null && target.type.isHouse && target.canUpgrade(state.resources);
        }

        float x = target != null ? target.rootX * cs : cx * cs;
        float y = target != null ? target.rootY * cs : cy * cs;
        float w = target != null ? target.effectiveW() * cs : cs;
        float h = target != null ? target.effectiveH() * cs : cs;

        sr.setProjectionMatrix(mapCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        if (valid) {
            if (input.selectedTool == GameInputHandler.ToolAction.DEMOLISH)
                sr.setColor(1f, 0.15f, 0.10f, 0.42f);
            else
                sr.setColor(0.15f, 0.90f, 0.25f, 0.42f);
        } else {
            sr.setColor(COL_CANTPLACE);
        }
        sr.rect(x, y, w, h);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(valid ? Color.WHITE : Color.RED);
        sr.rect(x, y, w, h);
        sr.end();
    }

    private void drawLineCells(java.util.List<int[]> line, GameInputHandler input) {
        int cs = GameState.CELL_SIZE;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int[] cell : line) {
            int lx = cell[0], ly = cell[1];
            if (lx < 0 || ly < 0 || lx >= GameState.MAP_WIDTH || ly >= GameState.MAP_HEIGHT)
                continue;
            boolean ok = input.canPlace(input.selectedBuilding, lx, ly);
            sr.setColor(ok ? COL_CANPLACE : COL_CANTPLACE);
            sr.rect(lx * cs, ly * cs, cs, cs);
        }
        sr.end();
    }
}