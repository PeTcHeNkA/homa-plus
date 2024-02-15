package plus.fserver.mixins.pf;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.fserver.patch.PBlockState;


@Mixin(targets = {"net.minecraftforge.registries.GameData$BlockCallbacks"}, remap = false, priority = 0)
public abstract class BlockCallbacksPatch implements IForgeRegistry.AddCallback<Block> {
    /**
     * @author Te4hno
     * @reason Speedy
     */
    @Inject(method = "onAdd(Lnet/minecraftforge/registries/IForgeRegistryInternal;Lnet/minecraftforge/registries/RegistryManager;ILnet/minecraft/block/Block;Lnet/minecraft/block/Block;)V", at = @At("RETURN"))
    public void onAdd0(IForgeRegistryInternal<Block> owner, RegistryManager stage, int id, Block block, Block oldBlock, CallbackInfo ci) {



//        for (IBlockState state:block.getBlockState().getValidStates()){
//            ((PBlockState)state).setId(Block.BLOCK_STATE_IDS.get(state));
//        }
        //((PBlockState)block).setId(id);
        //FMLLog.log(Level.DEBUG, block+": <->"+id);
    }
}