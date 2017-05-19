package com.darkyen.pv112game;

import com.badlogic.gdx.InputAdapter;

/**
 *
 */
public abstract class State extends InputAdapter {

    public void begin(){}
    public void end(){}

    public abstract void update(Game game, float delta);

    public abstract void renderUI(Game game);
}
