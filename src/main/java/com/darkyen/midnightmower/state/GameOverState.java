package com.darkyen.midnightmower.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.darkyen.midnightmower.Game;
import com.darkyen.midnightmower.State;
import com.darkyen.midnightmower.font.GlyphLayout;
import com.darkyen.midnightmower.game.Cameraman;
import com.darkyen.midnightmower.game.Level;
import com.darkyen.midnightmower.gl.SpriteBatch;

/**
 *
 */
public final class GameOverState extends State {

    private final int lastCompleteLevel;
    private final float levelTime;
    private final float previousTotalLevelTime;

    public GameOverState(Game game, int lastCompleteLevel, float levelTime, float previousTotalLevelTime) {
        super(game);
        this.lastCompleteLevel = lastCompleteLevel;
        this.levelTime = levelTime;
        this.previousTotalLevelTime = previousTotalLevelTime;
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
        glyphs.setText("Level "+lastCompleteLevel+" completed in {#AEA}"+Math.round(levelTime)+"{} seconds!", white, Gdx.graphics.getWidth(), Align.center);
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
            game.level = new Level(lastCompleteLevel + 1, System.currentTimeMillis());
            game.cameraman.next(game.CAMERA_SHOT_PLAYER_VIEW, 2f, Interpolation.smooth);
            game.schedule(2f, () -> game.setState(new GameState(game, previousTotalLevelTime + levelTime)));
            return true;
        }
        return false;
    }
}
