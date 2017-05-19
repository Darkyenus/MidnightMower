package com.darkyen.pv112game.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.darkyen.pv112game.Game;
import com.darkyen.pv112game.State;
import com.darkyen.pv112game.font.GlyphLayout;
import com.darkyen.pv112game.game.Level;
import com.darkyen.pv112game.gl.SpriteBatch;

/**
 *
 */
public class GameState extends State {

    private boolean debugCamera = false;
    private boolean debugDraw = false;
    private final FirstPersonCameraController debugCameraController;

    public GameState(Game game) {
        debugCameraController = new FirstPersonCameraController(game.getWorldViewport().getCamera());
    }

    @Override
    public void update(Game game, float delta) {
        final Level level = game.getLevel();

        if (!game.isPaused()) {
            final boolean forward = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
            final boolean backward = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
            if (forward ^ backward) {
                // Could better handle discrete updates with rotation
                final Vector2 movement = new Vector2(0f, level.playerSpeed * delta * (forward ? 1f : -1f)).rotate(-level.playerAngle);
                final Level.CollisionData collisionData = level.playerMove(movement);
                if (collisionData.collision != Level.CollisionData.NO_COLLISION) {
                    // Slide
                    movement.mulAdd(movement, -collisionData.distanceTravelled / movement.len());
                    if (collisionData.collision == Level.CollisionData.COLLIDED_CROSSING_X) {
                        movement.x = 0f;
                    } else {
                        movement.y = 0f;
                    }
                    level.playerMove(movement);
                }
            }

            final boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            final boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

            if (left ^ right) {
                final float turnAngle = delta * level.playerRotationSpeed;
                level.playerAngle += left ? turnAngle : -turnAngle;
            }
        }

        // Update camera
        if (debugCamera) {
            debugCameraController.update(delta);
            return;
        }

        final Camera camera = game.getWorldViewport().getCamera();
        cameraToMowerView(level, camera.position, camera.direction);
    }

    public static void cameraToMowerView(Level level, Vector3 position, Vector3 direction) {
        position.set(level.playerPos.x, 1.5f, level.playerPos.y);

        final Vector2 offset = new Vector2(0f, -2f).rotate(-level.playerAngle);
        position.add(offset.x, 0f, offset.y);

        direction.set(-offset.x, -0.3f, -offset.y).nor();
    }

    @Override
    public void renderUI(Game game) {
        final ScreenViewport uiViewport = game.getUiViewport();
        final SpriteBatch uiBatch = game.getUiBatch();
        final GlyphLayout glyphLayout = game.getSharedGlyphLayout();
        final Level level = game.getLevel();

        final GL20 gl = Gdx.gl30;
        gl.glDisable(GL20.GL_DEPTH_TEST);
        gl.glDisable(GL20.GL_CULL_FACE);
        gl.glDepthMask(false);

        uiViewport.apply(true);

        uiBatch.begin(uiViewport.getCamera(), true);
        if (debugDraw) {
            glyphLayout.setText("FPS: "+Gdx.graphics.getFramesPerSecond()+"\nPos: "+level.playerPos+"\nTile: "+level.playerTileX+" "+level.playerTileY+"\nA: "+level.playerAngle+"\nGrass: "+level.remainingGrass, Color.WHITE, Gdx.graphics.getWidth(), Align.left);
        } else {
            glyphLayout.setText("Remaining: "+level.remainingGrass, Color.WHITE, Gdx.graphics.getWidth(), Align.left);
        }
        glyphLayout.draw(uiBatch, 0, Gdx.graphics.getHeight());
        uiBatch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (debugCamera && debugCameraController.keyDown(keycode)) return true;
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (debugCamera && debugCameraController.keyUp(keycode)) return true;
        if (keycode == Input.Keys.F3) {
            debugDraw = !debugDraw;
            return true;
        }
        if (keycode == Input.Keys.F4) {
            debugCamera = !debugCamera;
            return true;
        }
        return super.keyUp(keycode);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (debugCamera && debugCameraController.touchDragged(screenX, screenY, pointer)) return true;
        return super.touchDragged(screenX, screenY, pointer);
    }
}
