package plus.fserver.world.addon.pvpworld;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.UUID;


public class MinigameAwaitRoom {
    private int nextGame = 0;
    private final MinigameProvider.Map.Info[] infs;
    private final Team[] teams;

    @SideOnly(Side.CLIENT)
    public MinigameAwaitRoom(int cTeams, int players){
        Team[] teams = this.teams = new Team[cTeams];
        for (int i = 0; i < cTeams; i++)
            teams[i] = new Team(players);
        this.infs = null;
    }

    public MinigameAwaitRoom(MinigameProvider.Map.Info[] infs) {
        if(infs.length == 0)throw new IllegalArgumentException("Infs shold be > 0");
        MinigameProvider.Map.Info inf = infs[0];

        int cTeams = inf.teams();
        int players = inf.playersPerTeam();

        if(infs.length > 1){
            for (int i = 1; i < infs.length; i++){
                inf = infs[i];
                if(cTeams != inf.teams() || inf.playersPerTeam() != players){
                    throw new IllegalArgumentException("Infs should contains be equal!");
                }
            }
        }

        Team[] teams = this.teams = new Team[cTeams];
        for (int i = 0; i < cTeams; i++)
            teams[i] = new Team(players);

        this.infs = infs;
    }


    protected static class Team{
        int cursor;
        boolean canRebalance =true;
        final UUID[] players;

        Team(int size) {
            this.players = new UUID[size];
        }


        public boolean remove(UUID uuid){
            int max;
            if((max = cursor) == 0)return false;
            UUID[] players = this.players;
            for (int cur = 0;cur < max;cur++){
                if(players[cur].equals(uuid)){
                    players[cur] = players[--cursor];
                    return true;
                }
            }
            return false;
        }


        public boolean add(UUID uuid){
            UUID[] players;
            if(cursor < (players = this.players).length){
                players[cursor++] = uuid;
                return true;
            }
            return false;
        }


        public boolean add(UUID[] team){
            UUID[] players;
            if(cursor + team.length > (players = this.players).length)
                return false;
            for (UUID uuid : team)
                players[cursor++] = uuid;

            canRebalance = false;
            return true;
        }


        public boolean canStart(){
            return cursor == players.length;
        }
    }


    public boolean remove(UUID uuid){
        for (Team team:teams){
            if(team.remove(uuid))return true;
        }
        return false;
    }


    public boolean tryAdd(UUID uuid){
        for (Team team:teams){
            if(team.add(uuid))return true;
        }
        return false;
    }


    public boolean tryAdd(UUID[] team){
        if(team.length > teams[0].players.length)return false;
        for (Team cTeam:teams){
            if(cTeam.add(team))return true;
        }
        return false;
    }


    public boolean canStart(){
        for (Team cTeam:teams)
            if(!cTeam.canStart())return false;
        return true;
    }


    public boolean isAllOnline(MinecraftServer server){
        PlayerList pl = server.getPlayerList();
        for (Team team:teams){
            for (UUID uuid:team.players){
                if(uuid == null)return false;
                if(pl.getPlayerByUUID(uuid) == null)return false;
            }
        }
        return true;
    }


    public int[][] calcScores(){
        Team[] teams = this.teams;

        int[][] score = new int[teams.length][];
        int lenTeam = teams[0].players.length;

        for (int i = 0, l = teams.length; i < l; i++){
            Team rTeam = teams[i];
            UUID[] team  = rTeam.players;
            int[] score0 = score[i] = new int[lenTeam];

            for (int j = 0; j < lenTeam; j++){
                score0[j] = getScore(team[j]);
            }
        }
        return score;
    }


    public void reBalance(){
        Team[] teams = this.teams;
        int[][] score = calcScores();
        int attempts = 2;
        while (--attempts > 0){
            for (int i = 0, l = teams.length; i < l; i++){
                Team cur1 = teams[i];
                if(!cur1.canRebalance)continue;
                int[] cScore1 = score[i];

                for (int j = i + 1; j < l; j++){
                    if(i == j)continue;

                    Team cur2 = teams[j];
                    if(!cur2.canRebalance)continue;
                    int[] cScore2 = score[j];

                    int index = swap(cScore1, cScore2);
                    if(index != -1){
                        UUID pCur1 = cur1.players[index];
                        UUID pCur2 = cur2.players[index];

                        cur1.players[index] = pCur2;
                        cur2.players[index] = pCur1;

                        int pScore1 = cScore1[index];
                        int pScore2 = cScore2[index];

                        cScore1[index] = pScore2;
                        cScore2[index] = pScore1;
                    }
                }
            }
        }
    }


    public int swap(int[] first, int[] second){
        int sum1 = sum(first);
        int sum2 = sum(second);

        int delta = delta(sum1, sum2);

        if(delta == 0)return -1;

        for (int i = 0; i < first.length; i++){
            int cur1 = first[i];
            int cur2 = second[i];
            if(cur1 == cur2)continue;

            int ns1 = sum1 - cur1 + cur2;
            int ns2 = sum2 - cur2 + cur1;

            int dl2 = delta(ns1, ns2);

            if(dl2 < delta){
                return i;
            }
        }
        return -1;
    }


    private static int delta(int sum1, int sum2){
        if(sum1 > sum2) return sum1-sum2;
        else return sum2-sum1;
    }


    public static int sum(int[] arr){
        int s = 0;
        for (int i:arr) s += i;
        return s;
    }


    //todo
    protected int getScore(UUID uuid){
        return 1;
    }


    public UUID[][] dumpPlayersAndClean(){
        Team[] teams = this.teams;
        UUID[][] uTeams = new UUID[teams.length][];
        int teamSize = teams[0].players.length;

        for (int i = 0; i < uTeams.length; i++){
            UUID[] uTeam = uTeams[i] = new UUID[teamSize];
            Team rTeam = teams[i];
            UUID[] team  = rTeam.players;
            rTeam.cursor = 0;
            rTeam.canRebalance = true;


            for (int j = 0; j < uTeam.length; j++){
                uTeam[i] = team[i];
                team[i]  = null;
            }
        }
        onPreDump(uTeams);
        return uTeams;
    }


    protected void onPreDump(UUID[][] uTeams){}


    public MinigameProvider.Map.Info nextInfo(){
        MinigameProvider.Map.Info[] infs1 = this.infs;
        return infs1[(nextGame++) % infs1.length];
    }
}