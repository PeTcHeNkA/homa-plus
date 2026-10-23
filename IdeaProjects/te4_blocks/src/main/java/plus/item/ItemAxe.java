package plus.item;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import java.util.Set;


class ItemAxe extends ItemTool {
    private static final Set<Block> EFFECTIVE_ON;

    protected ItemAxe(ToolMaterial material) {
        super(material, EFFECTIVE_ON);
        this.attackDamage = (float) (material.getAttackDamage()*1.5);
        this.attackSpeed = -3f;
    }


    public float getDestroySpeed(ItemStack p_getDestroySpeed_1_, IBlockState p_getDestroySpeed_2_) {
        Material material = p_getDestroySpeed_2_.getMaterial();
        return material != Material.WOOD && material != Material.PLANTS && material != Material.VINE ? super.getDestroySpeed(p_getDestroySpeed_1_, p_getDestroySpeed_2_) : this.efficiency;
    }


    static {
        EFFECTIVE_ON = Sets.newHashSet(
                Blocks.PLANKS,
                Blocks.BOOKSHELF,
                Blocks.LOG,
                Blocks.LOG2,
                Blocks.CHEST,
                Blocks.PUMPKIN,
                Blocks.LIT_PUMPKIN,
                Blocks.MELON_BLOCK,
                Blocks.LADDER,
                Blocks.WOODEN_BUTTON,
                Blocks.WOODEN_PRESSURE_PLATE
        );
    }
}
