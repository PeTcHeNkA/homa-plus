package plus.space.client.lscreen;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import plus.fserver.utls.math.FastRandom;
import plus.space.SpaceWorldsMod;


public class LResLocation extends ResourceLocation {
    public static final LResLocation MEH_BG = new LResLocation("sel_besh_bg");



    private static final int[] images = {1,1};
    private static final int[] titles = {1,4};
    private static final IntObjectHashMap<SafeGetter> objs = new IntObjectHashMap<>();

    public LResLocation(Object num) {
        super(SpaceWorldsMod.MOD_ID, "textures/lscreen/"+num+".png");
    }

    private interface SafeGetter{
        Object[] get();
        default Object[] getSafe(){
            try {
                Object[] res = get();
                return res;
            } catch (Exception e){
                return null;
            }
        }
    }

    static {
        objs.put(3, () -> new Object[]{Minecraft.getMinecraft().player.getDisplayNameString()});
        objs.put(4, () -> {
            WorldClient world = Minecraft.getMinecraft().world;
            if(world==null)return new Object[]{"iris"};
            return new Object[]{world.getWorldInfo().getWorldName()};
        });
    }


    public static ResourceLocation getRandomImage(){
        int id = FastRandom.INST.nextInt(images[1]+images[0]+1)-images[0];
        return new LResLocation(id);
    }


    public static String getRandomTitle(){
        return getTitle(FastRandom.INST.nextInt(titles[1]+titles[0]+1)-titles[0]);
    }


    public static String getTitle(int id){
        SafeGetter getter = objs.get(id);
        if(getter==null)
            return I18n.translateToLocal("lscreen.title."+id);
        Object[] res = getter.getSafe();
        if(res==null)
            return I18n.translateToLocal("lscreen.title.def"+id);
        return I18n.translateToLocalFormatted("lscreen.title."+id, res);
    }
}



