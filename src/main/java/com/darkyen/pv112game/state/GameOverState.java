package com.darkyen.pv112game.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.darkyen.pv112game.Game;
import com.darkyen.pv112game.State;
import com.darkyen.pv112game.font.GlyphLayout;
import com.darkyen.pv112game.game.Cameraman;
import com.darkyen.pv112game.game.Level;
import com.darkyen.pv112game.gl.SpriteBatch;

/**
 *
 */
public final class GameOverState extends State {

    private final int lastCompleteLevel;

    public GameOverState(Game game, int lastCompleteLevel) {
        super(game);
        this.lastCompleteLevel = lastCompleteLevel;
    }

    private float time = 0f;
    private boolean goingToNextLevel = false;

    @Override
    public void begin() {
        game.cameraman.next(new Cameraman.StaticCameraShot(0f, 5f, 0f, 1f, 2f, 0f), 2f, Interpolation.smooth);
    }

    @Override
    public void update(float delta) {
        time += delta;
        game.cameraman.apply(game.getWorldViewport().getCamera());
    }

    @Override
    public void renderUI() {
        final SpriteBatch uiBatch = game.getUiBatch();
        uiBatch.begin(game.getUiViewport().getCamera(), true);

        final GlyphLayout glyphs = game.getSharedGlyphLayout();
        final Color white = Color.WHITE.cpy();
        white.a = MathUtils.clamp(time * 0.5f, 0f, 1f);
        glyphs.setText("Level "+lastCompleteLevel+" completed!", white, Gdx.graphics.getWidth(), Align.center);
        glyphs.draw(uiBatch, 0f, Gdx.graphics.getHeight() / 4 * 3 + glyphs.height/2);

        if (time > 2f) {
            white.a = MathUtils.clamp((time - 2f) * 0.5f, 0f, 1f);
            glyphs.setText("Onwards to the next level!", white, Gdx.graphics.getWidth(), Align.center);
            glyphs.draw(uiBatch, 0f, Gdx.graphics.getHeight() / 4 * 2 + glyphs.height/2);
        }

        uiBatch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (time > 2f && !goingToNextLevel) {
            goingToNextLevel = true;
            game.level = new Level(lastCompleteLevel + 1, 5 + lastCompleteLevel, 5 + lastCompleteLevel, System.currentTimeMillis());
            game.cameraman.next(game.CAMERA_SHOT_PLAYER_VIEW, 2f, Interpolation.smooth);
            game.schedule(2f, () -> game.setState(new GameState(game)));
            return true;
        }
        return false;
    }
}
