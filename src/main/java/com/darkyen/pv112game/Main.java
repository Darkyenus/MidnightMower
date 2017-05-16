package com.darkyen.pv112game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 *
 */
public class Main {
    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.useOpenGL3(true, 3, 3);
        configuration.setResizable(true);
        configuration.useVsync(false);
        configuration.setTitle("PV112 Game");

        new Lwjgl3Application(new Game(), configuration);
    }
}
