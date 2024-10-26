package net.minecraft.stats;

import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.api.inject.VarOnly;


public class StatCrafting extends StatBase {
    @SideOnly(Side.CLIENT)
    private Item item;

    public StatCrafting(String id, String id2, ITextComponent display, Item item) {
        super(id + id2, display);
        setItem(item);
    }


    @VarOnly(var = "SIDE_SERVER", code = true)
    private void setItem(Item item){
        this.item = item;
    }


    @SideOnly(Side.CLIENT)
    public Item getItem() {
        return this.item;
    }
}