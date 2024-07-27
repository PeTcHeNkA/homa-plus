package plus.fmsg.cmd;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.MathHelper;
import plus.tson.format.IVarGetter;
import plus.tson.format.IVarProvider;


public class EntityVarProvider implements IVarProvider<Entity> {
    @Override
    public IVarGetter<Entity> getFmGetter(String name) {
        switch (name) {
            case "hp":
                return entity -> {
                    if(entity instanceof EntityLiving){
                        return String.format("%.1f", ((EntityLiving)entity).getHealth());
                    } else return "<infinity>";
                };
            case "name":
                return Entity::getName;
            case "uuid":
                return msg -> msg.getUniqueID().toString();
            case "pos":
                return entity -> String.format("[%.2f,%.2f,%.2f]", entity.posX, entity.posY, entity.posZ);
            case "bpos":
                return entity -> "["+ MathHelper.floor(entity.posX) +","+ MathHelper.floor(entity.posY) +","+ MathHelper.floor(entity.posZ) +"]";
            case "args-pos":
                return entity -> MathHelper.floor(entity.posX) +" "+ MathHelper.floor(entity.posY) +" "+ MathHelper.floor(entity.posZ);
        }
//        if(name.startsWith("nbt:")) {
//            return new McVarProvider.NbtDeepGetter(name.substring(4));
//        }
        return null;
    }
}
