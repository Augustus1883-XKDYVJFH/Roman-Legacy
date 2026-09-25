package com.RomanLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gestisce le 3 fazioni attive della partita.
 */
public class FactionManager {

    // ── Relazione con una fazione ───────────────────────────────────────────────
    public static class FactionState {
        public final Faction faction;
        public boolean arrived = false;
        public int relation = 0;

        public FactionState(Faction faction) {
            this.faction = faction;
        }
    }

    private final FactionState[] slots = new FactionState[3];
    private int arrivedCount = 0;
    private final long seed;

    public FactionManager(long seed) {
        this.seed = seed;
        Faction[] chosen = selectFactions(seed);
        for (int i = 0; i < 3; i++) {
            slots[i] = new FactionState(chosen[i]);
        }
        arriveNext();
        System.out.println("[FactionManager] Initialized with seed " + seed +
                ", factions: " + chosen[0].label + ", " + chosen[1].label + ", " + chosen[2].label);
    }

    /**
     * Restituisce il seed usato per selezionare le fazioni — necessario per il
     * salvataggio.
     */
    public long getSeed() {
        return seed;
    }

    public void onFirstVilla() {
        if (arrivedCount == 1) {
            arriveNext();
            System.out
                    .println("[FactionManager] First Villa built - second faction arrived: " + slots[1].faction.label);
        }
    }

    public void onNewWorldUnlocked() {
        if (arrivedCount == 2) {
            arriveNext();
            System.out
                    .println("[FactionManager] New World unlocked - third faction arrived: " + slots[2].faction.label);
        }
    }

    public List<FactionState> getArrivedFactions() {
        List<FactionState> list = new ArrayList<>();
        for (FactionState fs : slots) {
            if (fs.arrived)
                list.add(fs);
        }
        return list;
    }

    public FactionState[] getAllSlots() {
        return slots;
    }

    public void changeRelation(Faction f, int delta) {
        for (FactionState fs : slots) {
            if (fs.faction == f) {
                int oldRelation = fs.relation;
                fs.relation = Math.max(-100, Math.min(100, fs.relation + delta));
                if (oldRelation != fs.relation) {
                    System.out.println(
                            "[FactionManager] " + f.label + " relation: " + oldRelation + " -> " + fs.relation);
                }
                return;
            }
        }
        System.err.println("[FactionManager] Cannot change relation for unknown faction: " + f.label);
    }

    public int getRelation(Faction f) {
        for (FactionState fs : slots) {
            if (fs.faction == f)
                return fs.relation;
        }
        return 0;
    }

    public boolean isArrived(Faction f) {
        for (FactionState fs : slots) {
            if (fs.faction == f)
                return fs.arrived;
        }
        return false;
    }

    private static Faction[] selectFactions(long seed) {
        Faction[] all = Faction.values();
        int n = all.length;
        int bestScore = -1;
        List<int[]> bestCombos = new ArrayList<>();

        for (int i = 0; i < n - 2; i++) {
            for (int j = i + 1; j < n - 1; j++) {
                for (int k = j + 1; k < n; k++) {
                    int score = all[i].distance(all[j]) + all[j].distance(all[k]) + all[i].distance(all[k]);
                    if (score > bestScore) {
                        bestScore = score;
                        bestCombos.clear();
                        bestCombos.add(new int[] { i, j, k });
                    } else if (score == bestScore) {
                        bestCombos.add(new int[] { i, j, k });
                    }
                }
            }
        }

        if (bestCombos.isEmpty()) {
            System.err.println("[FactionManager] No faction combinations found! Using fallback.");
            return new Faction[] { Faction.SENATORS, Faction.LEGIONARIES, Faction.THE_BLUES };
        }

        Random rng = new Random(seed);
        int[] chosen = bestCombos.get(rng.nextInt(bestCombos.size()));
        Faction[] result = { all[chosen[0]], all[chosen[1]], all[chosen[2]] };

        for (int i = 2; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            Faction tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }

        System.out.println("[FactionManager] Selected factions with score " + bestScore);
        return result;
    }

    private void arriveNext() {
        if (arrivedCount < slots.length) {
            slots[arrivedCount].arrived = true;
            arrivedCount++;
            System.out.println("[FactionManager] Faction arrived: " + slots[arrivedCount - 1].faction.label +
                    " (" + arrivedCount + "/3)");
        }
    }
}