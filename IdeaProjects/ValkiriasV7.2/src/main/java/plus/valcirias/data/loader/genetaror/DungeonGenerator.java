package plus.valcirias.data.loader.genetaror;

import plus.valcirias.DungeonValkiria;
import plus.valcirias.Valkiria;

public class DungeonGenerator implements ValkiriaGenerator{

    private final StandartGenerator g;

    public DungeonGenerator(){
        g = new StandartGenerator();
    }

    public Valkiria ofString(String ClassName){
        return new DungeonValkiria(g.ofString(ClassName));
    }
}
