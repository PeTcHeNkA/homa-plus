package plus.common.spship.item.craft;

import plus.common.mashine.item.recept.IItemValidator;


public class CraftCRavenProcessor extends CraftCHunterProcessor{
    public CraftCRavenProcessor(IItemValidator... validators) {
        super(validators);
    }


    @Override
    protected int getPrefC(int v){
        switch (v){
            case 3:
            case 5:
                return 3;
            case 4:return 2;
            case 6:return 1;
            default:return 0;
        }
    }
}