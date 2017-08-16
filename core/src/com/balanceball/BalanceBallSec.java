package com.balanceball;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.balanceball.component.PositionComponent;
import com.balanceball.component.SizeComponent;
import com.balanceball.enity.GameStateEntity;
import com.balanceball.enity.GameWorldEntity;
import com.balanceball.enity.GravityEntity;
import com.balanceball.enity.BallEntity;
import com.balanceball.enity.CameraEntity;
import com.balanceball.enity.HighScoreGuiEntity;
import com.balanceball.enity.IngameGuiEntity;
import com.balanceball.enity.LeaderBoardGuiEntity;
import com.balanceball.enity.PhysicsDebugEntity;
import com.balanceball.enity.PointEntity;
import com.balanceball.enity.RollInputEntity;
import com.balanceball.enity.SpriteEntity;
import com.balanceball.enity.StickLeafEntity;
import com.balanceball.enity.UserNameGuiEntity;
import com.balanceball.system.GuiRendererSystem;
import com.balanceball.system.PhysicsSystem;
import com.balanceball.system.SpriteRenderSystem;
import com.sec.Engine;

/**
 * Created by tijs on 14/07/2017.
 */

public class BalanceBallSec extends ApplicationAdapter {

    private static final String TAG = Balanceball.class.getSimpleName();

    private static final Color COLOR_BACKGROUND = new Color(0xb8eaf9ff);

    private ShowAdmobListener mAdMobListener = null;

    private Engine mEngine;

    private static final int WORLD_WIDTH = 300;

    private static boolean DEBUG_RENDERER = false;

    private BitmapFont mFont;
    private Skin mSkin;

    private boolean mHasShownAdd = false;

    public BalanceBallSec() {

    }

    public BalanceBallSec(ShowAdmobListener listener) {
        mAdMobListener = listener;
    }

    @Override
    public void create () {
        init();

        mEngine.create();
    }

    private void initGuiResources() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("kenvector_future.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int) (16.f * Gdx.graphics.getDensity());
        mFont = generator.generateFont(parameter);
        generator.dispose();

        mSkin = new Skin(Gdx.files.internal("skin/flat-earth-ui.json"));
    }

    private void init() {
        initGuiResources();

        // debug system
        PhysicsDebugEntity physicsDebugEntity = DEBUG_RENDERER ? new PhysicsDebugEntity() : null;

        // renderer
        final float aspectRatio = (float) Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth();
        int worldHeight = (int) ((float)WORLD_WIDTH * aspectRatio);

        GameWorldEntity gameWorldEntity = new GameWorldEntity(WORLD_WIDTH, worldHeight);

        // gravity
        GravityEntity gravityEntity = new GravityEntity();

        // render system
        SpriteRenderSystem spriteRenderSystem = new SpriteRenderSystem();

        // camera
        CameraEntity cameraEntity = new CameraEntity();

        // handle
        final float handleHeight = worldHeight * 0.4f; // 40% of screen height
        final float handleWidth = 20.f;
        SpriteEntity stickHandle = new SpriteEntity(
                "stick_handle.png", new Vector2(WORLD_WIDTH * 0.5f, handleHeight * 0.5f),
                handleWidth, handleHeight, 0);
        StickLeafEntity stickLeaf = new StickLeafEntity(
                WORLD_WIDTH, (int) handleHeight);

        // points
        PointEntity pointEntityLeft = new PointEntity(true,
                stickLeaf.getComponentByType(PositionComponent.class).position,
                stickLeaf.getComponentByType(SizeComponent.class).width,
                stickLeaf.getComponentByType(SizeComponent.class).height);

        PointEntity pointEntityRight = new PointEntity(false,
                stickLeaf.getComponentByType(PositionComponent.class).position,
                stickLeaf.getComponentByType(SizeComponent.class).width,
                stickLeaf.getComponentByType(SizeComponent.class).height);

        GameStateEntity gameStateEntity = new GameStateEntity(pointEntityLeft, pointEntityRight);

        PhysicsSystem physicsSystem = new PhysicsSystem(gameStateEntity);

        // ball
        BallEntity ball = new BallEntity(WORLD_WIDTH, (int) handleHeight);

        // (background) clouds
        SpriteEntity cloud1 = new SpriteEntity("cloud5.png", new Vector2(60, 320), 200);
        SpriteEntity cloud2 = new SpriteEntity("cloud6.png", new Vector2(-20, 60), 220);
        SpriteEntity cloud3 = new SpriteEntity("cloud7.png", new Vector2(300, 210), 180);

        // input entity
        RollInputEntity rollInputEntity = new RollInputEntity();

        // GUI
        IngameGuiEntity ingameGuiEntity = new IngameGuiEntity(mFont);
        HighScoreGuiEntity highScoreGuiEntity = new HighScoreGuiEntity(mSkin, mFont);
        UserNameGuiEntity userNameGuiEntity = new UserNameGuiEntity(mSkin, mFont);
        LeaderBoardGuiEntity leaderBoardGuiEntity = new LeaderBoardGuiEntity(mSkin, mFont);

        // Gui system
        GuiRendererSystem guiRendererSystem = new GuiRendererSystem();

        // init engine
        mEngine = new Engine();
        mEngine.registerSystem(physicsSystem);
        mEngine.registerSystem(spriteRenderSystem);
        mEngine.registerSystem(guiRendererSystem);

        mEngine.registerEntity(gameWorldEntity);
        mEngine.registerEntity(gameStateEntity);
        mEngine.registerEntity(cloud1);
        mEngine.registerEntity(cloud2);
        mEngine.registerEntity(cloud3);
        mEngine.registerEntity(stickHandle);
        mEngine.registerEntity(stickLeaf);
        mEngine.registerEntity(pointEntityLeft);
        mEngine.registerEntity(pointEntityRight);
        mEngine.registerEntity(ball);
        mEngine.registerEntity(rollInputEntity);
        mEngine.registerEntity(cameraEntity);
        mEngine.registerEntity(gravityEntity);
        mEngine.registerEntity(ingameGuiEntity);
        mEngine.registerEntity(highScoreGuiEntity);
        mEngine.registerEntity(userNameGuiEntity);
        mEngine.registerEntity(leaderBoardGuiEntity);

        if (physicsDebugEntity != null) {
            mEngine.registerEntity(physicsDebugEntity);
        }

        mEngine.resume();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(COLOR_BACKGROUND.r, COLOR_BACKGROUND.g, COLOR_BACKGROUND.b, COLOR_BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mEngine.update();

        if (mAdMobListener != null && !mHasShownAdd) {
            mAdMobListener.showAd();
            mHasShownAdd = true;
        }
    }

    @Override
    public void dispose () {
        mEngine.pause();

        mEngine.dispose();

        mFont.dispose();

        mSkin.dispose();
    }
}