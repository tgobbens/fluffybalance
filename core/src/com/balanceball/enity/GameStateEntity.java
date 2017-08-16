package com.balanceball.enity;

import com.balanceball.component.GuiComponent;
import com.balanceball.component.VisibleComponent;
import com.balanceball.controller.HighScoreController;
import com.balanceball.system.GuiRendererSystem;
import com.balanceball.system.PhysicsSystem;
import com.sec.Entity;

/**
 * Created by tijs on 18/07/2017.
 */

public class GameStateEntity extends Entity implements PhysicsSystem.BallPointContactListener {
    // game mechanics
    private final static int GAME_STATE_GAME_OVER = 0;
    private final static int GAME_STATE_PLAYING = 1;
    private final static int GAME_STATE_STARTING = 2;

    private int mGameState = GAME_STATE_STARTING;

    private PointEntity mPointLeft;
    private PointEntity mPointRight;

    private int mPointTotal = 0;
    private int mRound = 0;

    public GameStateEntity(PointEntity left, PointEntity right) {
        mPointLeft = left;
        mPointRight = right;
    }

    public int getPointsTotal() {
        return mPointTotal;
    }

    public int getRound() {
        return mRound;
    }

    @Override
    public void update() {
        if (mGameState == GAME_STATE_STARTING) {
            startGame();
        }
    }

    private void startGame() {
        if (mGameState == GAME_STATE_PLAYING) {
            return;
        }

        mEngine.getFirstEntityOfType(BallEntity.class).onGameStart();

        mPointLeft.setIsAvailable(true);
        mPointRight.setIsAvailable(true);

        mPointTotal = 0;
        mRound++;

        mGameState = GAME_STATE_PLAYING;

        mEngine.getFirstSystemOfType(GuiRendererSystem.class)
                .activateGui(IngameGuiEntity.class);

        mEngine.getFirstEntityOfType(RollInputEntity.class)
                .resetInput();
    }

    public void onStartGame() {
        if (mGameState == GAME_STATE_GAME_OVER) {
            startGame();
        }
    }

    public void onGameOver() {
        if (mGameState != GAME_STATE_PLAYING) {
            return;
        }

        mGameState = GAME_STATE_GAME_OVER;

        mEngine.getFirstSystemOfType(GuiRendererSystem.class)
                .activateGui(HighScoreGuiEntity.class);

        HighScoreController.addHighScore(mPointTotal);
    }

    @Override
    public void onPointContact(int pointType) {
        if (pointType == PhysicsSystem.BODY_USER_DATA_TYPE_POINT_LEFT) {
            if (!mPointLeft.getIsAvailable()) {
                return; // don't increase points if this point is not available
            }

            mPointLeft.setIsAvailable(false);
            mPointRight.setIsAvailable(true);
        } else {
            if (!mPointRight.getIsAvailable()) {
                return; // don't increase points if this point is not available
            }

            mPointLeft.setIsAvailable(true);
            mPointRight.setIsAvailable(false);
        }

        mPointTotal++;

        mEngine.getFirstEntityOfType(BallEntity.class).onScoreChanged(mPointTotal);

        mEngine.getFirstEntityOfType(IngameGuiEntity.class).onScoreChanged(mPointTotal);
    }
}
