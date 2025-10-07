package com.monstrous.wgjolt.jolt;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.monstrous.gdx.webgpu.graphics.WgMesh;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.graphics.g3d.WgModel;
import com.monstrous.gdx.webgpu.graphics.g3d.WgModelBatch;
import com.monstrous.gdx.webgpu.graphics.g3d.model.WgMeshPart;
import jolt.gdx.JoltDebugRenderer;


public class WGPUDebugRenderer extends JoltDebugRenderer {

    WgModelBatch batch;

    public WGPUDebugRenderer() {
        WgModelBatch.Config config = new WgModelBatch.Config();
        config.maxDirectionalLights = 1;
        config.maxPointLights = 0;
        config.numBones = 2;
        config.maxRigged = 2;
        config.usePBR = false;

        System.out.println("Webgpu debug renderer");
        batch = new WgModelBatch(config);
    }

    @Override
    protected Mesh createMesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        return new WgMesh(isStatic, maxVertices, maxIndices, attributes);
    }

    @Override
    protected Texture createTexture(Pixmap pixmap, boolean useMipMaps) {
        PixmapTextureData pixmapTextureData = new PixmapTextureData(pixmap, null, useMipMaps, false);
        return new WgTexture(pixmapTextureData);
    }

    @Override
    protected MeshPart createMeshPart() {
        return new WgMeshPart();
    }

    @Override
    protected MeshPart createMeshPart(String id, Mesh mesh, int offset, int size, int type) {
        return new WgMeshPart(id, mesh, offset, size, type);
    }

    @Override
    protected Model createModel() {
        return new WgModel();
    }

    @Override
    protected void batchBegin(Camera camera) {
        batch.begin(camera);
    }

    @Override
    protected void batchEnd() {
        batch.end();
    }

    @Override
    protected void batchRender(RenderableProvider renderableProvider, Environment environment) {
        batch.render(renderableProvider, environment);
    }
}
