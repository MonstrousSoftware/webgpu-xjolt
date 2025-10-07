package com.monstrous.wgjolt.lwjgl3;



import com.github.xpenatan.webgpu.JWebGPUBackend;
import com.monstrous.gdx.webgpu.application.WebGPUContext;
import com.monstrous.gdx.webgpu.backends.desktop.WgDesktopApplication;
import com.monstrous.gdx.webgpu.backends.desktop.WgDesktopApplicationConfiguration;
import com.monstrous.wgjolt.Main;


/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main (String[] argv) {

        WgDesktopApplicationConfiguration config = new WgDesktopApplicationConfiguration();
        config.backendWebGPU = JWebGPUBackend.WGPU; // WGPU or DAWN
        config.backend = WebGPUContext.Backend.DEFAULT; // Vulkan, DX12, etc.
        config.setWindowedMode(800, 600);
        config.setTitle("WebGPU + Jolt");
        config.enableGPUtiming = false;

        config.useVsync(false);
        new WgDesktopApplication(new Main(), config);
    }
}
