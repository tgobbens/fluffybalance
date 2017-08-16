package com.balanceball.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.balanceball.component.GuiComponent;
import com.balanceball.component.VisibleComponent;
import com.sec.Component;
import com.sec.Entity;
import com.sec.System;

/**
 * Created by tijs on 18/07/2017.
 */

public class GuiRendererSystem extends System {

    private Class<? extends Entity> mActiveGui;

    @Override
    public void resume() {
        Array<Entity> entities = mEngine.getAllEntitiesWithComponentOfType(GuiComponent.class);
        for (Entity entity : entities) {
            if (mActiveGui != null && mActiveGui.isInstance(entity)) {
                continue;
            }

            entity.becomePaused();
        }
    }

    public GuiRendererSystem() {
        addComponentType(GuiComponent.class);
    }

    public void activateGui(Class<? extends Entity> guiEntityClass) {
        if (mActiveGui != null) {
            Entity activeGui = mEngine.getFirstEntityOfType(mActiveGui);
            activeGui.becomePaused();
        }

        mActiveGui = guiEntityClass;

        if (mActiveGui != null) {
            Entity toBecomeActiveEntity = mEngine.getFirstEntityOfType(mActiveGui);
            toBecomeActiveEntity.becomeActive();

            GuiComponent guiComponent = toBecomeActiveEntity.getComponentByType(GuiComponent.class);
            if (guiComponent != null) {
                Gdx.input.setInputProcessor(guiComponent.stage);
            }
        }
    }

    @Override
    public void updateFromEntities(Array<Entity> entities) {
        for (Entity entity : entities) {
            VisibleComponent visibleComponent = entity.getComponentByType(VisibleComponent.class);
            if (visibleComponent != null && !visibleComponent.visible) {
                continue;
            }

            GuiComponent guiComponent = entity.getComponentByType(GuiComponent.class);
            if (guiComponent != null && guiComponent.stage != null) {
                guiComponent.stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
                guiComponent.stage.draw();
            }
        }
    }
}
