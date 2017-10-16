package com.artursak.mariobros.sprites.tile_objects;

import com.artursak.mariobros.MarioBros;
import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.utils.BodyFactory;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;

public class FlagPole extends InteractiveTileObject {
    public static Flag flag;
    private       Body anchor;

    public FlagPole(PlayScreen screen, MapObject object) {
        super(screen, object);
        flag = null;
        anchor = null;
        setCategoryFilter(MarioBros.FLAGPOLE_BIT);
        fixture.setUserData(this);
        createAnchor();
        flag = new Flag(screen, body.getPosition().x, body.getPosition().y, this);
    }

    private void createAnchor() {
        Vector2 position = new Vector2(body.getPosition().x - 8 / MarioBros.PPM, body.getPosition().y + 64 / MarioBros.PPM);
        Vector2 boxDims  = new Vector2(16 / 2 / MarioBros.PPM, 16 / 2 / MarioBros.PPM);

        anchor = BodyFactory.getInstance().makeBody(position, boxDims, MarioBros.NOTHING_BIT, BodyDef.BodyType.StaticBody);
        for (Fixture fixture : anchor.getFixtureList())
            fixture.setUserData(this);

    }

    public static boolean isStatic() {
        return flag.staticBod;
    }

    @Override
    public void onHeadHit(Mario mario) {
        flag.pullFlag(mario);
    }

    public void update(float dt) {
        flag.update();
    }

    public void draw(Batch batch) {
        flag.draw(batch);
    }

    public class Flag extends Sprite {
        public final  PlayScreen     screen;
        public final  World          world;
        private final FlagPole       pole;
        private final TextureRegion  texture;
        public        Body           b2body;
        public        boolean        staticBod;
        private       PrismaticJoint joint;
        private       boolean        down;

        private Flag(PlayScreen screen, float x, float y, FlagPole pole) {
            this.screen = screen;
            this.world = screen.getWorld();
            this.pole = pole;
            down = false;
            staticBod = false;

            texture = new TextureRegion(screen.getAtlas().findRegion("flag"), 0, 0, 16, 16);

            setBounds(x - 16 / MarioBros.PPM, y + 30 / MarioBros.PPM, 16 / MarioBros.PPM, 16 / MarioBros.PPM);
            setRegion(texture);
            makeFlag();
        }

        private void makeFlag() {
            Vector2 position = new Vector2(getX() + 8 / MarioBros.PPM, getY() + 34 / MarioBros.PPM);
            Vector2 boxDims  = new Vector2(8 / MarioBros.PPM, 8 / MarioBros.PPM);

            b2body = BodyFactory.getInstance().makeBody(position, boxDims, MarioBros.FLAG_BIT, BodyDef.BodyType.DynamicBody);
            joint = BodyFactory.getInstance().connectPrismaticJoint(pole.anchor, b2body);
            joint.setUserData(this);

            for (Fixture fixture : b2body.getFixtureList())
                fixture.setUserData(this);

        }

        void update() {
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            setRegion(texture);
            if (!world.isLocked() && b2body.getLinearVelocity().y == 0 && down) {
                b2body.setType(BodyDef.BodyType.StaticBody);
                staticBod = true;
            }
        }

        public void pullFlag(Mario mario) {
            if (!down && !staticBod) {
                joint.setMotorSpeed(-joint.getMotorSpeed());
                down = true;
//                mario.goal();
            }
        }
    }
}
