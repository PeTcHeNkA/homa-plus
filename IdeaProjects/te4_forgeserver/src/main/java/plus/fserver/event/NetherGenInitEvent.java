package plus.fserver.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;


public class NetherGenInitEvent extends WorldEvent {
    public int liquidLvl = 63;
    public double perlinNoiseY    = 2053.236D;
    public double perlinNoiseXZ   = 684.412D;
    public double depthFactor     = 3;
    public double fillChance      = 0.25;
    public double noiseScaleZX    = 100;
    public double noiseScaleY     = 0;
    public double fillFactor      = 0;
    public double minFillFactor   = Double.MIN_VALUE;
    public int maxHeight          = 127;
    public int minHeight          = 0;
    public boolean borderTop = true;
    public boolean borderDown = true;
    public boolean structures     = true;
    public int solidNoiseOctaves  = 4;

    public IBlockState boundState  = Blocks.BEDROCK.getDefaultState();
    public IBlockState fillState   = Blocks.NETHERRACK.getDefaultState();
    public IBlockState airState    = Blocks.AIR.getDefaultState();
    public IBlockState gravelState = Blocks.GRAVEL.getDefaultState();
    public IBlockState sandState   = Blocks.SOUL_SAND.getDefaultState();
    public IBlockState liquidState = Blocks.LAVA.getDefaultState();

    public NetherGenInitEvent(World world, boolean structures) {
        super(world);
        this.structures = structures;
    }
}