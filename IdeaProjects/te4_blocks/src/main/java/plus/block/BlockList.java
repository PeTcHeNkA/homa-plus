package plus.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.math.Vec3d;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.TsonSerelizable;
import plus.utl.App;
import plus.utl.Applicator;
import plus.utl.HitBoxBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BlockList extends ArrayList<net.minecraft.block.Block> implements TsonSerelizable {

    protected static final Applicator<net.minecraft.block.Block, TsonObj> app = new Applicator<>(
            new App<net.minecraft.block.Block, TsonObj>("unbreakable") {
                @Override
                public void apply(net.minecraft.block.Block block, TsonObj obj) {
                    if(!obj.isBool() || obj.getBool()){
                        block.setHardness(-1.0F);
                    }
                }
            },
            new App<net.minecraft.block.Block, TsonObj>("hardness") {
                @Override
                public void apply(net.minecraft.block.Block block, TsonObj obj) {
                    block.setHardness(obj.getFloat());
                }
            },
            new App<net.minecraft.block.Block, TsonObj>("resistance") {
                @Override
                public void apply(net.minecraft.block.Block block, TsonObj obj) {
                    block.setResistance(obj.getFloat());
                }
            }
    );

    public String id;
    public String name;
    public int minId = -1;
    public int maxId = -1;
    public HitBoxBuilder[] hitBoxBuilder;
    public boolean isTranslusent = false;
    public boolean isDirectional = false;
    public boolean genStairs = false;
    public boolean genSlabs = false;
    public boolean genWall = false;
    public boolean genPane = false;
    public TsonMap tags;
    public String material;
    public boolean useSmartTransparency = false;
    public boolean useGlassRender = false;

    public BlockList(){}


    public BlockList(TsonMap map){
        id = map.getStr("id");
        if(map.containsKey("name")){
            name = map.getStr("name");
        } else {
            name = id;
        }
        if(map.containsKey("range")){
            TsonList list = map.getList("range");
            int n1 = list.getInt(0);
            int n2 = list.getInt(1);
            minId = Math.min(n1, n2);
            maxId = Math.max(n1, n2);
        }
        if(map.containsKey("add")){
            TsonList additional = map.getList("add");
            isTranslusent = additional.contains("translucent");
            useSmartTransparency = !additional.contains("strong-transparency");
            isDirectional = additional.contains("directional");
            useGlassRender = additional.contains("glass-render");
            if(!isDirectional){
                genStairs = additional.contains("stairs");
                genSlabs = additional.contains("slabs");
                genWall = additional.contains("wall");
                genPane = additional.contains("pane");
            }
        }
        if(!genStairs && !genSlabs && map.containsKey("hitbox")){
            TsonObj obj = map.get("hitbox");
            if(obj.isString()){
                hitBoxBuilder = new HitBoxBuilder[1];
                hitBoxBuilder[0] = parse(obj.getStr());
            } else if(obj.isList()){
                hitBoxBuilder = new HitBoxBuilder[obj.getList().size()];
                int i = 0;
                for (TsonObj obj2:obj.getList()){
                    hitBoxBuilder[i] = parse(obj2.getStr());
                    ++i;
                }
            } else {
                hitBoxBuilder = null;
            }
        } else {
            hitBoxBuilder = null;
        }
        if(map.containsKey("tag")){
            tags = map.getMap("tag");
        } else {
            tags = new TsonMap();
        }
        TsonObj mat = tags.get("material");
        material = mat==null?null:mat.getStr();

        //generate
        if(minId != maxId && minId != -1){
            for(int i = minId;i<=maxId;i++){
                addAll(gen(id+"_"+i, name+"_"+i));
            }
        } else {
            addAll(gen(id, name));
        }
    }


    @Override
    public TsonMap toTson() {
        TsonMap map = new TsonMap();
        map.put("id", id);
        if(!id.equals(name)){
            map.put("name", name);
        }
        if(minId != maxId && minId != -1){
            TsonList list = map.addList("range");
            list.add(minId);
            list.add(maxId);
        }
        if(hitBoxBuilder != null){
            TsonList list = map.addList("hitbox");
            for(HitBoxBuilder b:hitBoxBuilder){
                list.add(b.toString());
            }
        }
        if(isDirectional || genStairs || genSlabs || genWall || genPane || useGlassRender
                || isTranslusent || useSmartTransparency){
            TsonList additional = map.addList("add");
            if(isDirectional){
                additional.add("directional");
            }
            if(genStairs){
                additional.add("stairs");
            }
            if(genSlabs){
                additional.add("slabs");
            }
            if(genWall){
                additional.add("wall");
            }
            if(genPane){
                additional.add("pane");
            }
            if(isTranslusent){
                additional.add("translucent");
            }
            if(useSmartTransparency){
                additional.add("strong-transparency");
            }
            if(useGlassRender){
                additional.add("glass-render");
            }
        }
        if(!tags.isEmpty()){
            map.put("tag", tags);
        }
        return map;
    }


    protected Collection<net.minecraft.block.Block> gen(String id, String name){
        List<net.minecraft.block.Block> blocks = new ArrayList<>();
        net.minecraft.block.Block block;

        if(!isDirectional && tags.containsKey("meta")){
            TsonList meta = tags.getList("meta");
            IProperty<?>[] properties = new IProperty[meta.size()];
            int i = 0;
            for(TsonObj obj:meta){
                //TsonObj obj = meta.get(key);
                //if(obj.isBool() ||obj.getStr().equalsIgnoreCase("bool")){
                properties[i] = PropertyBool.create(obj.getStr());
                //}// else if (obj.isNumber()) {
                //    properties[i] = PropertyInteger.create(key, 0,obj.getInt());
                //}
                i++;
            }
            if(hitBoxBuilder == null) {
                block = new Block.Parametrized(material, id, name, !isTranslusent) {
                    @Override
                    IProperty<?>[] iProperties() {
                        return properties;
                    }
                };
            } else {
                block = new BlockHitboxed.Parametrized(material, id, name,
                        !(isTranslusent || useSmartTransparency), hitBoxBuilder){
                    @Override
                    IProperty<?>[] iProperties() {
                        return properties;
                    }
                };
            }
        } else {
            if(isDirectional){
                if (hitBoxBuilder == null) {
                    block = new BlockDirectional(material, id, name, !isTranslusent);
                } else {
                    block = new BlockDirectionalHitboxed(material, id, name,
                            !(isTranslusent || useSmartTransparency), hitBoxBuilder);
                }
            } else {
                if (hitBoxBuilder == null) {
                    if(useGlassRender){
                        block = new BlockGlass(material, id, name, !isTranslusent);
                    } else {
                        block = new Block(material, id, name, !isTranslusent);
                    }
                } else {
                    block = new BlockHitboxed(
                            material, id, name, !(isTranslusent || useSmartTransparency), hitBoxBuilder);
                }
            }
        }

        blocks.add(block);

        if(!isDirectional && hitBoxBuilder == null) {
            if (genStairs) {
                blocks.add(new BlockStairs(id, block, name,
                        !(isTranslusent ||useSmartTransparency)
                ));
            }
            if(genWall){
                blocks.add(new BlockWall(id, block, name,
                        !(isTranslusent ||useSmartTransparency)
                ));
            }
            if(genPane){
                blocks.add(new BlockPane(material, id, name,
                        !(isTranslusent ||useSmartTransparency)
                ));
            }
            if (genSlabs) {
                blocks.add(new BlockSlab(material, id, name,
                        !(isTranslusent ||useSmartTransparency)
                ));
            }
        }

        for (String key : tags.keySet()) {
            TsonObj tag = tags.get(key);
            for(net.minecraft.block.Block b:blocks) {
                app.apply(key, b, tag);
            }
        }

        return blocks;
    }


    protected static HitBoxBuilder parse(String s){
        Vec3d vecStart;
        Vec3d vecEnd;
        if(s.contains(":")){
            String[] ss = s.split(":");
            vecStart = parseVec(ss[0]);
            vecEnd = parseVec(ss[1]);
        } else {
            vecEnd = parseVec(s);
            vecStart = new Vec3d(
                    ((1-vecEnd.x)/2),
                    ((1-vecEnd.y)/2),
                    ((1-vecEnd.z)/2)
            );
            vecEnd = new Vec3d(
                    vecStart.x+vecEnd.x,
                    vecStart.y+vecEnd.y,
                    vecStart.z+vecEnd.z
            );
        }
        return new HitBoxBuilder(vecStart, vecEnd);
    }


    protected static Vec3d parseVec(String data){
        data = data.trim();
        String[] raw = data.split(",");
        return new Vec3d(
                Double.parseDouble(raw[0]),
                Double.parseDouble(raw[1]),
                Double.parseDouble(raw[2])
        );
    }
}