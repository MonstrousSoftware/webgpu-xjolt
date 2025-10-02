package com.monstrous.wgjolt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.monstrous.gdx.webgpu.graphics.g2d.WgBitmapFont;
import com.monstrous.gdx.webgpu.graphics.g2d.WgSpriteBatch;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import jolt.JoltLoader;

public class InitScreen extends ScreenAdapter {
    private final Main game;
    private boolean init = false;
    private WgSpriteBatch batch;
    private BitmapFont font;
    private float duration;

    public InitScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new WgSpriteBatch();
        font = new WgBitmapFont();
        duration = 0;

        // load Jolt
        JoltLoader.init((joltSuccess, e2) -> init = joltSuccess);
    }

    @Override
    public void render(float delta) {
        // proceed to next screen once Jolt is loaded
        if(init) {
            System.out.println("JoltLoader.init completed at t = "+duration);
            game.setScreen(new GameScreen(game));
            return;
        }
        duration += delta;


        batch.begin(Color.TEAL );
        font.draw(batch, "Loading Jolt library...", 10, 80);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
