package com.darkyen.midnightmower;

import com.badlogic.gdx.InputAdapter;

/**
 *
 */
public abstract class State extends InputAdapter {

    protected final Game game;

    protected State(Game game) {
        this.game = game;
    }

    public void begin(){}
    public void end(){}

    public abstract void update(float delta);

    public void preRender(){}
    public void postRender(){}

    public abstract void renderUI();
}
