package plus.quest.script.quest.selector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import plus.util.cfg.BasicSector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class Selector<T extends Entity> {

    public Selector<T> build(Class<T> clazz, String rawData){
        String[] data = rawData.split(":");
        if(data[0].equalsIgnoreCase("range")){
            Location loc = BasicSector.encodeLoc(data[1]);
            double range = Double.parseDouble(data[2]);
            return new Selector<>() {
                @Override
                public Collection<T> select() {
                    return loc.getNearbyEntitiesByType(clazz, range);
                }
            };
        } else if(data[0].equalsIgnoreCase("region")){
            World world = Bukkit.getWorld(data[1]);
            String[] boxData = data[2].split(";");

            BoundingBox boundingBox = new BoundingBox(
                    Double.parseDouble(boxData[0]),
                    Double.parseDouble(boxData[1]),
                    Double.parseDouble(boxData[2]),
                    Double.parseDouble(boxData[3]),
                    Double.parseDouble(boxData[4]),
                    Double.parseDouble(boxData[5])
            );
            return new Selector<>() {
                @Override
                public Collection<T> select() {
                    Collection<Entity> entities = world.getNearbyEntities(boundingBox);
                    Collection<T> result = new ArrayList<>();
                    for (Entity e : entities) {
                        if(e.getClass().isAssignableFrom(clazz)){
                            result.add((T) e);
                        }
                    }
                    return result;
                }
            };
        }
        return this;
    }


    public Collection<T> select() {
        return Collections.emptyList();
    }
}