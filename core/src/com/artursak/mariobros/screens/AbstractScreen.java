package com.artursak.mariobros.screens;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.sprites.items.Item;
import com.artursak.mariobros.sprites.items.ItemDef;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractScreen implements Screen {
    protected final MarioBros                    game;
    protected final OrthographicCamera           camera;
    protected final Array<Item>                  items;
    protected final LinkedBlockingQueue<ItemDef> spawningItems;
    protected       Viewport                     viewport;
    protected       World                        world;

    AbstractScreen(MarioBros game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(MarioBros.V_WIDTH, MarioBros.V_HEIGHT, camera);
        items = new Array<Item>();
        spawningItems = new LinkedBlockingQueue<ItemDef>();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

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

    }

    public void spawnItem(ItemDef idef) {
        spawningItems.add(idef);
    }
}
