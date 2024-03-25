package plus.bw.cli;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;

import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;
import jme3test.water.TestMultiPostWater;
import plus.bw.utl.MatrixUtl;
import plus.bw.utl.RandomCtx;
import plus.bw.world.Matrix;
import plus.bw.world.gen.GlobalMapCGen;

public class Main extends SimpleApplication{


    public static void test(){
//        Win32GraphicsDevice.
//
//        WGLGraphicsConfig.getConfig()
//


    }

    public static void main(String[] args) {
        if(11==1){

            TestMultiPostWater app = new TestMultiPostWater();
            AppSettings s = new AppSettings(true);
            s.setRenderer(AppSettings.LWJGL_OPENGL2);
            s.setAudioRenderer(AppSettings.LWJGL_OPENAL);
            app.setSettings(s);

            app.start();

            return;
        }

//        if(21==1){
//            TestDirectionalLightShadow app = new TestDirectionalLightShadow(){
//                @Override
//                public void simpleInitApp() {
//                    super.simpleInitApp();
//                    MatrixVisual visual = new MatrixVisual();
//
//                    RandomCtx ctx = new RandomCtx();
//                    Matrix matrix = new GlobalMapCGen(64, 64, 3, ctx).make();
//
//                    matrix = MatrixUtl.copyOf(matrix, 150, 50, 200-25, 100-25);
//
//                    matrix.apply(key -> {
//                        return Math.max(key, 1);
//                    });
//                    matrix.apply(MatrixUtl.boost(0.1f));
//
//                    print(matrix);
//
//                    Material aMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//
//                    aMaterial.setTexture("DiffuseMap",
//                            assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
//                    aMaterial.setTexture("NormalMap",
//                            assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall_normal.jpg"));
//                    aMaterial.setFloat("Shininess", 64f);
//
//                    Node node = visual.make("Tets", matrix, key -> aMaterial, new AbstractInt2FloatFunction() {
//                        @Override
//                        public float get(int key) {
//                            return key;
//                        }
//                    });
//
//                    node.setLocalTranslation(0, 10, 0);
//                    node.setLocalScale(10);
//
//                    node.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//
//                    TangentBinormalGenerator.generate(node);
//                    rootNode.attachChild(node);
//                }
//            };
//            app.start();
//            return;
//        }

        new Main().start();
    }


    public Main(){
        //showSettings = false;
    }

    Material aMaterial;

    private void init(){
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
        //MatrixVisual visual = new MatrixVisual();

        RandomCtx ctx = new RandomCtx();
        Matrix orig = new GlobalMapCGen(64, 64, 4, ctx).make();

        manager.setViFunc(this::getMat, this::getH);
        manager.setMatrix(orig);

//        orig = MatrixUtl.zoom(orig, 2);
//        orig = MatrixUtl.smooth9(orig);
//        orig = MatrixUtl.smooth9(orig);
//        orig = MatrixUtl.smooth9(orig);

        int rescale = 1;

        rootNode.attachChild(manager.root());

        for (int x=7;x < (orig.width()/24)+1;x++){
            for (int y=7;y < (orig.height()/24)+1;y++){
                manager.show(x, y);
            }
        }

//        Node cenMap = new Node("map");
//        cenMap.setLocalTranslation(-25*4*rescale, 0, -25*4*rescale);
//
//        l1: for (int x=3;x<6;x++){
//            for (int y=3;y<6;y++){
//                int dx = 25*x;
//                int dy = 25*y;
//
//                Matrix matrix = MatrixUtl.copyOf(orig, dx, dy, dx + 25, dy + 25);
//
//                matrix.apply(key -> {
//                    return Math.max(key, -5);
//                });
//                matrix.apply(MatrixUtl.boost(0.1f));
//
//                //print(matrix);
//
//                Node node = visual.make("Tets-x="+x+",y="+y, matrix, this::getMat, this::getH);
//                TangentBinormalGenerator.generate(node);
//
//                node.setLocalTranslation(dx, -5, dy);
//
//                node.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//
//                cenMap.attachChild(node);
//
//                //break l1;
//            }
//        }
//        cenMap.setLocalScale(rescale);


        setDirectLight();
        setupLighting();
        addWater();

        getFlyByCamera().setMoveSpeed(93*rescale);
    }


    public static final int SHADOWMAP_SIZE = 1024;

    public void setDirectLight(){

        DirectionalLight l = new DirectionalLight();
        //l.setDirection(new Vector3f(0.5973172f, -0.16583486f, 0.7846725f).normalizeLocal());
        l.setDirection(new Vector3f(0.5f, -1.5f, -1));
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

        WaterFilter water = new WaterFilter(rootNode, new Vector3f(0.5f, -1.5f, -1));
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
        al.setColor(ColorRGBA.White.mult(0.15f));
        rootNode.addLight(al);
    }


    @Override
    public void simpleUpdate(float tpf) {
        manager.onTick();
    }


    @Override
    public void stop(boolean waitFor) {
        super.stop(waitFor);
        manager.service.shutdown();
    }
}
