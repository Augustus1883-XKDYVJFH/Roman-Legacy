package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import java.util.List;

public class AchievementManager {

    // Devono combaciare ESATTAMENTE con gli API Name su Steamworks.
    // Ho tolto apostrofo, spazi e "?" dagli ID: rinominali anche sul sito partner.
    public static final String DYRBMG = "DID_YOU_REALLY_BUY_MY_GAME";
    public static final String TFB = "THE_FIRST_BRICK";
    public static final String RWBIAD = "ROME_WASNT_BUILT_IN_A_DAY";
    public static final String IR = "INDUSTRIAL_REVOLUTION";
    public static final String FL = "FIAT_LUX";
    public static final String NM = "NOVUS_MUNDUS";
    public static final String BG = "BLACK_GOLD";
    public static final String CYHM = "CAN_YOU_HEAR_ME";
    public static final String AMTR = "A_MONUMENT_TO_ROME";
    public static final String TGEIH = "THE_GREATEST_EMPIRE_IN_HISTORY";
    public static final String SMQR = "SMQR";
    public static final String DYRT = "DID_YOU_READ_THIS";
    public static final String BAS = "BITTER_AND_SWEET";
    public static final String OPIOF = "OUR_PAST_IS_OUR_FUTURE";
    public static final String EII = "EVOLUTION_IS_INEVITABLE";
    public static final String AI = "AMICISSIMI_INTIMI";
    public static final String PIF = "PLASTIC_IS_FANTASTIC";

    /**
     * Aggiorna quando aggiungi i monumenti delle altre fazioni (ora ce ne sono 5).
     */
    private static final String[] MONUMENT_CHAINS = {
            "FORUM", "COLOSSEUM", "UNIVERSAL_EXPOSITION", "STEEL_TOWER", "ACCADEMY"
    };

    private static final int PATRICIAN_CLASS = 3;
    private static final int MAGNATE_CLASS = 5;
    private static final int PANEL_OPENS_GOAL = 100;
    private static final int OPPOSITE_DISTANCE = 6; // Faction.distance() max = 6

    public enum PanelKind {
        BUILDING, FACTIONS, BALANCE
    }

    private final SteamManager steam;
    private float timer = 0f;

    public AchievementManager(SteamManager steam) {
        this.steam = steam;
    }

    public void checkFirstLaunch(GameSettings settings) {
        if (settings.firstLaunchAchievementGranted)
            return;
        unlock(DYRBMG);
        // Segna come "dato" solo se Steam ha davvero ricevuto lo sblocco
        settings.firstLaunchAchievementGranted = steam != null && steam.isInitialized();
        settings.save();
    }

    public void unlock(String id) {
        GameSettings s = GameSettings.get();
        if (s.unlockedAchievements.contains(id))
            return;
        if (steam == null || !steam.isInitialized())
            return; // niente Steam: non marcare, riproverà alla prossima sessione
        steam.unlockAchievement(id);
        s.unlockedAchievements.add(id);
        s.save();
        Gdx.app.log("Achievement", "Unlocked " + id);
    }

    // ── Contatore pannelli (DID_YOU_READ_THIS) ──────────────────────────────
    public void onPanelOpened(PanelKind kind) {
        GameSettings s = GameSettings.get();
        int n;
        switch (kind) {
            case BUILDING:
                n = ++s.buildingPanelOpens;
                break;
            case FACTIONS:
                n = ++s.factionPanelOpens;
                break;
            default:
                n = ++s.balanceOpens;
                break;
        }
        if (n >= PANEL_OPENS_GOAL)
            unlock(DYRT);
        else if (n % 10 == 0)
            s.save();
    }

    // ── Polling (chiamato ogni frame, lavora 1 volta al secondo) ────────────
    public void tick(GameState state, float delta) {
        timer += delta;
        if (timer < 1f)
            return;
        timer = 0f;

        // Primo edificio / strada
        if (!state.buildingsOld.isEmpty() || !state.buildingsNew.isEmpty())
            unlock(TFB);

        // Classi sociali
        if (state.getClassPopulation(PATRICIAN_CLASS) >= 1000)
            unlock(RWBIAD);
        if (state.getClassPopulation(MAGNATE_CLASS) >= 1)
            unlock(SMQR);

        // Nuovo mondo
        if (state.newWorldUnlocked)
            unlock(NM);

        // Primo giacimento petrolifero
        if (state.countBuildingsOfType(BuildingType.OILRIG) >= 1)
            unlock(BG);

        // Prima radio prodotta
        if (state.everProduced.contains("radio"))
            unlock(CYHM);

        // Vapore + 10 acciaio/min
        if (state.eventManager.hasFired("BOILERS") && perMinute(state, "steel") >= 10f)
            unlock(IR);

        // Elettricità + 10 lampadine/min
        if (state.eventManager.hasFired("ELECTRICITY") && perMinute(state, "lightbulb") >= 10f)
            unlock(FL);

        // Plastica 100/min
        if (perMinute(state, "plastic") >= 100f)
            unlock(PIF);

        // Zucchero e caffè: stessa produzione esatta (> 0)
        float sugar = perMinute(state, "sugar");
        float coffee = perMinute(state, "coffee");
        if (sugar > 0f && Math.abs(sugar - coffee) < 0.001f)
            unlock(BAS);

        // Ideologia
        float ideo = state.ideologyManager.getValue();
        if (ideo <= IdeologyManager.MIN_VALUE)
            unlock(OPIOF);
        if (ideo >= IdeologyManager.MAX_VALUE)
            unlock(EII);

        // Monumenti
        int done = 0;
        for (String chain : MONUMENT_CHAINS)
            if (monumentDone(state, chain))
                done++;
        if (done >= 1)
            unlock(AMTR);
        if (done == MONUMENT_CHAINS.length)
            unlock(TGEIH);

        // Fazioni opposte entrambe con relazione positiva
        List<FactionManager.FactionState> arrived = state.factionManager.getArrivedFactions();
        for (int i = 0; i < arrived.size(); i++) {
            for (int j = i + 1; j < arrived.size(); j++) {
                FactionManager.FactionState a = arrived.get(i), b = arrived.get(j);
                if (a.relation > 0 && b.relation > 0
                        && a.faction.distance(b.faction) >= OPPOSITE_DISTANCE)
                    unlock(AI);
            }
        }
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    /** Produzione nominale al minuto (stessa formula della schermata Balance). */
    private float perMinute(GameState s, String resId) {
        float total = 0f;
        for (List<BuildingInstance> list : List.of(s.buildingsOld, s.buildingsNew)) {
            for (BuildingInstance b : list) {
                if (!resId.equals(b.type.prod) || b.type.consumableOnly || b.paused || !b.connected)
                    continue;
                if (!powerOk(b))
                    continue;
                total += b.type.rate * (60f / b.type.cycleTime) * b.penEfficiency;
            }
        }
        return total;
    }

    private boolean powerOk(BuildingInstance b) {
        switch (b.type.requiredPower) {
            case STEAM:
                return b.hasSteam;
            case ELECTRICITY:
                return b.hasElectricity;
            case BOTH:
                return b.hasSteam && b.hasElectricity;
            default:
                return true;
        }
    }

    private boolean monumentDone(GameState s, String chainId) {
        for (List<BuildingInstance> list : List.of(s.buildingsOld, s.buildingsNew)) {
            for (BuildingInstance b : list) {
                if (b.type.isMonument && chainId.equals(b.type.monumentChainId)
                        && b.type.isLastMonumentLevel() && !b.monumentUpgrading)
                    return true;
            }
        }
        return false;
    }
}