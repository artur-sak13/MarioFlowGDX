package com.artursak.mariobros.screens;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.overlays.Hud;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.sprites.enemies.Enemy;
import com.artursak.mariobros.sprites.items.Flower;
import com.artursak.mariobros.sprites.items.Item;
import com.artursak.mariobros.sprites.items.ItemDef;
import com.artursak.mariobros.sprites.items.Mushroom;
import com.artursak.mariobros.sprites.tile_objects.FlagPole;
import com.artursak.mariobros.utils.BodyFactory;
import com.artursak.mariobros.utils.GameWorldCreator;
import com.artursak.mariobros.utils.WorldContactListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PlayScreen extends AbstractScreen {

    private final TiledMap                   map;
    private       TextureAtlas               atlas;
    private       Hud                        hud;
    private       OrthogonalTiledMapRenderer renderer;

    private Box2DDebugRenderer b2dr;
    private GameWorldCreator   creator;

    private Mario    mario;
    private FlagPole flag;

    private float mapWidth;

    public PlayScreen(MarioBros game, String level) {
        super(game);
        map = new TmxMapLoader().load(level);
//        mapWidth = ((TiledMapTileLayer) map.getLayers().get(0)).getWidth();
    }

    @Override
    public void show() {
        viewport = new FitViewport(MarioBros.V_WIDTH / MarioBros.PPM, MarioBros.V_HEIGHT / MarioBros.PPM, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        atlas = new TextureAtlas("mario_all.pack");

        world = new World(MarioBros.GRAVITY, true);
        world.setContactListener(new WorldContactListener());
        BodyFactory.getInstance().init(world);

        creator = new GameWorldCreator(this);
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioBros.PPM);

        hud = new Hud(game.batch);

        float cameraLeftLimit  = (MarioBros.V_WIDTH / MarioBros.PPM) / 2;
        float cameraRightLimit = mapWidth - (MarioBros.V_WIDTH / MarioBros.PPM) / 2;

        b2dr = new Box2DDebugRenderer(true, true, false, false, false, false);

        mario = new Mario(this);

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render(new int[]{0, 1});

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        flag.draw(game.batch);


        for (Enemy enemy : creator.getEnemies())
            enemy.draw(game.batch);

        for (Item item : items)
            item.draw(game.batch);

        mario.draw(game.batch);

        game.batch.end();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        b2dr.render(world, camera.combined);

        if (gameOver()) {
            game.changeScreen(MarioBros.ScreenEnum.GAME_OVER);
            dispose();
        } else if (goal()) {
            game.changeScreen(MarioBros.ScreenEnum.GAME_WIN);
            dispose();
        }
    }

    private void update(float dt) {
        handleSpawningItems();

        world.step(1 / 60f, 6, 2);
        flag.update(dt);

        for (Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            if (!enemy.isDead() && (enemy.getX() < mario.getX() + 224 / MarioBros.PPM))
                enemy.b2body.setActive(true);
        }

        for (Item item : items)
            item.update(dt);

        mario.update(dt);

//        float targetX = camera.position.x;
        if (!mario.isDead())
            camera.position.x = MathUtils.clamp(mario.b2body.getPosition().x, viewport.getWorldWidth() / 2, (227 * 16) / MarioBros.PPM);
//            targetX = camera.position.x = MathUtils.clamp(mario.b2body.getPosition().x, cameraLeftLimit, cameraRightLimit);

//        camera.position.x = MathUtils.lerp(camera.position.x, targetX, 0.1f);
//        if(Math.abs(camera.position.x - targetX) < 0.1f)
//            camera.position.x = targetX;

        camera.update();
        renderer.setView(camera);

        hud.update(dt);

        cleanUp();
    }

//    public float getMapWidth() { return mapWidth; }

    private boolean gameOver() {
        return (mario.currentState == Mario.State.DYING && mario.getStateTimer() > 3);
    }

    private boolean goal() {
        return (mario.currentState == Mario.State.WINNING && mario.getStateTimer() > 3 && FlagPole.isStatic());
    }

    private void handleSpawningItems() {
        if (!spawningItems.isEmpty()) {
            ItemDef idef = spawningItems.poll();
            if (idef.type == Mushroom.class)
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            else if (idef.type == Flower.class)
                items.add(new Flower(this, idef.position.x, idef.position.y));
        }
    }

    private void cleanUp() {
        for (int i = 0; i < items.size; i++) {
            if (items.get(i).isDestroyed())
                items.removeIndex(i);
        }
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
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
        atlas.dispose();
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public TiledMap getMap() {
        return map;
    }

    public World getWorld() {
        return world;
    }

    public void setFlag(FlagPole flag) {
        this.flag = flag;
    }

    public Hud getHud() {
        return hud;
    }
}
