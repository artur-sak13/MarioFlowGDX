package com.artursak.mariobros.sprites.tile_objects;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.overlays.Hud;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.items.Flower;
import com.artursak.mariobros.sprites.items.ItemDef;
import com.artursak.mariobros.sprites.items.Mushroom;
import com.artursak.mariobros.sprites.Mario;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;

public class Coin extends InteractiveTileObject {
    private static TiledMapTileSet tileSet;

    public Coin(PlayScreen screen, MapObject object) {
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("mario_tileset");
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.COIN_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        int BLANK_COIN = 28;
        if(getCell().getTile().getId() != BLANK_COIN) {
            if(object.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBros.PPM),
                        Mushroom.class));
            } else if(object.getProperties().containsKey("flower")) {
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBros.PPM),
                        Flower.class));
            }
        }
        getCell().setTile(tileSet.getTile(BLANK_COIN));
        Hud.addScore(200);
    }
}
