package plus.mobs.ai.model;

import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import plus.mobs.frame.AbcMob;


public class AiLikeItemModel implements AbcMob.IEntityApplicator {
    private final Item likedItem;

    public AiLikeItemModel(String itemId){
        likedItem = Item.REGISTRY.getObject(new ResourceLocation(itemId));
    }


    @Override
    public void apply(AbcMob mob) {
        mob.tasks.addTask(5, new EntityAITempt(mob, 1.25D,
                likedItem, false));
    }
}