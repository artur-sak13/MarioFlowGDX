package com.artursak.mariobros.sprites.tile_objects;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;

public class FlagPole extends InteractiveTileObject {
    private Flag flag;
    private Body anchor;
    public static class Flag extends Sprite {
        public PlayScreen screen;
        public World world;

        private Body b2body;
        private FlagPole pole;
        private PrismaticJoint joint;
        private static TextureRegion texture;
        private boolean down;
        public static boolean staticBod;

        private Flag(PlayScreen screen, float x, float y, FlagPole pole) {
            this.screen = screen;
            this.world = screen.getWorld();
            this.pole = pole;
            down = false;
            staticBod = false;

            texture = new TextureRegion(screen.getAtlas().findRegion("flag"), 0,0,16,16);

            setBounds(x - 16 / MarioBros.PPM, y + 30 / MarioBros.PPM, 16 / MarioBros.PPM, 16 / MarioBros.PPM);
            setRegion(texture);
            makeFlag();
        }

        private void makeFlag() {
            BodyDef bdef = new BodyDef();
            bdef.position.set(getX() + 8 / MarioBros.PPM, getY() + 34 / MarioBros.PPM);
            bdef.type = BodyDef.BodyType.DynamicBody;
            if(!world.isLocked())
                b2body = world.createBody(bdef);

            FixtureDef fdef = new FixtureDef();
            PolygonShape shape = new PolygonShape();

            shape.setAsBox(16 / 2 / MarioBros.PPM, 16 / 2 / MarioBros.PPM);

            fdef.shape = shape;
            fdef.filter.categoryBits = MarioBros.FLAG_BIT;
            b2body.createFixture(fdef).setUserData(this);

            shape.dispose();

            PrismaticJointDef pDef = new PrismaticJointDef();
            pDef.enableMotor = true;
            pDef.localAxisA.set(0,1);
            pDef.motorSpeed = 1;
            pDef.maxMotorForce = 500;
            pDef.bodyA = pole.anchor;
            pDef.bodyB = b2body;
            pDef.collideConnected = false;

            pDef.enableLimit = true;
            pDef.upperTranslation = 0 / MarioBros.PPM;
            pDef.lowerTranslation = -130 / MarioBros.PPM;
            if(!world.isLocked())
                joint = ((PrismaticJoint) world.createJoint(pDef));

        }
        public void update(float dt) {
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            setRegion(texture);
            if(!world.isLocked() && b2body.getLinearVelocity().y == 0 && down) {
                b2body.setType(BodyDef.BodyType.StaticBody);
                staticBod = true;
            }

        }

        public void pullFlag(Mario mario) {
            if(!down) {
                joint.setMotorSpeed(-joint.getMotorSpeed());
                down = true;
//                mario.goal();
            }
        }
    }
    public FlagPole(PlayScreen screen, MapObject object) {
        super(screen, object);
        flag = null;
        anchor = null;
        setCategoryFilter(MarioBros.FLAGPOLE_BIT);
        fixture.setUserData(this);
        createAnchor();
        flag = new Flag(screen, body.getPosition().x, body.getPosition().y, this);
    }

    @Override
    public void onHeadHit(Mario mario) {
        flag.pullFlag(mario);
    }

    private void createAnchor() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(body.getPosition().x - 8 / MarioBros.PPM, body.getPosition().y + 64 / MarioBros.PPM);
        bdef.type = BodyDef.BodyType.StaticBody;
        anchor = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        shape.setAsBox(16 / 2 / MarioBros.PPM, 16 / 2 / MarioBros.PPM);

        fdef.shape = shape;
        anchor.createFixture(fdef).setUserData(this);

        shape.dispose();
    }
    public void update(float dt) {
        flag.update(dt);
    }

    public void draw(Batch batch) {
        flag.draw(batch);
    }
}
