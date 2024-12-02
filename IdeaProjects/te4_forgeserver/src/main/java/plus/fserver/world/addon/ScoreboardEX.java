package plus.fserver.world.addon;

import net.minecraft.scoreboard.Scoreboard;


public class ScoreboardEX extends Scoreboard {
    @Override
    protected ScorePlayerTeamEX newTeam(String name) {
        return new ScorePlayerTeamEX(this, name);
    }
}
