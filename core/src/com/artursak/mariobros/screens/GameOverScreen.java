package com.artursak.mariobros.screens;

import com.artursak.mariobros.MarioBros;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class GameOverScreen extends AbstractScreen {
    private final Stage stage;

    public GameOverScreen(MarioBros game) {
        super(game);
        stage = new Stage(viewport, game.batch);
    }

    @Override
    public void show() {
        Table table = new Table();
        table.center();
        table.setFillParent(true);
        stage.addActor(table);

        Label.LabelStyle font           = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        Label            gameOverLabel  = new Label("GAME OVER", font);
        Label            playAgainLabel = new Label("Click to Play Again", font);

        table.add(gameOverLabel).expandX();
        table.row();
        table.add(playAgainLabel).expandX().padTop(10f);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.justTouched()) {
            game.changeScreen(MarioBros.ScreenEnum.PLAY_SCREEN);
            dispose();
        }
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
