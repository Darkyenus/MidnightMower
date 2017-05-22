package com.darkyen.midnightmower;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class Main {
    public static void main (String[] args) throws IOException, InterruptedException {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.useOpenGL3(true, 3, 3);
        configuration.setResizable(true);
        configuration.useVsync(false);
        configuration.setTitle("Midnight Mower");

        final List<String> arguments = Arrays.asList(args);
        if (arguments.contains("nosound")) {
            configuration.disableAudio(true);
        }

        try {
            new Lwjgl3Application(new Game(), configuration);
        } catch (ExceptionInInitializerError e) {
            if (e.getCause() instanceof IllegalStateException) {
                // Restart on main thread
                final String message = e.getCause().getMessage();
                if (message != null && message.contains("-XstartOnFirstThread")) {
                    if (arguments.contains("firstThreadRestarted")) {
                        System.err.println("Failure to restart");
                        throw e;
                    } else {
                        restartOnFirstThread(arguments);
                    }
                }
            } else {
                throw e;
            }
        }
    }

    private static void restartOnFirstThread(List<String> args) throws IOException, InterruptedException {
        final List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        command.add("-XstartOnFirstThread");
        command.add("-cp");
        command.add(ManagementFactory.getRuntimeMXBean().getClassPath());
        command.add(Main.class.getName());
        command.addAll(args);
        command.add("firstThreadRestarted");

        System.out.println("Restarting: "+command);
        final Process process = new ProcessBuilder().command(command).directory(new File(".")).inheritIO().start();
        process.waitFor();
        System.exit(process.exitValue());
    }
}
