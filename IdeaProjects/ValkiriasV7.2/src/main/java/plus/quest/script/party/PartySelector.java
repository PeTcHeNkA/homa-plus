package plus.quest.script.party;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import plus.Main;
import plus.quest.AbstractQuestSession;
import plus.quest.script.dungeon.SimplePartyQuest;
import plus.util.cfg.BasicSector;
import plus.valcirias.DungeonValkiria;

import java.util.*;


public abstract class PartySelector {

    public static PartySelector build(SimplePartyQuest quest, String rawData){
        String[] data = rawData.split(":");
        if(data[0].equalsIgnoreCase("range")){
            if(data.length==2){
                return new PartySelector(quest) {
                    private final int range = Integer.parseInt(data[1]);
                    @Override
                    public List<Player> select(AbstractQuestSession session) {
                        List<Player> players = new ArrayList<>();
                        Location loc = session.getPlayer().getLocation();
                        for(Player player: Bukkit.getOnlinePlayers()){
                            Location ploc = player.getLocation();
                            if(loc.getWorld()==ploc.getWorld() && ploc.distance(loc)<range) players.add(player);
                        }
                        return players;
                    }
                };
            } else return new PartySelector(quest) {
                private final int range = Integer.parseInt(data[2]);
                private final Location loc = BasicSector.encodeLoc(data[1]);
                @Override
                public List<Player> select(AbstractQuestSession session) {
                    List<Player> players = new ArrayList<>();
                    for(Player player: Bukkit.getOnlinePlayers()){
                        Location ploc = player.getLocation();
                        if(loc.getWorld()==ploc.getWorld() && ploc.distance(loc)<range) players.add(player);
                    }
                    return players;
                }
            };
        }
        return new PartySelector(quest) {
            @Override
            public List<DungeonValkiria> selectPlayers(AbstractQuestSession session) {
                return Collections.singletonList((DungeonValkiria) Main.floader.getValkiria(session.getPlayer()));
            }
        };
    }

    public List<Player> select(AbstractQuestSession session){return null;}

    private HashMap<String, DungeonValkiria> players;

    public Collection<DungeonValkiria> selectPlayers(AbstractQuestSession session){
        players = new HashMap<>();
        for(Player p:select(session)){
            players.put(session.getPlayer().getName(), (DungeonValkiria) Main.floader.getValkiria(p));
        }
        return players.values();
    }

    private final SimplePartyQuest quest;

    public PartySelector(SimplePartyQuest quest){
        this.quest = quest;
    }


    public Collection<DungeonValkiria> getSessions(){
        return players.values();
    }

    public List<Player> toPlayers(){
        ArrayList<Player> players = new ArrayList<>();
        for(DungeonValkiria v:this.players.values()){
            players.add(v.toPlayer());
        }
        return players;
    }


    public SimplePartyQuest getQuest(){
        return quest;
    }


    public void remove(String player){
        players.remove(player);
        if(players.isEmpty()){
            quest.unLock();
        }
    }


    public void clear(){
        players.clear();
    }
}