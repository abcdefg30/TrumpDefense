package mygame;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Amir, Daniel
 */
class Projectile extends AbstractControl {

    public static final int TYPE_LASER = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_TASER = 2;
    private Spatial geom;
    private IllegalImmigrant target;
    private float speedFactor = 60f;
    private final float distance = 0.3f;
    private Vector3f velocity = new Vector3f(0, 0, 0);
    private float lifetime = 0;
    private int type = -1;
    private boolean exists = true;
    private Tower tower;
    private Spatial geom2;

    public Projectile(IllegalImmigrant target, Tower tower, int type) {
        this.type = type;
        this.target = target;
        geom = GeometryCreator.instance.createProjectile(tower.getPosition(), type, target.getPosition());
        geom.addControl(this);
        MainGame.instance.attachToRootNode(geom);
        this.tower = tower;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(MainGame.instance.isEnabled()){
        if(exists) {
            if(!tower.isInRange(this, target)) {
                switch(type) {
                    case TYPE_LASER:
                        remove();
                        break;
                    default:
                        break;
                }
            }
                
            switch (type) {
                case TYPE_LASER:
                    hit(tpf);
                    if (geom2 != null) {
                        geom2.removeFromParent();
                    }
                    if (exists) {
                        geom2 = GeometryCreator.instance.createRainbowLaser(spatial.getLocalTranslation(), target.getPosition());
                        MainGame.instance.attachToRootNode(geom2);
                    }
                    break;
                case TYPE_NORMAL:
                    moveToTarget(tpf);
                    break;
                case TYPE_TASER:
                    moveToTarget(tpf);
                    break;
            }
        } 
    }}
    
    public void hit(float fixedTpf) {
        switch (type) {
            case TYPE_LASER:
                target.hit(this, 100f * fixedTpf);
                break;
            default:
                target.hit(this, 50f);
                break;
        }

    }

    public void moveToTarget(float tpf) {
        float fixedTpf = MainGame.instance.GetFixedTpf(tpf);
        Vector3f targetPosition = target.getPosition();
        Vector3f direction = targetPosition.subtract(spatial.getLocalTranslation());
        direction.normalizeLocal();
        direction.multLocal(speedFactor);
        velocity.addLocal(direction);

        // Kontrolliert geschwindigkeit damit nicht außer kontrolle bewegt wird (macht komische Ellipsen)
        velocity.multLocal(0.86f);

        // Bewegt und Kontrolliert Geschwindikeit damit auf allen pcs gleich
        spatial.move(velocity.mult(0.15f * fixedTpf));
        if (target.getPosition().distance(spatial.getLocalTranslation()) < distance) {
            hit(fixedTpf);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) { }

    public void remove() {
        exists = false;
        if (geom2 != null) {
            geom2.removeFromParent();
        }
        spatial.removeFromParent();
        geom2 = null;
        tower.removeProjectile();
    }

    public int getType() {
        return type;
    }
}