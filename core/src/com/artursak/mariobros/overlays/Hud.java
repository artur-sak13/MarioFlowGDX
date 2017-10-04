package com.artursak.mariobros.overlays;

import com.artursak.mariobros.MarioBros;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Hud implements Disposable {
    public Stage stage;

    private Integer worldTimer;
    private float timeCount;
    private static Integer score;
    private boolean timeUp;

    private Label countdownLabel;
    private static Label scoreLabel;

    public Hud(SpriteBatch sb) {
        worldTimer = 300;
        timeCount = 0;
        score = 0;
        timeUp = false;

        Viewport viewport = new FitViewport(MarioBros.V_WIDTH, MarioBros.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        Label.LabelStyle labelstyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        countdownLabel = new Label(String.format("%03d", worldTimer), labelstyle);
        scoreLabel     = new Label(String.format("%06d", score), labelstyle);
        Label timeLabel = new Label("TIME", labelstyle);
        Label levelLabel = new Label("1-1", labelstyle);
        Label worldLabel = new Label("WORLD", labelstyle);
        Label marioLabel = new Label("MARIO", labelstyle);

        table.add(marioLabel).expandX().padTop(10);
        table.add(worldLabel).expandX().padTop(10);
        table.add(timeLabel).expandX().padTop(10);

        table.row();

        table.add(scoreLabel).expandX();
        table.add(levelLabel).expandX();
        table.add(countdownLabel).expandX();

        stage.addActor(table);
    }

    public void update(float dt) {
        timeCount += dt;
        if(timeCount >= 1) {
            worldTimer--;
            countdownLabel.setText(String.format("%03d", worldTimer));
            timeCount = 0;
        } else if(worldTimer == 0)
            timeUp = true;
    }

    public static void addScore(int value) {
        score += value;
        scoreLabel.setText(String.format("%06d", score));
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public boolean isTimeUp() {
        return timeUp;
    }

}