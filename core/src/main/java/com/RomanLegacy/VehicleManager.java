package com.RomanLegacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.IntConsumer;

/**
 * Genera e muove i veicoli estetici sulle strade. Non tocca risorse né
 * simulazione.
 */
public class VehicleManager {

    public static final int MAX_VEHICLES = 200;
    private static final int MAX_PATH_LEN = 100;
    private static final int MAX_CONSUMER_DIST = 60; // celle, distanza Manhattan
    private static final float SPAWN_MIN = 0.5f; // secondi di gioco tra un tentativo e l'altro
    private static final float SPAWN_MAX = 2.5f;

    private static final int W = GameState.MAP_WIDTH;
    private static final int H = GameState.MAP_HEIGHT;

    public final List<Vehicle> vehicles = new ArrayList<>();

    private final Random rng = new Random();
    private float spawnTimer = 1f;

    private final boolean[][] roadGrid = new boolean[H][W];
    private boolean roadsDirty = true;

    // Strutture della ricerca, riusate a ogni percorso (lo "stamp" evita di
    // azzerarle)
    private final int[] prev = new int[W * H];
    private final int[] visitStamp = new int[W * H];
    private final int[] targetStamp = new int[W * H];
    private final int[] queue = new int[W * H];
    private int qTail = 0;
    private int stamp = 0;

    public void markRoadsDirty() {
        roadsDirty = true;
    }

    public void clear() {
        vehicles.clear();
        roadsDirty = true;
    }

    /**
     * delta = tempo di gioco (già scalato dalla velocità); left/right/bottom/top =
     * area visibile in unità mondo.
     */
    public void tick(GameState state, float delta, float left, float right, float bottom, float top) {
        if (roadsDirty)
            rebuildRoadGrid(state);

        Iterator<Vehicle> it = vehicles.iterator();
        while (it.hasNext()) {
            if (!it.next().advance(delta, roadGrid))
                it.remove();
        }

        spawnTimer -= delta;
        if (spawnTimer > 0f)
            return;
        spawnTimer = SPAWN_MIN + rng.nextFloat() * (SPAWN_MAX - SPAWN_MIN);
        if (vehicles.size() >= MAX_VEHICLES)
            return;
        trySpawn(state, left, right, bottom, top);
    }

    private void rebuildRoadGrid(GameState state) {
        for (boolean[] row : roadGrid)
            Arrays.fill(row, false);
        for (BuildingInstance b : state.buildings) {
            if (b.type == BuildingType.ROAD && b.rootX >= 0 && b.rootY >= 0 && b.rootX < W && b.rootY < H)
                roadGrid[b.rootY][b.rootX] = true;
        }
        roadsDirty = false;
    }

    private void trySpawn(GameState state, float left, float right, float bottom, float top) {
        List<BuildingInstance> list = state.buildings;
        int n = list.size();
        if (n == 0)
            return;
        int cs = GameState.CELL_SIZE;
        float margin = 10f * cs;

        // Origine: un edificio che produce, collegato e visibile
        BuildingInstance origin = null;
        for (int attempt = 0; attempt < 25 && origin == null; attempt++) {
            BuildingInstance b = list.get(rng.nextInt(n));
            if (b.type.prod == null || b.type.consumableOnly || b.paused || !b.connected)
                continue;
            float cx = (b.rootX + b.effectiveW() / 2f) * cs;
            float cy = (b.rootY + b.effectiveH() / 2f) * cs;
            if (cx < left - margin || cx > right + margin || cy < bottom - margin || cy > top + margin)
                continue;
            origin = b;
        }
        if (origin == null)
            return;

        // Destinazione: chi usa quel prodotto (ingrediente o bisogno di una casa),
        // altrimenti i porti
        String prod = origin.type.prod;
        List<BuildingInstance> consumers = new ArrayList<>();
        List<BuildingInstance> docks = new ArrayList<>();
        for (BuildingInstance c : list) {
            if (c == origin)
                continue;
            if (c.type == BuildingType.DOCK) {
                docks.add(c);
                continue;
            }
            if (!c.connected)
                continue;
            boolean uses = (c.type.input != null && c.type.input.containsKey(prod))
                    || (c.type.isHouse && contains(c.type.houseNeeds, prod));
            int dist = Math.abs(c.rootX - origin.rootX) + Math.abs(c.rootY - origin.rootY);
            if (uses && dist <= MAX_CONSUMER_DIST)
                consumers.add(c);
        }

        List<BuildingInstance> targets;
        if (!consumers.isEmpty())
            targets = Collections.singletonList(consumers.get(rng.nextInt(consumers.size())));
        else if (!docks.isEmpty())
            targets = docks; // il più vicino lo trova la ricerca
        else
            return;

        int[][] path = findPath(origin, targets);
        if (path == null || path[0].length < 3)
            return; // nessun percorso o viaggio troppo breve per valere la pena

        Vehicle.Kind kind = origin.hasTruckLogistics ? Vehicle.Kind.TRUCK
                : (origin.hasCarriageLogistics ? Vehicle.Kind.STEAM_CARRIAGE : Vehicle.Kind.CART);
        float baseSpeed = kind == Vehicle.Kind.TRUCK ? 4.5f : (kind == Vehicle.Kind.STEAM_CARRIAGE ? 3f : 2f);
        float speed = baseSpeed * (0.9f + rng.nextFloat() * 0.2f);
        vehicles.add(new Vehicle(kind, path[0], path[1], speed));
    }

    private static boolean contains(String[] arr, String value) {
        if (arr == null)
            return false;
        for (String s : arr)
            if (s.equals(value))
                return true;
        return false;
    }

    /**
     * BFS sulle celle strada: dalle adiacenti all'origine a quelle adiacenti a un
     * bersaglio qualsiasi.
     */
    private int[][] findPath(BuildingInstance origin, List<BuildingInstance> targets) {
        stamp++;

        int targetCells = 0;
        for (BuildingInstance t : targets)
            targetCells += forAdjacentRoad(t, idx -> targetStamp[idx] = stamp);
        if (targetCells == 0)
            return null;

        qTail = 0;
        int sources = forAdjacentRoad(origin, idx -> {
            if (visitStamp[idx] != stamp) {
                visitStamp[idx] = stamp;
                prev[idx] = -1;
                queue[qTail++] = idx;
            }
        });
        if (sources == 0)
            return null;

        int[] ddx = { 0, 1, 0, -1 };
        int[] ddy = { 1, 0, -1, 0 };
        int head = 0;
        int found = -1;
        while (head < qTail) {
            int cur = queue[head++];
            if (targetStamp[cur] == stamp) {
                found = cur;
                break;
            }
            int cx = cur % W;
            int cy = cur / W;
            for (int d = 0; d < 4; d++) {
                int nx = cx + ddx[d];
                int ny = cy + ddy[d];
                if (nx < 0 || ny < 0 || nx >= W || ny >= H || !roadGrid[ny][nx])
                    continue;
                int ni = ny * W + nx;
                if (visitStamp[ni] == stamp)
                    continue;
                visitStamp[ni] = stamp;
                prev[ni] = cur;
                queue[qTail++] = ni;
            }
        }
        if (found < 0)
            return null;

        int len = 0;
        for (int c = found; c != -1; c = prev[c])
            len++;
        if (len > MAX_PATH_LEN)
            return null;

        int[] px = new int[len];
        int[] py = new int[len];
        int i = len - 1;
        for (int c = found; c != -1; c = prev[c]) {
            px[i] = c % W;
            py[i] = c / W;
            i--;
        }
        return new int[][] { px, py };
    }

    /**
     * Chiama action per ogni cella strada adiacente (sui 4 lati) all'edificio;
     * ritorna quante ne ha trovate.
     */
    private int forAdjacentRoad(BuildingInstance b, IntConsumer action) {
        int x0 = b.rootX;
        int y0 = b.rootY;
        int w = b.effectiveW();
        int h = b.effectiveH();
        int count = 0;
        for (int x = x0; x < x0 + w; x++) {
            count += visitRoad(x, y0 - 1, action);
            count += visitRoad(x, y0 + h, action);
        }
        for (int y = y0; y < y0 + h; y++) {
            count += visitRoad(x0 - 1, y, action);
            count += visitRoad(x0 + w, y, action);
        }
        return count;
    }

    private int visitRoad(int x, int y, IntConsumer action) {
        if (x < 0 || y < 0 || x >= W || y >= H || !roadGrid[y][x])
            return 0;
        action.accept(y * W + x);
        return 1;
    }
}