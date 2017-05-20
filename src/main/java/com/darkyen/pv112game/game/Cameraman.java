package com.darkyen.pv112game.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Queue;

/**
 *
 */
public final class Cameraman {

    private CameraShot lastShot;

    private final Queue<CameraShot> nextShots = new Queue<>();
    private final Queue<Interpolation> nextInterpolations = new Queue<>();
    private final FloatArray nextDurations = new FloatArray(true, 16);

    private float time = 0f;

    public Cameraman(CameraShot lastShot) {
        this.lastShot = lastShot;
    }

    public void update(float delta){
        if (nextDurations.size == 0) return;
        time += delta;
        if (nextDurations.first() <= time) {
            time -= nextDurations.removeIndex(0);
            lastShot = nextShots.removeFirst();
            nextInterpolations.removeFirst();
            if (nextDurations.size == 0f) {
                time = 0f;
            }
        }
    }

    public void next(CameraShot shot, float transitionTime, Interpolation transitionInterpolation) {
        nextShots.addLast(shot);
        nextDurations.add(transitionTime);
        nextInterpolations.addLast(transitionInterpolation);
    }

    public void reset(CameraShot shot) {
        time = 0f;
        nextShots.clear();
        nextInterpolations.clear();
        nextDurations.clear();
        lastShot = shot;
    }

    private final Vector3 apply_tmp_position = new Vector3();
    private final Vector3 apply_tmp_direction = new Vector3();

    public float getTransitionProgress() {
        if (nextDurations.size > 0) {
            return time / nextDurations.first();
        }
        return 0f;
    }

    public void apply(Camera camera) {
        lastShot.set(camera.position, camera.direction);
        if (time != 0f && nextShots.size > 0) {
            final Vector3 nextPos = this.apply_tmp_position;
            final Vector3 nextDir = this.apply_tmp_direction;

            nextShots.first().set(nextPos, nextDir);
            final float alpha = nextInterpolations.first().apply(time / nextDurations.first());

            camera.position.lerp(nextPos, alpha);
            camera.direction.lerp(nextDir, alpha);
        }
        camera.update();
    }


    public interface CameraShot {
        void set(Vector3 position, Vector3 direction);
    }

    public static final class StaticCameraShot implements CameraShot {
        private final float posX, posY, posZ;
        private final float dirX, dirY, dirZ;

        public StaticCameraShot(Vector3 position, Vector3 direction) {
            this.posX = position.x;
            this.posY = position.y;
            this.posZ = position.z;

            this.dirX = direction.x;
            this.dirY = direction.y;
            this.dirZ = direction.z;
        }

        public StaticCameraShot(float posX, float posY, float posZ, float dirX, float dirY, float dirZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;

            final float dirLen = Vector3.len(dirX, dirY, dirZ);
            this.dirX = dirX / dirLen;
            this.dirY = dirY / dirLen;
            this.dirZ = dirZ / dirLen;
        }

        @Override
        public void set(Vector3 position, Vector3 direction) {
            position.set(posX, posY, posZ);
            direction.set(dirX, dirY, dirZ);
        }
    }

    public static final CameraShot NULL_CAMERA_SHOT = (position, direction) -> {};
}
