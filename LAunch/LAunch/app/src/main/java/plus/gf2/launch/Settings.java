package plus.gf2.launch;

import java.io.File;

import plus.tson.TJsonParser;
import plus.tson.TJsonWriter;
import plus.tson.TsonFile;
import plus.tson.TsonMap;


public class Settings {
    public static final String DEF_REPO_1 = "https://gitlab.com/gf2ruteam/grils-frontline-team-translate/-/raw/master/translations_release.json?ref_type=heads&inline=false";
    public String repo1;
    public String repo2;
    public String repo3;
    public boolean enableTranslation;


    public void read(MainActivity act){
        try {

            byte[] data = TsonFile.readFile(new File(act.getFilesDir(), "settings.json"));
            if(data != null){
                TJsonParser parser = new TJsonParser(data, true);
                TsonMap map = parser.getMap();

                repo1 = map.getStrSafe("repo1");
                repo2 = map.getStrSafe("repo2");
                repo3 = map.getStrSafe("repo3");

                if(map.containsKey("translation")) {
                    enableTranslation = map.getBool("translation");
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void write(MainActivity act){
        File file = new File(act.getFilesDir(), "settings.json");
        TsonMap map = new TsonMap();

        if(repo1 != null)
            map.put("repo1", repo1);
        if(repo2 != null)
            map.put("repo2", repo2);
        if(repo3 != null)
            map.put("repo3", repo3);
        if(enableTranslation)
            map.put("translation", true);

        TJsonWriter writer = new TJsonWriter(map);
        TsonFile.write(file, writer.toString());
    }


    public String getRepo1(){
        if(repo1 == null){
            return DEF_REPO_1;
        }
        if(repo1.isBlank())return DEF_REPO_1;
        return repo1;
    }


    public String getRepo2(){
        if(repo2.isBlank())return null;
        return repo2;
    }


    public String getRepo3(){
        if(repo3.isBlank())return null;
        return repo3;
    }


    public boolean isEnableTranslation(){
        return enableTranslation;
    }
}
