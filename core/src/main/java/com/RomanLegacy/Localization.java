package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * Accesso centralizzato alle traduzioni (I18NBundle + file .properties UTF-8).
 *
 * REGOLA D'ORO: non salvare mai stringhe tradotte in campi o static final.
 * Chiama sempre get()/getOr()/format() al momento del disegno, così il
 * cambio lingua funziona senza riavviare.
 *
 * Convenzione chiavi (tutto minuscolo tranne gli id, che restano come nel
 * codice):
 * ui.menu.new_game
 * building.WOOD_CAMP.name
 * resource.grain
 * event.THE_NEW_CITY.title / .description / .choice.0
 * faction.SENATORS.name
 */
public final class Localization {

    public static final String BUNDLE_PATH = "i18n/strings";
    public static final String DEFAULT_LANG = "en";
    public static final String[] SUPPORTED = { "en", "it" };

    private static final String MISSING = new String("<missing>"); // sentinella per identità

    private static I18NBundle bundle;
    private static final Map<String, String> cache = new HashMap<>();

    private Localization() {
    }

    public static void load(String lang) {
        if (!isSupported(lang))
            lang = DEFAULT_LANG;
        try {
            FileHandle base = Gdx.files.internal(BUNDLE_PATH);
            FileHandle root = Gdx.files.internal(BUNDLE_PATH + ".properties");
            FileHandle it = Gdx.files.internal(BUNDLE_PATH + "_it.properties");
            Gdx.app.log("Localization", "root exists=" + root.exists() + " path=" + root.file().getAbsolutePath());
            Gdx.app.log("Localization", "it exists=" + it.exists() + " path=" + it.file().getAbsolutePath());

            Locale locale = lang.equals(DEFAULT_LANG) ? Locale.ROOT : Locale.forLanguageTag(lang);
            bundle = I18NBundle.createBundle(base, locale);
            Gdx.app.log("Localization", "Bundle loaded, lang=" + lang + ", test=" + bundle.get("ui.menu.new_game"));
        } catch (Exception e) {
            Gdx.app.error("Localization", "Cannot load bundle for '" + lang + "'", e);
            bundle = null;
        }
        cache.clear();
    }

    public static boolean isSupported(String lang) {
        if (lang == null)
            return false;
        for (String s : SUPPORTED)
            if (s.equals(lang))
                return true;
        return false;
    }

    /** Traduzione, oppure "!chiave!" se manca (così l'errore si nota a schermo). */
    public static String get(String key) {
        return getOr(key, "!" + key + "!");
    }

    /**
     * Traduzione, oppure il fallback se la chiave non esiste (migrazione graduale).
     */
    public static String getOr(String key, String fallback) {
        String v = cache.get(key);
        if (v == null) {
            v = lookup(key);
            cache.put(key, v);
        }
        return v == MISSING ? fallback : v;
    }

    /** Come get(), con argomenti MessageFormat: "Phase {0} / {1}". */
    public static String format(String key, Object... args) {
        if (getOr(key, null) == null)
            return "!" + key + "!";
        try {
            return bundle.format(key, args);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    private static String lookup(String key) {
        if (bundle == null)
            return MISSING;
        try {
            return bundle.get(key);
        } catch (MissingResourceException e) {
            Gdx.app.log("Localization", "Missing key: " + key); // una volta sola, poi è in cache
            return MISSING;
        }
    }
}