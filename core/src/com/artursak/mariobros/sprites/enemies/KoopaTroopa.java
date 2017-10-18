package com.artursak.mariobros.sprites.enemies;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.overlays.Hud;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.sprites.weapons.FireBall;
import com.artursak.mariobros.utils.BodyFactory;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;

public class KoopaTroopa extends Enemy {
    public static final int KICK_LEFT  = -2;
    public static final int KICK_RIGHT = 2;
    private final Animation<TextureRegion> walkAnimation;
    private final TextureRegion            shell;
    public        State                    currentState;
    private       State                    previousState;
    private       float                    stateTime;

    public KoopaTroopa(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        Array<TextureRegion> frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("koopa"), 0, 0, 16, 24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("koopa"), 16, 0, 16, 24));
        shell = new TextureRegion(screen.getAtlas().findRegion("koopa"), 64, 0, 16, 24);

        walkAnimation = new Animation<TextureRegion>(0.2f, frames);
        currentState = previousState = State.WALKING;

        setBounds(getX(), getY(), 16 / MarioBros.PPM, 24 / MarioBros.PPM);
    }

    @Override
    protected void defineEnemy() {
        b2body = BodyFactory.getInstance().makeBody(new Vector2(getX(), getY()), 7f, 1.8f, MarioBros.ENEMY_BIT);

        for (Fixture fixture : b2body.getFixtureList())
            fixture.setUserData(this);
    }

    @Override
    public void update(float dt) {
        if (currentState == State.STANDING_SHELL && stateTime > 5) {
            currentState = State.WALKING;
            velocity.x = 1;
        }
//        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - 8 / MarioBros.PPM);
        b2body.setLinearVelocity(velocity);
        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - 8 / MarioBros.PPM);
        setRegion(getFrame(dt));

    }

    @Override
    public void hitOnHead(Mario mario) {
        if (currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        } else
            kick(mario.getX() <= getX() ? KICK_RIGHT : KICK_LEFT);
    }

    @Override
    public void flamed(FireBall fireball) {
        if (currentState == State.WALKING) {
            Hud.addScore(100);
            killed();
        }
        fireball.setToDestroy();
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        reverseVelocity(true, false);
//        if(enemy instanceof KoopaTroopa) {
//            if(((KoopaTroopa) enemy).currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL) {
//                killed();
//            } else if(currentState == State.MOVING_SHELL && ((KoopaTroopa) enemy).currentState == State.WALKING)
//                return;
//            else
//                reverseVelocity(true, false);
//        } else if(currentState != State.MOVING_SHELL)
//            reverseVelocity(true, false);
    }

    @Override
    public boolean isDead() {
        return (currentState == State.DEAD);
    }

    private TextureRegion getFrame(float dt) {
        TextureRegion region;

        switch (currentState) {
            case STANDING_SHELL:
            case MOVING_SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if (velocity.x > 0 && !region.isFlipX()) {
            region.flip(true, false);
        }
        if (velocity.x < 0 && region.isFlipX()) {
            region.flip(true, false);
        }
        stateTime = currentState == previousState ? stateTime + dt : 0;
        previousState = currentState;
        return region;
    }

    private void killed() {
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;

        for (Fixture fixture : b2body.getFixtureList())
            fixture.setFilterData(filter);
        b2body.applyLinearImpulse(new Vector2(0, 5f), b2body.getWorldCenter(), true);

    }

    public void kick(int direction) {
        velocity.x = direction;
        currentState = State.MOVING_SHELL;
    }

    public enum State {WALKING, STANDING_SHELL, MOVING_SHELL, DEAD}
}
