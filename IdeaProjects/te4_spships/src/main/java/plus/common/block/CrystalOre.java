package plus.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.common.mashine.item.BasicItem;
import java.util.Random;


public class CrystalOre extends SimpleOre{
    public static final SimpleOre DIAMOND_ORE_MET = new CrystalOre("diamond_ore_met", 3, Items.DIAMOND);
    public static final SimpleOre EMERALD_ORE_MET = new CrystalOre("emerald_ore_met", 3, Items.EMERALD);



    public static final SimpleOre AETHER_ORE = new CrystalOre("aether_ore", 3, BasicItem.AETHER_CRYSTAL){
        @Override
        public int quantityDropped(Random random) {
            return 2 + random.nextInt(3);
        }
        @Override
        public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune){
            Random rand = world instanceof World ? ((World)world).rand : RANDOM;
            return MathHelper.getInt(rand, 3, 7);
        }
    }.addSmeling(new ItemStack(BasicItem.AETHER_CRYSTAL, 3));


    private final Item drop;

    public CrystalOre(String regName, int lvl, Item drop) {
        super(regName, lvl);
        this.drop = drop;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return drop;
    }
}
