package plus.client.entity;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.player.PlayerSpship;
import plus.common.spship.item.ItemCorpus;


@SideOnly(Side.CLIENT)
public final class SpModelManager {
    private static final SpModelManager INST = new SpModelManager();

    private final IntObjectHashMap<ItemStack> models = new IntObjectHashMap<>();

    private SpModelManager() {}

    private ItemStack getSpModelByID0(int id){
        ItemStack result = models.get(id);
        if(result==null){
            models.put(id, result = ItemCorpus.SP_MODEL.createSimple(id));
        }
        return result;
    }


    public static ItemStack getSpModelByID(int id){
        return INST.getSpModelByID0(id);
    }


    public static ItemStack getSpModelByUser(PlayerSpship player){
        return INST.getSpModelByID0(player.getSpModelId());
    }
}