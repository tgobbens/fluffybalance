package com.balanceball.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.balanceball.component.GravityComponent;
import com.balanceball.enity.GravityEntity;
import com.balanceball.component.PhysicsComponent;
import com.balanceball.component.PositionComponent;
import com.balanceball.component.RotationComponent;
import com.sec.Entity;
import com.sec.System;

/**
 * Created by tijs on 14/07/2017.
 */

public class PhysicsSystem extends System {

    public interface BallPointContactListener {
        void onPointContact(int type);
    }

    public final static int BODY_USER_DATA_TYPE_BALL = 0;
    public final static int BODY_USER_DATA_TYPE_POINT_LEFT = 1;
    public final static int BODY_USER_DATA_TYPE_POINT_RIGHT = 2;
    public final static int BODY_USER_DATA_TYPE_OTHER = 3;

    public static class BodyUserDate {
        public BodyUserDate(int type) {
            this.type = type;
        }

        int type = BODY_USER_DATA_TYPE_OTHER;
    }

    private final float BASE_GRAVITY = 150.f;

    private float mAccumulator = 0.f;
    private World mWorld;
    private boolean mIsPaused = false;

    private BallPointContactListener mBallPointContactListener = null;

    public PhysicsSystem(BallPointContactListener ballPointContactListener) {
        addComponentType(PhysicsComponent.class);

        mBallPointContactListener = ballPointContactListener;
    }

    public void setIsPaused(boolean isPaused) {
        mIsPaused = isPaused;
    }

    public World getWorld() {
        return mWorld;
    }

    @Override
    public void create() {
        Box2D.init();

        mWorld = new World(new Vector2(0, BASE_GRAVITY), true);
        mWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                BodyUserDate userDateA = (BodyUserDate) contact.getFixtureA().getBody().getUserData();
                BodyUserDate userDateB = (BodyUserDate) contact.getFixtureB().getBody().getUserData();

                // check if there was a contact between the left or right point
                if (userDateA != null && userDateB != null) {
                    if ((userDateA.type == BODY_USER_DATA_TYPE_POINT_LEFT && userDateB.type == BODY_USER_DATA_TYPE_BALL) ||
                            (userDateA.type == BODY_USER_DATA_TYPE_BALL && userDateB.type == BODY_USER_DATA_TYPE_POINT_LEFT)) {
                        // touched left ball
                        mBallPointContactListener.onPointContact(BODY_USER_DATA_TYPE_POINT_LEFT);

                    } else if ((userDateA.type == BODY_USER_DATA_TYPE_POINT_RIGHT && userDateB.type == BODY_USER_DATA_TYPE_BALL) ||
                            (userDateA.type == BODY_USER_DATA_TYPE_BALL && userDateB.type == BODY_USER_DATA_TYPE_POINT_RIGHT)) {
                        // touched right ball
                        mBallPointContactListener.onPointContact(BODY_USER_DATA_TYPE_POINT_RIGHT);
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    public void updateFromEntities(Array<Entity> entities) {
        // if the game is not running don't update the physics
        if (mIsPaused) {
            return;
        }

        GravityComponent gravityComponent = mEngine.getComponentFromFirstEntity(
                GravityEntity.class, GravityComponent.class);
        if (gravityComponent != null) {
            Vector2 gravity = new Vector2(gravityComponent.normalisedGravity);
            mWorld.setGravity(gravity.scl(BASE_GRAVITY));
        }

        // update object position
        for (Entity entity : entities) {
            PhysicsComponent physicsComp = entity.getComponentByType(PhysicsComponent.class);
            PositionComponent positionComp = entity.getComponentByType(PositionComponent.class);
            RotationComponent rotationComp = entity.getComponentByType(RotationComponent.class);

            if (physicsComp == null) {
                continue;
            }

            if (positionComp != null) {
                positionComp.position.set(physicsComp.body.getPosition());
            }

            if (rotationComp != null) {
                rotationComp.degree = physicsComp.body.getAngle() * MathUtils.radDeg;
            }
        }

        // some constants taken from documentation, could be tweaked
        final float timeStep = 1.f / 45.f;
        final int velocityIterations = 6;
        final int positionIterations = 2;

        float deltaTime = Gdx.graphics.getDeltaTime();

        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        mAccumulator += frameTime;
        while (mAccumulator >= timeStep) {
            mWorld.step(timeStep, velocityIterations, positionIterations);
            mAccumulator -= timeStep;
        }
    }

    @Override
    public void dispose() {
        mWorld.dispose();
    }
}
