package com.monstrous.wgjolt;

import com.badlogic.gdx.ScreenAdapter;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import jolt.JoltLoader;

public class InitScreen extends ScreenAdapter {
    private final Main game;
    private boolean init = false;

    public InitScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // load Jolt
        JoltLoader.init((joltSuccess, e2) -> init = joltSuccess);
    }

    @Override
    public void render(float delta) {
        // proceed to next screen once Jolt is loaded
        if(init) {
            game.setScreen(new GameScreen(game));
            return;
        }

        System.out.println("JoltLoader.init: "+init);
        WgScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
    }
}
