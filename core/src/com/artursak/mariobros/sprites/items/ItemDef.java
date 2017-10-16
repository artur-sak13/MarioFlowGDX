package com.artursak.mariobros.sprites.items;

import com.badlogic.gdx.math.Vector2;

public class ItemDef {
    public final Vector2  position;
    public final Class<?> type;

    public ItemDef(Vector2 position, Class<?> type) {
        this.position = position;
        this.type = type;
    }
}
