package com.darkyen.midnightmower.gl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/**
 *
 */
public final class Light {
    public final Color color = new Color(1f, 1f, 1f, 1f);
    public final Vector3 position = new Vector3();
    public final Vector3 attenuation = new Vector3(0.1f, 0.3f, 0.02f);
    public final Vector3 direction = new Vector3(0f, 0f, 0f);
    public float directionCutoff = -5f;
}
