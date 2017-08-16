package com.balanceball.enity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.balanceball.component.GuiComponent;
import com.balanceball.controller.HighScoreController;
import com.balanceball.controller.LeaderBoardController;
import com.balanceball.controller.LeaderBoardEntry;
import com.sec.Entity;

public class LeaderBoardGuiEntity extends Entity {

    private static final Color COLOR_FONT = new Color(0xffffffff);

    private Skin mSkin;

    private BitmapFont mFont;

    private float mDefaultMargin;

    private Table mScrollPaneTable;

    private LeaderBoardController mController;

    private IntArray mRoundPosted;

    private int mLoadingRetries;

    private Array<GuiLeaderBoardEntry> mLeaderBoardEntries;

    private boolean mLeaderBoardDirty = false;

    private class GuiLeaderBoardEntry {
        int rank;
        String name;
        int score;
    }

    public LeaderBoardGuiEntity(Skin skin, BitmapFont font) {
        mSkin = skin;
        mFont = font;

        mLeaderBoardEntries = new Array<GuiLeaderBoardEntry>();

        mRoundPosted = new IntArray();

        mController = new LeaderBoardController();

        addComponent(new GuiComponent());
    }

    @Override
    public void resume() {
        super.resume();

        mLeaderBoardEntries.clear();
        mLeaderBoardDirty = true;

        mLoadingRetries = 3;

        submitLatestScore();
    }

    @Override
    public void create() {
        Stage stage = new Stage(new ScreenViewport());

        getComponentByType(GuiComponent.class).stage = stage;

        Table rootTable = new Table();
        // add margin on the bottom for iOS
        final float dialogBottomOffset = (Gdx.app.getType() == Application.ApplicationType.iOS) ? 220 : 0;

        final float dialogMargin = 50.f;
        final float dialogWidth = Gdx.graphics.getWidth() - (dialogMargin * 2.f);
        final float dialogHeight = Gdx.graphics.getHeight() - (dialogMargin * 2.f) - dialogBottomOffset;

        mDefaultMargin = dialogWidth * 0.03f;

        rootTable.setWidth(dialogWidth);
        rootTable.setHeight(dialogHeight);
        rootTable.setPosition(dialogMargin, dialogMargin + dialogBottomOffset);

        rootTable.background(
                new NinePatchDrawable(
                        new NinePatch(new Texture(Gdx.files.internal("blue_panel.png")), 24, 24, 24, 24)));

        // HEADER
        Label label = new Label("LEADERBOARD", mSkin);
        label.setFontScale(1.6f);
        label.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        rootTable.add(label)
                .expand(true, false)
                .padTop(dialogHeight * 0.05f)
                .padBottom(mDefaultMargin * 0.5f);

        rootTable.row();

        mScrollPaneTable = new Table(mSkin);
        ScrollPane scrollPane = new ScrollPane(mScrollPaneTable);

        rootTable.add(scrollPane)
                .fill()
                .expand()
                .padTop(mDefaultMargin * 0.5f)
                .padBottom(mDefaultMargin * 0.5f);
        rootTable.row();

        // RESTART button
        TextButton restartButton = new TextButton("RESTART", mSkin);
        restartButton.getLabel().setFontScale(1.f);
        restartButton.getLabel().setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        restartButton.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mEngine.getFirstEntityOfType(GameStateEntity.class).onStartGame();
            }
        });

        // try to correctly position the bottom restart button
        rootTable.add(restartButton)
                .expand(true, false)
                .width(dialogWidth - (dialogMargin * 4.f))
                .height(dialogHeight * 0.08f)
                .padTop(mDefaultMargin * 0.5f)
                .padBottom(mDefaultMargin)
                .padLeft(mDefaultMargin)
                .padRight(mDefaultMargin)
                .center();

        stage.addActor(rootTable);
    }

    private void loadLeaderBoard() {
        LeaderBoardController controller = new LeaderBoardController();
        controller.fetchLeaderBoard(new LeaderBoardController.FetchLeaderBoardListener() {
            @Override
            public void onSuccess(Array<LeaderBoardEntry> leaderBoardEntries) {
                mLeaderBoardEntries.clear();

                for (LeaderBoardEntry entry : leaderBoardEntries) {
                    GuiLeaderBoardEntry guiEntry = new GuiLeaderBoardEntry();
                    guiEntry.rank = entry.rank;
                    guiEntry.name = entry.name;
                    guiEntry.score = entry.score;
                    mLeaderBoardEntries.add(guiEntry);
                }

                mLeaderBoardDirty = true;
            }

            @Override
            public void onFailed() {
                if (mLoadingRetries > 0) {
                    loadLeaderBoard();
                    mLoadingRetries--;
                }
            }
        });
    }

    private void addLeaderBoardEntries() {
        mScrollPaneTable.clear();

        for (int i = -1; i < mLeaderBoardEntries.size; i++) {
            String rank;
            String name;
            String score;

            if (i == -1) {
                rank = "RANK";
                name = "NAME";
                score = "SCORE";
            } else {
                rank = "" + mLeaderBoardEntries.get(i).rank;
                name = mLeaderBoardEntries.get(i).name;
                score = "" + mLeaderBoardEntries.get(i).score;
            }

            addLeadBoardRow(rank, name, score);
        }
    }

    private void addLeadBoardRow(String rank, String name, String score) {
        Label indexLabel = new Label(rank, mSkin);
        indexLabel.setFontScale(0.7f);
        indexLabel.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        mScrollPaneTable.add(indexLabel)
                .expand(true, false)
                .center()
                .padTop(mDefaultMargin * 0.5f)
                .padBottom(mDefaultMargin * 0.5f)
                .padRight(mDefaultMargin * 0.5f);

        Label scoreNameLabel = new Label(name, mSkin);
        scoreNameLabel.setFontScale(0.7f);
        scoreNameLabel.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        mScrollPaneTable.add(scoreNameLabel)
                .expand(true, false)
                .center()
                .padTop(mDefaultMargin * 0.5f)
                .padBottom(mDefaultMargin * 0.5f)
                .padRight(mDefaultMargin * 0.5f);

        Label scoreLabel = new Label(score, mSkin);
        scoreLabel.setFontScale(0.7f);
        scoreLabel.setStyle(new Label.LabelStyle(mFont, COLOR_FONT));
        mScrollPaneTable.add(scoreLabel)
                .expand(true, false)
                .center()
                .padTop(mDefaultMargin * 0.5f)
                .padBottom(mDefaultMargin * 0.5f);

        mScrollPaneTable.row();
    }

    private void submitLatestScore() {
        GameStateEntity gameStateEntity = mEngine.getFirstEntityOfType(GameStateEntity.class);

        int pointTotal = gameStateEntity.getPointsTotal();
        int round = gameStateEntity.getRound();

        if (mRoundPosted.contains(round)) {
            loadLeaderBoard();
        } else {
            mRoundPosted.add(round);

            mController.addScore(new LeaderBoardController.AddScoreListener() {
                @Override
                public void onSuccess() {
                    loadLeaderBoard();
                }

                @Override
                public void onFailed() {
                    loadLeaderBoard();
                }
            }, pointTotal, HighScoreController.getUserName());
        }
    }

    @Override
    public void update() {
        super.update();

        if (mLeaderBoardDirty) {
            addLeaderBoardEntries();
            mLeaderBoardDirty = false;
        }
    }

    @Override
    public void dispose() {
        GuiComponent guiComponent = getComponentByType(GuiComponent.class);
        if (guiComponent != null && guiComponent.stage != null) {
            guiComponent.stage.dispose();
        }
    }
}
