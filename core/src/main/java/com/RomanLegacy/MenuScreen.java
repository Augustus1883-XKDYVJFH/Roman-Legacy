package com.RomanLegacy;

import com.badlogic.gdx.Gdx;
import java.util.List;

public class MenuScreen extends BaseMenuScreen {

    public MenuScreen(Main game) {
        super(game);
        menuItems = new String[] {
                "ui.menu.new_game",
                "ui.menu.continue",
                "ui.menu.load_game",
                "ui.menu.options",
                "ui.menu.exit"
        };

    }

    @Override
    protected String getTitle() {
        return Localization.get("ui.menu.title");
    }

    @Override
    protected String getFooterText() {
        return Localization.get("ui.menu.footer");
    }

    @Override
    protected void onMenuItemSelected(int index) {
        switch (index) {
            case 0: // Nuova Partita
                startNewGame();
                break;
            case 1: // Continua — carica il salvataggio più recente tra tutti gli slot
                List<SaveManager.SaveSlotInfo> saves = SaveManager.getInstance().listSaves();
                if (!saves.isEmpty()) {
                    loadSave(saves.get(0).filename);
                } else {
                    startNewGame();
                }
                break;
            case 2: // Carica Partita
                game.setScreen(new SaveSlotScreen(game, this, null, false));
                break;
            case 3: // Impostazioni
                game.setScreen(new SettingsScreen(game, this));
                break;
            case 4: // Esci dal gioco
                Gdx.app.exit();
                break;
        }
    }

    @Override
    protected void onBackPressed() {
        Gdx.app.exit();
    }

    private void loadSave(String filename) {
        if (game.audio != null) {
            game.audio.playMusic(AudioManager.MUSIC_GAME);
        }
        game.setScreen(new LoadGameScreen(game, filename));
    }

    private void startNewGame() {
        if (game.audio != null) {
            game.audio.playMusic(AudioManager.MUSIC_GAME);
        }
        game.setScreen(new LoadingScreen(game, new GameScreen(game)));
    }
}