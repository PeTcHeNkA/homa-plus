package plus.item;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plus.Main;
import plus.item.stat.StatGenerator;
import plus.util.ConfigHelper;
import plus.versions.AbstractItemGenerator;
import plus.versions.gen1164;
import plus.versions.gen1171;
import java.util.*;


public class IItemGenerator{

    private static final String WeaponsPath = "weapons.";
    private static final String ArtifactPath = "artifacts.";
    private static final String CraftItemPath = "items.";
    private static final String FoodPath = "food.";
    private static final String EnergeticPath = "energetics.";

    private static final String SystemPAth = "system.";
    private static final String BASE_INFO = "BI";
    private static final String BOOST_STATS = "BY";
    private static final String NON_INIT = "NI";

    private final AbstractItemGenerator g;
    public final CashedConfig c;


    private final int LEGENDARY_LLIMIT;
    private final int EPYC_LLIMIT;
    private final int RARE_LLIMIT;


    protected final HashMap<String, ItemStack> hashStandartItems = new HashMap<>();


    public IItemGenerator(){
        StatGenerator.define();
        if(Main.instance.getServer().getBukkitVersion().contains("16")){
            g = new gen1164();
        } else {
            g = new gen1171();
        }
        this.c = new CashedConfig(Main.instance, "rpg_items.yml"); //p.getOrCreateYandeReConfig("rpg_items");

        RARE_LLIMIT = c.getOrSetInt("rare.rare_llimit", 4);
        EPYC_LLIMIT = c.getOrSetInt("rare.epyc_llimit", 9);
        LEGENDARY_LLIMIT = c.getOrSetInt("rare.legend_llimit", 14);

        reloadCashe();
    }


    public List<String> getCashedItemsId(){
        return new ArrayList<>(hashStandartItems.keySet());
    }


    public void reloadCashe(){
        c.reload();
        try {
            for(String s:c.getConfigurationSection("items").getKeys(false)){
                hashStandartItems.put(s, buildStandartItem(s));
            }
            for(String s:c.getConfigurationSection("food").getKeys(false)){
                BaseInfo info = new BaseInfo(s, -1, 4, -1);
                hashStandartItems.put(s, buildFood(info));
            }
            for(String s:c.getConfigurationSection("energetics").getKeys(false)){
                BaseInfo info = new BaseInfo(s, -1, 5, -1);
                hashStandartItems.put(s, buildEnergetic(info));
            }
            c.ClearHash();
            System.out.printf("| Cashed items: %s%n", hashStandartItems.size());
        } catch (NullPointerException err){
            err.printStackTrace();
        }
    }


    public ItemStack writeTag(ItemStack item, String tag, String value) {
        return g.writeTag(item, tag, value);
    }


    public String getTag(ItemStack item, String tag) {
        return g.getTag(item, tag);
    }


    public boolean hasTag(ItemStack item, String tag) {
        return g.hasTag(item, tag);
    }


    public BoostStats getBoostStats(ItemStack item){
        String st = g.getTag(item, BOOST_STATS);
        if(st==null)return null;
        else return new BoostStats(st);
    }


    public ItemStack setBoostStats(ItemStack item, BoostStats stats){
        //System.out.println(String.format("| %s", stats.toString()));
        return g.writeTag(item,BOOST_STATS, stats.toString());
    }


    public BaseInfo getBaseInfo(ItemStack item){
        String st = g.getTag(item, BASE_INFO);
        if(st==null)return null;
        else return new BaseInfo(st);
    }


    public ItemStack setBaseInfo(ItemStack item, BaseInfo info){
        return g.writeTag(item,BASE_INFO, info.toString());
    }


    public ItemStack getNIItem(BaseInfo info, BoostStats stats){//class = saturation
        return stats==null?getNIItem(info, info.toString()+'^'):
                getNIItem(info, info.toString()+'^'+stats.toString());
    }
//    public ItemStack getNIItem(BaseInfo info, String codedStats){
//        return writeTag(buildStandartItem(SystemPAth+"non_init",
//                new BaseInfo("non_init", info.POWER, 6, -1), new ItemStack(Material.DIAMOND)),
//                NON_INIT, codedStats
//        );
//    }

    public ItemStack getNIBox(BaseInfo info, String boxId){
        return writeTag(buildStandartItem(SystemPAth+"loot_box",
                new BaseInfo("loot_box", info.POWER, 7, -1), new ItemStack(Material.DIAMOND)),
                NON_INIT, boxId
        );
    }


    public String getNITagValue(ItemStack item){
        return getTag(item, NON_INIT);
    }


    public ItemStack InitItem(ItemStack item){
        String v = getTag(item, NON_INIT);
        if(v==null)return null;
        String[] args = v.split("\\^");
        BaseInfo info = new BaseInfo(args[0]);
        if(info.TYPE != 1 && info.TYPE != 2)return buildStandartItem(info);
        if(args.length<2 || args[1].isEmpty()) return getBoostable(info, null);
        else return getBoostable(info, new BoostStats(args[1]), true);
    }


    public ItemStack getNIItem(BaseInfo info, String codedStats){
        String path = SystemPAth+"non_init";

        List<String> lore = new ArrayList<>();
        if(info.POWER>-1) {
            lore.add(getRareLabel(info.POWER));
        } else lore.add(getRareLabel(c.getCashedInt(path+"_class_"+info.CLASS+".rare", 1)));


        ItemStack item = writeTag(
                setBaseInfo(new ItemStack(Material.DIAMOND), new BaseInfo("non_init", info.POWER, 6, -1)),
                NON_INIT, codedStats
        );

        lore.add(" ");
        lore.add(" ");
        lore.add(" ");

        String ico = c.getCashedString(path+"class_"+info.CLASS+".ico", "none");
        lore.add(String.format(
                c.ICO_STRING_FORMAT_3,
                ico.equals("none")?c.getCashedString(path+".ico", "none"):ico,
                c.getCashedString(path+"class_"+info.CLASS+".view", "???"),
                info.POWER
                )
        );

        lore.add(c.getCashedString(path+".lore1", " "));
        lore.add(c.getCashedString(path+".lore2", " "));
        ItemMeta m = item.getItemMeta();

        m.setCustomModelData(
                c.getCashedInt(path+"class_"+info.CLASS+".cmd",
                        c.getCashedInt(path+".cmd", 0)
                )
        );

        m.setDisplayName(" ");
        m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }


    public ItemStack buildStandartItem(String info){
        ItemStack r = hashStandartItems.get(info);
        if(r==null)return buildStandartItem(new BaseInfo(info));
        else return r.clone();
    }


    public ItemStack buildStandartItem(BaseInfo info){
        if(info.TYPE==3) return buildStandartItem(CraftItemPath+info.ID, info, new ItemStack(Material.DIAMOND));
        else if(info.TYPE==4) return buildFood(info);
        else if(info.TYPE==5) return buildEnergetic(info);
        return buildStandartItem(CraftItemPath+info.ID, info, new ItemStack(Material.DIAMOND));
    }


    public ItemStack buildEnergetic(BaseInfo info){
        return buildFood(info, EnergeticPath+info.ID);
    }
    public ItemStack buildFood(BaseInfo info){
        return buildFood(info, FoodPath+info.ID);
    }
    public ItemStack buildFood(BaseInfo info, String path){

        List<String> lore = new ArrayList<>();
        if(info.POWER>-1) {
            lore.add(getRareLabel(info.POWER));
        } else lore.add(getRareLabel(c.getCashedInt(path+".rare", 1)));

        if(info.CLASS<0) {
            info = new BaseInfo(
                    info.ID,
                    info.POWER,
                    info.TYPE,
                    c.getCashedInt(path+".saturation", 3)
            );
        }

        ItemStack item = new ItemStack(Material.APPLE);
        item = setBaseInfo(item, info);
        lore.add(String.format(ConfigHelper.advantureItemLoreFormat,  c.getCashedString(path+".view", info.ID)));
        lore.add(" ");

        if(info.TYPE==4) {
            lore.add(String.format(c.getCashedString("format.saturation_format", "   saturation: &6%s"), info.CLASS));
        } else {
            lore.add(String.format(c.getCashedString("format.regeneration_format", "   regeneration: &6%s"), info.CLASS));
        }

        lore.add(
                String.format(c.ICO_STRING_FORMAT_1, c.getCashedString(path+".ico", "none"))
        );

        lore.add(c.getCashedString(path+".lore1", " "));
        lore.add(c.getCashedString(path+".lore2", " "));
        ItemMeta m = item.getItemMeta();
        m.setCustomModelData(c.getCashedInt(path+".cmd", 0));
        m.setDisplayName(" ");
        m.setLore(lore);
        item.setItemMeta(m);

        return item;
    }


    public ItemStack buildStandartItem(String path, BaseInfo info, ItemStack item /*parent*/){
        item = setBaseInfo(item, info);

        List<String> lore = new ArrayList<>();
        if(info.POWER>-1) {
            lore.add(getRareLabel(info.POWER));
        } else lore.add(getRareLabel(c.getCashedInt(path+".rare", 1)));

        List<String> rawlore = c.getCashedStringList(path+".lore", Collections.singletonList("lore1, lore2"));
        try {
            lore.add(String.format(ConfigHelper.advantureItemLoreFormat, rawlore.get(0)));
            lore.add(String.format(ConfigHelper.advantureItemLoreFormat, rawlore.get(1)));
            lore.add(String.format(ConfigHelper.advantureItemLoreFormat, rawlore.get(2)));
        } catch (IndexOutOfBoundsException ignored){
            while (lore.size()<4){
                lore.add(" ");
            }
        }
        if(rawlore.size()>3) {
            lore.add(String.format(
                            c.ICO_STRING_FORMAT_2,//ConfigHelper.AdvantureItemLoreFormat4,
                            c.getCashedString(path+".ico", "none"),
                            rawlore.get(3)
                    ));
        } else {
            lore.add(
                    String.format(
                            c.ICO_STRING_FORMAT_1,
                            c.getCashedString(path+".ico", "none")
                    )
            );
        }
        try {
            lore.add(String.format(ConfigHelper.advantureItemLoreFormat56, rawlore.get(4)));
            lore.add(String.format(ConfigHelper.advantureItemLoreFormat56, rawlore.get(5)));
        } catch (IndexOutOfBoundsException ignored){
            while (lore.size()<7){
                lore.add(" ");
            }
        }

        ItemMeta m = item.getItemMeta();
        m.setCustomModelData(c.getCashedInt(path+".cmd", 0));
        m.setDisplayName(" ");
        m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }


    public ItemStack getBoostable(BaseInfo info, BoostStats stats, boolean isFrame){
        if(info.TYPE==1)return buildWeapon(info, stats, isFrame);
        else if(info.TYPE==2)return buildArtifact(info, stats, isFrame);
        else return null;
    }


    public ItemStack getBoostable(BaseInfo info, BoostStats stats){
        if(info.TYPE==1)return buildWeapon(info, stats, false);
        else if(info.TYPE==2)return buildArtifact(info, stats, false);
        else return null;
    }


    private String getRareLabel(int Power){
        if(Power<EPYC_LLIMIT) {
            if(Power>RARE_LLIMIT) {
                return ConfigHelper.A_QUALITY_NORMAL;
            } else {
                return ConfigHelper.A_QUALITY_RARE;
            }
        } else {
            if(Power>LEGENDARY_LLIMIT){
                return ConfigHelper.A_QUALITY_LEGENDARY;
            } else {
                return ConfigHelper.A_QUALITY_EPIC;
            }
        }
    }


    private final Random r = new Random();


    public int[] genRandomLvlStats(int power, BoostStats baseStats){
        int[] stats;

        int CountLines = 0;
        int[] boostedIndexes = new int[]{-1, -1, -1};

        if(baseStats==null){
            stats = new int[]{0 , 0, 0, 0, 0, 0, 0};
        } else {
            stats = new int[]{
                    (int) baseStats.BOOST_ATTACK,
                    (int) baseStats.BOOST_CRIT,
                    (int) baseStats.BOOST_SHIELD,
                    (int) baseStats.BOOST_CRIT_RATE,
                    (int) baseStats.ADDITIONAL_ATTACK,
                    (int) baseStats.ADDITIONAL_SHIELD,
                    (int) baseStats.ADDITIONAL_ENERGY
            };
            for(int i=0;i<7;i++){
                if(CountLines>2)stats[i]=0;
                if(stats[i]>0.01){
                    boostedIndexes[CountLines]=i;
                    CountLines+=1;
                }
            }
        }
        if(power<1)return stats;
        int a = r.nextInt(power);
        int b = r.nextInt(power-a);
        int c = power-(a+b);

        for(int i:new int[]{a, b, c}){
            int Stat;

            if(CountLines<3) {
                Stat = r.nextInt(7);
                if(stats[Stat] < 0.01){
                    boostedIndexes[CountLines]=Stat;
                    CountLines += 1;
                }
            } else {
                Stat = boostedIndexes[r.nextInt(3)];
            }
            stats[Stat] += i;
        }
        return stats;
    }


    private BoostStats getRandomArtifactBoostStats(int power, BoostStats baseStats){
        int[] stats = genRandomLvlStats(power, baseStats);
        return new BoostStats(
                StatGenerator.DAMAGE_PERCENT.genA(stats[0]),
                StatGenerator.CRIT_DAMAGE_PERCENT.genA(stats[1]),
                StatGenerator.SHIELD_PERCENT.genA(stats[2]),
                StatGenerator.CRIT_RATE_PERCENT.genA(stats[3]),
                StatGenerator.DAMAGE.genA(stats[4]),
                StatGenerator.SHIELD.genA(stats[5]),
                StatGenerator.ENERGY.genA(stats[6])
        );
    }


    private BoostStats getRandomWeaponBoostStats(int power, BoostStats baseStats){
        int[] stats = genRandomLvlStats(power, baseStats);
        return new BoostStats(
                StatGenerator.DAMAGE_PERCENT.genW(stats[0]),
                StatGenerator.CRIT_DAMAGE_PERCENT.genW(stats[1]),
                StatGenerator.SHIELD_PERCENT.genW(stats[2]),
                StatGenerator.CRIT_RATE_PERCENT.genW(stats[3]),
                StatGenerator.DAMAGE.genW(stats[4]),
                StatGenerator.SHIELD.genW(stats[5]),
                StatGenerator.ENERGY.genW(stats[6])
        );
    }


    private List<String> convertStatsToLorePart(BoostStats stats){
        List<String> lore = new ArrayList<>();
        int CountLines = 0;
        if (stats.ADDITIONAL_ATTACK > 0) {
            lore.add(String.format(ConfigHelper.BOOST_ATTACK, stats.ADDITIONAL_ATTACK));
            ++CountLines;
        }
        if (stats.BOOST_ATTACK > 0) {
            lore.add(String.format(ConfigHelper.BOOST_ATTACK_PR, stats.BOOST_ATTACK*100));
            ++CountLines;
        }
        if (stats.BOOST_CRIT > 0) {
            lore.add(String.format(ConfigHelper.BOOST_CRIT_POWER, stats.BOOST_CRIT*100));
            ++CountLines;
        }
        if (stats.BOOST_CRIT_RATE > 0) {
            lore.add(String.format(ConfigHelper.BOOST_CRIT_RATE, stats.BOOST_CRIT_RATE*100));
            ++CountLines;
        }
        if (stats.BOOST_SHIELD > 0) {
            lore.add(String.format(ConfigHelper.BOOST_SHIELD_PR, stats.BOOST_SHIELD*100));
            ++CountLines;
        }
        if (stats.ADDITIONAL_ENERGY > 0) {
            lore.add(String.format(ConfigHelper.BOOST_ENERGY, stats.ADDITIONAL_ENERGY));
            ++CountLines;
        }
        if (stats.ADDITIONAL_SHIELD > 0) {
            lore.add(String.format(ConfigHelper.BOOST_SHIELD, stats.ADDITIONAL_SHIELD));
            ++CountLines;
        }
        while (CountLines<3){
            lore.add(" ");
            ++CountLines;
        }
        return lore;
    }


    public ItemStack buildWeapon(BaseInfo info, BoostStats stats){
        return buildWeapon(info, stats, false);
    }


    public ItemStack buildWeapon(BaseInfo info, BoostStats stats, boolean isFrame){
        if(info.CLASS == -1){
            int newClass = c.getRandomInt(WeaponsPath+info.ID+".classes", "0;1");
            info = new BaseInfo(
                    info.ID,
                    Math.max(info.POWER, 0),
                    info.TYPE,
                    newClass
            );
        }

        String path = WeaponsPath+info.ID+".class_"+info.CLASS;

        if(stats==null){
            stats = getRandomWeaponBoostStats(info.POWER, new BoostStats(
                    c.getCashedString(path+".stats", info.POWER +"&0&0&0&0&0&0", false)
            ));
        }

        if(isFrame){
            stats = getRandomWeaponBoostStats(
                    info.POWER,
                    new BoostStats(
                            stats.BOOST_ATTACK,
                            stats.BOOST_CRIT,
                            stats.BOOST_SHIELD,
                            stats.BOOST_CRIT_RATE,
                            stats.ADDITIONAL_ATTACK+info.POWER,
                            stats.ADDITIONAL_SHIELD,
                            stats.ADDITIONAL_ENERGY
                    )
            );
        }
        ItemStack item = setBoostStats(setBaseInfo(new ItemStack(Material.BOW), info), stats);


        ItemMeta m = item.getItemMeta();
        m.setDisplayName(" ");
        m.setCustomModelData(c.getCashedInt(path+".cmd", 0));
        m.addAttributeModifier(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        UUID.randomUUID(),"", -0.38f,
                        AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HAND
                )
        );
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        m.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        List<String> lore = new ArrayList<>();
        lore.add(getRareLabel(info.POWER));
        lore.addAll(convertStatsToLorePart(stats));

        lore.add(String.format(
                c.ICO_STRING_FORMAT_3,
                c.getCashedString(path+".ico", "none"),
                c.getCashedString(WeaponsPath+"class_"+info.CLASS+"_view", "none"),
                info.POWER
            )
        );

        lore.add(c.getCashedString(path+".lore1", " "));
        lore.add(c.getCashedString(path+".lore2", " "));
        m.setLore(lore);

        item.setItemMeta(m);
        return item;
    }


    public ItemStack buildArtifact(BaseInfo info, BoostStats stats){
        return buildArtifact(info, stats, false);
    }


    public ItemStack buildArtifact(BaseInfo info, BoostStats stats, boolean isFrame){
        if(info.CLASS == -1){
            int newClass = c.getRandomInt(ArtifactPath+info.ID+".slots", "0;1;2;3;4");
            info = new BaseInfo(
                    info.ID,
                    info.POWER,
                    info.TYPE,
                    newClass
            );
        }
        String path = ArtifactPath+info.ID+".slot_"+info.CLASS;

        if(stats==null){
            stats = getRandomArtifactBoostStats(info.POWER, new BoostStats(
                    c.getCashedString(path+".stats", "0&0&0&0&0&0&0", false)
            ));
        } else {
            if(isFrame){
                stats = getRandomArtifactBoostStats(info.POWER, stats);
            }
        }

        ItemStack item = setBoostStats(setBaseInfo(new ItemStack(Material.DIAMOND), info), stats);

        ItemMeta m = item.getItemMeta();
        m.setDisplayName(" ");
        m.setCustomModelData(c.getCashedInt(path+".cmd", 0));

        List<String> lore = new ArrayList<>();
        lore.add(getRareLabel(info.POWER));
        lore.addAll(convertStatsToLorePart(stats));

        lore.add(String.format(
                c.ICO_STRING_FORMAT_3,
                c.getCashedString(path+".ico", "none"),
                c.getCashedString(ArtifactPath+"slot_"+info.CLASS+"_view", "none"),
                info.POWER
                )
        );
        
        lore.add(c.getCashedString(path+".lore1", " "));
        lore.add(c.getCashedString(path+".lore2", " "));
        m.setLore(lore);

        item.setItemMeta(m);
        return item;
    }
}