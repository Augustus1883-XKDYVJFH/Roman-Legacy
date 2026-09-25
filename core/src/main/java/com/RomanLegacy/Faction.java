package com.RomanLegacy;

/**
 * Le 8 fazioni del gioco.
 * Ogni fazione ha 3 keyword ideologiche tra: T, E, O, L, H, U
 * (Tradizione, Evoluzione, Ordine, Libertà, Onore, Unione)
 */
public enum Faction {

    SENATORS("Senators", "#e4a13c", Keyword.T, Keyword.L, Keyword.H),
    LEGIONARIES("Legionaries", "#811709", Keyword.T, Keyword.O, Keyword.H),
    THE_BLUES("The blues", "#58b6e2", Keyword.T, Keyword.O, Keyword.U),
    OLDBLOODS("Oldbloods", "#ffffff", Keyword.T, Keyword.L, Keyword.U),
    ACADEMICS("Academics", "#d6659a", Keyword.E, Keyword.L, Keyword.H),
    FUTURISTS("Futurists", "#404699", Keyword.E, Keyword.O, Keyword.U),
    INDUSTRIALISTS("Industrialists", "#684726", Keyword.E, Keyword.O, Keyword.H),
    EXPLORERS("Explorers", "#3eb963", Keyword.E, Keyword.L, Keyword.U);

    // ── Keyword ideologiche ─────────────────────────────────────────────────────
    public enum Keyword {
        T("Tradition"),
        E("Evolution"),
        O("Order"),
        L("Liberty"),
        H("Honor"),
        U("Union");

        public final String label;

        Keyword(String label) {
            this.label = label;
        }
    }

    public final String label;
    public final String colorHex;
    public final Keyword k1, k2, k3;

    Faction(String label, String colorHex, Keyword k1, Keyword k2, Keyword k3) {
        this.label = label;
        this.colorHex = colorHex;
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
    }

    /** Keyword comuni con un'altra fazione (0–3). */
    public int sharedKeywords(Faction other) {
        int count = 0;
        for (Keyword k : keywords()) {
            for (Keyword ok : other.keywords()) {
                if (k == ok) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    /** Distanza ideologica con un'altra fazione (keyword non condivise, 0–6). */
    public int distance(Faction other) {
        // Ogni fazione ha 3 keyword, quelle non condivise sono 3 - shared per lato = 6
        // - 2*shared
        return 6 - 2 * sharedKeywords(other);
    }

    /** Array delle 3 keyword di questa fazione. */
    public Keyword[] keywords() {
        return new Keyword[] { k1, k2, k3 };
    }
}