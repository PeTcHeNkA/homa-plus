package net.minecraftforge.event.world;

import net.minecraftforge.fml.common.eventhandler.Event;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;


public class ChunkWatchEvent extends Event{
    private EntityPlayerMP player;
    private Chunk chunkInstance;
    private IEventListener[] listeners;

    public ChunkWatchEvent(){super();}
    public ChunkWatchEvent(EventBus bus){forBus(bus);}


    public ChunkWatchEvent forBus(EventBus bus){
        listeners = getListenerList().getListeners(bus.id());
        return this;
    }


    public void post(Chunk chunk, EntityPlayerMP player){
        this.chunkInstance = chunk;
        this.player = player;
        for (IEventListener listener:listeners)listener.invoke(this);
    }


    @Deprecated //TODO: Remove in 1.13
    public ChunkWatchEvent(ChunkPos chunk, EntityPlayerMP player) {
        this.player = player;
        this.chunkInstance = player.world.getChunk(chunk.x, chunk.z);
    }

    public ChunkWatchEvent(Chunk chunk, EntityPlayerMP player) {
        this.player = player;
        this.chunkInstance = chunk;
    }


    @Deprecated //TODO: Remove in 1.13
    public ChunkPos getChunk() {
        return chunkInstance.getPos();
    }


    public EntityPlayerMP getPlayer() {
        return player;
    }


    @Nullable
    public Chunk getChunkInstance() {
        return chunkInstance;
    }


    public static class Watch extends ChunkWatchEvent {
        @Deprecated //TODO: Remove in 1.13
        public Watch(ChunkPos chunk, EntityPlayerMP player) { super(chunk, player); }

        public Watch(Chunk chunk, EntityPlayerMP player) { super(chunk, player); }

        public Watch(){}


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    public static class UnWatch extends ChunkWatchEvent {
        @Deprecated //TODO: Remove in 1.13
        public UnWatch(ChunkPos chunkLocation, EntityPlayerMP player) {super(chunkLocation, player);}

        public UnWatch(Chunk chunk, EntityPlayerMP player) {super(chunk, player);}

        public UnWatch(){}


        //Elca-Forge event system
        //Start generated code
        private static ListenerList LISTENER_LIST;
        protected void setup() {
            super.setup();
            if (LISTENER_LIST == null)
                LISTENER_LIST = new ListenerList(super.getListenerList());
        }
        public ListenerList getListenerList() {
            return LISTENER_LIST;
        }
        //End generated code
    }


    //Elca-Forge event system
    //Start generated code
    private static ListenerList LISTENER_LIST;
    protected void setup() {
        super.setup();
        if (LISTENER_LIST == null)
            LISTENER_LIST = new ListenerList(super.getListenerList());
    }
    public ListenerList getListenerList() {
        return LISTENER_LIST;
    }
    //End generated code
}