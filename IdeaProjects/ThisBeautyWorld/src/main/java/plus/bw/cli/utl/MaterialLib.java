package plus.bw.cli.utl;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import plus.tson.utl.Te4HashMap;


public class MaterialLib extends Te4HashMap<String, Material> {
    private static final String prefix = "assets/Textures/", normalSuffix = "_n", defEnding = ".png";
    private final AssetManager assetManager;

    public MaterialLib(AssetManager assetManager) {
        this.assetManager = assetManager;
    }


    public AssetManager getAssetManager(){
        return assetManager;
    }


    public Material add(String name, boolean normal){
        return add(name, normal, 64);
    }


    public Material add(String name, boolean normal, float shininess){
        if(containsKey(name))return get(name);
        Material material;
        put(name, material = createBase(assetManager, name, normal, shininess));
        return material;
    }


    public static Material createBase(AssetManager manager, String name, boolean normal, float shininess){
        String fullName = prefix+name;
        String ending = chooseEnding(name);

        Material material = new Material(manager, "Common/MatDefs/Light/Lighting.j3md");

        material.setTexture("DiffuseMap", manager.loadTexture(fullName+ending));
        if(normal) material.setTexture("NormalMap", manager.loadTexture(fullName+normalSuffix+ending));



        if(shininess == shininess) material.setFloat("Shininess", shininess);//64f
        return material;
    }



    private static String chooseEnding(String name){
        int index = name.indexOf('.', Math.max(0, name.length()-7));
        if(index == -1)return defEnding;
        return name.substring(index);
    }
}



