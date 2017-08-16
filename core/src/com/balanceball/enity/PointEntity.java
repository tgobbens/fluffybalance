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
import com.balanceball.component.PositionComponent;
import com.balanceball.component.RotationComponent;
import com.balanceball.component.SizeComponent;
import com.balanceball.component.TextureComponent;
import com.balanceball.component.VisibleComponent;
import com.balanceball.system.PhysicsSystem;
import com.sec.Entity;

/**
 * Created by tijs on 16/07/2017.
 */

public class PointEntity extends Entity {

    final static float RADIUS = 12.f;
    private float mAnimationTime;

    private boolean mIsPointLeft = true;
    private boolean mIsAvailable = true;

    public PointEntity(boolean isPointLeft, Vector2 leafHandlePosition, float leafHandleWidth,
                       float leafHandleHeight) {
        mIsPointLeft = isPointLeft;
        mAnimationTime = mIsPointLeft ? 0.f : 1.f;

        final float pointOffsetX = 22.f;
        final float pointOffsetY = 32.f;

        addComponent(new PhysicsComponent());

        float pointOffsetXTotal = (leafHandleWidth * 0.5f) + pointOffsetX;
        if (isPointLeft) {
            pointOffsetXTotal = -pointOffsetXTotal;
        }

        PositionComponent positionComponent = new PositionComponent();
        positionComponent.position.set(
                leafHandlePosition.x + pointOffsetXTotal,
                leafHandlePosition.y + (leafHandleHeight * 0.5f) + pointOffsetY);
        addComponent(positionComponent);

        SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = RADIUS * 2.f;
        sizeComponent.height = RADIUS * 2.f;
        addComponent(sizeComponent);

        RotationComponent rotationComponent = new RotationComponent();
        addComponent(rotationComponent);

        addComponent(new TextureComponent());

        addComponent(new VisibleComponent());
    }

    @Override
    public void create() {
        TextureComponent textureComponent = getComponentByType(TextureComponent.class);
        if (textureComponent != null) {
            textureComponent.textureRegion = new TextureRegion(
                    new Texture(Gdx.files.internal("element_yellow_polygon_glossy.png")));
        }

        // create physics body
        PhysicsComponent physicsComponent = getComponentByType(PhysicsComponent.class);
        PositionComponent positionComponent = getComponentByType(PositionComponent.class);
        SizeComponent sizeComponent = getComponentByType(SizeComponent.class);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(positionComponent.position);
        bodyDef.angle = (mIsPointLeft ? 10.f : -10.f) * MathUtils.degRad;

        CircleShape pointShape = new CircleShape();
        pointShape.setRadius(sizeComponent.width * 0.5f);

        PhysicsSystem physicsSystem = mEngine.getFirstSystemOfType(PhysicsSystem.class);

        physicsComponent.body = physicsSystem.getWorld().createBody(bodyDef);
        physicsComponent.body.setUserData(
                new PhysicsSystem.BodyUserDate(
                        mIsPointLeft ? PhysicsSystem.BODY_USER_DATA_TYPE_POINT_LEFT : PhysicsSystem.BODY_USER_DATA_TYPE_POINT_RIGHT));
        Fixture fixtureLeftPoint = physicsComponent.body.createFixture(pointShape, 0.f);
        fixtureLeftPoint.setSensor(true);

        pointShape.dispose();
    }

    public void update() {
        mAnimationTime += Gdx.graphics.getDeltaTime();

        SizeComponent sizeComponent = getComponentByType(SizeComponent.class);
        float size = (RADIUS * 2.f) + (MathUtils.sin(mAnimationTime * (mIsPointLeft ? 4.0f : 4.1f)) * 2.f);
        sizeComponent.width = size;
        sizeComponent.height = size;
    }

    public void setIsAvailable(boolean isAvailable) {
        mIsAvailable = isAvailable;

        VisibleComponent visibleComponent = getComponentByType(VisibleComponent.class);
        if (visibleComponent != null) {
            visibleComponent.visible = isAvailable;
        }
    }

    public boolean getIsAvailable() {
        return mIsAvailable;
    }
}
