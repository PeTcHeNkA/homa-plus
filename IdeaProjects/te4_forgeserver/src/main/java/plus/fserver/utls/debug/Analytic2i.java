package plus.fserver.utls.debug;

import io.netty.util.collection.IntObjectHashMap;
import plus.asyutl.shoulder.IBasicTask;
import plus.asyutl.shoulder.TaskShoulder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class Analytic2i implements IBasicTask {
    private IntObjectHashMap<Entry> wtited = new IntObjectHashMap<>();
    private IntObjectHashMap<Entry> data = new IntObjectHashMap<>();
    private static TaskShoulder shoulder;
    private final String file;
    private final String sep;
    private int mods = 0;

    public Analytic2i(String file){
        this(file, ",");
    }

    public Analytic2i(String file, String sep) {
        this.file = file;
        this.sep = sep;
        if(shoulder == null){
            shoulder = new TaskShoulder(1500);
        }
        shoulder.addTask(this);
    }


    public void add(int i){
        Entry entr = data.get(i);
        if(entr == null)data.put(i, entr = new Entry());
        entr.up();
        ++mods;
    }


    private void dump(){
        if(mods==0)return;
        IntObjectHashMap<Entry> items = data;
        data = new IntObjectHashMap<>();
        join(wtited, items);
        mods = 0;
        dumpSCV(wtited, file, sep);
    }


    private static void join(IntObjectHashMap<Entry> items, IntObjectHashMap<Entry> add){
        for (Map.Entry<Integer,Entry> addE:add.entrySet()){
            Entry entry = items.get(addE.getKey());
            if(entry == null)items.put(addE.getKey(), addE.getValue());
            else entry.usages += addE.getValue().usages;
        }
    }


    private static void dumpSCV(IntObjectHashMap<Entry> items, String file, String sep){
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file, false);
            byte[] sep0 = sep.getBytes(StandardCharsets.UTF_8);
            byte[] newLine = new byte[]{'\n'};

            for (Map.Entry<Integer,Entry> entry:items.entrySet()){
                stream.write(String.valueOf(entry.getKey()).getBytes(StandardCharsets.UTF_8));
                stream.write(sep0);
                stream.write(String.valueOf(entry.getValue().usages).getBytes(StandardCharsets.UTF_8));
                //stream.write(sep0);
                stream.write(newLine);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {}
        }
    }


    @Override
    public boolean needRun() {
        dump();
        return false;
    }


    @Override
    public void run() {}


    private static class Entry{
        private int usages = 0;
        public void up(){
            ++usages;
        }
    }
}