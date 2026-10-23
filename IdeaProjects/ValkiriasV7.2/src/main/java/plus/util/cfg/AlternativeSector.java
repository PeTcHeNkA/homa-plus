package plus.util.cfg;

import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

public class AlternativeSector extends BasicSector{

    public AlternativeSector(File file) throws IOException, InvalidConfigurationException {
        this.name = file.getName();
        super.file = file;
        load(file);
    }


    @Override
    public SimpleSector reloadAsFile(){
        try {
            return new AlternativeSector(file);
        } catch (IOException | InvalidConfigurationException err){
            err.printStackTrace();
            return this;
        }
    }
}
