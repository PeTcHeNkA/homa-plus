package plus.fserver.api;

import net.minecraft.entity.player.EntityPlayerMP;
import java.util.function.Function;


public interface IPlayerDataModel {
    Function<String, String> EMPTY_MODEL = name -> null;

    EntityPlayerMP getSelfPlayer();


    default Function<String, String> model(){
        return EMPTY_MODEL;
    };
}