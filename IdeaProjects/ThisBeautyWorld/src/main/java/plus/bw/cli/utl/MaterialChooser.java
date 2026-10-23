package plus.bw.cli.utl;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.bw.utl.collect.Int2ObjectTuple;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;


public class MaterialChooser implements Int2ObjectFunction<Material> {
    private final AssetManager manager;
    private final Material[] mats;
    private final int[] index;

    public MaterialChooser(AssetManager manager, String name) {
        this.manager = manager;
        TsonMap map = manager.loadAsset(TsonLoader.key(name));
        TsonList list = map.getList("materials");

        Int2ObjectOpenHashMap<Material> materials = new Int2ObjectOpenHashMap<>();

        for (TsonObj obj:list){
            addMaterial(materials, obj.getMap());
        }

        ArrayList<Int2ObjectTuple<Material>> matArr = new ArrayList<>();

        for (Int2ObjectMap.Entry<Material> entry:materials.int2ObjectEntrySet()){
            matArr.add(new Int2ObjectTuple<>(entry.getIntKey(), entry.getValue()));
        }

        index = new int[matArr.size()];
        mats = new Material[index.length];

        for (int i=0;i<matArr.size();i++){
            Int2ObjectTuple<Material> item = matArr.get(i);
            index[i] = item.num;
            mats[i] = item.obj;
        }

        matArr.sort(Int2ObjectTuple.NUM_COMPARATOR);
    }


    private void addMaterial(Int2ObjectOpenHashMap<Material> materials, TsonMap map){
        TsonObj range = map.get("range");
        if(range == null)range = map.get("height");
        if(range == null)return;

        Material material = materialOfMap(map.getMap("material"));

        if(range.isMap() || range.isNumber() || range.isString()){
            addRange(materials, material, getRange(range));
        } else if (range.isList()) {
            for (TsonObj obj: range.getList()){
                addRange(materials, material, getRange(obj));
            }
        }
    }


    private void addRange(Int2ObjectOpenHashMap<Material> materials, Material material, int... rang){
        for (int i = 0;i<rang.length;i++) materials.put(rang[i], material);
    }


    private static int[] getRange(TsonObj range){
        int min = -1;
        int max = -1;

        if(range.isMap()){
            min = range.getMap().getOrDefaultInt("min", min);
            max = range.getMap().getOrDefaultInt("max", min);
        } else if (range.isNumber()) {
            min = max = range.getInt();
        } else if (range.isString()) {
            String str = range.getStr();
            if(str.contains(":")){
                String[] items = str.split(":");
                min = Integer.parseInt(items[0]);
                max = Integer.parseInt(items[1]);
            } else {
                min = max = Integer.parseInt(str);
            }
        }

        if(min > max){
            int temp = min;
            min = max;
            max = temp;
        }
        if(min == max)return new int[]{min};
        return new int[]{min, max};
    }


    public Material materialOfMap(TsonMap map){
        AssetManager asm = manager;
        Material material = new Material(asm,
                map.getOrDefaultStr("template", "Common/MatDefs/Light/Lighting.j3md")
        );
        map.ifContainsMap("texture", map1 -> {
            for (Map.Entry<String,TsonObj> entry: map1.entrySet()){
                String name = shortTextureKeyName(entry.getKey());
                material.setTexture(name, asm.loadTexture(entry.getValue().getStr()));
            }
        });

        map.ifContainsMap("flag", map12 -> {
            for (Map.Entry<String,TsonObj> entry: map12.entrySet()){
                String name = shortFlagName(entry.getKey());
                material.setBoolean(name, entry.getValue().isBool());
            }
        });

        map.ifContainsMap("color", map13 -> {
            for (Map.Entry<String,TsonObj> entry: map13.entrySet()){
                String name = shortColorName(entry.getKey());
                material.setColor(name, colorOf(entry.getValue()));
            }
        });

        map.ifContainsMap("float", new Consumer<TsonMap>() {
            @Override
            public void accept(TsonMap map) {
                for (Map.Entry<String,TsonObj> entry: map.entrySet()){
                    String name = shortFloatName(entry.getKey());
                    material.setFloat(name, entry.getValue().getFloat());
                }
            }
        });

        return material;
    }


    private static ColorRGBA colorOf(TsonObj obj){
        if(obj.isString()){
            String[] items = obj.getStr().split(",");
            float r = Float.parseFloat(items[0]);
            float g = Float.parseFloat(items[1]);
            float b = Float.parseFloat(items[2]);
            float a = items.length>3?Float.parseFloat(items[3]):1;
            return new ColorRGBA(r, g, b, a);
        }
        if(obj.isMap()){
            TsonMap map = obj.getMap();
            return new ColorRGBA(
                    map.getOrDefaultFloat("r",0),
                    map.getOrDefaultFloat("g",0),
                    map.getOrDefaultFloat("b",0),
                    map.getOrDefaultFloat("a",1)
            );
        }
        if(obj.isList()){
            TsonList list = obj.getList();
            return new ColorRGBA(
                    list.getFloat(0), list.getFloat(1), list.getFloat(2),
                    list.size()>3?list.getFloat(3):1
            );
        }
        throw new IllegalArgumentException();
    }


    private static String shortFloatName(String str){
        switch (str){
            case "shininess": return "Shininess";
            default: return str;
        }
    }


    private static String shortColorName(String str){
        switch (str){
            case "col"          :
            case "color"        :
            case "ambient"      : return "Ambient";
            case "diffuse"      : return "Diffuse";
            case "specular"     : return "Specular";
            case "glow"         : return "GlowColor";
            default: return str;
        }
    }


    private static String shortFlagName(String str){
        switch (str){
            case "mat-col"          :
            case "mat-color"        :
            case "material-col"     :
            case "material-colors"  : return "UseMaterialColors";
            case "alpha"            : return "UseAlpha";
            default: return str;
        }
    }


    private static String shortTextureKeyName(String str){
        switch (str){
            case "color"   : return "ColorMap";
            case "diffuse" : return "DiffuseMap";
            case "normal"  : return "NormalMap";
            case "glow"    : return "GlowMap";
            case "alpha"   : return "AlphaMap";
            case "specular": return "SpecularMap";
            default: return str;
        }
    }


    private static int binarySearchMinB(int[] a, int fromIndex, int toIndex,
                                        int key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return low-1; // key not found.
    }


    @Override
    public Material get(int height) {
        int num = binarySearchMinB(index, 0, index.length, height);
        if(num < 0)return mats[0];
        return mats[num];
    }
}

