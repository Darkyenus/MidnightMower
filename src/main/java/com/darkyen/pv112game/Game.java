package com.darkyen.pv112game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.darkyen.pv112game.font.Font;
import com.darkyen.pv112game.font.GlyphLayout;
import com.darkyen.pv112game.game.Level;
import com.darkyen.pv112game.game.WorldRenderer;
import com.darkyen.pv112game.gl.Environment;
import com.darkyen.pv112game.gl.Skybox;
import com.darkyen.pv112game.gl.SpriteBatch;

/**
 *
 */
public class Game implements ApplicationListener {

    // Game
    private boolean paused = false;
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

    //Debug
    private boolean debugMode = false;
    private FirstPersonCameraController debugCameraController;

    @Override
    public void create() {
        // Debug
        GLProfiler.enable();
        GLProfiler.listener = GLErrorListener.LOGGING_LISTENER;
        debugCameraController = new FirstPersonCameraController(worldViewport.getCamera());
        worldViewport.getCamera().position.set(10, 10, 10);
        worldViewport.getCamera().lookAt(0, 0, 0);
        worldViewport.getCamera().up.set(0, 1, 0);

        // Input
        final InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                return super.keyDown(keycode);
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.F3) {
                    if (debugMode) {
                        debugMode = false;
                        inputMultiplexer.removeProcessor(debugCameraController);
                    } else {
                        debugMode = true;
                        inputMultiplexer.addProcessor(debugCameraController);
                    }
                    return true;
                }
                return super.keyUp(keycode);
            }
        });
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Game
        level = new Level(7, 7);

        // World
        environment = new Environment(worldViewport.getCamera());

        final Environment.PointLight sun = new Environment.PointLight();
        sun.color.set(1f, 1f, 0.8f, 1f);
        sun.position.set(10_000f, 10_000f, 10_000f);
        sun.attenuation.setZero().x = 2f;
        environment.getPointLights().add(sun);

        skybox = new Skybox();

        // UI
        uiBatch = new SpriteBatch();
        font = new Font(Gdx.files.internal("fonts/Abibas.stbfont"));
        glyphLayout = new GlyphLayout(font, true);
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, false);
        uiViewport.update(width, height, true);
    }

    private void update(float gameDelta) {
        level.playerAngle += gameDelta;
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

    private void renderUI() {
        final GL20 gl = Gdx.gl30;
        gl.glDisable(GL20.GL_DEPTH_TEST);
        gl.glDisable(GL20.GL_CULL_FACE);
        gl.glDepthMask(false);

        uiViewport.apply(true);

        uiBatch.begin(uiViewport.getCamera(), true);
        if (debugMode) {
            glyphLayout.setText("FPS: "+Gdx.graphics.getFramesPerSecond()+"\nX: "+level.playerX+"\nY: "+level.playerY+"\nA: "+level.playerAngle+"\nGrass: "+level.remainingGrass, Color.WHITE, Gdx.graphics.getWidth(), Align.left);
        } else {
            glyphLayout.setText("Remaining: "+level.remainingGrass, Color.WHITE, Gdx.graphics.getWidth(), Align.left);
        }
        glyphLayout.draw(uiBatch, 0, Gdx.graphics.getHeight());
        uiBatch.end();
    }

    @Override
    public void render() {
        debugCameraController.update();

        update(paused ? 0f : Gdx.graphics.getDeltaTime());
        renderWorld();
        renderUI();
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
