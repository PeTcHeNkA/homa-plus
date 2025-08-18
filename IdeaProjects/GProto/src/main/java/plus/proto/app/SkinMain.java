package plus.proto.app;

import plus.proto.clo.CloData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class SkinMain {
    public static void main(String[] args) throws Exception{
        if(args.length < 4){
            System.out.println("-target CharacterName:skinNum -src CharacterName2:skinNum2 -file inFile.bytes -out outFile.bytes | -list -file inFile.bytes");
        }

        String prev = null;
        Map<String,String> args0 = new HashMap<>();
        for (String s:args){
            if(s.startsWith("-")){
                prev = s;
            } else {
                if(prev == null){
                    prev = s;
                    s = "true";
                }
                args0.put(prev, s);
                prev = null;
            }
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

        clo.search(target.name).getSkin(target.skin).copySkinOf(
                clo.search(src.name).getSkin(0)
        );

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