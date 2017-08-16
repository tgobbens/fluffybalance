package com.balanceball.enity;

import com.sec.Entity;

/**
 * Created by tijs on 17/07/2017.
 */

public class GameWorldEntity extends Entity {
    private int mWorldWidth;
    private int mWorldHeight;

    public GameWorldEntity(int worldWidth, int worldHeight) {
        mWorldWidth = worldWidth;
        mWorldHeight = worldHeight;
    }

    public int getWorldWidth() {
        return mWorldWidth;
    }

    public int getWorldHeight() {
        return mWorldHeight;
    }
}
