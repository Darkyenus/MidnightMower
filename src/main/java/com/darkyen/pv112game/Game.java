package com.darkyen.pv112game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.darkyen.pv112game.font.Font;
import com.darkyen.pv112game.font.GlyphLayout;
import com.darkyen.pv112game.game.Models;
import com.darkyen.pv112game.game.World;
import com.darkyen.pv112game.gl.*;

/**
 *
 */
public class Game implements ApplicationListener {

    private final ScreenViewport worldViewport = new ScreenViewport(new PerspectiveCamera());
    private final ScreenViewport uiViewport = new ScreenViewport(new OrthographicCamera());

    private Environment environment;
    private Skybox skybox;

    private World world;
    private FirstPersonCameraController cameraController;

    private SpriteBatch uiBatch;

    private Font font;
    private GlyphLayout glyphLayout;

    @Override
    public void create() {
        GLProfiler.enable();
        GLProfiler.listener = GLErrorListener.LOGGING_LISTENER;

        environment = new Environment(worldViewport.getCamera(), new Shader(Gdx.files.internal("shaders/world-vert.glsl"), Gdx.files.internal("shaders/world-frag.glsl")));

        final Environment.PointLight sun = new Environment.PointLight();
        sun.color.set(1f, 1f, 0.8f, 1f);
        sun.position.set(10_000f, 10_000f, 10_000f);
        sun.attenuation.setZero().x = 2f;
        environment.pointLights.add(sun);

        skybox = new Skybox();

        world = new World(100, 100);

        cameraController = new FirstPersonCameraController(worldViewport.getCamera());
        worldViewport.getCamera().position.set(10, 10, 10);
        worldViewport.getCamera().lookAt(0, 0, 0);
        worldViewport.getCamera().up.set(0, 1, 0);
        Gdx.input.setInputProcessor(cameraController);

        uiBatch = new SpriteBatch();
        font = new Font(Gdx.files.internal("fonts/Abibas.stbfont"));
        glyphLayout = new GlyphLayout(font, true);
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, false);
        uiViewport.update(width, height, true);
    }

    @Override
    public void render() {
        cameraController.update();

        worldViewport.apply(false);
        final GL20 gl = Gdx.gl30;
        gl.glDepthMask(true);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        gl.glEnable(GL20.GL_DEPTH_TEST);
        gl.glEnable(GL20.GL_CULL_FACE);


        environment.begin();

        world.draw(environment);

        environment.end();

        skybox.draw(worldViewport);

        gl.glDisable(GL20.GL_DEPTH_TEST);
        gl.glDisable(GL20.GL_CULL_FACE);
        gl.glDepthMask(false);

        uiBatch.begin(uiViewport.getCamera(), true);
        glyphLayout.setText("{RED}FPS "+Gdx.graphics.getFramesPerSecond(), Color.WHITE, Gdx.graphics.getWidth(), Align.center);
        glyphLayout.draw(uiBatch, 0, Gdx.graphics.getHeight());
        uiBatch.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
