package com.balanceball.enity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.balanceball.component.GuiComponent;
import com.balanceball.controller.HighScoreController;
import com.balanceball.system.GuiRendererSystem;
import com.sec.Entity;

/**
 * Created by tijs on 19/07/2017.
 */

public class UserNameGuiEntity extends Entity {

    private static final Color COLOR_FONT = new Color(0xffffffff);

    private Skin mSkin;

    private BitmapFont mFont;

    private TextField mNameInput;

    private Image mErrorFeedbackImage;

    public UserNameGuiEntity(Skin skin, BitmapFont font) {
        mSkin = skin;
        mFont = font;

        addComponent(new GuiComponent());
    }

    @Override
    public void create() {
        final Stage stage = new Stage(new ScreenViewport());
        getComponentByType(GuiComponent.class).stage = stage;

        Table rootTable = new Table();
        // add margin on the bottom for iOS
        final float dialogBottomOffset = (Gdx.app.getType() == Application.ApplicationType.iOS) ? 220 : 0;

        final float dialogMargin = 50.f;
        final float dialogWidth = Gdx.graphics.getWidth() - (dialogMargin * 2.f);
        final float dialogHeight = Gdx.graphics.getHeight() - (dialogMargin * 2.f) - dialogBottomOffset;

        rootTable.setWidth(dialogWidth);
        rootTable.setHeight(dialogHeight);
        rootTable.setPosition(dialogMargin, dialogMargin + dialogBottomOffset);

        rootTable.background(
                new NinePatchDrawable(
                        new NinePatch(new Texture(Gdx.files.internal("blue_panel.png")), 24, 24, 24, 24)));

        Label label = new Label("name", mSkin);
        label.setFontScale(1.6f);
        label.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        rootTable.add(label)
                .expand(true, false)
                .padTop(dialogHeight * 0.1f)
                .padBottom(dialogHeight * 0.025f);
        rootTable.row();

        Label label2 = new Label("What is your name?", mSkin);
        label2.setFontScale(1.f);
        label2.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        rootTable.add(label2)
                .expand(true, false)
                .padBottom(32.f)
                .padTop(32.f);
        rootTable.row();

        Table inputTable = new Table();

        TextField.TextFieldStyle textFieldStyle = mSkin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font = mFont;
        textFieldStyle.background.setLeftWidth(16.f);
        textFieldStyle.background.setRightWidth(16.f);
        textFieldStyle.background.setTopHeight(16.f);
        textFieldStyle.background.setBottomHeight(16.f);

        mNameInput = new TextField("", mSkin);
        mNameInput.setMaxLength(HighScoreController.MAX_NAME_LENGTH);
        mNameInput.setAlignment(Align.center);

        final float errorFeedbackWidth = mFont.getLineHeight();
        final float padTextField = 16.f;

        inputTable.add(mNameInput)
                .width(dialogWidth - (dialogMargin * 5.f))
                .padLeft(errorFeedbackWidth + padTextField)
                .padRight(padTextField)
                .padBottom(dialogHeight * 0.2f);

        mErrorFeedbackImage = new Image(new Texture(Gdx.files.internal("red_cross.png")));
        inputTable.add(mErrorFeedbackImage)
                .width(errorFeedbackWidth)
                .height(errorFeedbackWidth)
                .padBottom(dialogHeight * 0.2f);

        mErrorFeedbackImage.setVisible(false);

        rootTable.add(inputTable)
                .expand()
                .width(dialogWidth - (dialogMargin * 4.f));

        rootTable.row();

        // continue button
        TextButton okButton = new TextButton("Continue", mSkin);
        okButton.getLabel().setFontScale(1.f);
        okButton.getLabel().setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        okButton.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = mNameInput.getText();
                if (username != null
                        && username.length() >= HighScoreController.MIN_NAME_LENGTH
                        && username.length() <= HighScoreController.MAX_NAME_LENGTH) {
                    HighScoreController.setUserName(username);
                    mEngine.getFirstSystemOfType(GuiRendererSystem.class)
                            .activateGui(LeaderBoardGuiEntity.class);
                } else {
                    mErrorFeedbackImage.setVisible(true);
                }
            }
        });

        rootTable.add(okButton)
                .expand(true, false)
                .width(dialogWidth - (dialogMargin * 4.f))
                .height(dialogHeight * 0.08f)
                .padTop(16.f)
                .padBottom(16.f)
                .padLeft(32.f)
                .padRight(32.f)
                .center();
        rootTable.row();

        // cancel button
        TextButton restartButton = new TextButton("CANCEL", mSkin);
        restartButton.getLabel().setFontScale(1.f);
        restartButton.getLabel().setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        restartButton.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mEngine.getFirstSystemOfType(GuiRendererSystem.class)
                        .activateGui(HighScoreGuiEntity.class);
            }
        });

        // try to correctly position the bottom restart button
        rootTable.add(restartButton)
                .expand(true, false)
                .width(dialogWidth - (dialogMargin * 4.f))
                .height(dialogHeight * 0.08f)
                .padTop(16.f)
                .padBottom(32.f)
                .padLeft(32.f)
                .padRight(32.f)
                .center();

        stage.addActor(rootTable);
    }

    @Override
    public void resume() {
        super.resume();

        String userName = HighScoreController.getUserName();
        if (mNameInput != null && userName != null) {
            mNameInput.setText(userName);
        }

        mErrorFeedbackImage.setVisible(false);
    }

    @Override
    public void dispose() {
        GuiComponent guiComponent = getComponentByType(GuiComponent.class);
        if (guiComponent != null && guiComponent.stage != null) {
            guiComponent.stage.dispose();
        }
    }
}
