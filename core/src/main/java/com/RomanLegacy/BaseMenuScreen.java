package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Base class per tutte le menu screen con design standardizzato.
 */
public abstract class BaseMenuScreen implements Screen {

    protected final Main game;
    protected final OrthographicCamera camera;
    protected final ShapeRenderer shapeRenderer;
    protected final SpriteBatch batch;
    protected final Vector3 mousePos = new Vector3();
    protected final GlyphLayout layout = new GlyphLayout();

    protected String[] menuItems;
    protected int selectedIndex = 0;
    protected int hoveredIndex = -1;
    protected boolean inSubMenu = false;

    // ── Layout card menu ─────────────────────────────────────────────────────
    private static final float BUTTON_H = 46f;
    private static final float ACCENT_W = 4f;

    public BaseMenuScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.SCREEN_W, Main.SCREEN_H);
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        updateHover();
        handleInput();
        ScreenUtils.clear(UITheme.BG_DARK.r, UITheme.BG_DARK.g, UITheme.BG_DARK.b, 1);
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawBackground();

        if (!inSubMenu) {
            drawTitle();
        }

        if (inSubMenu) {
            drawSubMenu();
        } else {
            drawMenu();
        }

        drawFooter();
        drawEarlyAccessBadge();
    }

    protected void drawBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.BG_DARK);
        shapeRenderer.rect(0, 0, Main.SCREEN_W, Main.SCREEN_H);

        // Decorative top band
        shapeRenderer.setColor(UITheme.BG_DECORATIVE);
        shapeRenderer.rect(0, Main.SCREEN_H - 230, Main.SCREEN_W, 230);
        shapeRenderer.end();
    }

    protected void drawTitle() {
        batch.begin();

        String title = getTitle();
        layout.setText(game.fonts.huge, title);

        game.fonts.huge.setColor(UITheme.TEXT_GOLD);
        game.fonts.huge.draw(batch, title,
                Main.SCREEN_W / 2f - layout.width / 2f,
                Main.SCREEN_H + UITheme.TITLE_Y_OFFSET);
        batch.end();
    }

    /** Disegna una singola card di menu (riusata da menu principale e submenu). */
    protected void drawMenuRow(int index, String label, float y, boolean selected, boolean hovered) {
        UITheme.drawMenuRow(shapeRenderer, batch, game.fonts.large, layout,
                label, Main.SCREEN_W / 2f, y, UITheme.MENU_ITEM_WIDTH, BUTTON_H, selected, hovered);
    }

    protected void drawMenu() {
        int startY = Main.SCREEN_H / 2 + (int) UITheme.MENU_START_Y_OFFSET;
        for (int i = 0; i < menuItems.length; i++) {
            float y = startY - i * UITheme.MENU_ITEM_SPACING;
            drawMenuRow(i, menuItems[i], y, i == selectedIndex, i == hoveredIndex);
        }
    }

    protected void drawSubMenu() {
        // Override nelle sottoclassi
        drawMenu();
    }

    protected void drawFooter() {
        batch.begin();
        layout.setText(game.fonts.small, getFooterText());
        game.fonts.small.setColor(UITheme.TEXT_DARK_GRAY);
        game.fonts.small.draw(batch, getFooterText(),
                Main.SCREEN_W / 2f - layout.width / 2f, 65);
        batch.end();
    }

    protected void drawEarlyAccessBadge() {
        String badge = "EARLY ACCESS";
        float pad = 10f;
        layout.setText(game.fonts.small, badge);
        float bw = layout.width + pad * 2f;
        float bh = 26f;
        float bx = Main.SCREEN_W - bw - 20f;
        float by = 20f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.BG_PANEL);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UITheme.GOLD_DARK);
        shapeRenderer.rect(bx, by, bw, bh);
        shapeRenderer.end();

        batch.begin();
        game.fonts.small.setColor(UITheme.TEXT_GOLD);
        game.fonts.small.draw(batch, badge, bx + pad, by + bh - 8f);
        batch.end();
    }

    protected void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + menuItems.length) % menuItems.length;
            game.audio.playSound(AudioManager.SFX_UI_CLICK);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % menuItems.length;
            game.audio.playSound(AudioManager.SFX_UI_CLICK);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            onMenuItemSelected(selectedIndex);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            onBackPressed();
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handleMouseClick();
        }
    }

    /** Aggiorna hoveredIndex in base alla posizione del mouse (solo visuale). */
    protected void updateHover() {
        hoveredIndex = -1;
        if (inSubMenu)
            return; // le sottoclassi gestiscono l'hover del proprio submenu se serve

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);

        int startY = Main.SCREEN_H / 2 + (int) UITheme.MENU_START_Y_OFFSET;
        float x = Main.SCREEN_W / 2f - UITheme.MENU_ITEM_WIDTH / 2f;

        for (int i = 0; i < menuItems.length; i++) {
            float y = startY - i * UITheme.MENU_ITEM_SPACING;
            float by = y - BUTTON_H / 2f;
            if (mousePos.x >= x && mousePos.x <= x + UITheme.MENU_ITEM_WIDTH &&
                    mousePos.y >= by && mousePos.y <= by + BUTTON_H) {
                hoveredIndex = i;
                break;
            }
        }
    }

    protected void handleMouseClick() {
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);

        int startY = Main.SCREEN_H / 2 + (int) UITheme.MENU_START_Y_OFFSET;

        for (int i = 0; i < menuItems.length; i++) {
            float y = startY - i * UITheme.MENU_ITEM_SPACING;
            if (mousePos.x >= Main.SCREEN_W / 2f - UITheme.MENU_ITEM_WIDTH / 2 &&
                    mousePos.x <= Main.SCREEN_W / 2f + UITheme.MENU_ITEM_WIDTH / 2 &&
                    mousePos.y >= y - 22 && mousePos.y <= y + 22) {
                selectedIndex = i;
                onMenuItemSelected(i);
                break;
            }
        }
    }

    // Abstract methods
    protected abstract String getTitle();

    protected abstract String getFooterText();

    protected abstract void onMenuItemSelected(int index);

    protected void onBackPressed() {
        if (inSubMenu) {
            inSubMenu = false;
        } else {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int w, int h) {
        // NON usare camera.setToOrtho(false, w, h): riancorerebbe la camera
        // alle dimensioni reali della finestra invece che al canvas virtuale
        // Main.SCREEN_W/SCREEN_H su cui è calcolato tutto il layout dei
        // bottoni (drawMenu, drawMenuRow ecc.), causando menu deformati o
        // tagliati quando la finestra reale è più piccola del canvas.
        camera.update();
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
    }
}