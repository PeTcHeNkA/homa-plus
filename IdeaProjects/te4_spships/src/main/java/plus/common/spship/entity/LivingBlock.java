package plus.common.spship.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.client.mem.Memory;
//import static plus.client.camera.Camera.memoryCache;


public abstract class LivingBlock extends Entity {
    @SideOnly(Side.CLIENT)
    protected final Memory mem = buildDef();

    public LivingBlock(World worldIn) {
        super(worldIn);
    }


    @SideOnly(Side.CLIENT)
    public final Memory getOrDefMemory(){
        return mem;
    }


    @SideOnly(Side.CLIENT)
    protected Memory buildDef(){
        return new Memory();
    }


    public abstract int getModelId();
}