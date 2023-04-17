package plus.client.entity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.player.PlayerSpship;
import plus.common.spship.item.ItemCorpus;


@SideOnly(Side.CLIENT)
public final class SpModelManager {
    private static final SpModelManager INST = new SpModelManager(33);

    private final ItemStack[] models;

    private SpModelManager(int count) {
        models = new ItemStack[count];
        for(int i=0;i<count;i++){
            models[i] = ItemCorpus.SP_MODEL.createSimple(i);
        }
    }

    private ItemStack getSpModelByID0(int id){
        if(id>= models.length || id<0)return models[0];
        return models[id];
    }


    public static ItemStack getSpModelByID(int id){
        return INST.getSpModelByID0(id);
    }


    public static ItemStack getSpModelByUser(PlayerSpship player){
        return INST.getSpModelByID0(player.getSpModelId());
    }
}