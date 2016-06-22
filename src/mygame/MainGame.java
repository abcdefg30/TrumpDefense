package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;

/**
 *
 * @author Daniel
 */
public class MainGame extends AbstractAppState {
    private SimpleApplication app;
    private Node rootNode;
    private AssetManager assetManager;

    Sound sound;
    private int Health;
    protected Geometry player;
    protected Geometry blas;
    private Node spheres;
    Boolean isRunning = true;
    protected Node shootables;
    private Node cubes;
    private BitmapText healthbar;
    public static MainGame instance;
    private Geometry backgroundGeom;
    private Quad fsq;
    private Image mapImage;
    private int sphere_nr = 0;
    private float speedFactor_Ball = 30f;
    private TdMap map;
    private boolean showCursor = false;
    private Geometry cursor;
    private WaveSpawner spawner;
    private boolean previewCount = true;
    Tower preview = null;
    HUD hud;
    private DirectionalLight dl;
    private DirectionalLight dl2;
    private GeometryCreator geometryCreator;
    private InputManager inputManager;
    private FlyByCamera flyCam;
    private Camera cam;
    private Node guiNode;
    private BitmapFont guiFont;
    private AudioRenderer audioRenderer;
    private ViewPort guiViewPort;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication)app;
        this.rootNode= this.app.getRootNode();
        this.assetManager =this.app.getAssetManager();
        this.inputManager = this.app.getInputManager();
        this.flyCam = this.app.getFlyByCamera(); 
        this.cam = cam = this.app.getCamera();
        this.guiNode = this.app.getGuiNode();
        this.guiFont = new BitmapFont();
        this.audioRenderer = this.app.getAudioRenderer();
        this.guiViewPort = this.app.getGuiViewPort();
        instance = this;
        mapImage = assetManager.loadTexture("Textures/map1fields.png").getImage();
        map = new TdMap(mapImage, 15, 10);

        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);

        cam.setLocation(new Vector3f(0, 0, 1f));
        initKeys();
        cubes = new Node("Cube");
        spheres = new Node("Spheres");
        shootables = new Node("Shootables");
        shootables.attachChild(makeFloor());
        shootables.attachChild(spheres);
        shootables.attachChild(cubes);
        rootNode.attachChild(shootables);
        spawner = new WaveSpawner(2000);
        Health = 10;
            
        // healthbar = new BitmapText(guiFont);
        // healthbar.setText(""+Health);
        // guiNode.attachChild(healthbar);

        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(0.0f, 0.0f, -1.0f).normalizeLocal());
        dl.setColor(ColorRGBA.White);
        rootNode.addLight(dl);
        dl2 = new DirectionalLight();
        dl2.setDirection(new Vector3f(0.0f, 0.0f, 1.0f).normalizeLocal());
        dl2.setColor(ColorRGBA.White);
        rootNode.addLight(dl2);
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White);

        rootNode.addLight(al);
        hud = new HUD(this, assetManager, inputManager, audioRenderer, guiViewPort);

        sound = new Sound(assetManager);
        sound.startMusic();

        geometryCreator = new GeometryCreator();
        // TODO: initialize your AppState, e.g. attach spatials to rootNode
        // this is called on the OpenGL thread after the AppState has been attached
    }

    @Override
    public void update(float tpf) {

        // TODO: implement behavior during runtime
        if (showCursor) {
            updateCursor();
        }
        spawner.update(tpf);
    }

    private void towerPreview(Tower t) {
        initCursor(t.createGeometry());
    }

    private void initCursor(Geometry g) {
        cursor = g;
        rootNode.attachChild(cursor);
        showCursor = true;
        updateCursor();
    }

    private void destroyCursor() {
        if (cursor == null) {
            return;
        }

        rootNode.detachChild(cursor);
        cursor = null;
        showCursor = false;
    }

    public Vector3f getMousePosition() {
        Vector3f position = null;
        CollisionResults results = new CollisionResults();
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(click2d, 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(click2d, 1f).subtractLocal(click3d).normalizeLocal();
        Ray ray = new Ray(click3d, dir);
        shootables.collideWith(ray, results);
        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            position = closest.getContactPoint();
        }
        return position;
    }

    private void updateCursor() {
        Vector3f position = getMousePosition();
        cursor.setLocalTranslation(position);
        if (map.towerplace(position, fsq) == true) {
            cursor.getMaterial().setColor("Color", ColorRGBA.Green);
        } else {
            cursor.getMaterial().setColor("Color", ColorRGBA.Red);
        }
    }

    private void initKeys() {
        inputManager.addMapping("move", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("tower", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("left", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("right", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addListener(actionListener, "move", "left", "right", "tower");
    }

    protected Geometry makeFloor() {
        // https://hub.jmonkeyengine.org/t/how-to-set-a-background-texture/22996
        // TODO: Die map ueberlappt noch mit der UI
        float w = this.app.getContext().getSettings().getWidth();
        float h = this.app.getContext().getSettings().getHeight();
        float ratio = w / h;

        // Move the Camera back
        cam.setLocation(Vector3f.ZERO.add(new Vector3f(0.0f, 0.0f, 102.5f)));

        // No Idea why I need to subtract 17.5
        float camZ = cam.getLocation().getZ() - 17.5f;
        float width = camZ * ratio;

        Material backgroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture backgroundTex = assetManager.loadTexture("Textures/map1texture.png");
        backgroundMat.setTexture("ColorMap", backgroundTex);

        fsq = new Quad(width, camZ);
        backgroundGeom = new Geometry("the Floor", fsq);
        backgroundGeom.setQueueBucket(RenderQueue.Bucket.Sky);
        backgroundGeom.setCullHint(Spatial.CullHint.Never);
        backgroundGeom.setMaterial(backgroundMat);

        // Need to Divide by two because the quad origin is bottom left
        backgroundGeom.setLocalTranslation(-(width / 2), -(camZ / 2), 0);
        return backgroundGeom;
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("move")) {
                flyCam.setEnabled(true);
                inputManager.setCursorVisible(false);
                flyCam.setMoveSpeed(100);
                flyCam.setRotationSpeed(5);
            } else if (name.equals("left") && keyPressed) {
                if (hud.CurrentTower != -1) {
                    Vector3f position = getMousePosition();
                    if (map.towerplace(position, fsq) == true) {
                        preview.init(position);
                        destroyCursor();
                        hud.CurrentTower = -1;
                    }
                }
            } else if (name.equals("right") && keyPressed) {
                destroyTowerPreview();
                hud.CurrentTower = -1;
            }
        }
    };

    public TdMap getTdMap() {
        return map;
    }

    public Quad getFloor() {
        return fsq;
    }

    public void attachToRootNode(Geometry g) {
        rootNode.attachChild(g);
    }

    public void attachToRootNode(Spatial g) {
        rootNode.attachChild(g);
    }

    public WaveSpawner getSpawner() {
        return spawner;
    }

    public void removeTower(Tower t) {
    }

    public void CreateTowerPreview() {
        preview = new Tower(hud.CurrentTower);
        towerPreview(preview);
    }

    private void destroyTowerPreview() {
        preview = null;
        destroyCursor();
    }

    public void detachFromRootNode(Spatial s) {
        rootNode.detachChild(s);
    }

    public void reduceHealth(){
        Health--;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        rootNode.detachAllChildren();
        // TODO: clean up what you initialized in the initialize method,
        // e.g. remove all spatials from rootNode
        // this is called on the OpenGL thread after the AppState has been detached
    }

    AssetManager getAssetManager() {
        return assetManager;
    }

    Camera getCamera() {
        return cam;
    }
}