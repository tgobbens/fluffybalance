package com.balanceball.enity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.balanceball.component.GravityComponent;
import com.sec.Entity;

/**
 * Created by tijs on 15/07/2017.
 */

public class RollInputEntity extends Entity {

    // SMOOTH THE INPUT SAMPLING, HIGHER VALUE LESS RESPONSIVE GAME SO HARDER
    private static final int INPUT_SAMPLE_SIZE = 50;

    private FloatArray mInputRollList;
    private float mInputRollAverage;
    private int mInsertIndex;

    public RollInputEntity() {
        mInputRollList = new FloatArray(INPUT_SAMPLE_SIZE);
        mInputRollAverage = 0.f;
        mInsertIndex = 0;
    }

    public void update() {
        // prevent user from manipulating input to much
        addValue(MathUtils.clamp(Gdx.input.getRoll(), -25.f, 25.f));

        // normalised normalisedGravity
        Vector2 gravity = new Vector2(0, -1).rotate(mInputRollAverage).nor();

        // find the gravity entity
        GravityComponent gravityComponent = mEngine.getComponentFromFirstEntity(
                GravityEntity.class, GravityComponent.class);
        if (gravityComponent != null) {
            gravityComponent.normalisedGravity.set(gravity);
        }
    }

    public void resetInput() {
        mInputRollList.clear();
        mInsertIndex = 0;
    }

    private void addValue(float value) {
        mInsertIndex = (mInsertIndex + 1) % INPUT_SAMPLE_SIZE;

        if (mInsertIndex >= mInputRollList.size) {
            mInputRollList.add(value);
        } else {
            mInputRollList.set(mInsertIndex, value);
        }

        float total = 0.f;
        for (int i = 0; i < mInputRollList.size; ++i) {
            total += mInputRollList.get(i);
        }

        mInputRollAverage = total / mInputRollList.size;
    }
}
