package com.darkyen.pv112game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.darkyen.pv112game.font.Font;
import com.darkyen.pv112game.font.GlyphLayout;
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
    private Level level;

    //Render world
    private final ScreenViewport worldViewport = new ScreenViewport(new PerspectiveCamera());
    private Environment environment;
    private Skybox skybox;

    //Render UI
    private final ScreenViewport uiViewport = new ScreenViewport(new OrthographicCamera());
    private SpriteBatch uiBatch;
    private Font font;
    private GlyphLayout glyphLayout;

    public boolean isPaused() {
        return paused;
    }

    public Level getLevel() {
        return level;
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

    @Override
    public void create() {
        // Debug
        GLProfiler.enable();
        GLProfiler.listener = GLErrorListener.LOGGING_LISTENER;

        // Game
        level = new Level(5, 5);

        // World
        environment = new Environment(worldViewport.getCamera());

        /*
        final Environment.PointLight sun = new Environment.PointLight();
        sun.color.set(1f, 1f, 0.8f, 1f);
        sun.position.set(10_000f, 10_000f, 10_000f);
        sun.attenuation.setZero().x = 2f;
        environment.getPointLights().add(sun);
        */
        final Environment.PointLight moon = new Environment.PointLight();
        moon.color.set(0.6f, 0.6f, 1f, 1f);
        moon.position.set(10_000f, 10_000f, 10_000f);
        moon.attenuation.setZero().x = 8f;
        environment.getPointLights().add(moon);

        skybox = new Skybox();

        // UI
        uiBatch = new SpriteBatch();
        font = new Font(Gdx.files.internal("fonts/Abibas.stbfont"));
        glyphLayout = new GlyphLayout(font, true);

        // Begin
        setState(new IntroState());
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

        environment.begin();

        WorldRenderer.render(level, environment);

        environment.end();

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

        state.update(this, Gdx.graphics.getDeltaTime());
        renderWorld();
        state.renderUI(this);
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
}
