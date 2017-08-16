package com.balanceball.enity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.balanceball.component.GuiComponent;
import com.balanceball.component.VisibleComponent;
import com.balanceball.controller.HighScoreController;
import com.balanceball.system.GuiRendererSystem;
import com.sec.Entity;

/**
 * Created by tijs on 18/07/2017.
 */

public class HighScoreGuiEntity extends Entity {

    private static final Color COLOR_FONT = new Color(0xffffffff);

    private BitmapFont mFont;

    private Skin mSkin;

    private Array<Label> mHighScoreLabel;

    public HighScoreGuiEntity(Skin skin, BitmapFont font) {
        mSkin = skin;
        mFont = font;

        mHighScoreLabel = new Array<Label>();

        addComponent(new GuiComponent());
    }

    public void create() {
        Stage stage = new Stage(new ScreenViewport());

        getComponentByType(GuiComponent.class).stage = stage;

        Table rootTable = new Table();

        // add margin on the bottom for iOS
        final float dialogBottomOffset = (Gdx.app.getType() == Application.ApplicationType.iOS) ? 220 : 0;

        final float dialogMargin = 50.f;
        final float dialogWidth = Gdx.graphics.getWidth() - (dialogMargin * 2.f);
        final float dialogHeight = Gdx.graphics.getHeight() - (dialogMargin * 2.f) - dialogBottomOffset;

        final float margin = dialogWidth * 0.03f;

        rootTable.setWidth(dialogWidth);
        rootTable.setHeight(dialogHeight);
        rootTable.setPosition(dialogMargin, dialogMargin + dialogBottomOffset);

        rootTable.background(
                new NinePatchDrawable(
                        new NinePatch(new Texture(Gdx.files.internal("blue_panel.png")), 24, 24, 24, 24)));

        Label label = new Label("GAME OVER", mSkin);
        label.setFontScale(1.6f);
        label.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        rootTable.add(label)
                .expand(true, false)
                .padTop(dialogHeight * 0.05f)
                .padBottom(dialogHeight * 0.02f);

        rootTable.row();

        Table scrollPaneTable = new Table(mSkin);
        ScrollPane scrollPane = new ScrollPane(scrollPaneTable);

        IntArray highScores = HighScoreController.fetchHighScores();
        final int maxHighScores = 32;

        for (int i = 0; i < maxHighScores; ++i) {
            boolean hasScore = i < highScores.size;

            Label scoreTextLabel = new Label("high score:", mSkin);
            scoreTextLabel.setFontScale(0.7f);
            scoreTextLabel.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
            scrollPaneTable.add(scoreTextLabel)
                    .expand()
                    .padRight(64.f)
                    .padBottom(24.f)
                    .padRight(16.f)
                    .center();

            Label scoreLabel = new Label("" + (hasScore ? highScores.get(i) : "-"), mSkin);
            scoreLabel.setFontScale(0.7f);
            scoreLabel.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
            scrollPaneTable.add(scoreLabel)
                    .expand()
                    .padBottom(24.f)
                    .padRight(16.f)
                    .center();

            scrollPaneTable.row();

            mHighScoreLabel.add(scoreLabel);
        }

        rootTable.add(scrollPane)
                .fill()
                .expand()
                .padTop(16.f)
                .padBottom(16.f);

        rootTable.row();

        // restart button
        TextButton restartButton = new TextButton("RESTART", mSkin);
        restartButton.getLabel().setFontScale(1.f);
        restartButton.getLabel().setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        restartButton.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mEngine.getFirstEntityOfType(GameStateEntity.class).onStartGame();
            }
        });

        rootTable.add(restartButton)
                .expand(true, false)
                .width(dialogWidth - (dialogMargin * 2.f))
                .height(dialogHeight * 0.08f)
                .padTop(margin)
                .padBottom(margin * 0.5f)
                .padLeft(margin)
                .padRight(margin)
                .center();

        rootTable.row();

        Table leaderBoardTable = new Table();

        final float settingsWidth = mFont.getLineHeight() * 1.4f;

        TextButton leaderBoardButton = new TextButton("LEADERBOARD", mSkin);
        leaderBoardButton.getLabel().setFontScale(1.f);
        leaderBoardButton.getLabel().setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        leaderBoardButton.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (HighScoreController.getUserName() == null) {
                    mEngine.getFirstSystemOfType(GuiRendererSystem.class)
                            .activateGui(UserNameGuiEntity.class);
                } else {
                    mEngine.getFirstSystemOfType(GuiRendererSystem.class)
                            .activateGui(LeaderBoardGuiEntity.class);
                }
            }
        });

        leaderBoardTable.add(leaderBoardButton)
                .expand(true, false)
                .width(dialogWidth - (dialogMargin * 2.f) - (settingsWidth + margin))
                .height(dialogHeight * 0.08f)
                .padLeft(margin)
                .center();

        Drawable drawable = new TextureRegionDrawable(
                new TextureRegion(new Texture(Gdx.files.internal("ic_settings.png"))));

        ImageButton imageButton = new ImageButton(drawable);
        imageButton.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mEngine.getFirstSystemOfType(GuiRendererSystem.class)
                        .activateGui(UserNameGuiEntity.class);
            }
        });

        leaderBoardTable.add(imageButton)
                .expand(false, false)
                .right()
                .width(settingsWidth)
                .height(settingsWidth)
                .pad(margin);

        rootTable.add(leaderBoardTable)
                .expand(true, false)
                .padTop(margin * 0.5f)
                .padBottom(margin)
                .padLeft(margin)
                .padRight(margin);

        stage.addActor(rootTable);
    }

    @Override
    public void resume() {
        super.resume();

        updateHighScore();
    }

    private void updateHighScore() {
        IntArray highScores = HighScoreController.fetchHighScores();

        for (int i = 0; i < Math.min(mHighScoreLabel.size, highScores.size); i++) {
            mHighScoreLabel.get(i).setText("" + highScores.get(i));
        }
    }

    public void dispose() {
        GuiComponent guiComponent = getComponentByType(GuiComponent.class);
        if (guiComponent != null) {
            guiComponent.stage.dispose();
        }
    }
}
