package plus.elch.chat;

import org.bukkit.entity.Player;
import plus.elch.chat.data.MessagePackData;
import plus.elch.utl.CfgUtils;
import plus.elch.utl.FastExitException;
import plus.tson.TsonMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static plus.tson.utl.uns.UnsafeUtils.*;


public class ElMessageBus {
    private static final long nextOffset = offset(ElMessageBus.class, "nextId");
    private long nextId;
    private final File dir;
    private final int maxFiles;
    private final long fileTime;

    private ArrayList<MessagePackData> cachedFiles = new ArrayList<>();
    private MessagePackData curFile;


    public ElMessageBus(TsonMap map) throws FastExitException, IOException {
        this.dir = new File(map.getOrDefaultStr("dir", "messages"));
        this.maxFiles = map.getOrDefaultInt("max-files", 64);
        this.fileTime = CfgUtils.timeOf(map.getOrDefaultStr("file-time", "12:00:00"));

        curFile = getByTime(System.currentTimeMillis());
    }


    private MessagePackData getByTime(long time) throws IOException, FastExitException {
        time /= fileTime;
        File file = new File(dir, time + ".mspack");
        if(!file.exists())
            file.createNewFile();
        return new MessagePackData(file);
    }


    public ElMessage create(Player player, String msg, UUID... targets) throws IOException, FastExitException {
        return new ElMessage(this, player, msg).setTargets(targets);
    }


    public long nextId() {
        long curId = nextId, next = curId + 1;
        if(compareAndSwap(this, nextOffset, curId, next))
            return next;
        return nextId();
    }
}