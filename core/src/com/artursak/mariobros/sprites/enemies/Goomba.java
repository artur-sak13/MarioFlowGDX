package com.artursak.mariobros.sprites.enemies;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.overlays.Hud;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.sprites.weapons.FireBall;
import com.artursak.mariobros.utils.BodyFactory;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;

public class Goomba extends Enemy {
    private final Animation<TextureRegion> walkAnimation;
    private       float                    stateTime;
    private       boolean                  setToDestroy;
    private       boolean                  destroyed;

    public Goomba(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 2; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("goomba"), i * 16, 0, 16, 16));
        walkAnimation = new Animation<TextureRegion>(0.4f, frames);
        stateTime = 0;
        setBounds(getX(), getY(), 16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setToDestroy = false;
        destroyed = false;
    }

    @Override
    protected void defineEnemy() {
        b2body = BodyFactory.getInstance().makeBody(new Vector2(getX(), getY()), 6f, 0.5f, MarioBros.ENEMY_BIT);
        for (Fixture fixture : b2body.getFixtureList())
            fixture.setUserData(this);

    }

    public void update(float dt) {
        stateTime += dt;
        if (!world.isLocked() && b2body != null && setToDestroy && !destroyed) {
            world.destroyBody(b2body);
            b2body = null;
            destroyed = true;
            setRegion(new TextureRegion(screen.getAtlas().findRegion("goomba"), 32, 0, 16, 16));
            stateTime = 0;
        } else if (!destroyed) {
            assert b2body != null;
            b2body.setLinearVelocity(velocity);
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            setRegion(walkAnimation.getKeyFrame(stateTime, true));
        }
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
        if (enemy instanceof KoopaTroopa && ((KoopaTroopa) enemy).currentState == KoopaTroopa.State.MOVING_SHELL)
            setToDestroy = true;
        else
            reverseVelocity(true, false);
    }

    @Override
    public boolean isDead() {
        return destroyed;
    }

    @Override
    public void draw(Batch batch) {
        if (!destroyed || stateTime < 1)
            super.draw(batch);
    }
}
