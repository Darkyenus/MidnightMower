package com.darkyen.pv112game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.darkyen.pv112game.font.Font;
import com.darkyen.pv112game.font.GlyphLayout;
import com.darkyen.pv112game.game.Cameraman;
import com.darkyen.pv112game.game.Level;
import com.darkyen.pv112game.game.WorldRenderer;
import com.darkyen.pv112game.gl.Environment;
import com.darkyen.pv112game.gl.Skybox;
import com.darkyen.pv112game.gl.SpriteBatch;
import com.darkyen.pv112game.state.IntroState;

/**
 *
 */
public final class Game implements ApplicationListener {

    // Game
    private boolean paused = false;
    private State state = null;
    private State nextState = null;
    public Level level;

    //Render world
    private final ScreenViewport worldViewport = new ScreenViewport(new PerspectiveCamera());
    private Environment environment;
    private Skybox skybox;

    private final Environment.PointLight leftHeadlight = new Environment.PointLight();
    private final Environment.PointLight rightHeadlight = new Environment.PointLight();

    public final Cameraman cameraman = new Cameraman(Cameraman.NULL_CAMERA_SHOT);

    //Render UI
    private final ScreenViewport uiViewport = new ScreenViewport(new OrthographicCamera());
    private SpriteBatch uiBatch;
    private Font font;
    private GlyphLayout glyphLayout;

    //Scheduling
    private final FloatArray scheduledTime = new FloatArray();
    private final Array<Runnable> scheduledAction = new Array<>();

    // Sounds
    private Sound soundIntroCrickets;
    private long soundIntroCricketsId = -1;


    public boolean isPaused() {
        return paused;
    }

    public ScreenViewport getWorldViewport() {
        return worldViewport;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ScreenViewport getUiViewport() {
        return uiViewport;
    }

    public SpriteBatch getUiBatch() {
        return uiBatch;
    }

    public Font getFont() {
        return font;
    }

    public GlyphLayout getSharedGlyphLayout() {
        return glyphLayout;
    }

    public void setState(State state) {
        nextState = state;
    }

    public void schedule(float inSeconds, Runnable action) {
        scheduledTime.add(inSeconds);
        scheduledAction.add(action);
    }

    public void clearSchedule() {
        scheduledTime.clear();
        scheduledAction.clear();
    }

    public void startCrickets() {
        if (soundIntroCrickets == null) {
            soundIntroCrickets = Gdx.audio.newSound(Gdx.files.internal("sounds/crickets.ogg"));
        }
        if (soundIntroCricketsId == -1) {
            soundIntroCricketsId = soundIntroCrickets.loop();
        }
    }

    public void stopCrickets() {
        if (soundIntroCricketsId != -1) {
            soundIntroCrickets.stop(soundIntroCricketsId);
            soundIntroCricketsId = -1;
        }
    }

    @Override
    public void create() {
        // Debug
        GLProfiler.enable();
        GLProfiler.listener = GLErrorListener.LOGGING_LISTENER;

        // Game
        level = new Level(1, 5, 5, System.currentTimeMillis());

        // World
        environment = new Environment(worldViewport.getCamera());

        /*
        final Environment.PointLight sun = new Environment.PointLight();
        sun.color.set(1f, 1f, 0.8f, 1f);
        sun.position.set(10_000f, 10_000f, 10_000f);
        sun.attenuation.setZero().x = 2f;
        environment.getPointLights().add(sun);
        */
        environment.setAmbientLight(new Color(0.2f, 0.2f, 0.2f, 1f));

        final Environment.PointLight moon = new Environment.PointLight();
        moon.color.set(0.6f, 0.6f, 1f, 1f);
        moon.position.set(10_000f, 10_000f, 10_000f);
        moon.attenuation.setZero().x = 4f;
        environment.getPointLights().add(moon);

        {
            leftHeadlight.color.set(Color.WHITE);
            rightHeadlight.color.set(leftHeadlight.color);

            leftHeadlight.attenuation.set(0.1f, 0f, 0.6f);
            rightHeadlight.attenuation.set(leftHeadlight.attenuation);

            leftHeadlight.directionCutoff = 0.85f;
            rightHeadlight.directionCutoff = leftHeadlight.directionCutoff;
        }

        skybox = new Skybox();

        // UI
        uiBatch = new SpriteBatch();
        font = new Font(Gdx.files.internal("fonts/Abibas.stbfont"));
        glyphLayout = new GlyphLayout(font, true);

        // Begin
        setState(new IntroState(this));
        startCrickets();
    }

    public void enableHeadlights() {
        environment.getPointLights().add(leftHeadlight);
        environment.getPointLights().add(rightHeadlight);
    }

    public void disableHeadlights() {
        environment.getPointLights().removeValue(leftHeadlight, true);
        environment.getPointLights().removeValue(rightHeadlight, true);
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, false);
        uiViewport.update(width, height, true);
    }

    private void renderWorld() {
        final GL20 gl = Gdx.gl30;

        worldViewport.apply(false);
        gl.glDepthMask(true);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL20.GL_DEPTH_TEST);
        gl.glEnable(GL20.GL_CULL_FACE);

        state.preRender();

        leftHeadlight.direction.set(MathUtils.sinDeg(level.playerAngle), 0f, MathUtils.cosDeg(level.playerAngle));
        rightHeadlight.direction.set(leftHeadlight.direction);
        leftHeadlight.position.set(level.playerPos.x, 0.1f, level.playerPos.y);
        rightHeadlight.position.set(leftHeadlight.position);
        final float lightOffset = 0.15f;
        leftHeadlight.position.add(leftHeadlight.direction.z * lightOffset, 0f, -leftHeadlight.direction.x * lightOffset);
        rightHeadlight.position.add(-leftHeadlight.direction.z * lightOffset, 0f, leftHeadlight.direction.x * lightOffset);

        environment.getPointLights();// Mark dirty

        environment.begin();
        WorldRenderer.render(level, environment);
        environment.end();

        state.postRender();

        skybox.draw(worldViewport);
    }

    @Override
    public void render() {
        if (nextState != null) {
            if (this.state != null) {
                this.state.end();
            }
            this.state = nextState;
            nextState = null;
            if (this.state != null) {
                this.state.begin();
            }
            Gdx.input.setInputProcessor(state);
        }

        final float delta = Gdx.graphics.getDeltaTime();

        // Update scheduled
        for (int i = 0; i < scheduledTime.size;) {
            scheduledTime.items[i] -= delta;
            if (scheduledTime.items[i] <= 0f) {
                scheduledTime.removeIndex(i);
                scheduledAction.removeIndex(i).run();
            } else {
                i++;
            }
        }

        cameraman.update(delta);
        state.update(delta);
        renderWorld();
        state.renderUI();
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void dispose() {
        environment.dispose();
        skybox.dispose();
        uiBatch.dispose();
        font.dispose();
    }

    public final Cameraman.CameraShot CAMERA_SHOT_PLAYER_VIEW = new Cameraman.CameraShot() {

        private final Vector2 offset = new Vector2();

        @Override
        public void set(Vector3 position, Vector3 direction) {
            position.set(level.playerPos.x, 1.5f, level.playerPos.y);

            final Vector2 offset = this.offset.set(0f, -2f).rotate(-level.playerAngle);
            position.add(offset.x, 0f, offset.y);

            direction.set(-offset.x, -0.4f, -offset.y).nor();
        }
    };
}
