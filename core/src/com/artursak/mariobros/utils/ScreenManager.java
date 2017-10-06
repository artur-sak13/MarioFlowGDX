package com.artursak.mariobros.utils;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.AbstractScreen;
import com.artursak.mariobros.screens.GameOverScreen;
import com.artursak.mariobros.screens.GameWinScreen;
import com.artursak.mariobros.screens.PlayScreen;

public class ScreenManager {
    public enum ScreenEnum { LEVEL_1_1, GAME_OVER, GAME_WIN };

    private static ScreenManager screenManager;

    private MarioBros game;

    private ScreenManager() {
        super();
    }

    public static ScreenManager getInstance() {
        screenManager = (screenManager != null) ? screenManager : new ScreenManager();
        return screenManager;
    }

    public void init(MarioBros game) {
        this.game = game;
    }

    public void showScreen(ScreenEnum screenEnum) {
        AbstractScreen screen;

        switch (screenEnum) {
            case GAME_OVER:
                screen = new GameOverScreen(game);
                break;
            case GAME_WIN:
                screen = new GameWinScreen(game);
                break;
            case LEVEL_1_1:
            default:
                String level = "level1_1.tmx";
                screen = new PlayScreen(game, level);
                break;
        }
        game.setScreen(screen);
    }
}
