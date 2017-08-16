package com.balanceball.enity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.balanceball.component.PhysicsComponent;
import com.balanceball.component.RotationComponent;
import com.balanceball.system.PhysicsSystem;
import com.balanceball.component.PositionComponent;
import com.balanceball.component.SizeComponent;
import com.balanceball.component.TextureComponent;
import com.sec.Entity;

/**
 * Created by tijs on 14/07/2017.
 */

public class BallEntity extends Entity {

    private final float RADIUS = 26.f;
    private final float HEIGHT_START_OFFSET = 80.f;

    private final float BALL_FRICTION_BASE = 0.4f;
    private final float BALL_FRICTION_DECREASE = 0.02f;
    private final float BALL_FRICTION_MIN = 0.05f;

    private Vector2 mBallStartPosition;

    public BallEntity(int worldWidth, int stickHandleHeight) {
        mBallStartPosition = new Vector2(worldWidth * 0.5f, stickHandleHeight + HEIGHT_START_OFFSET);

        addComponent(new PositionComponent());
        addComponent(new SizeComponent());
        addComponent(new RotationComponent());
        addComponent(new TextureComponent());
        addComponent(new PhysicsComponent());
    }

    @Override
    public void create() {
        getComponentByType(TextureComponent.class).textureRegion = new TextureRegion(
                new Texture(Gdx.files.internal("ball.png")));

        SizeComponent sizeComponent = getComponentByType(SizeComponent.class);
        sizeComponent.width = RADIUS * 2.f;
        sizeComponent.height = RADIUS * 2.f;

        // create physics body
        PhysicsComponent physicsComponent = getComponentByType(PhysicsComponent.class);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(mBallStartPosition.x, mBallStartPosition.y);

        CircleShape ballShape = new CircleShape();
        ballShape.setRadius(RADIUS - 2.f);

        PhysicsSystem physicsSystem = mEngine.getFirstSystemOfType(PhysicsSystem.class);
        physicsComponent.body = physicsSystem.getWorld().createBody(bodyDef);
        physicsComponent.body.setUserData(new PhysicsSystem.BodyUserDate(PhysicsSystem.BODY_USER_DATA_TYPE_BALL));
        Fixture fixture = physicsComponent.body.createFixture(ballShape, 0.5f);
        fixture.setFriction(BALL_FRICTION_BASE);
        fixture.setRestitution(0.4f);

        ballShape.dispose();
    }

    @Override
    public void update() {
        if (checkForGameOver()) {
            mEngine.getFirstEntityOfType(GameStateEntity.class).onGameOver();
        }
    }

    @Override
    public void dispose() {

    }

    public void onGameStart() {
        PhysicsComponent physicsComponent = getComponentByType(PhysicsComponent.class);
        physicsComponent.body.setTransform(mBallStartPosition.x, mBallStartPosition.y, 0);
        physicsComponent.body.setLinearVelocity(MathUtils.randomBoolean() ? -10.f : 10.f, 0.f);
        physicsComponent.body.setAngularVelocity(0.f);

        PositionComponent positionComponent = getComponentByType(PositionComponent.class);
        positionComponent.position.set(mBallStartPosition.x, mBallStartPosition.y);
    }

    public void onScoreChanged(int score) {
        final float friction = MathUtils.clamp(
                BALL_FRICTION_BASE - (BALL_FRICTION_DECREASE * score), BALL_FRICTION_MIN, 1.f);

        PhysicsComponent physicsComponent = getComponentByType(PhysicsComponent.class);
        physicsComponent.body.getFixtureList().get(0).setFriction(friction);
    }

    public boolean checkForGameOver() {
        GameWorldEntity gameWorldEntity = mEngine.getFirstEntityOfType(GameWorldEntity.class);
        PhysicsComponent physicsComponent = getComponentByType(PhysicsComponent.class);
        Vector2 position = physicsComponent == null ? null : physicsComponent.body.getPosition();

        if (position != null && gameWorldEntity != null) {
            return position.x < 0 ||
                    position.x > gameWorldEntity.getWorldWidth() ||
                    position.y < 0 ||
                    position.y > gameWorldEntity.getWorldHeight();
        }
        return true;
    }
}
