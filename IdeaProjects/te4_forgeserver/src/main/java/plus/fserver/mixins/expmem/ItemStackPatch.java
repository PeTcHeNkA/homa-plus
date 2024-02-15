package plus.fserver.mixins.expmem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.IRegistryDelegate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


@Mixin(ItemStack.class)
public abstract class ItemStackPatch{
    @Shadow private boolean isEmpty;
    @Shadow private int stackSize;
    @Shadow int itemDamage;
    @Shadow private NBTTagCompound stackTagCompound;
    @Shadow(remap = false) private CapabilityDispatcher capabilities;
    @Shadow(remap = false) private NBTTagCompound capNBT;
    @Shadow(remap = false) private IRegistryDelegate<Item> delegate;
    @Shadow public abstract Item getItem();
    @Shadow public abstract int getAnimationsToGo();
    @Shadow(remap = false) @Nullable
    protected abstract Item getItemRaw();
    @Unique
    private boolean needInitCap = true;
    /**
     * @author PrototypeTrousers, Rongmario
     */
    @Unique
    public void initializeCapabilities() {
        needInitCap = false;
        Item item = getItemRaw();
        if (item != null) {
            this.capabilities = ForgeEventFactory.gatherCapabilities((ItemStack) (Object) this, item.initCapabilities((ItemStack) (Object) this, this.capNBT));
            if (this.capNBT != null && this.capabilities != null) {
                this.capabilities.deserializeNBT(this.capNBT);
            }
        }
    }

    /**
     * @author Rongmario
     * @reason If capabilities isn't present, copy capNBT as it maybe cached from previous copies while capabilities isn't instantiated yet
     */
    @Overwrite
    public ItemStack copy() {
        ItemStack stack = new ItemStack(this.getItem(), this.stackSize, this.itemDamage, this.capabilities != null ? this.capabilities.serializeNBT() : this.capNBT);
        stack.setAnimationsToGo(this.getAnimationsToGo());
        if (this.stackTagCompound != null) {
            ((ItemStackPatch) (Object) stack).stackTagCompound = this.stackTagCompound.copy();
        }
        return stack;
    }

    /**
     * @author PrototypeTrousers, Rongmario
     * @reason Only query capabilities when needed
     */
    @Overwrite(remap = false)
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (this.isEmpty) return false;
        if (needInitCap) initializeCapabilities();
        return this.capabilities != null && this.capabilities.hasCapability(capability, facing);
    }

    /**
     * @author PrototypeTrousers, Rongmario
     * @reason Only query capabilities when needed
     */
    @Nullable
    @Overwrite(remap = false)
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (this.isEmpty) return null;
        if (needInitCap) initializeCapabilities();
        return this.capabilities == null ? null : this.capabilities.getCapability(capability, facing);
    }

    /**
     * @author PrototypeTrousers, Rongmario
     * @reason Only query capabilities when needed
     */
    @Overwrite(remap = false)
    public boolean areCapsCompatible(ItemStack other) {
        if (needInitCap) initializeCapabilities();
        if (((ItemStackPatch) (Object) other).needInitCap) {
            ((ItemStackPatch) (Object) other).initializeCapabilities();
        }
        if (this.capabilities == null)
            return ((ItemStackPatch) (Object) other).capabilities == null || ((ItemStackPatch) (Object) other).capabilities.areCompatible(null);

        return this.capabilities.areCompatible(((ItemStackPatch) (Object) other).capabilities);
    }

    /**
     * @author PrototypeTrousers
     * @reason Only query capabilities when needed
     */
    @Overwrite(remap = false)
    private void forgeInit() {
        Item item = this.getItemRaw();
        if (item != null) this.delegate = item.delegate;
    }
}