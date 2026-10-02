package com.RomanLegacy;

/**
 * Gestisce lo sblocco degli achievement Steam.
 * Fa da wrapper attorno a SteamManager così il resto del gioco non
 * dipende direttamente da steamworks4j — se Steam non è disponibile
 * (es. build senza SDK, o Steam non in esecuzione), tutte le chiamate
 * diventano no-op silenziosi, nessun crash.
 */
public class AchievementManager {

    // ── ID achievement (devono combaciare ESATTAMENTE con quelli configurati
    // su Steamworks partner site, sezione Stats & Achievements) ──────────
    public static final String DYRBMG = "DID_YOU_REALLY_BUY_MY_GAME";
    public static final String TFB = "THE_FIRST_BRICK";// TODO: sbloccato alla costruzione del primo edificio/strada
    public static final String RWBIAD = "ROME_WASN'T_BUILT_IN_A_DAY";// TODO: sbloccato al raggiungimento di 1000
                                                                     // patricians
    public static final String IR = "INDUSTRIAL_REVOLUTION";// TODO: sbloccato allo sblocco dello steam e produzione di
                                                            // 10 di steel al minuto (insieme)
    public static final String FL = "FIAT_LUX";// TODO: sbloccato allo sblocco dell'elettricità e produzione di 10
                                               // lampadine al minuto (insieme)
    public static final String NM = "NOVUS_MUNDUS";// TODO: sbloccato allo sblocco del nuovo mondo
    public static final String BG = "BLACK_GOLD";// TODO: sbloccato alla costruzione del primo oil rig
    public static final String CYHM = "CAN_YOU_HEAR_ME?";// TODO: sbloccato alla prima radio prodotta
    public static final String AMTR = "A_MONUMENT_TO_ROME";// TODO: sbloccato dopo aver completato il primo monumento
    public static final String TGEIH = "THE_GREATEST_EMPIRE_IN_HISTORY";// TODO: sbloccato dopo aver completato tutti i
                                                                        // monumenti delle 8 fazioni
    public static final String SMQR = "SMQR";// TODO: sbloccato al raggiungimento di 1 magnate
    public static final String DYRT = "DID_YOU_READ_THIS?";// TODO: sbloccato dopo aver aperto lo stesso tio di pannello
                                                           // o schermata 100 volte (solo quando si è in partita, non
                                                           // contano i menù principale e di pausa ecc, solo pannelli
                                                           // edifici, fazioni ecc)
    public static final String BAS = "BITTER AND SWEET";// TODO: sbloccato se producendo la stessa quantità di sugar e
                                                        // coffee allo stesso tempo (la stessa esatta)
    public static final String OPIOF = "OUR_PAST_IS_OUR_FUTURE";// TODO: sbloccato dopo aver raggiunto 100 di tradizione
    public static final String EII = "EVOLUTION_IS_INEVITABLE";// TODO: sbloccato dopo aver raggiunto 100 di evoluzione
    public static final String AI = "AMICISSIMI_INTIMI";// TODO: sbloccato se 2 fazioni opposte (di parole chiave,
                                                        // distanza 12 se non ricordo male) hanno entrambe relazioni
                                                        // positive allo stesso tempo
    public static final String PIF = "PLASTIC_IS_FANTASTIC";// TODO: sbloccato dopo aver raggiunto una produzione di 100
                                                            // di plastica al minuto

    private final SteamManager steam;

    public AchievementManager(SteamManager steam) {
        this.steam = steam;
    }

    /** Da chiamare una sola volta, al primo avvio assoluto del gioco. */
    public void checkFirstLaunch(GameSettings settings) {
        if (settings.firstLaunchAchievementGranted)
            return;
        unlock(DYRBMG);
        settings.firstLaunchAchievementGranted = true;
        settings.save();
    }

    public void unlock(String achievementId) {
        if (steam != null) {
            steam.unlockAchievement(achievementId);
        }
    }
}