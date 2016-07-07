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
 * @author Daniel, Lukas
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
    public static MainGame instance;
    public DataControl DataControl;
    private Geometry backgroundGeom;
    private Quad fsq;
    private Image mapImage;
    private int sphere_nr = 0;
    private float speedFactor_Ball = 30f;
    private TdMap map;
    private boolean showCursor = false;
    private Geometry cursor;
    private WaveSpawner spawner;
    Tower preview = null;
    HUD hud;
    private DirectionalLight dl;
    private DirectionalLight dl2;
    private GeometryCreator geometryCreator;
    private InputManager inputManager;
    private FlyByCamera flyCam;
    private Camera cam;
    private AudioRenderer audioRenderer;
    private ViewPort guiViewPort;
    private Main main;
    public int money = 500;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.inputManager = this.app.getInputManager();
        this.flyCam = this.app.getFlyByCamera();
        this.cam = cam = this.app.getCamera();
        this.audioRenderer = this.app.getAudioRenderer();
        this.guiViewPort = this.app.getGuiViewPort();
        main = Main.instance;
        instance = this;
        mapImage = assetManager.loadTexture("Textures/map1fields.png").getImage();
        map = new TdMap(mapImage, 15, 10);
        sound = new Sound(assetManager);
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

        Health = 1;

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
        hud.setMoney(money);

        DataControl = new DataControl();

        // Only for debugging, can be removed later
        DataHelper helper = new DataHelper(DataControl);
        helper.SelectTuerme();
        helper.SelectGegner();

        spawner = new WaveSpawner(this);
        geometryCreator = new GeometryCreator();
        setEnabled(true);
        // TODO: initialize your AppState, e.g. attach spatials to rootNode
        // this is called on the OpenGL thread after the AppState has been attached
    }

    @Override
    public void update(float tpf) {
        if (Health <= 0) {
            GameOver();
            //this.stop();
        }
        // TODO: implement behavior during runtime
        if (showCursor) {
            updateCursor();
        }
        spawner.update(tpf);
    }

    private void towerPreview(Tower t) {
        initCursor(t.createGeometry());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            sound.startMusic();
            spawner.setEnabled(true);
        } else {
            sound.stopMusic();
            spawner.setEnabled(false);
            // PauseScreen.instance.setEnabled(true);
        }
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
        if (map.towerplace(position, fsq) && money >= 250) {
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
                if (hud.CurrentTower != -1 && money >= 250) {
                    Vector3f position = getMousePosition();
                    if (map.towerplace(position, fsq)) {
                        preview.init(position);
                        money -= 250;
                        hud.setMoney(money);
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

    public void reduceHealth() {
        Health--;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        rootNode.detachAllChildren();
        DataControl.Close();
    }

    AssetManager getAssetManager() {
        return assetManager;
    }

    Camera getCamera() {
        return cam;
    }

    public void NeueWelle(int welle) {
        hud.setzeWelle(welle);
    }

    public void SkipWave() {
        spawner.newWave();
    }

    public void GameOver(){
        setEnabled(false);
        hud.GameOver();
        System.out.println("hi");
    }

    public void ImmigrantKilled(IllegalImmigrant immigrant) {
        spawner.remove(immigrant);
        money += 50;
        hud.setMoney(money);
    }

    // Wie viel mal länger ein Tick maximal dauern darf
    float normalTpf = -1;
    static float maxTimeMult = 3;
    public float GetFixedTpf(float tpf) {
        float fixedTpf = tpf;

        if (normalTpf == -1) {
            normalTpf = tpf;
        }

        if (tpf > (maxTimeMult * normalTpf)) {
            fixedTpf = normalTpf;
        } else {
            normalTpf = ((normalTpf * (60 - 1)) + tpf) / 60;
        }

        return fixedTpf;
    }
}
