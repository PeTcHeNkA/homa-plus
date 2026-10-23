package plus.space.client;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.SpaceWorldsMod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;


@SideOnly(Side.CLIENT)
public class StarTextureRepo {
    private static final IntObjectHashMap<ItemStack> cache = new IntObjectHashMap<>();
    private static final IntObjectHashMap<ResourceLocation> locs = new IntObjectHashMap<>();
    protected static final Item starItem = new Item(){
        @Override
        public Item setMaxDamage(int maxDamageIn) {
            super.setMaxDamage(maxDamageIn);
            this.addPropertyOverride(new ResourceLocation("id"),this::ckeck);
            return this;
        }
        @Override
        public boolean isFull3D() {
            return true;
        }
        private float ckeck(ItemStack itemStack, World world, EntityLivingBase entityLivingBase) {
            return itemStack.getMetadata();
        }
    }.setRegistryName("block_star_vi").setMaxDamage(20000);


//    public static ItemStack getSelection(int id){
//        return getById(12+id);
//    }


    public static ItemStack getById(int id){
        ItemStack result = cache.get(id);
        if(result==null) {
            cache.put(id, result = new ItemStack(starItem, 1, id, null));
        }
        return result;
    }

    private static final float[][] colors;


    static {
        colors = new float[8][];
        for (int i=1;i<9;i++){
            ResourceLocation loc;
            locs.put(i, loc = new ResourceLocation(SpaceWorldsMod.MOD_ID, "textures/sun/sun_"+i+".png"));
            try {
                InputStream is;
                BufferedImage image;
                float[] texture = new float[4];
                is = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
                image = ImageIO.read(is);
                colors[i-1] = image.getRaster().getPixel(image.getWidth() / 2, image.getHeight() / 2, texture);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    public static ResourceLocation getObjTexture(int id){
        if(id<9)return locs.get(id);
        if(id>=101 && id <= 220) {
            int id2 = ((id-102) % 20);
            if(id2==0)id2 = 20;
            ResourceLocation result = locs.get(id2);
            if(result==null){
                result = new ResourceLocation(SpaceWorldsMod.MOD_ID, "textures/planets/p_"+id2+".png");
                locs.put(id2, result);
            }
            return result;
        }
        //TODO
        if(1==1)return getObjTexture(120);
        //TODO
        return null;
    }


    public static boolean planetHasWater(int id){
        if(id>=101 && id <= 220) {
            int id2 = ((id-101) / 6) + 1;
            return id2 <= 8 || id2 == 11 || id2 == 12 || id2>=17;
        }
        return false;
    }


    public static int stationFraction(int id){
        if(id>35 || id < 20)return -1;
        return id>=28?id>=32?4:3:id>=24?2:1;
//        if(id>=28){
//            return id>=32?4:3;
//        } else {
//            return id>=24?2:1;
//        }
    }


    public static float[] getStarColor(int id){
        if(id>colors.length)id = colors.length;
        return colors[id-1];
    }


    public static void init(){
        MinecraftForge.EVENT_BUS.register(new StarTextureRepo());
    }


    @SubscribeEvent
    public void onReg(RegistryEvent.Register<Item> e){
        e.getRegistry().register(starItem);
    }


    @SubscribeEvent
    public void onReg(ModelRegistryEvent e){
        regItem(starItem);
    }


    private void regItem(Item item) {
        ModelResourceLocation mrl = new ModelResourceLocation(item.getRegistryName(),
                "inventory");
        ModelBakery.registerItemVariants(item, mrl);
        ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
    }
}