package plus.mixins;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Item.class)
public abstract class MixinItem extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<Item> {
    private static int glHsc = 0;
    private int hsc;

    @Shadow
    public abstract void addPropertyOverride(ResourceLocation key, IItemPropertyGetter getter);


    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(CallbackInfo ci){
        hsc = ++glHsc;
        addPropertyOverride(new ResourceLocation("cmd"), this::apply);
    }


//    @SideOnly(Side.CLIENT)
    public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn) {
        NBTTagCompound nbt = stack.getTagCompound();
        if(nbt==null){
            return 0;
        }
        return nbt.getInteger("cmd");
    }


    @Override
    public int hashCode() {
        return hsc;
    }
}