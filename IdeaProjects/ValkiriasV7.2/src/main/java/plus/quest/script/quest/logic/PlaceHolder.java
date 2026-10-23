package plus.quest.script.quest.logic;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import plus.Main;
import plus.quest.AbstractQuestSession;
import plus.quest.Quest;
import plus.quest.memory.AbstractMemorySector;
import plus.quest.script.quest.listener.RegionListener;
import plus.valcirias.Valkiria;


public abstract class PlaceHolder<T> {

    public static PlaceHolder build(String value){
        //RANGE:10,10,10
        String[] data = value.strip().split(":");
        String name = data[0];

        if(name.equalsIgnoreCase("RANGE")){
            String[] cords = data[1].split(",");
            return new PlaceHolder<Number>() {
                final double X = Double.parseDouble(cords[0]);
                final double Y = Double.parseDouble(cords[1]);
                final double Z = Double.parseDouble(cords[2]);
                @Override
                public Number getValue(AbstractQuestSession session) {
                    Location loc = session.getPlayer().getLocation();
                    return Math.sqrt( pow(X - loc.getX())+ pow(Y - loc.getY()) + pow(Z - loc.getZ())   );
                }
            };
        } else if(name.equalsIgnoreCase("RANGE_XZ")){
            String[] cords = data[1].split(",");
            return new PlaceHolder<Number>() {
                final double X = Double.parseDouble(cords[0]);
                final double Z = Double.parseDouble(cords[1]);
                @Override
                public Number getValue(AbstractQuestSession session) {
                    Location loc = session.getPlayer().getLocation();
                    return Math.sqrt( pow(X - loc.getX()) + pow(Z - loc.getZ()));
                }
            };
        } else if(name.equalsIgnoreCase("MEMORY") || name.equalsIgnoreCase("MEM")){
            //System.out.println("|"+data[1]+"|");
            return new PlaceHolder() {
                private final String request = data[1];
                @Override
                public Object getValue(AbstractQuestSession session) {
                    AbstractMemorySector section = session.getMemory().Sections().get(request);
                    //System.out.println("LENGHT="+session.getMemory().Sections().keySet().size());
                    //for(String key:session.getMemory().Sections().keySet()){
                    //    System.out.println("|"+key+"|");
                    //}
                    if(section==null)return null;
                    return section.getValue();
                }
            };
        } else if(name.equalsIgnoreCase("RG_TRIGGER_ID")){
            return new PlaceHolder() {
                private final RegionQuery q = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
                @Override
                public Integer getValue(AbstractQuestSession session) {
                    LocalPlayer pl = WorldGuardPlugin.inst().wrapPlayer(session.getPlayer());
                    for(ProtectedRegion r:q.getApplicableRegions(pl.getLocation())){
                        Integer s = r.getFlag(RegionListener.QUEST_TRIGGER_ID);
                        if(s!=null) return s;
                    }
                    return -1;
                }
            };
        } else if(name.equalsIgnoreCase("NUM") || name.equalsIgnoreCase("NUMBER")){
            return new PlaceHolder() {
                private final int i = Integer.parseInt(data[1]);
                @Override
                public Integer getValue(AbstractQuestSession session) {
                    return i;
                }
            };
        } else if(name.equalsIgnoreCase("DUB") || name.equalsIgnoreCase("DOUBLE")){
            return new PlaceHolder() {
                private final double i = Double.parseDouble(data[1]);
                @Override
                public Double getValue(AbstractQuestSession session) {
                    return i;
                }
            };
        } else if(name.equalsIgnoreCase("QUEST_ID")) {
            return new PlaceHolder() {
                @Override
                public Integer getValue(AbstractQuestSession session) {
                    Quest quest = session.getActiveQuest();
                    if(quest==null)return -1;
                    return quest.getId();
                }
            };
        } else if(name.equalsIgnoreCase("STR") || name.equalsIgnoreCase("STRING")){
            return new PlaceHolder() {
                private final String s = data[1];
                @Override
                public String getValue(AbstractQuestSession session) {
                    return s;
                }
            };
        } else if(name.equalsIgnoreCase("BOOL") || name.equalsIgnoreCase("BOOLEAN")){
            return new PlaceHolder() {
                private final boolean b = data[1].equalsIgnoreCase("true");
                @Override
                public Boolean getValue(AbstractQuestSession session) {
                    return b;
                }
            };
        } else if(name.equalsIgnoreCase("QUEST_IS_COMPLETED")){
            return new PlaceHolder() {
                private final int id = Integer.parseInt(data[1]);
                @Override
                public Boolean getValue(AbstractQuestSession session) {
                    return session.inHistory(id);
                }
            };
        } else if(name.equalsIgnoreCase("QUEST_TIME")){
            return new PlaceHolder() {
                private final int id = Integer.parseInt(data[1]);
                @Override
                public Integer getValue(AbstractQuestSession session) {
                    long time = session.getQuestTime(id);
                    if(time==-1)return -1;
                    else return (int) (System.currentTimeMillis() - session.getQuestTime(id));
                }
            };
        } else if(name.equalsIgnoreCase("MAX_SHIELD")){
            return new PlaceHolder() {
                @Override
                public Double getValue(AbstractQuestSession session) {
                    Valkiria v = Main.floader.getValkiria(session.getPlayer());
                    return v.getMaxShield();
                }
            };
        } else if(name.equalsIgnoreCase("V_SHIELD")){
            return new PlaceHolder() {
                @Override
                public Double getValue(AbstractQuestSession session) {
                    Valkiria v = Main.floader.getValkiria(session.getPlayer());
                    return v.getShield();
                }
            };
        } else if(name.equalsIgnoreCase("HAS_BP")){
            return new PlaceHolder() {
                @Override
                public Boolean getValue(AbstractQuestSession session) {
                    Valkiria v = Main.floader.getValkiria(session.getPlayer());
                    return v.getCountBattlePass()>0;
                }
            };
        } else if(name.equalsIgnoreCase("COUNT_BP")){
            return new PlaceHolder() {
                @Override
                public Integer getValue(AbstractQuestSession session) {
                    Valkiria v = Main.floader.getValkiria(session.getPlayer());
                    return v.getCountBattlePass();
                }
            };
        }
        return null;
    }


    public abstract T getValue(AbstractQuestSession session);


    public static double pow(double n){
        return n*n;
    }
}