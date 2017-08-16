package com.sec;

/**
 * Created by tijs on 13/07/2017.
 */

interface GameLifecycleListener {

    void create();

    void resume();

    void update();

    void pause();

    void dispose();
}
