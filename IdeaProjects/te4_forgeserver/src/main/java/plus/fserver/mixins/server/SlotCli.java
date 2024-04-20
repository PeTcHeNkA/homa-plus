package plus.fserver.mixins.server;

import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(Slot.class)
public class SlotCli {
    /**
     * @author Te4hno
     * @reason ignore set xy slot
     */
    @Overwrite(remap = false)
    public void defSlotCli(int x, int y){}


    /**
     * @author Te4hno
     * @reason ignore set xy slot
     */
    @Overwrite(remap = false)
    public void setSlotNum(int num) {}
}