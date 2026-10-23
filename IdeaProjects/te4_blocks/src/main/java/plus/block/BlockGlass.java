package plus.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.Random;
import static plus.block.Block.*;


public class BlockGlass extends net.minecraft.block.BlockGlass{
    private final int drop;

    public BlockGlass(String material, String id) {
        this(material, id, id, false);
    }


    public BlockGlass(String material, String id, String name) {
        this(material, id, name, false);
    }


    public BlockGlass(String material, String id, String name, Boolean drop) {
        super(parseMaterial(material), false);
        setRegistryName(id);
        setTranslationKey(name);
        this.drop = drop?1:0;
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setCreativeTab(TE4BLOCKS_TAB);
    }


    public BlockGlass(TsonMap map){
        super(parseMaterial(getMaterialOfMap(map)), false);
        onBlockInitFromMap(map, this);
        if(map.containsKey("drop")){
            TsonObj obj = map.get("drop");
            if(obj.isBool()){
                drop = obj.isBool()?1:0;
            } else {
                drop = 0;
            }
        } else {
            drop = 0;
        }
        setCreativeTab(TE4BLOCKS_TAB);
    }


    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }


    public int quantityDropped(Random random) {
        return drop;
    }
}