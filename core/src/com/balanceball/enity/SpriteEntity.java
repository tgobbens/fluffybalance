package com.balanceball.enity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.balanceball.component.PositionComponent;
import com.balanceball.component.RotationComponent;
import com.balanceball.component.SizeComponent;
import com.balanceball.component.TextureComponent;
import com.sec.Entity;

/**
 * Created by tijs on 16/07/2017.
 */

public class SpriteEntity extends Entity {

    private String mTexturePath;

    public SpriteEntity(String texturePath, Vector2 position, float width) {
        init(texturePath, position, width, -1.f, 0.f);
    }

    public SpriteEntity(String texturePath, Vector2 position, float width, float height) {
        init(texturePath, position, width, height, 0.f);
    }

    public SpriteEntity(String texturePath, Vector2 position, float width, float height, float rotationDegree) {
        init(texturePath, position,  width, height, rotationDegree);
    }

    protected void init(String texturePath, Vector2 position, float width, float height, float rotationDegree) {
        mTexturePath = texturePath;

        PositionComponent positionComponent = new PositionComponent();
        positionComponent.position = new Vector2(position);
        addComponent(positionComponent);

        SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = width;
        sizeComponent.height = height;
        addComponent(sizeComponent);

        RotationComponent rotationComponent = new RotationComponent();
        rotationComponent.degree = rotationDegree;
        addComponent(rotationComponent);

        addComponent(new TextureComponent());
    }

    @Override
    public void create() {
        TextureComponent textureComponent = getComponentByType(TextureComponent.class);
        if (textureComponent != null) {
            Texture texture = new Texture(Gdx.files.internal(mTexturePath));
            textureComponent.textureRegion = new TextureRegion(texture);

            SizeComponent sizeComponent = getComponentByType(SizeComponent.class);
            if (sizeComponent != null && sizeComponent.height == -1.f) {
                float aspect = (float)texture.getWidth() / (float)texture.getHeight();
                sizeComponent.height = sizeComponent.width / aspect;
            }
        }
    }

    @Override
    public void dispose() {
        TextureComponent textureComponent = getComponentByType(TextureComponent.class);
        if (textureComponent != null && textureComponent.textureRegion != null) {
            textureComponent.textureRegion.getTexture().dispose();
        }
    }
}
