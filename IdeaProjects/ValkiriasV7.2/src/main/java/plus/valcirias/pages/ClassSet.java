package plus.valcirias.pages;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plus.util.ConfigHelper;
import plus.util.cfg.BasicSector;
import plus.util.cfg.Config;
import plus.util.FastRandom;
import plus.util.cfg.Sector;
import java.util.Collection;
import java.util.List;


public class ClassSet {

    private final String[] Classes = new String[4];
    private final ClassPage[] classPages = new ClassPage[4];
    private final Inventory inventory;
    private final boolean isPremiumSet;
    private final String name;

    public ClassSet(Config c, String path){
        isPremiumSet = c.getOrSetBoolean(path+".is_premium", false);
        String lpath = path+".classes";
        name = c.getOrSetString(path+".name", String.format("unndamed_%s", FastRandom.r.nextInt(1024)));
        ItemStack[] IcoClasses = new ItemStack[4];
        inventory = Bukkit.createInventory(null,45, "classpage:"+name);
        inventory.setItem(36, ConfigHelper.generateMenuItem(c, path+".bg_item", Material.DIAMOND));
        Collection<String> kays = c.getConfigurationSection(lpath).getKeys(false);
        int[] Indexes = {1, 2, 0, 3};

        if(kays.isEmpty())throw new NullPointerException();

        int index = 0;
        for(String key:kays){
            IcoClasses[Indexes[index]]=ConfigHelper.generateMenuItem(c,path+".classes."+key+".small_ico", Material.DIAMOND);
            index+=1;
        }
        index = 0;

        for(String kay:kays){
            String llpath = path+".classes."+kay;
            ClassPage page = new ClassPage(this,
                    //c.getOrSetIfExistString(llpath+".classname", "VALKIRIA"),
                    ConfigHelper.generateMenuItem(c,llpath+".big_ico", Material.DIAMOND),
                    ConfigHelper.generateMenuItem(c,llpath+".lore", Material.DIAMOND),
                    Indexes[index],
                    ConfigHelper.generateMenuItem(c, path+".one_class_bg_item", Material.DIAMOND), IcoClasses
            );
            Classes[Indexes[index]]=c.getOrSetString(llpath+".classname", "VALKIRIA");
            classPages[Indexes[index]]=page;


            index+=1;
        }

        int[] icoIndexes = {9, 18, 0, 37};
        index = 0;
        for(ItemStack ico:IcoClasses){
            inventory.setItem(icoIndexes[index], ico);
            index+=1;
        }
        index = 0;
        int[] classItemIndexes = {31, 33, 29, 35};
        for(ClassPage cp:classPages){
            if (cp != null) {
                inventory.setItem(classItemIndexes[index], cp.BigIco);
            }
            index+=1;
        }
    }
    protected String getName(){
        return name;
    }

    protected String getClassOf(int index){
        if(index>3 || index < 0)return null;
        else return Classes[index];
    }

    public ClassPage getPage(int index){
        if(index>3 || index < 0)return null;
        else return classPages[index];
    }

    public void Open(Player player){
        player.openInventory(inventory);
    }


    public boolean isPremiumSet(){
        return isPremiumSet;
    }
}