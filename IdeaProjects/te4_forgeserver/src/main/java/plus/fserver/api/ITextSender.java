package plus.fserver.api;

import net.minecraft.entity.player.EntityPlayerMP;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.List;


public interface ITextSender {
    void send(EntityPlayerMP player);


    void send(IPlayerDataModel model);


    void send(List<EntityPlayerMP> players);


    void send(IPlayerDataModel model, List<EntityPlayerMP> players);


    void sendToTeam(IPlayerDataModel model);


    static ITextSender of(TsonObj obj){
        if(obj.isList()){
            TsonList list = obj.getList();
            ITextSender[] senders = new ITextSender[list.size()];
            for (int i = 0; i < senders.length; i++){
                senders[i] = of(list.get(i));
            }
            if(senders.length == 1)return senders[0];
            return new MultiSender(senders);
        }
        if(obj.isMap()){
            TsonMap map = obj.getMap();
            String type0 = map.getStrSafe("type");
            if(type0 == null || type0.equals("chat")){
                return ChatPacketBuilder.of(map.get("msg"));
            } else {
                return TitlePacketBuilder.of(map);
            }
        } else {
            return ChatPacketBuilder.of(obj);
        }
    }
}
class MultiSender implements ITextSender{
    private final ITextSender[] parents;

    MultiSender(ITextSender[] parents) {
        this.parents = parents;
    }


    @Override
    public void send(EntityPlayerMP player) {
        for(ITextSender sender:parents)sender.send(player);
    }


    @Override
    public void send(IPlayerDataModel model) {
        for(ITextSender sender:parents)sender.send(model);
    }


    @Override
    public void send(List<EntityPlayerMP> players) {
        for(ITextSender sender:parents)sender.send(players);
    }


    @Override
    public void send(IPlayerDataModel model, List<EntityPlayerMP> players) {
        for(ITextSender sender:parents)sender.send(model, players);
    }


    @Override
    public void sendToTeam(IPlayerDataModel model) {
        for(ITextSender sender:parents)sender.sendToTeam(model);
    }
}