package com.darkyen.pv112game.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.darkyen.pv112game.Game;
import com.darkyen.pv112game.State;
import com.darkyen.pv112game.font.GlyphLayout;
import com.darkyen.pv112game.game.Level;
import com.darkyen.pv112game.gl.SpriteBatch;

/**
 *
 */
public class IntroState extends State {

    private float time = 0f;

    private boolean zoomingIn = false;
    private float timeBeforeZoomingIn;
    private final float ZOOMING_IN_DURATION = 1f;

    public IntroState(Game game) {
        super(game);
    }

    @Override
    public void update(float delta) {
        this.time += delta;

        final Camera camera = game.getWorldViewport().getCamera();
        final Level level = game.getLevel();

        final float radius = Vector2.len(level.width, level.height)/2f;
        final float rotationSpeed = 1f/radius;
        final float rotationTime = zoomingIn ? timeBeforeZoomingIn : time;
        camera.position.set(
                (float)Math.sin(rotationTime * rotationSpeed) * radius,
                radius/2f,
                (float)Math.cos(rotationTime * rotationSpeed) * radius);
        camera.direction.set(camera.position).scl(-1f).nor();
        camera.position.add(level.width/2f, 0f, level.height/2f);

        if (zoomingIn) {
            final Vector3 zoomedPosition = new Vector3();
            final Vector3 zoomedDirection = new Vector3();
            GameState.cameraToMowerView(level, zoomedPosition, zoomedDirection);

            final float progress = Interpolation.smooth.apply(time / ZOOMING_IN_DURATION);

            camera.position.lerp(zoomedPosition, progress);
            camera.direction.lerp(zoomedDirection, progress).nor();
        }

        camera.update();

        if (zoomingIn && time >= ZOOMING_IN_DURATION) {
            game.setState(new GameState(game));
        }

        if (!zoomingIn && (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) || Gdx.input.isTouched())) {
            timeBeforeZoomingIn = time;
            time = 0f;
            zoomingIn = true;
        }
    }

    @Override
    public void renderUI() {
        final SpriteBatch uiBatch = game.getUiBatch();
        uiBatch.begin(game.getUiViewport().getCamera(), true);

        final GlyphLayout glyphs = game.getSharedGlyphLayout();
        final Color white = Color.WHITE.cpy();
        if (zoomingIn) {
            white.a = Interpolation.smooth.apply(1f - time / ZOOMING_IN_DURATION);
        }
        glyphs.setText("{DARK_GRAY}Midnight{} {FOREST}Rider", white, Gdx.graphics.getWidth(), Align.center);
        glyphs.draw(uiBatch, 0f, Gdx.graphics.getHeight() / 4 * 3 + glyphs.height/2);

        float pressAnyKeyAlpha = 0f;
        if ((zoomingIn ? timeBeforeZoomingIn : time) > 5f) {
            pressAnyKeyAlpha = 1f;
        } else if ((zoomingIn ? timeBeforeZoomingIn : time) > 4f) {
            pressAnyKeyAlpha = time - 4f;
        }
        if (zoomingIn) {
            pressAnyKeyAlpha *= Interpolation.fade.apply(1f - (time / ZOOMING_IN_DURATION));
        }

        if (pressAnyKeyAlpha > 0f) {
            final Color color = Color.GRAY.cpy();
            color.a = pressAnyKeyAlpha;
            final String anyKeyText;
            if (time > 20f) {
                anyKeyText = "Press any key to play\nIf you don't have the {LIGHT_GRAY}any{} key,\npress any other key.";
            } else {
                anyKeyText = "Press any key to play";
            }
            glyphs.setText(anyKeyText, color, 0f, Align.center);
            glyphs.draw(uiBatch, (Gdx.graphics.getWidth() - glyphs.width)/2f, Gdx.graphics.getHeight() / 4 + glyphs.height/2);
        }

        uiBatch.end();
    }
}
