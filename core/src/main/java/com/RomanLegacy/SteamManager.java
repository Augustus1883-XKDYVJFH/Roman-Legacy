package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.codedisaster.steamworks.*;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Wrapper attorno a Steamworks (steamworks4j). Se l'inizializzazione fallisce
 * (Steam non in esecuzione, SDK mancante, steam_appid.txt assente durante lo
 * sviluppo) il manager resta "disabled" e tutte le chiamate diventano no-op:
 * il gioco deve funzionare identico anche senza Steam attivo.
 */
public class SteamManager {

    private boolean initialized = false;
    private boolean statsReceived = false;

    private SteamUserStats userStats;
    private final Deque<String> pendingAchievements = new ArrayDeque<>();

    private final SteamUserStatsCallback callback = new SteamUserStatsCallback() {
        @Override
        public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) {
            if (result != SteamResult.OK) {
                Gdx.app.error("SteamManager", "requestCurrentStats failed: " + result);
                return;
            }
            statsReceived = true;
            while (!pendingAchievements.isEmpty()) {
                doUnlock(pendingAchievements.poll());
            }
        }

        @Override
        public void onUserStatsStored(long gameId, SteamResult result) {
        }

        @Override
        public void onUserStatsUnloaded(SteamID steamIDUser) {
        }

        @Override
        public void onUserAchievementStored(long gameId, boolean isGroupAchievement,
                String achievementName, int curProgress, int maxProgress) {
        }

        @Override
        public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) {
        }

        @Override
        public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard,
                SteamLeaderboardEntriesHandle entries, int numEntries) {
        }

        @Override
        public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard,
                int score, boolean scoreChanged, int globalRankNew, int globalRankPrevious) {
        }

        @Override
        public void onGlobalStatsReceived(long gameId, SteamResult result) {
        }
    };

    public SteamManager() {
        try {
            SteamLibraryLoader loader = new SteamLibraryLoaderLwjgl3();
            if (!SteamAPI.loadLibraries(loader)) {
                Gdx.app.error("SteamManager", "Failed to load Steam native libraries");
                return;
            }
            if (!SteamAPI.init()) {
                Gdx.app.log("SteamManager", "Steam client not running - achievements disabled");
                return;
            }
            userStats = new SteamUserStats(callback);
            // requestCurrentStats() non esiste più nella libreria: il client Steam
            // carica già stats/achievement prima dell'avvio del processo, quindi
            // possiamo considerarle disponibili da subito.
            statsReceived = true;
            initialized = true;
        } catch (SteamException e) {
            Gdx.app.error("SteamManager", "Steam init failed", e);
            initialized = false;
        } catch (Throwable t) {
            initialized = false;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void unlockAchievement(String achievementId) {
        if (!initialized)
            return;
        if (!statsReceived) {
            pendingAchievements.add(achievementId);
            return;
        }
        doUnlock(achievementId);
    }

    private void doUnlock(String achievementId) {
        Gdx.app.log("SteamManager", "Unlocking achievement: " + achievementId);

        boolean result = userStats.setAchievement(achievementId);

        Gdx.app.log("SteamManager", "setAchievement result: " + result);

        boolean stored = userStats.storeStats();

        Gdx.app.log("SteamManager", "storeStats result: " + stored);
    }

    /** Da chiamare ogni frame da Main.render(). */
    public void update() {
        if (!initialized)
            return;
        if (SteamAPI.isSteamRunning()) {
            SteamAPI.runCallbacks();
        }
    }

    public void dispose() {
        if (!initialized)
            return;
        if (userStats != null)
            userStats.dispose();
        SteamAPI.shutdown();
    }
}