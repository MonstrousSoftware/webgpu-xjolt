package com.monstrous.wgjolt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.graphics.g2d.WgBitmapFont;
import com.monstrous.gdx.webgpu.graphics.g2d.WgSpriteBatch;
import com.monstrous.gdx.webgpu.graphics.g3d.WgModelBatch;
import com.monstrous.gdx.webgpu.graphics.g3d.utils.WgModelBuilder;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.monstrous.wgjolt.jolt.JoltInstance;
import com.monstrous.wgjolt.jolt.Layers;
import com.monstrous.wgjolt.jolt.WGPUDebugRenderer;
import jolt.JoltNew;
import jolt.enums.EActivation;
import jolt.enums.EMotionType;


import jolt.gdx.JoltDebugRenderer;
import jolt.math.Quat;
import jolt.math.Vec3;
import jolt.physics.PhysicsSystem;
import jolt.physics.body.*;
import jolt.physics.collision.shape.BoxShape;


// when in debug renderer mode, after 592 items we get
// Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
// due to material comparison in WgDefaultRenderableSorter

public class GameScreen extends ScreenAdapter {
    private final Main game;
    private WgSpriteBatch batch;
    private BitmapFont font;
    protected JoltInstance joltInstance = null;
    protected PhysicsSystem physicsSystem = null;
    protected BodyInterface bodyInterface = null;
    protected JoltDebugRenderer debugRenderer = null;
    private BodyManagerDrawSettings debugSettings;
    private WgModelBatch modelBatch;
    private Environment environment;
    private PerspectiveCamera cam;
    private Array<Disposable> disposables;
    private Array<ModelInstance> instances;
    private CameraInputController inputController;
    private Model boxModel;
    private boolean useDebugRender;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        joltInstance = new JoltInstance();
        physicsSystem = joltInstance.getPhysicsSystem();
        bodyInterface = physicsSystem.GetBodyInterface();

        debugRenderer = new WGPUDebugRenderer();
        debugSettings = new BodyManagerDrawSettings();
        // debugSettings.set_mDrawShapeColor(EShapeColor.EShapeColor_SleepColor );
        useDebugRender = false;

        disposables = new Array<>();

        WgModelBatch.Config config = new WgModelBatch.Config();
        config.maxInstances = 10000;        // allow lots of items
        modelBatch = new WgModelBatch(config);
        disposables.add( modelBatch );

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .4f, .4f, .4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0, 0, 0);
        cam.near = 0.1f;
        cam.far = 150f;
        cam.update();

        boxModel = createBoxModel();

        instances = new Array<>();
        populate();

        Gdx.input.setInputProcessor(new InputMultiplexer( inputController = new CameraInputController(cam)));

        batch = new WgSpriteBatch();
        font = new WgBitmapFont();
    }

    private Model createBoxModel(){
        Texture texture = new WgTexture(Gdx.files.internal("badlogic.jpg"), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        disposables.add( texture );
        Material mat = new Material(TextureAttribute.createDiffuse(texture));
        ModelBuilder modelBuilder = new WgModelBuilder();
        float sz = 1;
        Model boxModel = modelBuilder.createBox(sz, sz, sz, mat,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.Normal);
        disposables.add( boxModel );
        return boxModel;
    }

    private void reset(){
        joltInstance.clearWorld();
        instances.clear();
        populate();
    }

    private void populate(){
        ModelBuilder modelBuilder = new WgModelBuilder();
        float w = 15f;
        float h = 1;
        Model floorModel = modelBuilder.createBox(w, h, w, new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        ModelInstance instance = new ModelInstance(floorModel, 0, -h/2, 0);
        instances.add(instance);
        disposables.add(floorModel);
        createFloorBody(w,h);

        spawnBox(w);
    }

    private void spawnBox(float spawnWidth){

        float sz = 1;
        float ht = 9f;
        float x = spawnWidth * ((float)Math.random()- 0.5f);
        float z = spawnWidth * ((float)Math.random()- 0.5f);

        ModelInstance boxInstance = new ModelInstance(boxModel, x, ht, z);
        instances.add(boxInstance);
        BodyID boxID = createBlock(sz, x, ht, z);
        boxInstance.userData = boxID;   // link model instance to Jolt body via userData
    }

    protected void createFloorBody(float w, float h) {
        float scale = 1f;
        Vec3 inHalfExtent = JoltNew.Vec3(scale * (0.5f * w), scale * (0.5f*h), scale * (0.5f * w));
        Vec3 inPosition = JoltNew.Vec3(0.0f, scale * -0.5f*h, 0.0f);
        Quat inRotation = Quat.sIdentity();
        BoxShape bodyShape = new BoxShape(inHalfExtent, 0.0f);
        BodyCreationSettings bodySettings = JoltNew.BodyCreationSettings(bodyShape, inPosition, inRotation, EMotionType.Static, Layers.NON_MOVING);
        Body body = bodyInterface.CreateBody(bodySettings);
        bodyInterface.AddBody(body.GetID(), EActivation.DontActivate);
        bodySettings.dispose();
        inHalfExtent.dispose();
        inPosition.dispose();
    }


    private BodyID createBlock(float size, float x, float y, float z) {
        Vec3 vec3 = JoltNew.Vec3(0.5f * size, 0.5f * size, 0.5f * size);
        BoxShape box_shape = new BoxShape(vec3);
        vec3.dispose();

        Vec3 position = JoltNew.Vec3(x, y, z);
        BodyCreationSettings bodyCreationSettings = JoltNew.BodyCreationSettings(box_shape, position, Quat.sIdentity(), EMotionType.Dynamic, Layers.MOVING);
        Body body = bodyInterface.CreateBody(bodyCreationSettings);
        bodyInterface.SetRestitution(body.GetID(),0.5f);  // bouncy!
        bodyInterface.AddBody(body.GetID(), EActivation.Activate);
        bodyCreationSettings.dispose();
        position.dispose();
        return body.GetID();
    }

    @Override
    public void render(float deltaTime) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            reset();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            spawnBox(8f);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.T)){
            useDebugRender = !useDebugRender;
        }

        inputController.update();
        stepPhysics(deltaTime);

        WgScreenUtils.clear(Color.TEAL, true);
        if(useDebugRender) {

            debugRender();
        } else {
            updateVisuals();

            modelBatch.begin(cam);
            modelBatch.render(instances, environment);
            modelBatch.end();
        }

        batch.begin();
        float dy = 25f;
        float y = 180f;
        font.draw(batch, "R to reset, SPACE to spawn items, T to toggle debug render", 10, y -= dy);
        font.draw(batch, "Items: "+instances.size, 10, y -= dy);
        font.draw(batch, "Materials: "+modelBatch.materials.count() , 10, y -= dy);
        font.draw(batch, "FPS: "+Gdx.graphics.getFramesPerSecond(), 10, y -= dy);
        batch.end();
    }

    private void debugRender() {
        debugRenderer.begin(cam);
        debugRenderer.DrawBodies(joltInstance.getPhysicsSystem(), debugSettings);
        debugRenderer.end();
    }

    public void stepPhysics(float deltaTime) {
        // When running below 55 Hz, do 2 steps instead of 1
        int numSteps = deltaTime > 1.0 / 55.0 ? 2 : 1;
        joltInstance.update(deltaTime, numSteps);
    }

    /** sync model instances to physics bodies */
    public void updateVisuals(){
        Vec3 pos = JoltNew.Vec3();
        Quat rot = JoltNew.Quat();
        Vector3 position = new Vector3();
        Quaternion quaternion = new Quaternion();
        for(ModelInstance boxInstance : instances) {
            if(boxInstance.userData == null)
                continue;
            BodyID boxID = (BodyID) boxInstance.userData;

            bodyInterface.GetPositionAndRotation(boxID, pos, rot);
            position.set(pos.GetX(), pos.GetY(), pos.GetZ());
            quaternion.set(rot.GetX(), rot.GetY(), rot.GetZ(), rot.GetW());

            boxInstance.transform.set(position, quaternion);
        }
    }


    @Override
    public void dispose() {
        joltInstance.clearWorld();
        joltInstance.dispose();
        for(Disposable disposable : disposables)
            disposable.dispose();
        debugRenderer.dispose();
        debugSettings.dispose();
        batch.dispose();
        font.dispose();
    }

}
