package com.darkyen.pv112game.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.darkyen.pv112game.Game;
import com.darkyen.pv112game.State;
import com.darkyen.pv112game.font.GlyphLayout;
import com.darkyen.pv112game.game.Level;
import com.darkyen.pv112game.gl.SpriteBatch;

/**
 *
 */
public final class IntroState extends State {

    private float time = 0f;

    private boolean zoomingIn = false;
    private static final float ZOOMING_IN_DURATION = 1f;

    public IntroState(Game game) {
        super(game);
    }

    @Override
    public void begin() {
        game.cameraman.reset((position, direction) -> {
            final Level level = game.level;

            final float radius = Vector2.len(level.width, level.height)/2f;
            final float rotationSpeed = 1f/radius;
            final float rotationTime = time;
            position.set(
                    (float)Math.sin(rotationTime * rotationSpeed) * radius,
                    radius/2f,
                    (float)Math.cos(rotationTime * rotationSpeed) * radius);
            direction.set(position).scl(-1f).nor();
            position.add(level.width/2f, 0f, level.height/2f);
        });
    }

    @Override
    public void update(float delta) {
        if (!zoomingIn) {
            this.time += delta;
        }

        game.cameraman.apply(game.getWorldViewport().getCamera());
    }

    @Override
    public void renderUI() {
        final SpriteBatch uiBatch = game.getUiBatch();
        uiBatch.begin(game.getUiViewport().getCamera(), true);

        final GlyphLayout glyphs = game.getSharedGlyphLayout();
        final Color white = Color.WHITE.cpy();
        if (zoomingIn) {
            white.a = Interpolation.smooth.apply(1f - game.cameraman.getTransitionProgress());
        }
        glyphs.setText("{DARK_GRAY}Midnight{} {FOREST}Rider", white, Gdx.graphics.getWidth(), Align.center);
        glyphs.draw(uiBatch, 0f, Gdx.graphics.getHeight() / 4 * 3 + glyphs.height/2);

        float pressAnyKeyAlpha = 0f;
        if (time > 5f) {
            pressAnyKeyAlpha = 1f;
        } else if (time > 4f) {
            pressAnyKeyAlpha = time - 4f;
        }
        if (zoomingIn) {
            pressAnyKeyAlpha *= Interpolation.fade.apply(1f - game.cameraman.getTransitionProgress());
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

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.F5) {
            game.level = new Level(1, System.currentTimeMillis());
            return true;
        }
        if (keycode == Input.Keys.F6) {
            game.level = new Level((int) (System.currentTimeMillis() % 100), System.currentTimeMillis());
            return true;
        }
        if (keycode == Input.Keys.F7) {
            game.level = new Level(66, 66);
            return true;
        }

        if (!zoomingIn) {
            zoomingIn = true;
            game.cameraman.next(game.CAMERA_SHOT_PLAYER_VIEW, ZOOMING_IN_DURATION, Interpolation.smooth);
            game.schedule(ZOOMING_IN_DURATION, () -> {
                game.setState(new GameState(game, 0));
            });
            return true;
        }
        return super.keyUp(keycode);
    }
}
