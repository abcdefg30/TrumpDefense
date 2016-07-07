package mygame;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author Amir
 */
public class IllegalImmigrant extends AbstractControl {

    private Geometry geom;
    private int[] xn;
    private int[] yn;
    private Vector3f velocity;
    private float ratioxr;
    private float ratioyr;
    private Quad bgObject;
    private int counter = 0;
    private int nrCheckpoints = 0;
    private float speedFactor;
    private TdMap m;
    private WaveSpawner w;
    private float taserTicks = 0.0f;
    public Vector3f sphereDirection;
    private float health = 100.0f;
    private boolean living = true;

    public IllegalImmigrant(WaveSpawner w) {
        m = MainGame.instance.getTdMap();
        xn = m.getnodex();
        yn = m.getnodey();
        speedFactor = 30.0f;
        velocity = new Vector3f(0, 0, 0);
        nrCheckpoints = xn.length;
        bgObject = MainGame.instance.getFloor();
        ratioxr = bgObject.getWidth() / m.getWidth();
        ratioyr = bgObject.getHeight() / m.getHeight();
        Vector3f spawn = new Vector3f(-(bgObject.getWidth() / 2) + xn[0] * ratioxr, -(bgObject.getHeight() / 2) + yn[0] * ratioyr, 0.0f);
        geom = GeometryCreator.instance.createSphere(spawn);
        MainGame.instance.attachToRootNode(geom);
        geom.addControl(this);
        this.w = w;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(MainGame.instance.isEnabled()){
        float fixedTpf = MainGame.instance.GetFixedTpf(tpf);
        if (taserTicks == 0.0f) {
            if (counter < nrCheckpoints) {
                Vector3f checkPointPosition = new Vector3f(-(bgObject.getWidth() / 2) + xn[counter] * ratioxr, -(bgObject.getHeight() / 2) + yn[counter] * ratioyr, spatial.getLocalTranslation().getZ());

                sphereDirection = checkPointPosition.subtract(spatial.getLocalTranslation());
                sphereDirection.normalizeLocal();
                sphereDirection.multLocal(speedFactor);
                velocity.addLocal(sphereDirection);

                // Kontrolliert geschwindigkeit damit nicht außer kontrolle bewegt wird ( macht komische Ellipsen)
                velocity.multLocal(0.9f);

                // Bewegt und KOntrolliert GEschwindikeit damit auf allen pcs gleich
                spatial.move(velocity.mult(0.1f * fixedTpf));
                if (checkPointPosition.distance(spatial.getLocalTranslation()) < 1f) {
                    counter++;
                }
            } else {
                MainGame.instance.reduceHealth();
                spatial.removeFromParent();
                w.remove(this);
            }
        } else {
            taserTicks -= tpf;
            if (taserTicks < 0.0f) {
                taserTicks = 0.0f;
            }
        }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) { }

    public void hit(Projectile p) {
        switch (p.getType()) {
            case Projectile.TYPE_LASER:
                remove();
                break;
            case Projectile.TYPE_NORMAL:
                remove();
                break;
            case Projectile.TYPE_TASER:
                taserTicks = 2.0f;
                break;
        }
    }

    public Vector3f getPosition() {
        return spatial.getLocalTranslation();
    }
    
    public void hit(Projectile p, float dmg) {
        if(dmg != -1) {
            health -= dmg;
        }
        switch (p.getType()) {
            case Projectile.TYPE_LASER:
                if(health <= 0) {
                    remove();
                    MainGame.instance.money = MainGame.instance.money + 50;
                    MainGame.instance.setMoney(MainGame.instance.money + 50);
                    p.remove();
                }
                break;
            case Projectile.TYPE_NORMAL:
                if(health <= 0) {
                    remove();
                }
                p.remove();
                break;
            case Projectile.TYPE_TASER:
                taserTicks = 2.0f;
                p.remove();
                break;
        }
    }

    public Vector3f getSpheredirection() {
        return sphereDirection;
    }

    public void remove() {
        spatial.removeFromParent();
        w.remove(this);
        living = false;
    }


    public float getTaserTicks() {
        return taserTicks;
    }
    
    public boolean isLiving() {
        return living;
    }
}
