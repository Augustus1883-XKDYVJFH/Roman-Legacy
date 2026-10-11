package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontManager {

    public final BitmapFont small;
    public final BitmapFont normal;
    public final BitmapFont large;
    public final BitmapFont title;
    public final BitmapFont huge;
    public final BitmapFont xlarge;

    private final FreeTypeFontGenerator generator;

    public FontManager() {
        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/m5x7.ttf"));

        small = make(28, false);
        normal = make(32, false);
        large = make(46, false);
        title = make(60, false);
        huge = make(72, false);
        xlarge = make(84, false);
    }

    private BitmapFont make(int size, boolean fakeBold) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = size;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "àèéìòùÀÈÉÌÒÙáíóúÁÍÓÚâêîôûÂÊÎÔÛäëïöüÄËÏÖÜçÇñÑ"
                + "→←↑↓✓✗⏸▶×•—…«»";
        p.kerning = true;
        p.hinting = FreeTypeFontGenerator.Hinting.Full;

        if (fakeBold) {
            p.borderWidth = Math.max(0.6f, size / 30f); // 22px -> 0.7, 52px -> 1.7
            p.borderColor = Color.WHITE; // stesso colore del testo
            p.borderStraight = true; // bordo più netto
        } else {
            p.borderWidth = 0;
        }

        BitmapFont font = generator.generateFont(p);
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
        generator.dispose();
    }
}