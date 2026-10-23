package plus.inv.part;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.crafting.CraftingManager;
import plus.invlib.render.Updateable;


public class Recepts implements Updateable {


    @Override
    public void update() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        CraftingManager.REGISTRY.forEach(iRecipe -> {




        });


    }
}