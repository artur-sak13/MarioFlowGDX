package com.artursak.mariobros.utils;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.enemies.Enemy;
import com.artursak.mariobros.sprites.enemies.KoopaTroopa;
import com.artursak.mariobros.sprites.tile_objects.Brick;
import com.artursak.mariobros.sprites.tile_objects.Coin;
import com.artursak.mariobros.sprites.enemies.Goomba;
import com.artursak.mariobros.sprites.tile_objects.FlagPole;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class GameWorldCreator {
    public PlayScreen screen;

    private Array<Goomba> goombas;
    private Array<KoopaTroopa> koopas;

    private World world;
    private TiledMap map;

    private BodyDef bdef;
    private PolygonShape shape;
    private FixtureDef fdef;

    public GameWorldCreator(PlayScreen screen) {
        this.screen = screen;
        world = screen.getWorld();
        map = screen.getMap();

        bdef = new BodyDef();
        shape = new PolygonShape();
        fdef = new FixtureDef();

        goombas = new Array<Goomba>();
        koopas = new Array<KoopaTroopa>();

        for(int i = 2; i < 9; i++)
            generateWorld(i);
    }

    public void generateWorld(int idx) {
        int nidx = (idx < 4) ? 1 : idx;
        Rectangle rect = null;

        for(MapObject object : map.getLayers().get(idx).getObjects().getByType(RectangleMapObject.class)) {
            if(idx != 4 && idx != 5 && idx != 8)
                rect = ((RectangleMapObject) object).getRectangle();

            switch(nidx) {
                case 1:
                    bdef.type = BodyDef.BodyType.StaticBody;
                    bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);

                    Body body = world.createBody(bdef);

                    shape.setAsBox(rect.getWidth() / 2 / MarioBros.PPM, rect.getHeight() / 2 / MarioBros.PPM);
                    fdef.shape = shape;
                    fdef.filter.categoryBits = (idx == 3) ? MarioBros.OBJECT_BIT : fdef.filter.categoryBits;
                    body.createFixture(fdef);
                    break;
                case 4:
                    new Coin(screen, object);
                    break;
                case 5:
                    new Brick(screen, object);
                    break;
                case 6:
                    goombas.add(new Goomba(screen, rect.getX() / MarioBros.PPM, rect.getY() / MarioBros.PPM));
                    break;
                case 7:
                    koopas.add(new KoopaTroopa(screen, rect.getX() / MarioBros.PPM, rect.getY() / MarioBros.PPM));
                    break;
                case 8:
                    screen.setFlag(new FlagPole(screen, object));
                    break;

            }
        }
    }

    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(koopas);
        return enemies;
    }
}
