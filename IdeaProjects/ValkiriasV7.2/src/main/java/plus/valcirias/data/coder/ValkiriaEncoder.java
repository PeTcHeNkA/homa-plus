package plus.valcirias.data.coder;

import org.bukkit.entity.Player;
import plus.valcirias.data.loader.genetaror.ValkiriaGenerator;
import plus.valcirias.Valkiria;
import java.util.List;


public interface ValkiriaEncoder {

    Valkiria encode(Player player, List<String> data);
    List<String> code(Valkiria v);
    ValkiriaGenerator getGenerator();
}
