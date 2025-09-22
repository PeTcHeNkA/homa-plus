package plus.proto.app;

import plus.proto.clo.CloData;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class SkinMain {
    public static void main(String[] args) throws Exception{

        if(args.length < 2){
            System.out.println("Usage: -target CharacterName:skinNum -src CharacterName2:skinNum2 -file inFile.bytes -out outFile.bytes | -list -file inFile.bytes");
            return;
        }

        String prev = null;
        Map<String,String> args0 = new HashMap<>();
        for (String s:args){
            if(s.startsWith("-")){
                if(prev != null){
                    args0.put(prev, "true");
                }
                prev = s;
            } else {
                args0.put(prev, s);
                prev = null;
            }
        }
        if(prev != null){
            args0.put(prev, "true");
        }

        File file = new File(args0.get("-file"));
        CloData clo = new CloData();

        clo.readFrom(file);

        if(args0.containsKey("-list")){
            for (String name:clo.scanNames()){
                System.out.println(clo.search(name));
            }
            return;
        }
        File out = new File(args0.get("-out"));

        ModOpt target = new ModOpt(args0.get("-target"));
        ModOpt src    = new ModOpt(args0.get("-src"));

        CloData.CharacterSkinData t1 = clo.search(target.name);
        if(t1 == null){
            System.out.println("Character ["+target.name+"] not founded!");
            return;
        }

        CloData.LItem t2 = t1.getSkin(target.skin);

        if(t2 == null){
            System.out.println("Skin ["+target.skin+"] in ["+target.name+"] not founded!");
            return;
        }

        CloData.CharacterSkinData s1 = clo.search(src.name);

        if(s1 == null){
            System.out.println("Character ["+src.name+"] not founded!");
            return;
        }

        CloData.LItem s2 = s1.getSkin(src.skin);

        if(s2 == null){
            System.out.println("Skin ["+src.skin+"] in ["+src.name+"] not founded!");
            return;
        }

        //System.out.println("Search src    ["+src.name+":"+src.skin+"]");
        //CloData.LItem s2 = clo.search(src.name).getSkin(src.skin);

        t2.copySkinOf(s2);
        //t2.copyAllOf(s2);

        clo.write(out);
        System.out.println("Success");
    }

    private static class ModOpt{
        private String name;
        private int skin;

        ModOpt(String raw){
            if(raw.contains(":")){
                String[] r2 = raw.split(":");
                name = r2[0];
                skin = Integer.parseInt(r2[1]);
            } else {
                name = raw;
            }
        }
    }
}