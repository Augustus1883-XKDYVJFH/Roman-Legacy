package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Gestisce tutta la musica e i suoni del gioco.
 * Va istanziato UNA SOLA VOLTA in Main.create() e passato alle screen.
 * Chiamare dispose() alla chiusura del gioco.
 *
 * PLAYLIST — caricamento dinamico:
 * Per ogni contesto (menu/game/newworld) vengono cercati i file numerati
 * in assets/audio/, es. game1.ogg, game2.ogg, game3.ogg... La scansione si
 * ferma al primo numero mancante (niente buchi nella numerazione).
 * Aggiungere un nuovo brano = mettere il file con il numero successivo
 * nella cartella, nessuna modifica al codice richiesta.
 *
 * SFX attesi in assets/audio/:
 * place_building.ogg, demolish.ogg, ui_click.ogg,
 * expedition_start.ogg, expedition_complete.ogg,
 * upgrade_house.ogg, faction_arrive.ogg
 *
 * Se un file manca viene ignorato (no crash, no warning bloccante).
 */
public class AudioManager {

    // ── Nomi playlist (usati anche come prefisso file, es. "game" → game1.ogg) ──
    public static final String MUSIC_MENU = "menu";
    public static final String MUSIC_GAME = "game";
    public static final String MUSIC_NEWWORLD = "newworld";

    // ── Nomi SFX ─────────────────────────────────────────────────────────────────
    public static final String SFX_PLACE = "place_building";
    public static final String SFX_DEMOLISH = "demolish";
    public static final String SFX_UI_CLICK = "ui_click";
    public static final String SFX_EXPEDITION_START = "expedition_start";
    public static final String SFX_EXPEDITION_COMPLETE = "expedition_complete";
    public static final String SFX_UPGRADE_HOUSE = "upgrade_house";
    public static final String SFX_FACTION_ARRIVE = "faction_arrive";

    // Limite di sicurezza sulla scansione numerata (non un vero limite d'uso).
    private static final int MAX_TRACKS_PER_PLAYLIST = 200;

    // ── Stato interno ────────────────────────────────────────────────────────────
    private final Map<String, Music> musicTracks = new HashMap<>();
    private final Map<String, Sound> sounds = new HashMap<>();
    private final Map<String, String[]> playlists = new HashMap<>();

    private Music currentMusic = null;
    private String currentPlaylistKey = null;
    private String lastPlayedTrack = null;

    // ── Sblocco tracce via eventi ────────────────────────────────────────────
    // Traccia → id evento richiesto (NarrativeEvent.id). Assente = sempre
    // disponibile.
    // Esempio: "game4", "OIL_DISCOVERY"
    private static final Map<String, String> TRACK_REQUIREMENT = Map.of(
    // "game4", "OIL_DISCOVERY",
    // "newworld2", "MAGNATES"
    );

    private Set<String> unlockedEvents = new HashSet<>();

    private final Random shuffleRng = new Random();

    // ── Costruttore ───────────────────────────────────────────────────────────

    public AudioManager() {
        loadPlaylist(MUSIC_MENU);
        loadPlaylist(MUSIC_GAME);
        loadPlaylist(MUSIC_NEWWORLD);

        loadSound(SFX_PLACE, "audio/place_building.ogg");
        loadSound(SFX_DEMOLISH, "audio/demolish.ogg");
        loadSound(SFX_UI_CLICK, "audio/ui_click.ogg");
        loadSound(SFX_EXPEDITION_START, "audio/expedition_start.ogg");
        loadSound(SFX_EXPEDITION_COMPLETE, "audio/expedition_complete.ogg");
        loadSound(SFX_UPGRADE_HOUSE, "audio/upgrade_house.ogg");
        loadSound(SFX_FACTION_ARRIVE, "audio/faction_arrive.ogg");
    }

    // ── API pubblica — Musica ────────────────────────────────────────────────

    /**
     * Avvia la playlist di un contesto. Se già in riproduzione, non fa nulla.
     * La selezione del brano è casuale (shuffle) con leggera preferenza a non
     * ripetere l'ultimo brano suonato — ma può capitare, raramente.
     */
    public void playMusic(String playlistKey) {
        if (playlistKey.equals(currentPlaylistKey) && currentMusic != null && currentMusic.isPlaying())
            return;

        stopMusic();
        currentPlaylistKey = playlistKey;
        playNextTrack();
    }

    private void playNextTrack() {
        if (currentPlaylistKey == null)
            return;
        String[] tracks = playlists.get(currentPlaylistKey);
        if (tracks == null || tracks.length == 0)
            return;

        String trackName = pickTrack(tracks);
        Music track = musicTracks.get(trackName);
        if (track == null)
            return;

        track.setLooping(false);
        track.setVolume(GameSettings.get().effectiveMusicVolume());
        track.setOnCompletionListener(m -> playNextTrack());
        track.play();
        currentMusic = track;
        lastPlayedTrack = trackName;
    }

    /**
     * Sceglie un brano casuale dalla playlist. Se coincide con l'ultimo
     * suonato, ritenta fino a 3 volte (85% di probabilità di ritentare ogni
     * volta) — quindi la ripetizione resta possibile ma rara.
     */
    private String pickTrack(String[] tracks) {
        List<String> available = new ArrayList<>();
        for (String t : tracks) {
            String requiredEvent = TRACK_REQUIREMENT.get(t);
            if (requiredEvent == null || unlockedEvents.contains(requiredEvent)) {
                available.add(t);
            }
        }
        if (available.isEmpty())
            available.addAll(java.util.Arrays.asList(tracks)); // fallback: mai lista vuota

        if (available.size() == 1)
            return available.get(0);

        String candidate = available.get(shuffleRng.nextInt(available.size()));
        int tries = 0;
        while (candidate.equals(lastPlayedTrack) && tries < 3 && shuffleRng.nextFloat() < 0.85f) {
            candidate = available.get(shuffleRng.nextInt(available.size()));
            tries++;
        }
        return candidate;
    }

    /**
     * Da chiamare quando cambia lo stato eventi (es. ogni volta che uno ne risolve
     * uno).
     */
    public void setUnlockedEvents(Set<String> firedEventIds) {
        this.unlockedEvents = firedEventIds != null ? firedEventIds : new HashSet<>();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.setOnCompletionListener(null);
            currentMusic.stop();
            currentMusic = null;
        }
        currentPlaylistKey = null;
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying())
            currentMusic.pause();
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying())
            currentMusic.play();
    }

    // ── API pubblica — SFX ───────────────────────────────────────────────────

    public void playSound(String name) {
        Sound s = sounds.get(name);
        if (s == null) {
            return;
        }
        float vol = GameSettings.get().effectiveSfxVolume();
        if (vol > 0f)
            s.play(vol);
    }

    // ── Aggiornamento volume a runtime (chiamato da SettingsScreen) ──────────

    public void applyVolumeSettings() {
        if (currentMusic != null)
            currentMusic.setVolume(GameSettings.get().effectiveMusicVolume());
    }

    // ── Dispose ──────────────────────────────────────────────────────────────

    public void dispose() {
        stopMusic();
        for (Music m : musicTracks.values())
            m.dispose();
        for (Sound s : sounds.values())
            s.dispose();
        musicTracks.clear();
        sounds.clear();
        playlists.clear();
    }

    // ── Caricamento playlist numerata con fallback ──────────────────────────

    /**
     * Scansiona assets/audio/{playlistKey}1.ogg, {playlistKey}2.ogg, ...
     * finché il file esiste. Si ferma al primo numero mancante (nessun buco
     * ammesso nella numerazione). Se non trova nemmeno il primo file, la
     * playlist resta vuota — playMusic() la ignorerà senza crash.
     */
    private void loadPlaylist(String playlistKey) {
        List<String> found = new ArrayList<>();
        int i = 1;
        while (i <= MAX_TRACKS_PER_PLAYLIST) {
            String trackName = playlistKey + i;
            String path = "audio/" + trackName + ".ogg";
            try {
                if (!Gdx.files.internal(path).exists()) {
                    break;
                }
                musicTracks.put(trackName, Gdx.audio.newMusic(Gdx.files.internal(path)));
                found.add(trackName);
            } catch (Exception e) {
                break;
            }
            i++;
        }
        playlists.put(playlistKey, found.toArray(new String[0]));
    }

    private void loadSound(String name, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds.put(name, Gdx.audio.newSound(Gdx.files.internal(path)));
            }
        } catch (Exception e) {
            return;
        }
    }
}