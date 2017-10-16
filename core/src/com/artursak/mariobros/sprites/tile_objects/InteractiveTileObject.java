package com.artursak.mariobros.sprites.tile_objects;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.utils.BodyFactory;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public abstract class InteractiveTileObject {
    protected final World      world;
    protected final Body       body;
    protected final PlayScreen screen;
    final           TiledMap   map;
    final           MapObject  object;

    final Fixture fixture;

    InteractiveTileObject(PlayScreen screen, MapObject object) {
        this.object = object;
        this.screen = screen;
        this.world = screen.getWorld();
        this.map = screen.getMap();

        Rectangle bounds   = ((RectangleMapObject) object).getRectangle();
        Vector2   position = new Vector2((bounds.getX() + bounds.getWidth() / 2), (bounds.getY() + bounds.getHeight() / 2)).scl(1 / MarioBros.PPM);
        Vector2   boxDims  = new Vector2(bounds.getWidth() / 2, bounds.getHeight() / 2).scl(1 / MarioBros.PPM);
        body = BodyFactory.getInstance().makeBody(position, boxDims, MarioBros.NOTHING_BIT, BodyDef.BodyType.StaticBody);
        fixture = body.getFixtureList().get(0);
    }

    public abstract void onHeadHit(Mario mario);

    void setCategoryFilter(short filterBit) {
        Filter filter = new Filter();
        filter.categoryBits = filterBit;
        fixture.setFilterData(filter);
    }

    TiledMapTileLayer.Cell getCell() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(1);
        return layer.getCell((int) (body.getPosition().x * MarioBros.PPM / 16),
                             (int) (body.getPosition().y * MarioBros.PPM / 16));

    }
}
