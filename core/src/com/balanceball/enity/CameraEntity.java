package com.balanceball.enity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.balanceball.component.PositionComponent;
import com.sec.Entity;

/**
 * Created by tijs on 16/07/2017.
 */

public class CameraEntity extends Entity {
    private Camera mCamera;

    public CameraEntity() {
        addComponent(new PositionComponent());
    }

    @Override
    public void create() {
        GameWorldEntity gameWorldEntity = mEngine.getFirstEntityOfType(GameWorldEntity.class);
        if (gameWorldEntity == null) {
            // cannot create a camera without a game world
            return;
        }

        mCamera = new OrthographicCamera(gameWorldEntity.getWorldWidth(), gameWorldEntity.getWorldHeight());
        mCamera.position.set(mCamera.viewportWidth / 2f, mCamera.viewportHeight / 2f, 0.f);

        PositionComponent positionComponent = getComponentByType(PositionComponent.class);
        positionComponent.position.set(mCamera.viewportWidth / 2f, mCamera.viewportHeight / 2f);
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void update() {
        mCamera.position.set(getComponentByType(PositionComponent.class).position, 0.f);
        mCamera.update();
    }
}
