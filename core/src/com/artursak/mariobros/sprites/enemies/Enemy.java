package com.artursak.mariobros.sprites.enemies;

import com.artursak.mariobros.screens.PlayScreen;
import com.artursak.mariobros.sprites.Mario;
import com.artursak.mariobros.sprites.weapons.FireBall;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Enemy extends Sprite {
    protected World world;
    protected PlayScreen screen;
    public Body b2body;
    Vector2 velocity;

    Enemy(PlayScreen screen, float x, float y) {
        this.world = screen.getWorld();
        this.screen = screen;
        setPosition(x, y);
        defineEnemy();
        velocity = new Vector2(-1,-2);
        b2body.setActive(false);
    }

    protected abstract void defineEnemy();
    public abstract void update(float dt);
    public abstract void hitOnHead(Mario mario);
    public abstract void flamed(FireBall fireball);
    public abstract void onEnemyHit(Enemy enemy);
    public abstract boolean isDead();

    public void reverseVelocity(boolean x, boolean y) {
        if(x)
            velocity.x = -velocity.x;
        if(y)
            velocity.y = -velocity.y;
    }
}
