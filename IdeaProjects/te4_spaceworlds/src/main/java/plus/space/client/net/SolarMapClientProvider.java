package plus.space.client.net;

import io.netty.util.collection.IntObjectHashMap;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.utls.math.FastRandom;
import plus.space.client.SolarMap;
import plus.space.common.GalName;
import plus.space.common.net.CallBackMsg;
import plus.tson.TsonList;
import plus.tson.TsonObj;
import plus.tson.security.ClassManager;
import java.util.ArrayList;
import java.util.function.Consumer;


@SideOnly(Side.CLIENT)
public class SolarMapClientProvider implements IMessageHandler<CallBackMsg,
        CallBackMsg>, ClassManager {
    private static final IntObjectHashMap<Consumer<TsonList>> callBack1 = new IntObjectHashMap<>();
    private static final IntObjectHashMap<Consumer<Integer>> callBack2 = new IntObjectHashMap<>();
    private static final ArrayList<Consumer<SolarMap.MapObj>> callBack3 = new ArrayList<>();
    private static SimpleNetworkWrapper wrapper;
    private static GalName curWorld = null;
    private static boolean newWorld = false;


    public static void init(SimpleNetworkWrapper network, int id){
        wrapper = network;
        network.registerMessage(SolarMapClientProvider.class, CallBackMsg.class, id, Side.CLIENT);
    }


    public static void clear() {
        synchronized (callBack1) {
            callBack1.clear();
        }
        synchronized (callBack2) {
            callBack2.clear();
        }
        synchronized (callBack3) {
            callBack3.clear();
        }
    }


    public static boolean isNewWorld(){
        return newWorld;
    }


    public static GalName getCurrentNow(){
        return curWorld;
    }


    public static void getCurrent(Consumer<SolarMap.MapObj> task){
        if(curWorld==null){
            synchronized (callBack3){
                callBack3.add(task);
            }
        } else {
            SolarMap.INST.getSystems(stars -> {
                SolarMap.INST.getPlanets(curWorld, result -> task.accept(result.A));
            });
        }
    }


    public static class Provider implements INetMapProvider{
        @Override
        public void getPlanetFromServer(GalName name, Consumer<TsonList> task) {
            if (name == null) {
                getSystemsFromServer(task);
            } else {
                int id;
                synchronized (callBack1) {
                    do {
                        id = FastRandom.INST.nextInt();
                    } while (callBack1.containsKey(id));
                    callBack1.put(id, task);
                }
                wrapper.sendToServer(new CallBackMsg(id, 1, name.toString()));
            }
        }


        @Override
        public void getSystemsFromServer(Consumer<TsonList> task) {
            int id;
            synchronized (callBack1) {
                do {
                    id = FastRandom.INST.nextInt(Integer.MAX_VALUE);
                } while (callBack1.containsKey(id));
                callBack1.put(id, task);
            }
            wrapper.sendToServer(new CallBackMsg(id, 0, ""));
        }


        @Override
        public void getActualSizeFromServer(Consumer<Integer> task) {
            sendMsg2(2, "", task);
        }


        @Override
        public void sendFlyTo(GalName name, Consumer<Integer> task) {
            sendMsg2(4, name.toString(), task);
        }


        private void sendMsg2(int mtd, String data, Consumer<Integer> task){
            int id;
            synchronized (callBack2) {
                do {
                    id = FastRandom.INST.nextInt(Integer.MAX_VALUE);
                } while (callBack2.containsKey(id));
                callBack2.put(id, task);
            }
            wrapper.sendToServer(new CallBackMsg(id, mtd, data));
        }
    }


    @Override
    public CallBackMsg onMessage(CallBackMsg message, MessageContext ctx) {
        try {
            switch (message.getMtd()) {
                case 0:
                case 1:
                    String data = message.getData();
                    TsonList map = data==null?new TsonList():new TsonList(this, data);
                    synchronized (callBack1) {
                        callBack1.remove(message.getId()).accept(map);
                    }
                    break;
                case 4:
                case 2:
                    synchronized (callBack2) {
                        callBack2.remove(message.getId()).accept(Integer.parseInt(message.getData()));
                    }
                    break;
                case 3:
                    curWorld = GalName.ofStr(message.getData());
                    newWorld = true;
                    Consumer<SolarMap.MapObj>[] csms;
                    synchronized (callBack3){
                        csms = callBack3.toArray(new Consumer[callBack3.size()]);
                        callBack3.clear();
                    }
                    SolarMap.INST.getPlanets(curWorld, result -> {
                        for (Consumer<SolarMap.MapObj> cms:csms){
                            cms.accept(result.A);
                        }
                        newWorld = false;
                    });
                    break;
            }
        } catch (NullPointerException ignored){
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public Class<?> forName(String clazz) {
        return TsonObj.class;
    }
}