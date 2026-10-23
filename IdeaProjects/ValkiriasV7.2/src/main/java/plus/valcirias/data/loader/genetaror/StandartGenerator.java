package plus.valcirias.data.loader.genetaror;

import plus.valcirias.classes.ValkiriaKeQuing;
import plus.valcirias.classes.ValkiriaUeMia;
import plus.valcirias.Valkiria;
import plus.valcirias.classes.ValkiriaShooter;
import plus.valcirias.classes.ValkiriaSword;


public class StandartGenerator implements ValkiriaGenerator{

    public Valkiria ofString(String ClassName){
        if(ClassName==null){
            return new ValkiriaSword();
        }
        switch (ClassName){
            case "ValkiriaShooter":
                return new ValkiriaShooter();
            case "ValkiriaSword":
                return new ValkiriaSword();
            case "ValkiriaKeQuing":
                return new ValkiriaKeQuing();
            case "ValkiriaUeMia":
                return new ValkiriaUeMia();
            case "Valkiria":
                System.out.println("User logged without Class!");
                return new ValkiriaSword();
            default:
                throw new IllegalStateException("Unexpected value: " + ClassName);
        }
    }
}
