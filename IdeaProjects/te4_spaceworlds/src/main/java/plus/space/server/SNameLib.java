package plus.space.server;

import plus.fserver.utls.math.FastRandom;
import plus.tson.TsonFile;
import java.util.ArrayList;


public class SNameLib {
    private final ArrayList<String> usages = new ArrayList<>();
    private final ArrayList<String> notUsed = new ArrayList<>();
    private final String[] def;
    private int newFactor = 32;

    public SNameLib(String fileName){
        String result = TsonFile.read(fileName).trim();
        for(String name:result.split("\n")){
            name = name.replace("\r","").
                    replace("\t","").trim();
            if(name.isEmpty())continue;
            notUsed.add(name);
        }
        notUsed.trimToSize();
        def = notUsed.toArray(new String[0]);
    }


    public void use(String name){
        synchronized (this) {
            if(!usages.contains(name)) usages.add(name);
            notUsed.remove(name);
        }
    }


    public String nextAndUse(){
        String name = nextName();
        synchronized (this) {
            if(!usages.contains(name)) usages.add(name);
            notUsed.remove(name);
        }
        return name;
    }


    public int countUsages(){
        synchronized (this){
            return usages.size();
        }
    }


    public String nextName(){
        synchronized (this) {
            int index = notUsed.size();
            if (index == 0) return createName();
            return notUsed.remove(FastRandom.INST.nextInt(index));
        }
    }


    private String createName(){
        char[] chars = new char[]{'A','B','C','D','E','F','G','H','Z','X','L','M','N','S'};
        int errors = 0;
        String name = createName(chars);
        while (usages.contains(name)){
            name = createName(chars);
            if(errors++==3){
                newFactor+=10;
                errors = 0;
            }
        }
        return name;
    }


    private String createName(char[] chars){
        String fname = def.length==0?""+chars[FastRandom.INST.nextInt(chars.length)]:
                def[FastRandom.INST.nextInt(def.length)];
        return fname +"-"+FastRandom.INST.nextInt(newFactor);
    }
}