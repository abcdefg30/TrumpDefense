package mygame;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Amir, Daniel, Lukas
 */
class Tower extends AbstractControl {

    public static final int TYPE_MARINE = 0;
    public static final int TYPE_POLICE = 1;
    public static final int TYPE_UNICORN = 2;
    public static final int TYPE_MURICA = 3;
    private Spatial geom;
    private float cooldown = 1.0f;
    private float cooldownTimeLeft = 0;
    private float range = 20f;
    private WaveSpawner s;
    private int projectileType;
    public int type = 0;
    private IllegalImmigrant target;
    private float rotationSpeed = 3.0f;
    private float currentRotation = 0;
    private float maxAngle = 0.05f;
    private Projectile p;
    public String Beschreibung;
    private int damage;
    public boolean enabled = true;

    public Tower(int type) {
        this.type = type;

        try {
            java.sql.ResultSet res = MainGame.instance.DataControl.SelectTurm(typName(type));
            res.next();
            Beschreibung = res.getString(2);
            range = (float) res.getInt(3);
            cooldown = res.getInt(4);
            damage = res.getInt(5);
            res.close();
        } catch (Throwable e) {
            System.out.println(" . . . exception thrown:");
            e.printStackTrace(System.out);
        }

        projectileType = type == TYPE_UNICORN ? Projectile.TYPE_LASER : Projectile.TYPE_NORMAL;
        if (type == TYPE_POLICE)
            projectileType = Projectile.TYPE_TASER;
    }

    public void init(Vector3f position) {
        geom = GeometryCreator.instance.createTowerGeom(type);
        geom.setLocalTranslation(position);
        MainGame.instance.attachToRootNode(geom);
        s = MainGame.instance.getSpawner();
        geom.addControl(this);
    }

    // Für die Vorschau bevor der Turm platziert wird
    public Geometry createGeometry() {
        return GeometryCreator.instance.createBox(Vector3f.ZERO);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(MainGame.instance.isEnabled()){
        float fixedTpf = MainGame.instance.GetFixedTpf(tpf);
        cooldownTimeLeft -= fixedTpf * 1000;
        if(updateTarget() == false) return;
        
        switch (type) {
                    case TYPE_UNICORN:
                        rotate(fixedTpf);
                        float a = (float) (getTargetAngle());
                        if (Math.abs(a) < maxAngle && p == null) {
                            shoot();
                        }
                        break;
                    default:
                        if (cooldownTimeLeft <= 0) {
                            shoot();
                        }
                        break;
        } }
    }
    
    private void shoot() {
        p = new Projectile(target, this, projectileType);
        cooldownTimeLeft = cooldown;
    }
    
    private boolean updateTarget() {
        if (target == null) {
            target = getNearestImmigrant();
        }

        // Versuche nicht jemanden auserhalb der Reichweite zu erwischen
        if (target != null && (!IsInRange(target, true) || !target.isLiving())) {
            target = getNearestImmigrant();
        }

        // Immernoch kein target gefunden, tue nichts
        if (target == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) { }

    public IllegalImmigrant getNearestImmigrant() {
        IllegalImmigrant nearest = null;
        for (Object o : s.getImmigrants()) {
            IllegalImmigrant i = (IllegalImmigrant) o;
            switch (type) {
                case TYPE_MARINE:
                    if (IsInRange(i, false)) {
                        nearest = i;
                    }
                    break;
                case TYPE_POLICE:
                    if (IsInRange(i, true) && i.getTaserTicks() == 0) {
                        nearest = i;
                    }
                    break;
                case TYPE_UNICORN:
                    if (IsInRange(i, false)) {
                        nearest = i;
                    }
                    break;
            }
        }
        return nearest;
    }

    public boolean IsInRange(IllegalImmigrant targeted, boolean ignoreTargeted) {

        return getPosition().distance(targeted.getPosition()) < range || range == -1;
    }

    public Vector3f getPosition() {
        return geom.getLocalTranslation();
    }
    
    private void rotate(float fixedTpf) {
        float a = (float) (getTargetAngle());
        if (a < 0) {
            currentRotation += fixedTpf * rotationSpeed;
            yaw(currentRotation);
        } else {
            currentRotation -= fixedTpf * rotationSpeed;
            yaw(currentRotation);
        }
    }
    
    public void yaw(float radians) {
        Quaternion rot = new Quaternion();
        rot.fromAngles((float) (Math.PI / 2), 0.0f, radians);
        spatial.setLocalRotation(rot);
    }

    public float getTargetAngle() {
        if (target == null) {
            return 0;
        }
        Vector3f a = target.getPosition().clone();
        Vector3f b = spatial.getLocalTranslation().clone();
        Vector2f c = new Vector2f(a.getX(), a.getY());
        Vector2f d = new Vector2f(b.getX(), b.getY());
        Vector2f e = c.subtract(d);
        e.normalizeLocal();
        //e = Vektor zwischen Turm und Target als Einheitsvektor
        float z = getTowerAngle();
        Vector2f f = new Vector2f((float) Math.cos(z), (float) Math.sin(z));
        //f = Rotation des Turms als Einheitsvektor
        return e.angleBetween(f);
    }

    public float getTowerAngle() {
        return (float) (currentRotation - 0.5 * Math.PI);
    }
    
    public void removeProjectile() {
        p = null;
    }
    
    public boolean isInRange(Projectile projectile, IllegalImmigrant target) {
        if (spatial.getLocalTranslation().distance(target.getPosition()) < range) {
            return true;
        }
        return false;
    }

    String typName(int type)
    {
        String name = "";
        switch (type)
        {
            case (TYPE_POLICE):
                name = "Polizeistation";
                break;
            case (TYPE_UNICORN):
                name = "Einhornturm";
                break;
            case (TYPE_MURICA):
                name = "Einhornturm";
                break;
            case (TYPE_MARINE):
                name = "Marines";
                break;
            default:
                throw new IllegalArgumentException("Invalid type for tower name " + type);
        }
        return name;
    }
}
