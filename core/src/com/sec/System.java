package com.sec;

import com.badlogic.gdx.utils.Array;

/**
 * Created by tijs on 10/07/2017.
 */

public abstract class System implements SystemInterface, GameLifecycleListener {

    protected Engine mEngine = null;

    // register all type it want to listen for
    private Array<Class<? extends Component>> mComponentsType;

    protected System() {
        mComponentsType = new Array<Class<? extends Component>>();
    }

    void setEngine(Engine engine) {
        mEngine = engine;
    }

    protected final void addComponentType(Class<? extends Component> componentType) {
        mComponentsType.add(componentType);
    }

    Array<Class<? extends Component>> getComponentsType() {
        return mComponentsType;
    }

    @Override
    public void create() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void update() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void dispose() {

    }
}
