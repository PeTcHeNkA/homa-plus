package plus.client.mem;

import javafx.util.Builder;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.client.entity.ISpMemory;

import java.util.HashMap;
import java.util.UUID;

import static plus.utls.MathUtils.calcXZrot;
import static plus.utls.MathUtils.calcYrot;


@SideOnly(Side.CLIENT)
public class MemoryFolder extends HashMap<UUID, ISpMemory> {

    public ISpMemory getOrDef(Entity entity) {
        throw new UnsupportedOperationException();
//        UUID uid = entity.getUniqueID();
//        ISpMemory result = get(uid);
//        if (result == null) {
//            result = new Memory();
//
//            result.prevXZrot = calcXZrot(entity, 1, result);
//            result.prevYrot = calcYrot(entity, 1, result);
//            put(uid, result);
//        }
//        return result;
    }


    public Memory getOrDef(Entity entity, Builder<Memory> func){
        throw new UnsupportedOperationException();
//        UUID uid = entity.getUniqueID();
//        //Memory result = get(uid);
//        if (result == null) {
//            result = func.build();
//            put(uid, result);
//        }
//        return result;
    }
}