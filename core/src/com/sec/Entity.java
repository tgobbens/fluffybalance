package com.sec;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by tijs on 10/07/2017.
 */

public abstract class Entity implements GameLifecycleListener {
    private ObjectMap<String, Component> mComponents;

    public static final int STATE_BECOMING_ACTIVE = 0;
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_BECOMING_PAUSED = 2;
    public static final int STATE_PAUSED = 3;

    private int mState = STATE_BECOMING_ACTIVE;

    protected Engine mEngine;

    public Entity() {
        mComponents = new ObjectMap<String, Component>(16);
    }

    protected final void addComponent(Component component) {
        mComponents.put(component.getClass().getName(), component);
    }

    void setEngine(Engine engine) {
        mEngine = engine;
    }

    public int getState() {
        return mState;
    }

    public void becomeActive() {
        mState = STATE_BECOMING_ACTIVE;
    }

    public void becomePaused() {
        mState = STATE_BECOMING_PAUSED;
    }

    @Override
    public void resume() {
        mState = STATE_ACTIVE;
    }

    @Override
    public void update() {
        // do nothing
    }

    @Override
    public void pause() {
        mState = STATE_PAUSED;
    }

    @Override
    public void create() {
        // do nothing
    }

    @Override
    public void dispose() {
        // do nothing
    }

    /**
     * check if the entity matches all required components
     * @param componentsType
     * @return boolean
     */
    final boolean matchesComponentTypes(Array<Class<? extends Component>> componentsType) {
        for (Class<? extends Component> componentType : componentsType) {
            if (getComponentByType(componentType) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param componentType
     * @param <T> get the component of a given type
     * @return T the component, null if not found
     */
    public final <T extends Component> T getComponentByType(Class<T> componentType) {
        Object o = mComponents.get(componentType.getName());
        return (o == null) ? null : (T) o;
    }
}
