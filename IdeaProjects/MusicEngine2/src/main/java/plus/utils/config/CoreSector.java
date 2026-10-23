package plus.utils.config;

import java.io.File;

public class CoreSector extends BasicSector{

    public CoreSector(String name){
        this.name = name;
        try {
            file = new File(name);
            if (!file.exists()) {
                //load(new InputStreamReader(v.getResource(name), StandardCharsets.UTF_8));
                save(file);
            } else {
                load(file);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
