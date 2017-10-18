package com.artursak.mariobros.utils;

import com.artursak.mariobros.MarioBros;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;

public class BodyFactory {
    private static final    short       BASE_MASK     = MarioBros.GROUND_BIT
                                                        | MarioBros.COIN_BIT
                                                        | MarioBros.BRICK_BIT
                                                        | MarioBros.OBJECT_BIT;
    private static final    short       ENEMY_MASK    = BASE_MASK
                                                        | MarioBros.MARIO_BIT
                                                        | MarioBros.ENEMY_BIT
                                                        | MarioBros.ENEMY_HEAD_BIT
                                                        | MarioBros.FIREBALL_BIT;
    private static final    short       MARIO_MASK    = BASE_MASK
                                                        | MarioBros.ITEM_BIT
                                                        | MarioBros.ENEMY_BIT
                                                        | MarioBros.ENEMY_HEAD_BIT
                                                        | MarioBros.FLAGPOLE_BIT;

    private static final    short       FIREBALL_MASK = BASE_MASK
                                                        | MarioBros.ENEMY_BIT
                                                        | MarioBros.ENEMY_HEAD_BIT;
    private static final    short       ITEM_MASK     = BASE_MASK | MarioBros.MARIO_BIT;
    private static volatile BodyFactory thisInstance  = null;
    private World world;


    private BodyFactory() {
    }

    public static BodyFactory getInstance() {
        synchronized (BodyFactory.class) {
            if (thisInstance == null)
                thisInstance = new BodyFactory();
            return thisInstance;
        }
    }

    public void init(World world) {
        this.world = world;
    }

    public Body makeBody(Vector2 position, float radius, int offset_y, short catBit) {
        Body body = makeBody(position, BodyDef.BodyType.DynamicBody);
        body = makeCircularFixture(body, radius, (float) offset_y, catBit);
        return makeContactSensor(body, MarioBros.MARIO_HEAD_BIT);
    }

    private Body makeBody(Vector2 position, BodyDef.BodyType type) {
        BodyDef bdef = new BodyDef();
        bdef.type = type;
        bdef.position.set(position);
        return world.createBody(bdef);
    }

    private Body makeCircularFixture(Body body, float radius, float offset_y, short catBit) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius / MarioBros.PPM);
        body.createFixture(makeFixture(catBit, shape));
        shape.setPosition(new Vector2(0, offset_y / MarioBros.PPM));
        body.createFixture(makeFixture(catBit, shape));
        shape.dispose();
        return body;
    }

    private Body makeContactSensor(Body body, short catBit) {
        return makeEdgeFixture(body, catBit);
    }

    private FixtureDef makeFixture(short catBit, Shape shape) {
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        if (catBit != MarioBros.NOTHING_BIT)
            fdef.filter.categoryBits = catBit;
        switch (catBit) {
            case MarioBros.MARIO_BIT:
                fdef.filter.maskBits = MARIO_MASK;
                break;
            case MarioBros.MARIO_HEAD_BIT:
                fdef.filter.maskBits = MARIO_MASK;
                fdef.isSensor = true;
                break;
            case MarioBros.ENEMY_BIT:
                fdef.filter.maskBits = ENEMY_MASK;
                break;
            case MarioBros.ITEM_BIT:
                fdef.filter.maskBits = ITEM_MASK;
                break;
            case MarioBros.FIREBALL_BIT:
                fdef.filter.maskBits = FIREBALL_MASK;
                fdef.restitution = 1;
                fdef.friction = 0;
                break;
            default:
                break;
        }

        return fdef;
    }

    private Body makeEdgeFixture(Body body, short catBit) {
        EdgeShape shape = new EdgeShape();
        shape.set(new Vector2(-2, 6).scl(1 / MarioBros.PPM), new Vector2(2, 6).scl(1 / MarioBros.PPM));
        body.createFixture(makeFixture(catBit, shape));
        shape.dispose();
        return body;
    }

    public Body makeBody(Vector2 position, float radius, float restitution, short catBit) {
        Body body = makeBody(position, radius, catBit);

        if (catBit == MarioBros.ENEMY_BIT)
            body = makeContactSensor(body, restitution, MarioBros.ENEMY_HEAD_BIT);

        return body;
    }

    public Body makeBody(Vector2 position, float radius, short catBit) {
        Body body = makeBody(position, BodyDef.BodyType.DynamicBody);
        body = makeCircularFixture(body, radius, catBit);
        if (catBit == MarioBros.MARIO_BIT)
            body.setLinearDamping(0.75f);
        body = makeContactSensor(body, MarioBros.MARIO_HEAD_BIT);

        return body;
    }

    private Body makeContactSensor(Body body, float restitution, short catBit) {
        return makePolyFixture(body, restitution, catBit);
    }

    private Body makeCircularFixture(Body body, float radius, short catBit) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius / MarioBros.PPM);
        body.createFixture(makeFixture(catBit, shape));
        shape.dispose();
        return body;
    }

    private Body makePolyFixture(Body body, float restitution, short catBit) {
        PolygonShape shape = new PolygonShape();

        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-7, 8).scl(1 / MarioBros.PPM);
        vertices[1] = new Vector2(7, 8).scl(1 / MarioBros.PPM);
        vertices[2] = new Vector2(-5, 3).scl(1 / MarioBros.PPM);
        vertices[3] = new Vector2(5, 3).scl(1 / MarioBros.PPM);
        shape.set(vertices);

        FixtureDef fdef = makeFixture(catBit, shape);
        fdef.restitution = restitution;
        body.createFixture(fdef);

        shape.dispose();
        return body;
    }

    public Body makeBody(Vector2 position, Vector2 boxDims, short catBit, BodyDef.BodyType type) {
        Body body = makeBody(position, type);
        return makePolyFixture(body, boxDims, catBit);
    }

    private Body makePolyFixture(Body body, Vector2 boxDims, short catBit) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxDims.x, boxDims.y);
        body.createFixture(makeFixture(catBit, shape));
        shape.dispose();
        return body;
    }

}
