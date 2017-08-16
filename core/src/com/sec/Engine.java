package com.sec;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by tijs on 10/07/2017.
 */

public class Engine implements GameLifecycleListener {
    private Array<Entity> mEntities;
    private ObjectMap<String, Entity> mFirstEntities;
    private Array<System> mSystems;

    public Engine() {
        mEntities = new Array<Entity>();
        mFirstEntities = new ObjectMap<String, Entity>();
        mSystems = new Array<System>();
    }

    public Engine registerSystem(System system) {
        mSystems.add(system);
        system.setEngine(this);
        return this;
    }

    public Engine registerEntity(Entity entity) {
        entity.setEngine(this);

        String entityName = entity.getClass().getName();
        if (!mFirstEntities.containsKey(entityName)) {
            mFirstEntities.put(entityName, entity);
        }

        mEntities.add(entity);

        return this;
    }

    @Override
    public void create() {
        for (System system : mSystems) {
            system.create();
        }

        for (Entity entity : mEntities) {
            entity.create();
        }
    }

    @Override
    public void resume() {
        for (System system : mSystems) {
            system.resume();
        }
    }

    @Override
    public void update() {
        // first update the entity states
        for (int i = 0; i < mEntities.size; ++i) {
            Entity entity = mEntities.get(i);
            if (entity.getState() == Entity.STATE_BECOMING_ACTIVE) {
                entity.resume();
            } else if (entity.getState() == Entity.STATE_BECOMING_PAUSED) {
                entity.pause();
            }
        }

        for (int i = 0; i < mEntities.size; ++i) {
            Entity entity = mEntities.get(i);
            if (entity.getState() == Entity.STATE_ACTIVE) {
                entity.update();
            }
        }

        for (System system : mSystems) {
            Array<Class<? extends Component>> componentsTypes = system.getComponentsType();
            Array<Entity> matchingEntities = new Array<Entity>();

            for (int i = 0; i < mEntities.size; ++i) {
                Entity entity = mEntities.get(i);
                if (entity.getState() == Entity.STATE_ACTIVE &&
                        entity.matchesComponentTypes(componentsTypes)) {
                    matchingEntities.add(entity);
                }
            }

            system.updateFromEntities(matchingEntities);
        }
    }

    @Override
    public void pause() {
        for (System system : mSystems) {
            system.pause();
        }
    }

    @Override
    public void dispose() {
        for (System system : mSystems) {
            system.dispose();
        }

        for (Entity entity : mEntities) {
            entity.dispose();
        }
    }

    /**
     * Get the first entity of the E
     * @param type
     * @param <E>
     * @return
     */
    public <E extends Entity> E getFirstEntityOfType(Class<E> type) {
        Entity entity = mFirstEntities.get(type.getName());
        return entity == null ? null : (E) entity;
    }

    /**
     * get the component from a certain type C of the first entity from type E
     * @param entityType
     * @param componentType
     * @param <E>
     * @param <C>
     * @return
     */
    public <E extends Entity, C extends Component> C getComponentFromFirstEntity(
            Class<E> entityType, Class<C> componentType) {
        E entity = getFirstEntityOfType(entityType);
        return (entity == null) ? null : entity.getComponentByType(componentType);
    }

    /**
     * get the first system of a type
     * @param type
     * @param <S>
     * @return
     */
    public <S extends System> S getFirstSystemOfType(Class<S> type) {
        for (System system : mSystems) {
            if (type.isInstance(system)) {
                return (S) system;
            }
        }
        return null;
    }

    /**
     * get all entities of type E
     * @param type
     * @param <E>
     * @return
     */
    public <E extends Entity> Array<E> getAllEntitiesOfType(Class<E> type) {
        Array<E> entities = new Array<E>();
        for (Entity entity : mEntities) {
            if (type.isInstance(entity)) {
                entities.add((E) entity);
            }
        }
        return entities;
    }

    public <C extends Component> Array<Entity> getAllEntitiesWithComponentOfType(Class<C> componentType) {
        Array<Entity> entities = new Array<Entity>();
        for (Entity entity : mEntities) {
            if (entity.getComponentByType(componentType) != null) {
                entities.add(entity);
            }
        }
        return entities;
    }
}
