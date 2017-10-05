package com.artursak.mariobros.sprites;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.enemies.Enemy;
import com.artursak.mariobros.sprites.enemies.KoopaTroopa;
import com.artursak.mariobros.sprites.weapons.FireBall;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Mario extends Sprite {
    public enum State { FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD, WIN };
    public State currentState;
    private State previousState;

    public World world;
    public Body b2body;

    private TextureRegion marioStand;
    private Animation<TextureRegion> marioRun;
    private TextureRegion marioJump;
    private TextureRegion marioDead;
    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private Animation<TextureRegion> bigMarioRun;
    private Animation<TextureRegion> growMario;


    private float stateTimer;
    private boolean runningRight;
    private boolean marioIsBig;
    private boolean runGrowAnimation;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;
    private boolean marioIsDead;
    private boolean marioWins;
    private PlayScreen screen;

    private Array<FireBall> fireballs;


    public Mario(PlayScreen screen) {
        this.screen = screen;
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        Array<TextureRegion> frames = new Array<TextureRegion>();

        for(int i = 1; i < 4; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        marioRun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for(int i = 1; i < 4; i++)
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        bigMarioRun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240,0,16,32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0,0,16,32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240,0,16,32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0,0,16,32));
        growMario = new Animation<TextureRegion>(0.2f, frames);
        frames.clear();

        marioJump = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 80,0,16,16);
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 80,0,16,32);

        marioStand = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 0,0,16,16);
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0,0,16,32);

        marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 96,0,16,16);

        timeToDefineBigMario = false;
        timeToRedefineMario = false;
        marioWins = false;

        createMario();

        setBounds(0,0,16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setRegion(marioStand);

        fireballs = new Array<FireBall>();
    }

    public void update(float dt) {
        if((screen.getHud().isTimeUp() || b2body.getPosition().y < 0) && !isDead()) {
            die();
        }

        if(marioIsBig)
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2 - 6 / MarioBros.PPM);
        else
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);

        setRegion(getFrame(dt));

        if(timeToDefineBigMario || timeToRedefineMario)
            createMario();

        for(FireBall ball : fireballs) {
            ball.update(dt);
            if(ball.isDestroyed())
                fireballs.removeValue(ball, true);
        }
    }

    private TextureRegion getFrame(float dt) {
        currentState = getState();
        TextureRegion region;

        switch (currentState) {
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer);
                if(growMario.isAnimationFinished(stateTimer)) {
                    runGrowAnimation = false;
                }
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump : marioJump;
                break;
            case RUNNING:
                region = marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
                break;
            case WIN:
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;
        }

        if((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        } else if((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    private State getState() {
        if(marioIsDead)
            return State.DEAD;
        else if(marioWins)
            return State.WIN;
        else if(runGrowAnimation)
            return State.GROWING;
        else if((b2body.getLinearVelocity().y > 0 && currentState == State.JUMPING) || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING))
            return State.JUMPING;
        else if(b2body.getLinearVelocity().y < 0)
            return State.FALLING;
        else if(b2body.getLinearVelocity().x != 0)
            return State.RUNNING;
        else
            return State.STANDING;
    }

    public void grow() {
        if(!marioIsBig) {
            runGrowAnimation = true;
            marioIsBig = true;
            timeToDefineBigMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() * 2);
        }
    }

    private void die() {
        if(!marioIsDead) {
            marioIsDead = true;
            Filter filter = new Filter();
            filter.maskBits = MarioBros.NOTHING_BIT;

            for(Fixture fixture : b2body.getFixtureList())
                fixture.setFilterData(filter);
            b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
        }
    }

    private boolean isDead() {
        return marioIsDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public boolean isBig() {
        return marioIsBig;
    }

    public void jump() {
        if(currentState != State.JUMPING) {
            b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
            currentState = State.JUMPING;
        }
    }

    public void hit(Enemy enemy) {
        if(enemy instanceof KoopaTroopa && ((KoopaTroopa) enemy).currentState == KoopaTroopa.State.STANDING_SHELL)
            ((KoopaTroopa) enemy).kick(getX() <=  enemy.getX() ? KoopaTroopa.KICK_RIGHT : KoopaTroopa.KICK_LEFT);
        else {
            if (marioIsBig) {
                marioIsBig = false;
                timeToRedefineMario = true;
                setBounds(getX(), getY(), getWidth(), getHeight() / 2);
            }
               die();
        }
    }

    private void createMario() {
        Vector2 position;
        if(timeToDefineBigMario || timeToRedefineMario) {
            position = b2body.getPosition();
            world.destroyBody(b2body);
            b2body = null;
        } else
            position = new Vector2(32 / MarioBros.PPM, 32 / MarioBros.PPM);

        BodyDef bdef = new BodyDef();
        bdef.position.set((timeToDefineBigMario) ? position.add(0,10 / MarioBros.PPM) : position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);

        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT
                | MarioBros.COIN_BIT
                | MarioBros.BRICK_BIT
                | MarioBros.ENEMY_BIT
                | MarioBros.OBJECT_BIT
                | MarioBros.ENEMY_HEAD_BIT
                | MarioBros.ITEM_BIT
                | MarioBros.FLAGPOLE_BIT
                | MarioBros.FLAG_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        if(timeToDefineBigMario) {
            shape.setPosition(new Vector2(0, -14 / MarioBros.PPM));
            b2body.createFixture(fdef).setUserData(this);
        }

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData(this);

        if(timeToDefineBigMario)
            timeToDefineBigMario = !timeToDefineBigMario;
        else if(timeToRedefineMario)
            timeToRedefineMario = !timeToRedefineMario;

        shape.dispose();
        head.dispose();
    }

    public void fire() {
        if(b2body != null)
            fireballs.add(new FireBall(screen, b2body.getPosition().x, b2body.getPosition().y, runningRight));
    }

    public void goal() {
        marioWins = true;
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        for(FireBall ball : fireballs)
            if(ball != null)
                ball.draw(batch);
    }
}
