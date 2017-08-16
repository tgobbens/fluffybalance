package com.balanceball.system;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.balanceball.component.PositionComponent;
import com.balanceball.component.RotationComponent;
import com.balanceball.component.SizeComponent;
import com.balanceball.component.TextureComponent;
import com.balanceball.component.VisibleComponent;
import com.balanceball.component.ZIndexComponent;
import com.balanceball.enity.CameraEntity;
import com.sec.Entity;
import com.sec.System;

import java.util.Comparator;

/**
 * Created by tijs on 11/07/2017.
 */

public class SpriteRenderSystem extends System {
    private SpriteBatch mSpriteBatch;
    private Array<Entity> mZOrderedEntities;

    public SpriteRenderSystem() {
        addComponentType(TextureComponent.class);
        addComponentType(PositionComponent.class);
        addComponentType(SizeComponent.class);
    }

    @Override
    public void create() {
        mSpriteBatch = new SpriteBatch();
        mZOrderedEntities = new Array<Entity>();
    }

    public void refreshZOrder() {
        mZOrderedEntities.clear();
    }

    @Override
    public void updateFromEntities(Array<Entity> entities) {
        // update the (first) camera
        CameraEntity cameraEntity = mEngine.getFirstEntityOfType(CameraEntity.class);
        if (cameraEntity != null) {
            cameraEntity.update();

            mSpriteBatch.setProjectionMatrix(cameraEntity.getCamera().combined);
        }

        // only sort if this is required
        if (mZOrderedEntities.size == 0 && entities.size != 0) {
            mZOrderedEntities.addAll(entities);

            // this could be a performance killer, should do this once and only recalculate on change
            mZOrderedEntities.sort(new Comparator<Entity>() {
                @Override
                public int compare(Entity entity1, Entity entity2) {
                    ZIndexComponent zIndexComponent1 = entity1.getComponentByType(ZIndexComponent.class);
                    ZIndexComponent zIndexComponent2 = entity2.getComponentByType(ZIndexComponent.class);
                    int zIndex1 = zIndexComponent1 == null ? 0 : zIndexComponent1.index;
                    int zIndex2 = zIndexComponent2 == null ? 0 : zIndexComponent2.index;

                    return zIndex1 == zIndex2 ? 0 : zIndex1 > zIndex2 ? 1 : 0;
                }
            });
        }

        mSpriteBatch.begin();

        for (Entity entity : mZOrderedEntities) {
            VisibleComponent visibleComponent = entity.getComponentByType(VisibleComponent.class);
            if (visibleComponent != null && !visibleComponent.visible) {
                continue;
            }

            TextureComponent textureComponent = entity.getComponentByType(
                    TextureComponent.class);
            PositionComponent positionComponent = entity.getComponentByType(
                    PositionComponent.class);
            SizeComponent sizeComponent = entity.getComponentByType(
                    SizeComponent.class);
            RotationComponent rotationComponent = entity.getComponentByType(
                    RotationComponent.class);

            // need a texture, position and size, implicit by the componentsType
            if (textureComponent == null || positionComponent == null || sizeComponent == null) {
                continue;
            }

            float rotation = rotationComponent == null ? 0.f : rotationComponent.degree;

            mSpriteBatch.draw(
                    textureComponent.textureRegion,
                    positionComponent.position.x - (sizeComponent.width * 0.5f),
                    positionComponent.position.y - (sizeComponent.height * 0.5f),
                    sizeComponent.width * 0.5f,
                    sizeComponent.height * 0.5f,
                    sizeComponent.width,
                    sizeComponent.height,
                    1.f,
                    1.f,
                    rotation);
        }

        mSpriteBatch.end();
    }

    @Override
    public void dispose() {
        if (mSpriteBatch != null) {
            mSpriteBatch.dispose();
        }
    }
}