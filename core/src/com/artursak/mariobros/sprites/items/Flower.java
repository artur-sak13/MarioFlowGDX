package com.artursak.mariobros.sprites.items;

import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.badlogic.gdx.math.Vector2;

public class Flower extends Item {

    public Flower(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        setRegion(screen.getAtlas().findRegion("flower"), 0, 0, 16, 16);
        velocity = new Vector2(0, 0);
    }

    @Override
    public void use(Mario mario) {
        destroy();

    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (body != null) {
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
            velocity.y = body.getLinearVelocity().y;
            body.setLinearVelocity(velocity);
        }
    }
}
