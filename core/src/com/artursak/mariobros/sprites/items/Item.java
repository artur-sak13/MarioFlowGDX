package com.artursak.mariobros.sprites.items;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Item extends Sprite {
    protected PlayScreen screen;
    protected World world;
    Vector2 velocity;
    private boolean toDestroy;
    private boolean destroyed;
    protected Body body;

    Item(PlayScreen screen, float x, float y) {
        this.screen = screen;
        this.world = screen.getWorld();
        toDestroy = false;
        destroyed = false;

        setPosition(x,y);
        setBounds(getX(), getY(), 16/ MarioBros.PPM, 16 / MarioBros.PPM);
        defineItem();
    }

    public abstract void defineItem();
    public abstract void use(Mario mario);

    public void update(float dt) {
        if(!world.isLocked() && body != null && toDestroy && !destroyed) {
            world.destroyBody(body);
            body = null;
            destroyed = true;
        }
    }

    public void draw(Batch batch) {
        if(!destroyed)
            super.draw(batch);
    }

    void destroy() {
        toDestroy = true;
    }

    public void reverseVelocity(boolean x, boolean y) {
        if(x)
            velocity.x = -velocity.x;
        if(y)
            velocity.y = -velocity.y;
    }
}
