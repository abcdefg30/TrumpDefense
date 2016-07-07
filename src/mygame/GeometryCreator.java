package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;

/**
 *
 * @author Amir
 */
public class GeometryCreator {
    public static GeometryCreator instance;
    public AssetManager assetManager;

    public GeometryCreator() {
        instance = this;
        assetManager = MainGame.instance.getAssetManager();
    }
    
    public Spatial createTowerGeom(int type) {
        Spatial geom;
        switch (type) {
            case Tower.TYPE_UNICORN:
                geom = (Spatial) assetManager.loadModel("Models/unicornfuv2.j3o");
                geom.rotate((float) Math.toRadians(90), 0, 0);
                break;
            case Tower.TYPE_MARINE:
                geom = (Spatial) assetManager.loadModel("Models/Militower.j3o");
                geom.rotate((float) Math.toRadians(90), 0, 0);
                break;
            default:
                // Generiere einen Marinetower als default, bis wir die anderen haben
                geom = (Spatial) assetManager.loadModel("Models/trumpdefensetower.j3o");
                geom.rotate((float) Math.toRadians(90), 0, 0);
                break;
        }
        return geom;
    }
    
    public Geometry createSmallBox(Vector3f as) {
        Box b = new Box(0.01f, 0.01f, 0.01f);

        Geometry boxs = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        boxs.setMaterial(mat);
        boxs.setLocalTranslation(as);
        return boxs;
    }

    public Spatial createRainbowLaser(Vector3f pos1, Vector3f pos2) {
        Vector3f vec = pos2.subtract(pos1);
        Vector3f vec2 = vec.divide(3);
        Vector3f vec3 = vec2.mult(2.0f);
        Vector3f pos3 = vec2.add(pos1);
        Vector3f pos4 = vec3.add(pos1);
        //thickness
        float t = 0.2f;
        //blau
        float x1 = pos1.getX();
        float y1 = pos1.getY();
        float z1 = pos1.getZ();
        //rot
        float x2 = pos2.getX();
        float y2 = pos2.getY();
        float z2 = pos2.getZ();
        //grün
        float x3 = pos3.getX();
        float y3 = pos3.getY();
        float z3 = pos3.getZ();
        //gelb
        float x4 = pos4.getX();
        float y4 = pos4.getY();
        float z4 = pos4.getZ();
        
        Vector3f [] vertices = new Vector3f[16];
        //blau
        vertices[0] = new Vector3f(x1-t,y1,z1+t);
        vertices[1] = new Vector3f(x1-t,y1,z1-t);
        vertices[2] = new Vector3f(x1+t,y1,z1-t);
        vertices[3] = new Vector3f(x1+t,y1,z1+t);
        //grün
        vertices[4] = new Vector3f(x3-t,y3,z3+t);
        vertices[5] = new Vector3f(x3-t,y3,z3-t);
        vertices[6] = new Vector3f(x3+t,y3,z3-t);
        vertices[7] = new Vector3f(x3+t,y3,z3+t);
        //gelb
        vertices[8] = new Vector3f(x4-t,y4,z4+t);
        vertices[9] = new Vector3f(x4-t,y4,z4-t);
        vertices[10] = new Vector3f(x4+t,y4,z4-t);
        vertices[11] = new Vector3f(x4+t,y4,z4+t);
        //rot
        vertices[12] = new Vector3f(x2-t,y2,z2+t);
        vertices[13] = new Vector3f(x2-t,y2,z2-t);
        vertices[14] = new Vector3f(x2+t,y2,z2-t);
        vertices[15] = new Vector3f(x2+t,y2,z2+t);
        float colors[] = {
            //blau
            0, 0, 1, 1,
            0, 0, 1, 1,
            0, 0, 1, 1,
            0, 0, 1, 1,
            //grün
            0, 1, 0, 1,
            0, 1, 0, 1,
            0, 1, 0, 1,
            0, 1, 0, 1,
            //gelb
            1, 1, 0, 1,
            1, 1, 0, 1,
            1, 1, 0, 1,
            1, 1, 0, 1,
            //rot
            1, 0, 0, 1,
            1, 0, 0, 1,
            1, 0, 0, 1,
            1, 0, 0, 1
        };
        int [] indexes = { 
            //blau
            0,1,2,
            0,2,3,
            //blau-->grün
            0,3,4,
            3,4,7,
            1,6,5,
            1,6,2,
            2,7,6,
            2,7,3,
            0,5,1,
            0,5,4,
            //grün-->gelb
            6,11,10,
            6,11,7,
            4,9,8,
            4,9,5,
            7,8,4,
            7,8,11,
            9,6,5,
            9,6,10,
            //gelb-->rot
            10,15,11,
            10,15,14,
            8,13,12,
            8,13,9,
            8,15,12,
            8,15,11,
            9,14,10,
            9,14,13,
            //rot
            12,13,14,
            12,14,15
        };
        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(colors));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indexes));
        mesh.updateBound();
        Geometry geo = new Geometry("OurMesh", mesh);
        Material mat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        geo.setMaterial(mat);
        return geo;
    }
    
    public Spatial createProjectile(Vector3f position, int type, Vector3f targetPosition) {
       switch(type) {
            case Projectile.TYPE_LASER:
                return createSmallBox(position);
            case Projectile.TYPE_NORMAL:
            default:
                return createSphere(position);
        }
    }
    
    public Geometry createBox(Vector3f as) {
        Box b = new Box(1f, 1f, 1f);

        Geometry boxs = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        boxs.setMaterial(mat);
        boxs.setLocalTranslation(as);
        return boxs;
    }

    public Geometry createSphere(Vector3f contactPoint) {
        Sphere b = new Sphere(30, 30, 1f);
        Geometry sphere = new Geometry("Sphere", b);
        sphere.setUserData("Health", 100);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        sphere.setMaterial(mat);
        sphere.setLocalTranslation(contactPoint);
        return sphere;
    }
}
