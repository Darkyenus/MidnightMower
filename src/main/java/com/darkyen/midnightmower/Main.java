package com.darkyen.midnightmower;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class Main {
    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.useOpenGL3(true, 3, 3);
        configuration.setResizable(true);
        configuration.useVsync(false);
        configuration.setTitle("Midnight Mower");

        final List<String> arguments = Arrays.asList(args);
        if (arguments.contains("nosound")) {
            configuration.disableAudio(true);
        }

        new Lwjgl3Application(new Game(), configuration);
    }
}
