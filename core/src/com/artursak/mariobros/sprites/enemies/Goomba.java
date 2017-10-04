package com.artursak.mariobros.sprites.enemies;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.overlays.Hud;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.sprites.weapons.FireBall;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Goomba extends Enemy {
    private float stateTime;
    private Animation<TextureRegion> walkAnimation;
    private boolean setToDestroy;
    private boolean destroyed;

    public Goomba(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for(int i = 0; i < 2; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("goomba"), i * 16,0,16,16));
        walkAnimation = new Animation<TextureRegion>(0.4f, frames);
        stateTime = 0;
        setBounds(getX(), getY(), 16 / MarioBros.PPM,16 / MarioBros.PPM);
        setToDestroy = false;
        destroyed = false;
    }

    public void update(float dt) {
        stateTime += dt;
        if(!world.isLocked() && b2body != null && setToDestroy && !destroyed) {
            world.destroyBody(b2body);
            b2body = null;
            destroyed = true;
            setRegion(new TextureRegion(screen.getAtlas().findRegion("goomba"), 32, 0,16,16));
            stateTime = 0;
        } else if(!destroyed) {
            assert b2body != null;
            b2body.setLinearVelocity(velocity);
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            setRegion(walkAnimation.getKeyFrame(stateTime, true));
        }
    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.ENEMY_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT
               | MarioBros.COIN_BIT
               | MarioBros.BRICK_BIT
               | MarioBros.ENEMY_BIT
               | MarioBros.OBJECT_BIT
               | MarioBros.MARIO_BIT
               | MarioBros.FIREBALL_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        PolygonShape head = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-6, 8).scl(1 / MarioBros.PPM);
        vertices[1] = new Vector2(6, 8).scl(1 / MarioBros.PPM);
        vertices[2] = new Vector2(-4, 3).scl(1 / MarioBros.PPM);
        vertices[3] = new Vector2(4, 3).scl(1 / MarioBros.PPM);
        head.set(vertices);

        fdef.shape = head;
        fdef.restitution = 0.5f;
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);
    }

    @Override
    public void draw(Batch batch) {
        if(!destroyed || stateTime < 1)
            super.draw(batch);
    }

    @Override
    public void hitOnHead(Mario mario) {
        setToDestroy = true;
        Hud.addScore(100);
    }

    @Override
    public void flamed(FireBall fireball) {
        setToDestroy = true;
        Hud.addScore(100);
        fireball.setToDestroy();
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if(enemy instanceof KoopaTroopa && ((KoopaTroopa) enemy).currentState == KoopaTroopa.State.MOVING_SHELL)
            setToDestroy = true;
        else
            reverseVelocity(true,false);
    }
    @Override
    public boolean isDead() {
        return destroyed;
    }
}
