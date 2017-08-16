package com.balanceball.enity;

import com.balanceball.component.GravityComponent;
import com.sec.Entity;

/**
 * Created by tijs on 16/07/2017.
 */

public class GravityEntity extends Entity {

    public GravityEntity() {
        addComponent(new GravityComponent());
    }
}
