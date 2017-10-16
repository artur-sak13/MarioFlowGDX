package com.artursak.mariobros.sprites.items;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.utils.BodyFactory;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Item extends Sprite {
    protected final PlayScreen screen;
    protected final World      world;
    protected       Vector2    velocity;
    protected       Body       body;
    private         boolean    toDestroy;
    private         boolean    destroyed;

    Item(PlayScreen screen, float x, float y) {
        this.screen = screen;
        this.world = screen.getWorld();
        toDestroy = false;
        destroyed = false;

        setPosition(x, y);
        setBounds(getX(), getY(), 16 / MarioBros.PPM, 16 / MarioBros.PPM);
        defineItem();
    }

    private void defineItem() {
        Vector2 position = new Vector2(getX(), getY());
        body = BodyFactory.getInstance().makeBody(position, 6, MarioBros.ITEM_BIT);
        body.getFixtureList().get(0).setUserData(this);
    }

    public abstract void use(Mario mario);

    public void update(float dt) {
        if (!world.isLocked() && body != null && toDestroy && !destroyed) {
            world.destroyBody(body);
            body = null;
            destroyed = true;
        }
    }

    public void draw(Batch batch) {
        if (!destroyed)
            super.draw(batch);
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    void destroy() {
        toDestroy = true;
    }

    public void reverseVelocity(boolean x, boolean y) {
        if (x)
            velocity.x = -velocity.x;
        if (y)
            velocity.y = -velocity.y;
    }
}
