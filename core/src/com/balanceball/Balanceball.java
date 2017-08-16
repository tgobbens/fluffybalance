package com.balanceball;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * This code is not representative for my professional work, everything is in a large complex file
 *  ignoring all programming best practises like splitting responsibilities, extendability, etc..
 *   if someone says a class/function should do one thing, this class handles the game :)
 *
 */
public class Balanceball extends ApplicationAdapter {

    private static final String TAG = Balanceball.class.getSimpleName();

    private static boolean DEBUG_RENDERER = false;

    // SMOOTH THE INPUT SAMPLING, HIGHER VALUE LESS RESPONSIVE GAME SO HARDER
    private static final int INPUT_SAMPLE_SIZE = 50;

    private final float BASE_GRAVITY = 150.f;

    private final float BALL_FRICTION_BASE = 0.4f;
    private final float BALL_FRICTION_DECREASE = 0.02f;
    private final float BALL_FRICTION_MIN = 0.05f;

    private float mWorldWidth = 300;
    private float mWorldHeight;

    private OrthographicCamera mCamera;

    private SpriteBatch mSpriteBatch;

    // physics
    private float mAccumulator;
    private World mWorld;

    // stick
    private Texture mStickLeafTexture;
    private Texture mStickHandleTexture;

    private Rectangle mStickHandleDimensions;
    private Rectangle mStickLeafDimen;

    // ball
    private Circle mBall;
    private TextureRegion mBallTextureRegion;

    private Body mBallBody;

    // debug
    private Box2DDebugRenderer mDebugRenderer;
    private SpriteBatch mFontSpriteBatch;
    private BitmapFont mDebugFont;
    private GlyphLayout mDebugTextLayout;

    private Array<Float> mInputRollList = new Array<Float>();
    private float mInputRollAverage = 0.f;

    // points
    private Circle mPointLeft;
    private Circle mPointRight;

    private float mPointAnimationLeftTime = 0.f;
    private float mPointAnimationRightTime = 1.f;

    private TextureRegion mPointsTextureRegion;

    // background clouds
    private Texture mCloud1;
    private Texture mCloud2;
    private Texture mCloud3;

    // game mechanics
    private final static int GAME_STATE_GAME_OVER = 0;
    private final static int GAME_STATE_PLAYING = 1;
    private final static int GAME_STATE_STARTING = 2;

    private int mGameState = GAME_STATE_STARTING;

    private boolean mIsLeftPointAvailable;
    private boolean mIsRightPointAvailable;

    private int mPointTotal = 0;

    // GUI
    private BitmapFont mDefaultFont;

    private Skin mSkin;
    private Stage mGuiGameOverStage;
    private Stage mGuiStage;

    private Label mScoreLabel;
    private Array<Label> mHighScoreLabel;

    private static final Color COLOR_BACKGROUND = new Color(0xb8eaf9ff);
    private static final Color COLOR_FONT = new Color(0xffffffff);

    private final static int BODY_USER_DATA_TYPE_BALL = 0;
    private final static int BODY_USER_DATA_TYPE_POINT_LEFT = 1;
    private final static int BODY_USER_DATA_TYPE_POINT_RIGHT = 2;
    private final static int BODY_USER_DATA_TYPE_OTHER = 3;

    private class BodyUserDate {
        BodyUserDate(int type) {
            this.type = type;
        }

        int type = BODY_USER_DATA_TYPE_OTHER;
    }

    private ShowAdmobListener mAdMobListener = null;

    public Balanceball() {

    }

    public Balanceball(ShowAdmobListener listener) {
        mAdMobListener = listener;
    }

    @Override
	public void create () {
        initCamera();
        initPhysics();

        initRenderer();

        initGuiResources();
        initGuiGameOver();
        initGuiInGame();
        initStick();
        initBall();
        initPoints();
        initCloud();

        onStartGame();

        initDebugRenderer();
	}

	private void initRenderer() {
        mSpriteBatch = new SpriteBatch();
    }

    private void initGuiResources() {
        mFontSpriteBatch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("kenvector_future.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int) (16.f * Gdx.graphics.getDensity());
        mDefaultFont = generator.generateFont(parameter);
        generator.dispose();

        mSkin = new Skin(Gdx.files.internal("skin/flat-earth-ui.json"));
    }

	private void initGuiGameOver() {
        mGuiGameOverStage = new Stage(new ScreenViewport());

        Table rootTable = new Table();
        if (DEBUG_RENDERER) {
            rootTable.debug();
        }

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

        Label label = new Label("GAME OVER", mSkin);
        label.setFontScale(2.f);
        label.setStyle(new Label.LabelStyle(mDefaultFont, COLOR_FONT));
        rootTable.add(label).expand().padTop(dialogHeight * 0.025f);

        rootTable.row();

        TextButton restartButton = new TextButton("RESTART", mSkin);
        restartButton.getLabel().setFontScale(1.f);
        restartButton.getLabel().setStyle(new Label.LabelStyle(mDefaultFont, COLOR_FONT));
        restartButton.addCaptureListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onStartGame();
            }
        });

        Array<Integer> highScores = fetchHighScores();

        mHighScoreLabel = new Array<Label>();

        for (int i = 0; i < highScores.size; i++) {
            Label scoreLabel = new Label("high score: " + highScores.get(i), mSkin);
            scoreLabel.setFontScale(1.0f);
            scoreLabel.setStyle(new Label.LabelStyle(mDefaultFont, COLOR_FONT));
            rootTable.add(scoreLabel).expand();
            rootTable.row();

            mHighScoreLabel.add(scoreLabel);
        }

        // don't like the scene2d interface, if has bad defaults and documentation
        // try to correctly position the bottom restart button
        rootTable.add(restartButton).expand().width(
                dialogWidth - (dialogMargin * 4.f)).height(dialogHeight * 0.1f).bottom().padBottom(dialogHeight * 0.05f);

        mGuiGameOverStage.addActor(rootTable);
    }

    private void initGuiInGame() {
        mGuiStage = new Stage(new ScreenViewport());

        Table gameScore = new Table();
        if (DEBUG_RENDERER) {
            gameScore.debug();
        }

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
                mDefaultFont, COLOR_FONT));
        scoreLabel.setFontScale(1.2f);

        gameScore.add(scoreLabel).expand();

        gameScore.row();

        mScoreLabel = new Label("" + mPointTotal, new Label.LabelStyle(
                mDefaultFont, COLOR_FONT));
        mScoreLabel.setFontScale(2.0f);

        gameScore.add(mScoreLabel).expand();

        mGuiStage.addActor(gameScore);
    }

    private Array<Integer> fetchHighScores() {
        Preferences prefs = Gdx.app.getPreferences("preferences");

        Array<Integer> scores = new Array<Integer>();

        for (int i = 0; i < 5; i++) {
            scores.add(prefs.getInteger("score_" + i, 0));
        }

        return scores;
    }

    private Array<Integer> addHighScore(int score) {
        Array<Integer> scores = fetchHighScores();

        scores.add(score);

        scores.sort();
        scores.reverse();

        scores.pop();

        Preferences prefs = Gdx.app.getPreferences("preferences");

        for (int i = 0; i < scores.size; i++) {
            prefs.putInteger("score_" + i, scores.get(i));
        }

        prefs.flush();

        return scores;
    }

	private void initPhysics() {
        Box2D.init();

        mWorld = new World(new Vector2(0, BASE_GRAVITY), true);

        mWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                BodyUserDate userDateA = (BodyUserDate) contact.getFixtureA().getBody().getUserData();
                BodyUserDate userDateB = (BodyUserDate) contact.getFixtureB().getBody().getUserData();

                // check if there was a contact between the left or right point
                if (userDateA != null && userDateB != null) {
                    if ((userDateA.type == BODY_USER_DATA_TYPE_POINT_LEFT && userDateB.type == BODY_USER_DATA_TYPE_BALL) ||
                            (userDateA.type == BODY_USER_DATA_TYPE_BALL && userDateB.type == BODY_USER_DATA_TYPE_POINT_LEFT)) {
                        // touched left ball
                        onPointContact(BODY_USER_DATA_TYPE_POINT_LEFT);

                    } else if ((userDateA.type == BODY_USER_DATA_TYPE_POINT_RIGHT && userDateB.type == BODY_USER_DATA_TYPE_BALL) ||
                            (userDateA.type == BODY_USER_DATA_TYPE_BALL && userDateB.type == BODY_USER_DATA_TYPE_POINT_RIGHT)) {
                        // touched right ball
                        onPointContact(BODY_USER_DATA_TYPE_POINT_RIGHT);
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    private void initDebugRenderer() {
        if (!DEBUG_RENDERER) {
            return;
        }

        mDebugRenderer = new Box2DDebugRenderer();
        mDebugFont = new BitmapFont();

        mDebugTextLayout = new GlyphLayout(mDebugFont, "test");
    }

	private void initCamera() {
        final float aspectRatio = (float)Gdx.graphics.getHeight() / (float)Gdx.graphics.getWidth();

        mWorldHeight = mWorldWidth * aspectRatio;

        mCamera = new OrthographicCamera(mWorldWidth, mWorldHeight);
        mCamera.position.set(mCamera.viewportWidth / 2f, mCamera.viewportHeight / 2f, 0);
    }

    private void initStick() {
        mStickLeafTexture = new Texture(Gdx.files.internal("stick_leaf.png"));
        mStickHandleTexture = new Texture(Gdx.files.internal("stick_handle.png"));

        final float handleWidth = 20.f;
        final float handleHeight = mWorldHeight * 0.4f; // 40% of screen height

        mStickHandleDimensions = new Rectangle(
                (mWorldWidth / 2.f) - (handleWidth / 2.f),
                0,
                handleWidth,
                handleHeight
        );

        final float leafWidth = (mWorldWidth / 3.f) * 2.f; // 2/3 of screen width
        final float leafHeight = 20.f;

        mStickLeafDimen = new Rectangle(
                (mWorldWidth / 2) - (leafWidth / 2),
                handleHeight,
                leafWidth,
                leafHeight
        );

        // create physics body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(
                mStickLeafDimen.x + (mStickLeafDimen.width / 2),
                mStickLeafDimen.y + (mStickLeafDimen.height / 2));

        // Create a polygon shape
        PolygonShape leafPhysicsBox = new PolygonShape();
        leafPhysicsBox.setAsBox(mStickLeafDimen.width / 2, mStickLeafDimen.height /2);

        Body body = mWorld.createBody(bodyDef);
        body.setUserData(new BodyUserDate(BODY_USER_DATA_TYPE_OTHER));
        Fixture fixture = body.createFixture(leafPhysicsBox, 0.f);
        fixture.setFriction(0.4f);
        fixture.setRestitution(0.4f);

        leafPhysicsBox.dispose();
    }

    private void initBall() {
        float radius = 26;

        mBall = new Circle(0.f, 0.f, radius);

        mBallTextureRegion = new TextureRegion(new Texture(Gdx.files.internal("ball.png")));

        // create physics body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(mBall.x, mBall.y);

        CircleShape ballShape = new CircleShape();
        ballShape.setRadius(mBall.radius - 2.f);

        mBallBody = mWorld.createBody(bodyDef);
        mBallBody.setUserData(new BodyUserDate(BODY_USER_DATA_TYPE_BALL));
        Fixture fixture = mBallBody.createFixture(ballShape, 0.5f);
        fixture.setFriction(BALL_FRICTION_BASE);
        fixture.setRestitution(0.4f);

        ballShape.dispose();
    }

    private void initPoints() {
        float pointOffsetX = 22.f;
        float pointOffsetY = 32.f;
        float radius = 12.f;

        mPointsTextureRegion = new TextureRegion(
                new Texture(Gdx.files.internal("element_yellow_polygon_glossy.png")));

        mPointLeft = new Circle(mStickLeafDimen.x - pointOffsetX,
                mStickLeafDimen.y + mStickLeafDimen.height +  pointOffsetY,
                radius);

        // create physics body
        BodyDef bodyDefL = new BodyDef();
        bodyDefL.type = BodyDef.BodyType.StaticBody;
        bodyDefL.position.set(mPointLeft.x, mPointLeft.y);

        CircleShape pointShapeL = new CircleShape();
        pointShapeL.setRadius(mPointLeft.radius);

        Body mPointLeftBody = mWorld.createBody(bodyDefL);
        mPointLeftBody.setUserData(new BodyUserDate(BODY_USER_DATA_TYPE_POINT_LEFT));
        Fixture fixtureLeftPoint = mPointLeftBody.createFixture(pointShapeL, 0.f);
        fixtureLeftPoint.setSensor(true);

        pointShapeL.dispose();

        // Right points
        mPointRight = new Circle(mStickLeafDimen.x + mStickLeafDimen.width + pointOffsetX,
                mStickLeafDimen.y + mStickLeafDimen.height + pointOffsetY,
                radius);

        // create physics body
        BodyDef bodyDefR = new BodyDef();
        bodyDefR.type = BodyDef.BodyType.StaticBody;
        bodyDefR.position.set(mPointRight.x, mPointRight.y);

        CircleShape pointShapeR = new CircleShape();
        pointShapeR.setRadius(mPointRight.radius);

        Body mPointRightBody = mWorld.createBody(bodyDefR);
        mPointRightBody.setUserData(new BodyUserDate(BODY_USER_DATA_TYPE_POINT_RIGHT));
        Fixture fixtureRightPoint = mPointRightBody.createFixture(pointShapeR, 0.f);
        fixtureRightPoint.setSensor(true);

        pointShapeR.dispose();
    }

    private void initCloud(){
        mCloud1 = new Texture(Gdx.files.internal("cloud5.png"));
        mCloud2 = new Texture(Gdx.files.internal("cloud6.png"));
        mCloud3 = new Texture(Gdx.files.internal("cloud7.png"));
    }

	@Override
	public void render() {
		Gdx.gl.glClearColor(COLOR_BACKGROUND.r, COLOR_BACKGROUND.g, COLOR_BACKGROUND.b, COLOR_BACKGROUND.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mCamera.update();

        updateFromPhysics();

        if (mGameState == GAME_STATE_PLAYING) {
            if (checkGameOver()) {
                onGameOver();
            }
        } else if (mGameState == GAME_STATE_STARTING) {
            onStartGame();
        }

        mSpriteBatch.setProjectionMatrix(mCamera.combined);
        mSpriteBatch.begin();

        renderCloud();
        renderStick();
        renderPoints();
        renderBall();

        mSpriteBatch.end();

        renderGui();

        // prevent user from manipulating input to much
        mInputRollAverage = updateAverage(mInputRollList,
                MathUtils.clamp(Gdx.input.getRoll(), -25.f, 25.f));

        // change the normalisedGravity based upon device roll
        Vector2 gravity = new Vector2(0, -1).rotate(mInputRollAverage).nor().scl(BASE_GRAVITY);

        mWorld.setGravity(gravity);

        debugRender();

        stepWorld();
	}

	private void renderStick() {
        mSpriteBatch.draw(mStickHandleTexture, mStickHandleDimensions.x, mStickHandleDimensions.y,
                mStickHandleDimensions.width, mStickHandleDimensions.height);

        mSpriteBatch.draw(mStickLeafTexture, mStickLeafDimen.x, mStickLeafDimen.y,
                mStickLeafDimen.width, mStickLeafDimen.height);
    }

    private void renderPoints() {
        mPointAnimationLeftTime += Gdx.graphics.getDeltaTime();
        mPointAnimationRightTime += Gdx.graphics.getDeltaTime();

        // render points
        if (mIsLeftPointAvailable) {
            float radius = mPointLeft.radius + (MathUtils.sin(mPointAnimationLeftTime * 4.1f) * 2.f);

            mSpriteBatch.draw(mPointsTextureRegion,
                    mPointLeft.x - radius,
                    mPointLeft.y - radius,
                    radius,
                    radius,
                    radius * 2,
                    radius * 2,
                    1.f,
                    1.f,
                    10.f
            );
        }

        if (mIsRightPointAvailable) {
            float radius = mPointRight.radius + (MathUtils.sin(mPointAnimationRightTime * 4.f) * 2.f);

            mSpriteBatch.draw(mPointsTextureRegion,
                    mPointRight.x - radius,
                    mPointRight.y - radius,
                    radius,
                    radius,
                    radius * 2,
                    radius * 2,
                    1.f,
                    1.f,
                    -10.f
            );
        }
    }

    private void renderBall() {
        // render ball
        mSpriteBatch.draw(mBallTextureRegion,
                mBall.x - mBall.radius,
                mBall.y - mBall.radius,
                mBall.radius,
                mBall.radius,
                mBall.radius * 2,
                mBall.radius * 2,
                1.f,
                1.f,
                mBallBody.getAngle() * MathUtils.radDeg
        );
    }

    private void renderCloud() {
        mSpriteBatch.draw(mCloud1, -100, 350);
        mSpriteBatch.draw(mCloud2, -200, 20);
        mSpriteBatch.draw(mCloud3, 200, 150);
    }

	private void renderGui() {
        if (mGameState == GAME_STATE_PLAYING) {
            mScoreLabel.setText(""  + mPointTotal);

            mGuiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            mGuiStage.draw();
        } else if (mGameState == GAME_STATE_GAME_OVER) {
            mGuiGameOverStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            mGuiGameOverStage.draw();
        }
    }

	private float updateAverage(Array<Float> list, float value) {
        // this is not very efficient as it will trigger a memcopy every time
        if (list.size > INPUT_SAMPLE_SIZE) {
            list.removeIndex(0);
        }

        list.add(value);

        float total = 0.f;
        for (float gyro : list) {
            total += gyro;
        }

        return total / list.size;
    }

	private void debugRender() {
        if (!DEBUG_RENDERER) {
            return;
        }

        mDebugRenderer.render(mWorld, mCamera.combined);

        mDebugTextLayout.setText(mDebugFont,
                "getRoll: (" + mInputRollAverage + ") \n"
        );

        mFontSpriteBatch.begin();
        mFontSpriteBatch.setProjectionMatrix(mCamera.combined);

        mDebugFont.draw(mFontSpriteBatch, mDebugTextLayout,
                (mWorldWidth / 2) - (mDebugTextLayout.width / 2),
                (mWorldHeight / 2) - (mDebugTextLayout.height / 2));

        mFontSpriteBatch.end();
    }

	private void updateFromPhysics() {
        mBall.setPosition(mBallBody.getPosition().x, mBallBody.getPosition().y);
    }

    private void stepWorld() {
        // if the game is not running don't update the physics
        if (mGameState != GAME_STATE_PLAYING) {
            return;
        }

        // some constants taken from documentation, could be tweaked
        final float timeStep = 1.f / 45.f;
        final int velocityIterations = 6;
        final int positionIterations = 2;

        float deltaTime = Gdx.graphics.getDeltaTime();

        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        mAccumulator += frameTime;
        while (mAccumulator >= timeStep) {
            mWorld.step(timeStep, velocityIterations, positionIterations);
            mAccumulator -= timeStep;
        }
    }

    private boolean checkGameOver() {
        return mBall.x < 0 ||
                mBall.x > mWorldWidth ||
                mBall.y < 0 ||
                mBall.y > mWorldHeight;

    }

    private void onPointContact(int pointType) {
        if (pointType == BODY_USER_DATA_TYPE_POINT_LEFT) {
            mIsLeftPointAvailable = false;
            mIsRightPointAvailable = true;
        } else {
            mIsLeftPointAvailable = true;
            mIsRightPointAvailable = false;
        }

        mPointTotal++;

        final float friction = MathUtils.clamp(
                BALL_FRICTION_BASE - (BALL_FRICTION_DECREASE * mPointTotal), BALL_FRICTION_MIN, 1.f);

        mBallBody.getFixtureList().get(0).setFriction(friction);
    }

    private void onGameOver() {
        if (mGameState == GAME_STATE_GAME_OVER) {
            return;
        }

        mGameState = GAME_STATE_GAME_OVER;

        Array<Integer> highScores = addHighScore(mPointTotal);

        for (int i = 0; i < Math.min(mHighScoreLabel.size, highScores.size); i++) {
            mHighScoreLabel.get(i).setText("high score: " + highScores.get(i));
        }

        Gdx.input.setInputProcessor(mGuiGameOverStage);
    }

    private void onStartGame() {
        if (mGameState == GAME_STATE_PLAYING) {
            return;
        }

        mGameState = GAME_STATE_PLAYING;

        final float heightStartOffset = 20.f;

        float ballX = mStickLeafDimen.getX() + mStickLeafDimen.getWidth() / 2.f;
        float ballY = mStickLeafDimen.getY() + mStickLeafDimen.getHeight() + mBall.radius + heightStartOffset;

        mBallBody.setTransform(ballX, ballY, 0);

        mBallBody.setLinearVelocity(MathUtils.randomBoolean() ? -10.f : 10.f, 0.f);
        mBallBody.setAngularVelocity(0.f);

        mPointTotal = 0;

        mIsLeftPointAvailable = true;
        mIsRightPointAvailable = true;

        mInputRollList.clear();

        Gdx.input.setInputProcessor(mGuiStage);

        if (mAdMobListener != null) {
            mAdMobListener.showAd();
        }
    }
	
	@Override
	public void dispose () {
        mSpriteBatch.dispose();

        mBallTextureRegion.getTexture().dispose();

        mStickLeafTexture.dispose();
        mStickHandleTexture.dispose();

        mPointsTextureRegion.getTexture().dispose();

        mWorld.dispose();

        mFontSpriteBatch.dispose();
        mDefaultFont.dispose();

        if (mDebugRenderer != null) {
            mDebugRenderer.dispose();
        }

        if (mDebugFont != null) {
            mDebugFont.dispose();
        }

        mGuiGameOverStage.dispose();
        mGuiStage.dispose();

        mSkin.dispose();
	}
}
