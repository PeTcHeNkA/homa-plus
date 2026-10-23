package plus.bw.cli;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;

import com.jme3.system.AppSettings;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;
import jme3test.collision.TestMousePick;
import plus.bw.cli.utl.NodeUtl;
import plus.bw.cli.utl.TsonLoader;
import plus.bw.utl.MatrixUtl;
import plus.bw.utl.RandomCtx;
import plus.bw.cli.utl.collision.DeepNodeCollisionChecker;
import plus.bw.cli.utl.collision.NodeCollisionResults;
import plus.bw.world.Matrix;
import plus.bw.world.gen.GlobalMapCGen;

import java.util.List;
import java.util.Objects;

public class Main extends SimpleApplication{
    public static void main(String[] args) {
        if(12==1){
            TestMousePick app = new TestMousePick();
            app.start();
            return;
        }
        new Main().start();
    }


    public Main(){
        //showSettings = false;
        //settings.setResizable(true);
        settings = new AppSettings(true);
        settings.setTitle("This Beauty World :3 v-pre-0.1.4");
        settings.setWindowSize(1920, 1080);
        //settings.setIcons();
        //showSettings = false;
    }

    Material aMaterial;

    private void init(){
        assetManager.registerLoader(TsonLoader.class, "tson","TSON");

        aMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

        aMaterial.setTexture("DiffuseMap",
                assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
        aMaterial.setTexture("NormalMap",
                assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall_normal.jpg"));
        aMaterial.setFloat("Shininess", 64f);
    }


    public Material getMat(int num){
        return aMaterial;
    }

    public float getH(int num){
        return num+5;
    }

    private static String print(Matrix m){
        printL(131);
        String res = m.toString(" ", MatrixUtl.viFunc(5, 27, 70," ",".", "+", "#"));
        System.out.println(res);
        printL(131);
        return res;
    }
    private static void printL(int n){
        for (int i=0;i<n;i++) System.out.print('-');
        System.out.print('\n');
    }


    private final MapRenderManager manager = new MapRenderManager();

    @Override
    public void simpleInitApp() {
        init();

        flyCam.setEnabled(true);
        inputManager.addMapping("F", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                //if(isPressed){
                    System.out.println("NEN");
                    flyCam.setEnabled(!flyCam.isEnabled());
                    if(flyCam.isEnabled()){
                        inputManager.setCursorVisible(false);
                    }
                //}
            }
        }, "F");

        //MatrixVisual visual = new MatrixVisual();

        RandomCtx ctx = new RandomCtx(2000);
        Matrix orig = new GlobalMapCGen(64, 64, 4, ctx).make();

        manager.setViFunc(this::getMat, this::getH);
        manager.setMatrix(orig);

        int rescale = 1;

        rootNode.attachChild(manager.system());

        for (int x=1;x < (orig.width()/24)+1;x++){
            for (int y=1;y < (orig.height()/24)+1;y++){
                manager.show(x, y);
            }
        }

        setDirectLight();
        setupLighting();
        addWater();

        getFlyByCamera().setMoveSpeed(93*rescale);

        initMark();
    }

    private Geometry mark;
    @Deprecated
    private void initMark() {
        Arrow arrow = new Arrow(Vector3f.UNIT_Z.mult(2f));
        mark = new Geometry("BOOM!", arrow);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }


    boolean displayCursor = false;


    DeepNodeCollisionChecker checker = new DeepNodeCollisionChecker();
    @Deprecated
    public void updateRTX(){
        if(!inputManager.isCursorVisible()){
            cursor(false);
            return;
        }
        Vector3f origin    = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
        Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();
        Ray ray = new Ray(origin, direction);

        List<NodeCollisionResults> results = checker.searchCollisions(manager.root(), ray).getResults();
        //CollisionResults results = new CollisionResults();
        //manager.root().collideWith(ray, results);

        if (results.size() > 0) {
            CollisionResult closest = results.get(0).getClosestCollision();

            Triangle tr = closest.getTriangle(null);
            String trt = "["+tr.get1()+","+tr.get2()+","+tr.get3()+"]";
            if(!Objects.equals(trt, prev)){
                prev = trt;
                if(prevG != null){
                    prevG.removeFromParent();
                    prevG = null;
                }

                //results.get(0).getNode()

                prevG = triToBox(tr);

                Vector3f shift = NodeUtl.getShift(manager.system(), results.get(0).getNode());
                prevG.getLocalTranslation().addLocal(shift);

                manager.system().attachChild(prevG);
                //System.out.println(trt);
            }

            mark.setLocalTranslation(closest.getContactPoint());
            Quaternion q = new Quaternion();
            q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Y);
            mark.setLocalRotation(q);

            cursor(true);
        } else {
            cursor(false);
        }
    }

    private String prev = null;
    private Geometry prevG = null;

    private Geometry triToBox(Triangle tr){
        //[(22.5, 17.0, 10.5),(22.5, 17.0, 11.5),(23.5, 17.0, 10.5)]
        float minX = min(tr.get1().x, tr.get2().x, tr.get3().x);
        float maxX = max(tr.get1().x, tr.get2().x, tr.get3().x);

        float minY = min(tr.get1().y, tr.get2().y, tr.get3().y);
        float maxY = max(tr.get1().y, tr.get2().y, tr.get3().y);

        float minZ = min(tr.get1().z, tr.get2().z, tr.get3().z);
        float maxZ = max(tr.get1().z, tr.get2().z, tr.get3().z);

        Mesh box = new Box(0.5f, 0.5f, 0.5f);
        Geometry geo = new Geometry("Test", box);
        geo.setMaterial(getMat(0));

        geo.setLocalTranslation(minX+0.5f, minY+0.5f, minZ+0.5f);

        return geo;
    }

    private static float max(float a, float b, float c){
        if(a<b)return b < c?c:b;
        return a<c?c:a;
    }

    private static float min(float a, float b, float c){
        if(a>b)return b > c?c:b;
        return a>c?c:a;
    }

    private void cursor(boolean b){
        if(b) {
            if(!displayCursor) {
                System.out.println("ADD");
                rootNode.attachChild(mark);
                displayCursor = true;
            }
        } else {
            if(displayCursor) {
                System.out.println("REM");
                rootNode.detachChild(mark);
                //rootNode.detachChild(mark);
                displayCursor = false;
            }
        }
    }


    public static final int SHADOWMAP_SIZE = 1024;

    private Vector3f lightDirection = new Vector3f(0.5f, -1.5f, 1f);

    public void setDirectLight(){

        DirectionalLight l = new DirectionalLight();
        //l.setColor(ColorRGBA.White.mult(1.3f));
        //l.setDirection(new Vector3f(0.5973172f, -0.16583486f, 0.7846725f).normalizeLocal());
        l.setDirection(lightDirection);
        rootNode.addLight(l);

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(l);
        dlsr.setLambda(0.55f);
        dlsr.setShadowIntensity(0.8f);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        //dlsr.displayDebug();

        viewPort.addProcessor(dlsr);


//        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
//        dlsf.setLight(l);
//        dlsf.setLambda(0.75f);
//        dlsf.setShadowIntensity(0.55f);
//        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
//        dlsf.setEnabled(false);
//
//        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
//        fpp.addFilter(dlsf);

//        viewPort.addProcessor(fpp);
        
        //DIRECTIONAL LIGHT
//        DirectionalLight directionalLight = new DirectionalLight();
//        rootNode.addLight(directionalLight);
//        directionalLight.setColor(ColorRGBA.White);
//        directionalLight.setDirection(new Vector3f(-1f, -1.2f, 4f));
//
//        DirectionalLightShadowRenderer dlsr
//                = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE*2, 4);
//        dlsr.setLight(directionalLight);
//        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCF4);
//
//        DirectionalLightShadowFilter dlsf
//                = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE*2, 4);
//        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCF4);
//        dlsf.setLight(directionalLight);
//        dlsf.setEnabled(true);
//
//
//
//        viewPort.addProcessor(dlsr);
//
//        dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
//        dlsf.setLight(directionalLight);
//        dlsf.setLambda(0.55f);
//        dlsf.setShadowIntensity(0.8f);
//        dlsf.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
//        dlsf.setEnabled(false);
//
//        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
//        fpp.addFilter(dlsf);
//
//        viewPort.addProcessor(fpp);
    }


    public void addWater(){
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

        WaterFilter water = new WaterFilter(rootNode, lightDirection);
        water.setCenter(new Vector3f(200, -15.830074f, 200));
        water.setRadius(200*200);
        water.setWaveScale(0.005f);
        water.setSpeed(0.13f);
        water.setMaxAmplitude(0.4f);
        water.setFoamExistence(new Vector3f(0.5f, 2, 0.5f));
        water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
        water.setRefractionStrength(0.4f);
        water.setWaterHeight(1);

        fpp.addFilter(water);

        viewPort.addProcessor(fpp);
    }


    public void setupLighting(){
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.09f));
        rootNode.addLight(al);
    }


    @Override
    public void simpleUpdate(float tpf) {
        manager.onTick();

        updateRTX();
    }


    @Override
    public void stop(boolean waitFor) {
        super.stop(waitFor);
        manager.service.shutdown();
    }
}
