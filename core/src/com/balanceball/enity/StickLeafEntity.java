package com.balanceball.enity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.balanceball.component.PhysicsComponent;
import com.balanceball.system.PhysicsSystem;
import com.balanceball.component.PositionComponent;
import com.balanceball.component.SizeComponent;
import com.balanceball.component.TextureComponent;
import com.sec.Entity;

/**
 * Created by tijs on 14/07/2017.
 */

public class StickLeafEntity extends Entity {

    public StickLeafEntity(int worldWidth, int handleHeight) {
        final float leafWidth = worldWidth * 0.666f; // 2/3 of screen width
        final float leafHeight = 20.f;

        addComponent(new TextureComponent());
        addComponent(new PhysicsComponent());

        SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = leafWidth;
        sizeComponent.height = leafHeight;
        addComponent(sizeComponent);

        PositionComponent positionComponent = new PositionComponent();
        positionComponent.position.set(worldWidth * 0.5f, handleHeight + (leafHeight * 0.5f));
        addComponent(positionComponent);

        addComponent(new TextureComponent());
    }

    @Override
    public void create() {
        getComponentByType(TextureComponent.class).textureRegion = new TextureRegion(
                new Texture(Gdx.files.internal("stick_leaf.png")));

        PositionComponent positionComponent = getComponentByType(PositionComponent.class);
        SizeComponent sizeComponent = getComponentByType(SizeComponent.class);

        // create physics body
        PhysicsComponent physicsComponent = getComponentByType(PhysicsComponent.class);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(
                positionComponent.position.x,
                positionComponent.position.y);

        // Create a polygon shape
        PolygonShape leafPhysicsBox = new PolygonShape();
        leafPhysicsBox.setAsBox(sizeComponent.width * 0.5f, sizeComponent.height * 0.5f);

        PhysicsSystem physicsSystem = mEngine.getFirstSystemOfType(PhysicsSystem.class);
        physicsComponent.body = physicsSystem.getWorld().createBody(bodyDef);
        physicsComponent.body.setUserData(
                new PhysicsSystem.BodyUserDate(PhysicsSystem.BODY_USER_DATA_TYPE_OTHER));
        Fixture fixture = physicsComponent.body.createFixture(leafPhysicsBox, 0.f);
        fixture.setFriction(0.4f);
        fixture.setRestitution(0.4f);

        leafPhysicsBox.dispose();
    }

    @Override
    public void dispose() {
        getComponentByType(TextureComponent.class).textureRegion.getTexture().dispose();
    }
}
