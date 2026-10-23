package plus.craft;

import org.bukkit.inventory.ItemStack;
import java.util.List;


public class BetterCraft {

    private final CraftThing[] craftThings;
    private final CraftThing result;

    public BetterCraft(List<String> itemUidS, String result){

        this.result = defineCraftThing(result);
        int flag = 0;
        craftThings = new CraftThing[itemUidS.size()];
        for(String hashUid:itemUidS){

            craftThings[flag] = defineCraftThing(hashUid);
            flag+=1;
        }
    }

    public boolean isCraft(ItemStack[] items){//Ошибку править тут!!!!!!!
        int flag = 0;

        for(CraftThing thing:craftThings){

            if(thing == null) {
                if (items[flag]!=null)return false;
            } else {
                if (!thing.isSuccessful(items[flag])) return false;
            }
            flag+=1;
        }
        return true;
    }


    public ItemStack[] getNeedItems(){
        ItemStack[] items = new ItemStack[9];

        int flag = 0;
        for (CraftThing craftThing: craftThings){
            if(craftThing!=null){
                items[flag] = craftThing.view();
            }
            flag+=1;
        }
        return items;
    }


    public ItemStack getResult(){
        return result.toItem();
    }


    public ItemStack viewResult(){
        return result.view();
    }


    public static CraftThing defineCraftThing(String hashUid){
        if(hashUid.equals("air"))return null;
        String[] hash = hashUid.split(":");
        if(hash[0].equals("W")){
            return new CraftWeapon(hash[1]);
        } else if(hash[0].equals("A")){
            return new CraftArtifact(hash[1]);
        }else {
            return new CraftItem(hash[1]);
        }
    }
}