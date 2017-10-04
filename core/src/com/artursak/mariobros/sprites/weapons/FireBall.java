package com.artursak.mariobros.sprites.weapons;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class FireBall extends Sprite {
    public PlayScreen screen;
    public World world;
    private Body b2body;

    private Animation<TextureRegion> fireAnimation;
    private float stateTime;
    private boolean destroyed;
    private boolean setToDestroy;
    private boolean fireRight;

    public FireBall(PlayScreen screen, float x, float y, boolean fireRight) {
        this.fireRight = fireRight;
        this.screen = screen;
        this.world = screen.getWorld();
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for(int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("fireball"), i * 8,0,8,8));
        }
        fireAnimation = new Animation<TextureRegion>(0.2f, frames);
        setRegion(fireAnimation.getKeyFrame(0));
        setBounds(x, y,6 / MarioBros.PPM, 6 / MarioBros.PPM);
        defineFireBall();
        setToDestroy = false;
        destroyed = false;
    }

    private void defineFireBall() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(fireRight ? getX() + 12 / MarioBros.PPM : getX() - 12 / MarioBros.PPM, getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        if(!world.isLocked())
            b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(3 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.FIREBALL_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.OBJECT_BIT;

        fdef.shape = shape;
        fdef.restitution = 1;
        fdef.friction = 0;
        b2body.createFixture(fdef).setUserData(this);
        b2body.setLinearVelocity(new Vector2(fireRight ? 2 : -2, 1.5f));
        shape.dispose();
    }

    public void update(float dt) {
        stateTime += dt;
        setRegion(fireAnimation.getKeyFrame(stateTime,true));
        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        if((!world.isLocked() && b2body != null) && (stateTime > 2f || setToDestroy) && !destroyed) {
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

    @Override
    public void draw(Batch batch) {
        if(b2body != null &&  !destroyed || stateTime < 1)
            super.draw(batch);
    }

    public void setToDestroy() {
        setToDestroy = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
