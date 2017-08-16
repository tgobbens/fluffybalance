package com.balanceball.enity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.balanceball.component.GuiComponent;
import com.sec.Entity;

/**
 * Created by tijs on 18/07/2017.
 */

public class IngameGuiEntity extends Entity {

    private static final Color COLOR_FONT = new Color(0xffffffff);

    private BitmapFont mFont;

    private Label mScoreLabel;

    public IngameGuiEntity(BitmapFont font) {
        mFont = font;

        addComponent(new GuiComponent());
    }

    @Override
    public void resume() {
        super.resume();

        GameStateEntity gameStateEntity = mEngine.getFirstEntityOfType(GameStateEntity.class);

        onScoreChanged(gameStateEntity.getPointsTotal());
    }

    public void create() {
        Stage stage = new Stage(new ScreenViewport());
        getComponentByType(GuiComponent.class).stage = stage;

        Table gameScore = new Table();

        float scorePanelWidth = Gdx.graphics.getWidth() * 0.8f; // 80 % of screen
        float scorePanelHeight = Gdx.graphics.getHeight() * 0.2f; // 20 % of screen

        gameScore.setWidth(scorePanelWidth);
        gameScore.setHeight(scorePanelHeight);
        gameScore.setY(Gdx.graphics.getHeight() - (scorePanelHeight + 30.f));
        gameScore.setX((Gdx.graphics.getWidth() - scorePanelWidth) * 0.5f);

        gameScore.setBackground(new NinePatchDrawable(
                new NinePatch(new Texture(Gdx.files.internal("red_button13.png")), 24, 24, 24, 24)));
        gameScore.pad(32.f);

        Label scoreLabel = new Label("Score", new Label.LabelStyle(
                mFont, COLOR_FONT));
        scoreLabel.setFontScale(1.2f);

        gameScore.add(scoreLabel).expand();

        gameScore.row();

        mScoreLabel = new Label("" + 0, new Label.LabelStyle(mFont, COLOR_FONT));
        mScoreLabel.setFontScale(2.0f);

        gameScore.add(mScoreLabel).expand();

        stage.addActor(gameScore);
    }

    public void dispose() {
        GuiComponent guiComponent = getComponentByType(GuiComponent.class);
        if (guiComponent != null) {
            guiComponent.stage.dispose();
        }
    }

    // this will one day be an event
    public void onScoreChanged(int points) {
        if (mScoreLabel != null) {
            mScoreLabel.setText("" + points);
        }
    }
}
