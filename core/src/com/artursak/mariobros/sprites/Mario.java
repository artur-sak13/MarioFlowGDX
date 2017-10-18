package com.artursak.mariobros.sprites;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.enemies.Enemy;
import com.artursak.mariobros.sprites.enemies.KoopaTroopa;
import com.artursak.mariobros.sprites.weapons.FireBall;
import com.artursak.mariobros.utils.BodyFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Mario extends Sprite {
    private final float NORMAL_FORCE          = 0.1f;
    private final float MAX_SPEED             = 2f;
    private final float ACCELERATED_FORCE     = 0.18f;
    private final float ACCELERATED_MAX_SPEED = 4f;
    private final World                    world;
    private       float                    flagPosition;
    private final TextureRegion            marioStand;
    private final TextureRegion            marioBrake;
    private final Animation<TextureRegion> marioRun;
    private final Animation<TextureRegion> marioClimb;
    private final TextureRegion            marioJump;
    private final TextureRegion            marioDead;
    private final TextureRegion            bigMarioStand;
    private final TextureRegion            bigMarioBrake;
    private final TextureRegion            bigMarioJump;
    private final Animation<TextureRegion> bigMarioRun;
    private final Animation<TextureRegion> growMario;
    private final Animation<TextureRegion> shrinkMario;
    private final PlayScreen               screen;
    private final Array<FireBall>          fireballs;
    public        State                    currentState;
    public        Body                     b2body;
    private       State                    previousState;
    private       float                    stateTimer;
    private       boolean                  runningRight;
    private       boolean                  marioIsBig;
    private       boolean                  completeLevel;
    private       boolean                  isDead;
    private       boolean                  brake;
    private       boolean                  grow;
    private       boolean                  shrink;
    private       boolean                  climb;
    private       boolean                  marioWins;

    public Mario(PlayScreen screen) {
        this.screen = screen;
        this.world = screen.getWorld();

        marioStand = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 0, 0, 16, 16);
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32);

        marioJump = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 80, 0, 16, 16);
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 80, 0, 16, 32);

        marioBrake = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 64, 0, 16, 16);
        bigMarioBrake = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 64, 0, 16, 32);

        marioBrake.flip(true, false);
        bigMarioBrake.flip(true, false);

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        marioRun = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        for (int i = 1; i < 4; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        bigMarioRun = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        for (int i = 0; i < 5; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        }
        growMario = new Animation<TextureRegion>(0.15f, frames);

        frames.clear();
        for (int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        }
        shrinkMario = new Animation<TextureRegion>(0.15f, frames);

        frames.clear();
        for (int i = 7; i < 9; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        marioClimb = new Animation<TextureRegion>(0.1f, frames);

        marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 96, 0, 16, 16);

        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;

        runningRight = true;
        grow = false;
        brake = false;
        shrink = false;
        climb = false;
        marioIsBig = false;
        isDead = false;
        completeLevel = false;
        marioWins = false;

        setBounds(0, 0, 16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setRegion(marioStand);

        createMario();

        fireballs = new Array<FireBall>();
    }

    private void createMario() {
        Vector2 position;
        if (grow || shrink) {
            position = (grow) ? b2body.getPosition().add(0, 10 / MarioBros.PPM) : b2body.getPosition().sub(0, 10 / MarioBros.PPM);
            world.destroyBody(b2body);
            b2body = null;
        } else
            position = new Vector2(2900 / MarioBros.PPM, 32 / MarioBros.PPM);

        b2body = (grow)
                 ? BodyFactory.getInstance().makeBody(position, 6f, -14, MarioBros.MARIO_BIT)
                 : BodyFactory.getInstance().makeBody(position, 6f, MarioBros.MARIO_BIT);

        for (Fixture fixture : b2body.getFixtureList())
            fixture.setUserData(this);

        if (grow)
            grow = !grow;
        else if (shrink)
            shrink = !shrink;

    }

    public void update(float dt) {
        if (completeLevel)
            finishLevel();
        if (marioWins && stateTimer > 0.75f)
            setColor(0, 0, 0, 0);

        if ((screen.getHud().isTimeUp() || b2body.getPosition().y < 0f) && !isDead()) {
            die();
        }

        if (!isDead && !marioWins && !climb) {
            handleInput();
        }

        if (marioIsBig)
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2 - 6 / MarioBros.PPM);
        else
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);

        setRegion(getFrame(dt));


        for (FireBall ball : fireballs) {
            ball.update(dt);
            if (ball.isDestroyed())
                fireballs.removeValue(ball, true);
        }
    }

    public boolean isDead() {
        return isDead;
    }

    private void die() {
        if (!isDead()) {
            isDead = true;
            Filter filter = new Filter();
            filter.maskBits = MarioBros.NOTHING_BIT;
            for (Fixture fixture : b2body.getFixtureList())
                fixture.setFilterData(filter);
            b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
        }
    }

    private void walkRight() {
        runningRight = true;
        b2body.applyLinearImpulse(new Vector2(NORMAL_FORCE, 0), b2body.getWorldCenter(), true);
    }

    private void handleInput() {
        float maxSpeed = MAX_SPEED;
        float force    = NORMAL_FORCE;

        if (Gdx.input.isKeyPressed(Input.Keys.X) && currentState != State.JUMPING) {
            maxSpeed = ACCELERATED_MAX_SPEED;
            force = ACCELERATED_FORCE;
        }

        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)))
            jump();
        else if ((Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) && b2body.getLinearVelocity().x <= maxSpeed) {
            b2body.applyLinearImpulse(new Vector2(force, 0.0f), b2body.getWorldCenter(), true);
            if (b2body.getLinearVelocity().x <= -force || (currentState == State.BRAKING && b2body.getLinearVelocity().x <= 0))
                brake = true;
        } else if ((Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) && b2body.getLinearVelocity().x >= -maxSpeed) {
            b2body.applyLinearImpulse(new Vector2(-force, 0.0f), b2body.getWorldCenter(), true);
            if (b2body.getLinearVelocity().x >= force || (currentState == State.BRAKING && b2body.getLinearVelocity().x >= 0))
                brake = true;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            fire();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.G) && !marioIsBig && !shrink)
            grow();
        else if (Gdx.input.isKeyJustPressed(Input.Keys.G) && marioIsBig && !grow)
            shrink();
        else if (Gdx.input.isKeyJustPressed(Input.Keys.C) && !climb)
            climb = true;
    }

    private TextureRegion getFrame(float dt) {
        currentState = getState();
        TextureRegion region;
        Filter        filter;

        switch (currentState) {
            case DYING:
                region = marioDead;
                break;
            case GROWING:
                setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - 6 / MarioBros.PPM);
                region = growMario.getKeyFrame(stateTimer, false);
                if (growMario.isAnimationFinished(stateTimer)) {
                    createMario();
                }
                break;
            case SHRINKING:
                setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - 22 / MarioBros.PPM);
                region = shrinkMario.getKeyFrame(stateTimer, false);
                filter = new Filter();
                filter.maskBits = MarioBros.GROUND_BIT | MarioBros.ITEM_BIT | MarioBros.OBJECT_BIT;

                for (Fixture fixture : b2body.getFixtureList())
                    fixture.setFilterData(filter);
                if (shrinkMario.isAnimationFinished(stateTimer)) {
                    createMario();
                }
                break;
            case CLIMBING:
                region = marioClimb.getKeyFrame(stateTimer, false);
                filter = new Filter();
                filter.maskBits = MarioBros.GROUND_BIT | MarioBros.ITEM_BIT | MarioBros.OBJECT_BIT;

                for (Fixture fixture : b2body.getFixtureList())
                    fixture.setFilterData(filter);

                if (stateTimer > 2.5f) {
                    climb = false;
                    b2body.setType(BodyDef.BodyType.DynamicBody);
                }
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump : marioJump;
                break;
            case RUNNING:
                region = marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
                break;
            case BRAKING:
                region = marioIsBig ? bigMarioBrake : marioBrake;
                break;
            case WINNING:
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;
        }

        if ((b2body.getLinearVelocity().x <= -0.01f || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        } else if ((b2body.getLinearVelocity().x > 0.01 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    private void jump() {
        if (currentState != State.JUMPING) {
            b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
            currentState = State.JUMPING;
        }
    }

    private void fire() {
        if (b2body != null)
            fireballs.add(new FireBall(screen, b2body.getPosition().x, b2body.getPosition().y, runningRight));
    }

    public void grow() {
        if (!marioIsBig) {
            grow = true;
            marioIsBig = true;
            setBounds(getX(), getY(), getWidth(), getHeight() * 2);
        }
    }

    private void shrink() {
        if (marioIsBig) {
            marioIsBig = false;
            shrink = true;
            setBounds(getX(), getY(), getWidth(), getHeight() / 2);
        }
    }

    private State getState() {
        if (isDead)
            return State.DYING;
        else if (marioWins)
            return State.WINNING;
        else if (grow)
            return State.GROWING;
        else if (shrink)
            return State.SHRINKING;
        else if (climb)
            return State.CLIMBING;
        else if (isJumping())
            return State.JUMPING;
        else if (b2body.getLinearVelocity().y < 0)
            return State.FALLING;
        else if (brake) {
            brake = false;
            return State.BRAKING;
        } else
            return (b2body.getLinearVelocity().x != 0) ? State.RUNNING : State.STANDING;
    }

    private boolean isJumping() {
        return ((b2body.getLinearVelocity().y > 0 && currentState == State.JUMPING) || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING));
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public boolean isBig() {
        return marioIsBig;
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof KoopaTroopa && ((KoopaTroopa) enemy).currentState == KoopaTroopa.State.STANDING_SHELL)
            ((KoopaTroopa) enemy).kick(getX() <= enemy.getX() ? KoopaTroopa.KICK_RIGHT : KoopaTroopa.KICK_LEFT);
        else {
            if (marioIsBig) {
                shrink();
            } else {
                die();
            }
        }
    }

    public void finishLevel() {
        if (climb) {
            if (b2body.getPosition().y <= 60 / MarioBros.PPM) {
                runningRight = false;
                b2body.setTransform(flagPosition + 14 / MarioBros.PPM, b2body.getPosition().y, 0);
                b2body.setType(BodyDef.BodyType.StaticBody);
            } else {
                runningRight = true;
                b2body.setTransform(flagPosition + 4 / MarioBros.PPM, b2body.getPosition().y, 0);
            }

            b2body.setLinearVelocity(new Vector2(0, -0.70f));
        } else {
            if (getX() < 32.75f)
                b2body.applyLinearImpulse(new Vector2(b2body.getMass() * (1.0f - b2body.getLinearVelocity().x), 0.0f), b2body.getWorldCenter(), true);
            else {
                marioWins = true;
            }
        }

    }

    public void goal(float flagPosition) {
        climb = true;
        completeLevel = true;
        this.flagPosition = flagPosition;
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        for (FireBall ball : fireballs)
            if (ball != null)
                ball.draw(batch);
    }

    public enum State {
        STANDING,
        RUNNING,
        JUMPING,
        CROUCHING,
        FALLING,
        GROWING,
        SHRINKING,
        BRAKING,
        CLIMBING,
        DYING,
        WINNING
    }

}
