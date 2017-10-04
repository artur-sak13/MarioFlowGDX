package com.artursak.mariobros.screens;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.overlays.Hud;
import com.artursak.mariobros.sprites.enemies.Enemy;
import com.artursak.mariobros.sprites.items.*;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.sprites.tile_objects.FlagPole;
import com.artursak.mariobros.utils.GameWorldCreator;
import com.artursak.mariobros.utils.WorldContactListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.LinkedBlockingQueue;

public class PlayScreen implements Screen {
    private MarioBros game;
    private TextureAtlas atlas;

    private OrthographicCamera camera;
    private Viewport gamePort;
    private Hud hud;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private World world;
    private Box2DDebugRenderer b2dr;
    private GameWorldCreator creator;

    private Mario player;
    private FlagPole flag;

    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

    public PlayScreen(MarioBros game) {
        this.game = game;

        atlas = new TextureAtlas("mario_all.pack");
        camera = new OrthographicCamera();
        gamePort = new FitViewport(MarioBros.V_WIDTH / MarioBros.PPM, MarioBros.V_HEIGHT / MarioBros.PPM, camera);
        hud = new Hud(game.batch);

        map = new TmxMapLoader().load("level1_1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioBros.PPM);

        camera.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0,-10), true);
        b2dr = new Box2DDebugRenderer();

        player = new Mario(this);

        creator = new GameWorldCreator(this);

        world.setContactListener(new WorldContactListener());

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();

    }

    public void spawnItem(ItemDef idef) {
        itemsToSpawn.add(idef);
    }

    private void handleSpawningItems() {
        if(!itemsToSpawn.isEmpty()) {
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class)
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            else if(idef.type == Flower.class)
                items.add(new Flower(this, idef.position.x, idef.position.y));
        }
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    @Override
    public void show() {

    }

    private void handleInput() {
        if(player.currentState != Mario.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W))
                player.jump();
            else if ((Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) && player.b2body.getLinearVelocity().x <= 2)
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
            else if ((Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) && player.b2body.getLinearVelocity().x >= -2)
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
            else if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                player.fire();
        }
    }

    public void update(float dt) {
        handleInput();
        handleSpawningItems();

        world.step(1f / 60f, 6, 2);

        player.update(dt);
        flag.update(dt);

        for(Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            if(!enemy.isDead() && (enemy.getX() < player.getX() + 224 / MarioBros.PPM))
                enemy.b2body.setActive(true);
        }

        for(Item item : items)
            item.update(dt);

        hud.update(dt);

        if(player.currentState != Mario.State.DEAD)
            camera.position.x = MathUtils.clamp(player.b2body.getPosition().x, gamePort.getWorldWidth() / 2, (227 * 16) / MarioBros.PPM);

        camera.update();
        renderer.setView(camera);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        b2dr.render(world, camera.combined);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        player.draw(game.batch);
        flag.draw(game.batch);


        for(Enemy enemy : creator.getEnemies())
            enemy.draw(game.batch);
        for(Item item : items)
            item.draw(game.batch);

        game.batch.end();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        } else if(goal()) {
            game.setScreen(new GameWinScreen(game));
            dispose();
        }
    }

    public void setFlag(FlagPole flag) {
        this.flag = flag;
    }

    private boolean gameOver() {
        return (player.currentState == Mario.State.DEAD && player.getStateTimer() > 3);
    }

    private boolean goal() {
        return (player.currentState == Mario.State.WIN && player.getStateTimer() > 3 && FlagPole.Flag.staticBod);
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    public TiledMap getMap() {
        return map;
    }

    public World getWorld() {
        return world;
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
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public Hud getHud() {
        return hud;
    }
}
