package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * Crea e gestisce tutti i BitmapFont del gioco tramite FreeType.
 * Va istanziato UNA SOLA VOLTA (in Main.create()) e passato alle screen.
 * Chiamare dispose() quando il gioco viene chiuso.
 *
 * Font disponibili:
 * small — UI secondaria, tooltip, label risorse (14px)
 * normal — testo principale pannelli, bottombar (18px)
 * large — titoli pannelli, nomi edifici (22px)
 * title — titolo menu, intestazioni schermata (32px)
 * huge — titolo principale menu (52px)
 */
public class FontManager {

    public final BitmapFont small;
    public final BitmapFont normal;
    public final BitmapFont large;
    public final BitmapFont title;
    public final BitmapFont huge;
    public final BitmapFont xlarge;

    private final FreeTypeFontGenerator generatorRegular;
    private final FreeTypeFontGenerator generatorBold;

    public FontManager() {
        generatorRegular = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Cinzel-Regular.ttf"));
        generatorBold = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Cinzel-Bold.ttf"));

        small = make(generatorRegular, 14);
        normal = make(generatorRegular, 18);
        large = make(generatorBold, 22);
        title = make(generatorBold, 32);
        huge = make(generatorBold, 52);
        xlarge = make(generatorBold, 26);
    }

    private BitmapFont make(FreeTypeFontGenerator gen, int size) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = size;
        // Latin-1 base + caratteri italiani e simboli UI usati nel gioco
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "àèéìòùÀÈÉÌÒÙáíóúÁÍÓÚâêîôûÂÊÎÔÛäëïöüÄËÏÖÜçÇñÑ"
                + "→←↑↓✓✗⏸▶×•—…«»";
        p.kerning = true;
        p.borderWidth = 0;
        p.hinting = FreeTypeFontGenerator.Hinting.Full;
        BitmapFont font = gen.generateFont(p);
        font.getData().markupEnabled = false;
        return font;
    }

    public void dispose() {
        small.dispose();
        normal.dispose();
        large.dispose();
        title.dispose();
        huge.dispose();
        xlarge.dispose();
        generatorRegular.dispose();
        generatorBold.dispose();
    }
}