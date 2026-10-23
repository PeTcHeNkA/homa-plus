package plus.common.mashine.item.recept;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.lang3.tuple.Triple;
import plus.tson.utl.Tuple;

import java.util.ArrayList;


public interface ISimpleSmeling<T extends Block> {
    default T addSmeling(ItemStack result, float exp){
        T thz = (T) this;
        Helper.recepts.add(new Triple<Block, ItemStack, Float>() {
            @Override
            public Block getLeft() {
                return thz;
            }

            @Override
            public ItemStack getMiddle() {
                return result;
            }

            @Override
            public Float getRight() {
                return exp;
            }
        });
        return thz;
    }

    default T addSmeling(ItemStack result){
        return addSmeling(result, 1f);
    }

    static void regAll(){
        for(Triple<Block, ItemStack, Float> r:Helper.recepts){
            GameRegistry.addSmelting(r.getLeft(), r.getMiddle(), r.getRight());
        }
        Helper.recepts = null;
    }

    class Helper{
        private static ArrayList<Triple<Block, ItemStack, Float>> recepts = new ArrayList<>();
    }
}