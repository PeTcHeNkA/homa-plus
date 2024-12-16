package plus.fserver.event;

import net.minecraftforge.fml.common.eventhandler.Event;
import plus.fserver.api.bind.ItemBind;


public class ItemBindLoadingEvent extends Event {
    public ItemBindLoadingEvent(){}

    public void register(int id, ItemBind.Func func){
        ItemBind.register(id, func);
    }


    public boolean isRegistred(int id){
        return ItemBind.isRegistred(id);
    }
}