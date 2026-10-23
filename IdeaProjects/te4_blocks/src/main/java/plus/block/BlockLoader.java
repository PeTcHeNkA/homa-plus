package plus.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;
import plus.item.ItemComplect;
import plus.tson.*;
import plus.wepather.PathProxy;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


public class BlockLoader extends ArrayList<IForgeRegistryEntry.Impl<?>> {

    public BlockLoader(){
        this("te4blocks", true);
    }


    public BlockLoader(String path, boolean genDefaults){
        this(load(path, genDefaults));
    }


    public BlockLoader(TsonList list){
        for(TsonObj obj:list){
            Object obj0 = obj.getField();
            if(obj0 instanceof Item || obj0 instanceof Block){
                add((IForgeRegistryEntry.Impl<?>)obj0);
            } else if(obj0 instanceof Collection){
                for(Object o:(Collection)obj0){
                    add((IForgeRegistryEntry.Impl<?>)o);
                }
            }
        }
    }


    private static TsonList load(String filePath, boolean genDefaults){
        File file = new File(Loader.instance().getConfigDir(), filePath+".tson");
        String data = TsonFile.read(file, "")
                .replace("\r","").replace("\t","");
        TsonList l;

        if(data.equals("")){
            l = new TsonList();
            if(genDefaults) {
                BlockList gen = new BlockList();
                l.add(gen);

                gen.name = "example_key";
                gen.id = "example";
                gen.genSlabs = true;
                gen.genStairs = true;
                gen.genWall = true;
                gen.genPane = true;
                gen.isDirectional = true;
                gen.isTranslusent = true;
                gen.useSmartTransparency = true;
                gen.useGlassRender = true;
                gen.material = "ROCK";
                gen.maxId = 1;
                gen.minId = 0;

                gen.tags = new TsonMap();
                gen.tags.put("unbreakable", true);
                gen.tags.put("hardness", 1.5);
                gen.tags.put("resistance", 2.5);
                TsonList meta = gen.tags.addList("meta");
                meta.add("example_bool");

                ItemComplect complect = new ItemComplect();
                l.add(complect);
                complect.id = "example_item_kit";
                complect.name = "example_item_kit";
                complect.material = ItemComplect.newMaterial("MATERIAL_ID",
                        2, 1000, 10f, 5f, 15);

                TsonFile.write(file, "COMMENT: All supported classes in package plus.block: \n" +
                        "Block, BlockDirectional, BlockPane, BlockDirectionalHitboxed, BlockHitboxed, BlockSlab, BlockList, BlockLoader.\n" +
                        "Also, BlockSlab, BlockPane, BlockDirectional, BlocktGlass has simple constructor <(obj), \"material\", \"id\", \"name\", (Bool)>\n"+
                        "Block has 2 additional constructors: <(obj), \"material\", \"id\"> and <(obj), \"material\", \"id\", \"name\">\n"+
                        "BlockGlass has additional constructor <(obj), \"material\", \"id\", \"name\", (Bool)>\n"+
                        "All supported classes in package plus.item:\n" +
                        "Item, ItemFood, ItemComplect\n" +
                        "Item has 2 additional constructors: <(obj), \"id\"> and <(obj), \"id\", \"name\">\n"+
                        "ItemFood has 2 additional constructors: <(obj), \"id\", (Int), (Float)> and <(obj), \"id\", \"name\", (Int), (Float)>\n"+
                        "Warning! All tags supported only BlockList! However, it ignores conflicting tags.\n\n\n"
                + l);
            }
        } else {
            l = new TsonList(data);
        }
        return l;
    }


    public void register() {
        //Logger log = Logger.getLogger(getClass().getCanonicalName());
        System.out.printf("Loaded %s objects!%n", this.size());
        HashSet<String> registred = new HashSet<>();
        for (IForgeRegistryEntry.Impl<?> o : this) {
            if(o.getRegistryName()==null){
                continue;
            }
            if(registred.contains(o.getRegistryName().toString())){
                System.out.printf("Duplicate name (%s:%s)! Ignoring.%n", o.getClass(), o.getRegistryName());
                continue;
            } else {
                registred.add(o.getRegistryName().toString());
            }
            if(o instanceof Block) {
                Block block = (Block) o;

                ForgeRegistries.BLOCKS.register(block);
                ForgeRegistries.ITEMS.register(
                        new ItemBlock(block).setRegistryName(block.getRegistryName())
                );
            } else if(o instanceof Item) {
                Item item = (Item) o;
                ForgeRegistries.ITEMS.register(item);
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public void registerRender() {
        for (IForgeRegistryEntry.Impl<?> o : this) {
            if(o instanceof Block) {
                setRender((Block) o);
            } else {

            }
        }
    }


    @SideOnly(Side.CLIENT)
    private static ModelResourceLocation of(IForgeRegistryEntry.Impl<?> o){
        return new ModelResourceLocation(o.getRegistryName(), "inventory");
    }


    @SideOnly(Side.CLIENT)
    private static void setRender(Block block) {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(
                Item.getItemFromBlock(block), 0, of(block)
        );
    }


    public void onPostInit(){
        HashSet<String> registred = new HashSet<>();
        for (IForgeRegistryEntry.Impl<?> o : this) {
            if(o.getRegistryName()==null){
                continue;
            }
            if(registred.contains(o.getRegistryName().toString())){
                continue;
            } else {
                registred.add(o.getRegistryName().toString());
            }
            if(o instanceof Block) {
                Block block = (Block) o;

                if(block instanceof BlockStairs){
                    PathProxy.insertToWE((BlockStairs) block);
                } else if(block instanceof BlockDirectional){
                    PathProxy.insertToWE((BlockDirectional) block);
                } else if(block instanceof BlockSlab){
                    PathProxy.insertToWE((BlockSlab) block);
                }
            }
        }
    }
}