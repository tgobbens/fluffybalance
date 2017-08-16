package com.balanceball.enity;

import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.balanceball.component.DebugComponent;
import com.balanceball.system.PhysicsSystem;
import com.sec.Entity;

/**
 * Created by tijs on 16/07/2017.
 */

public class PhysicsDebugEntity extends Entity {

    private Box2DDebugRenderer mDebugRenderer = null;

    public PhysicsDebugEntity() {
        addComponent(new DebugComponent());
    }

    @Override
    public void update() {
        CameraEntity cameraEntity = mEngine.getFirstEntityOfType(CameraEntity.class);
        PhysicsSystem physicsSystem = mEngine.getFirstSystemOfType(PhysicsSystem.class);

        if (cameraEntity != null && physicsSystem != null) {
            mDebugRenderer.render(physicsSystem.getWorld(), cameraEntity.getCamera().combined);
        }
    }

    @Override
    public void create() {
        mDebugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void dispose() {
        mDebugRenderer.dispose();
    }
}
