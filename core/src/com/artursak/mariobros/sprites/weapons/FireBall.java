package com.artursak.mariobros.sprites.weapons;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.utils.BodyFactory;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class FireBall extends Sprite {
    private final World                    world;
    private final Animation<TextureRegion> fireAnimation;
    private final boolean                  fireRight;
    private       Body                     b2body;
    private       float                    stateTime;
    private       boolean                  destroyed;
    private       boolean                  setToDestroy;

    public FireBall(PlayScreen screen, float x, float y, boolean fireRight) {
        this.fireRight = fireRight;
        PlayScreen screen1 = screen;
        this.world = screen.getWorld();
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("fireball"), i * 8, 0, 8, 8));
        }
        fireAnimation = new Animation<TextureRegion>(0.2f, frames);
        setRegion(fireAnimation.getKeyFrame(0));
        setBounds(x, y, 6 / MarioBros.PPM, 6 / MarioBros.PPM);
        defineFireBall();
        setToDestroy = false;
        destroyed = false;
    }

    private void defineFireBall() {
        Vector2 position = new Vector2(fireRight ? getX() + 12 / MarioBros.PPM : getX() - 12 / MarioBros.PPM, getY());
        b2body = BodyFactory.getInstance().makeBody(position, 3f, MarioBros.FIREBALL_BIT);

        for (Fixture fixture : b2body.getFixtureList())
            fixture.setUserData(this);

        b2body.setLinearVelocity(new Vector2(fireRight ? 2 : -2, 1.5f));
    }

    public void update(float dt) {
        stateTime += dt;
        setRegion(fireAnimation.getKeyFrame(stateTime, true));
        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        if ((!world.isLocked() && b2body != null) && (stateTime > 2f || setToDestroy) && !destroyed) {
            world.destroyBody(b2body);
            b2body = null;
            destroyed = true;
        } else {
            if (b2body.getLinearVelocity().y > 2f)
                b2body.setLinearVelocity(b2body.getLinearVelocity().x, 2f);
            else if ((fireRight && b2body.getLinearVelocity().x < 0) || (!fireRight && b2body.getLinearVelocity().x > 0))
                setToDestroy();
        }
    }

    public void setToDestroy() {
        setToDestroy = true;
    }

    @Override
    public void draw(Batch batch) {
        if (b2body != null && !destroyed || stateTime < 1)
            super.draw(batch);
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
