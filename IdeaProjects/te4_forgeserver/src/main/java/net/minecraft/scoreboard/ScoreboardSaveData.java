package net.minecraft.scoreboard;

import java.util.Collection;
import java.util.Iterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.IScoreCriteria.EnumRenderType;
import net.minecraft.scoreboard.Team.CollisionRule;
import net.minecraft.scoreboard.Team.EnumVisible;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ScoreboardSaveData extends WorldSavedData {
    private static final Logger LOGGER = LogManager.getLogger();
    private Scoreboard scoreboard;
    private NBTTagCompound delayedInitNbt;

    public ScoreboardSaveData() {
        this("scoreboard");
    }


    public ScoreboardSaveData(String name) {
        super(name);
    }


    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        if (this.delayedInitNbt != null) {
            this.readFromNBT(this.delayedInitNbt);
        }
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (this.scoreboard == null) {
            this.delayedInitNbt = nbt;
        } else {
            this.readObjectives(nbt.getTagList("Objectives", 10));
            this.readScores(nbt.getTagList("PlayerScores", 10));
            if (nbt.hasKey("DisplaySlots", 10)) {
                this.readDisplayConfig(nbt.getCompoundTag("DisplaySlots"));
            }

            if (nbt.hasKey("Teams", 9)) {
                this.readTeams(nbt.getTagList("Teams", 10));
            }
        }
    }


    protected void readTeams(NBTTagList list) {
        for(int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            String name = item.getString("Name");
            if (name.length() > 16) {
                name = name.substring(0, 16);
            }

            ScorePlayerTeam team = this.scoreboard.createTeam(name);
            String display = item.getString("DisplayName");
            if (display.length() > 32) {
                display = display.substring(0, 32);
            }

            team.setDisplayName(display);
            if (item.hasKey("TeamColor", 8)) {
                team.setColor(TextFormatting.getValueByName(item.getString("TeamColor")));
            }

            team.setPrefix(item.getString("Prefix"));
            team.setSuffix(item.getString("Suffix"));
            if (item.hasKey("AllowFriendlyFire", 99)) {
                team.setAllowFriendlyFire(item.getBoolean("AllowFriendlyFire"));
            }

            if (item.hasKey("SeeFriendlyInvisibles", 99)) {
                team.setSeeFriendlyInvisiblesEnabled(item.getBoolean("SeeFriendlyInvisibles"));
            }

            Team.EnumVisible visible;
            if (item.hasKey("NameTagVisibility", 8)) {
                visible = EnumVisible.getByName(item.getString("NameTagVisibility"));
                if (visible != null) {
                    team.setNameTagVisibility(visible);
                }
            }

            if (item.hasKey("DeathMessageVisibility", 8)) {
                visible = EnumVisible.getByName(item.getString("DeathMessageVisibility"));
                if (visible != null) {
                    team.setDeathMessageVisibility(visible);
                }
            }

            if (item.hasKey("CollisionRule", 8)) {
                Team.CollisionRule lvt_7_3_ = CollisionRule.getByName(item.getString("CollisionRule"));
                if (lvt_7_3_ != null) {
                    team.setCollisionRule(lvt_7_3_);
                }
            }

            this.loadTeamPlayers(team, item.getTagList("Players", 8));
        }
    }


    protected void loadTeamPlayers(ScorePlayerTeam team, NBTTagList list) {
        for(int i = 0; i < list.tagCount(); ++i) {
            this.scoreboard.addPlayerToTeam(list.getStringTagAt(i), team.getName());
        }
    }


    protected void readDisplayConfig(NBTTagCompound nbt) {
        for(int i = 0; i < 19; ++i) {
            if (nbt.hasKey("slot_" + i, 8)) {
                String slot = nbt.getString("slot_" + i);
                ScoreObjective score = this.scoreboard.getObjective(slot);
                this.scoreboard.setObjectiveInDisplaySlot(i, score);
            }
        }
    }


    protected void readObjectives(NBTTagList list) {
        for(int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            IScoreCriteria criteria = IScoreCriteria.INSTANCES.get(item.getString("CriteriaName"));
            if (criteria != null) {
                String name = item.getString("Name");
                if (name.length() > 16) {
                    name = name.substring(0, 16);
                }

                ScoreObjective score = this.scoreboard.addScoreObjective(name, criteria);
                score.setDisplayName(item.getString("DisplayName"));
                score.setRenderType(EnumRenderType.getByName(item.getString("RenderType")));
            }
        }
    }


    protected void readScores(NBTTagList list) {
        for(int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            ScoreObjective objScore = this.scoreboard.getObjective(item.getString("Objective"));
            String name = item.getString("Name");
            if (name.length() > 40) {
                name = name.substring(0, 40);
            }

            Score score = this.scoreboard.getOrCreateScore(name, objScore);
            score.setScorePoints(item.getInteger("Score"));
            if (item.hasKey("Locked")) {
                score.setLocked(item.getBoolean("Locked"));
            }
        }
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        if (this.scoreboard == null) {
            LOGGER.warn("Tried to save scoreboard without having a scoreboard...");
            return nbt;
        } else {
            nbt.setTag("Objectives", this.objectivesToNbt());
            nbt.setTag("PlayerScores", this.scoresToNbt());
            nbt.setTag("Teams", this.teamsToNbt());
            this.fillInDisplaySlots(nbt);
            return nbt;
        }
    }


    protected NBTTagList teamsToNbt() {
        NBTTagList list = new NBTTagList();
        Collection<ScorePlayerTeam> teams = this.scoreboard.getTeams();
        Iterator iter = teams.iterator();

        while(iter.hasNext()) {
            ScorePlayerTeam team = (ScorePlayerTeam)iter.next();
            NBTTagCompound tNbt = new NBTTagCompound();
            tNbt.setString("Name", team.getName());
            tNbt.setString("DisplayName", team.getDisplayName());
            if (team.getColor().getColorIndex() >= 0) {
                tNbt.setString("TeamColor", team.getColor().getFriendlyName());
            }

            tNbt.setString("Prefix", team.getPrefix());
            tNbt.setString("Suffix", team.getSuffix());
            tNbt.setBoolean("AllowFriendlyFire", team.getAllowFriendlyFire());
            tNbt.setBoolean("SeeFriendlyInvisibles", team.getSeeFriendlyInvisiblesEnabled());
            tNbt.setString("NameTagVisibility", team.getNameTagVisibility().internalName);
            tNbt.setString("DeathMessageVisibility", team.getDeathMessageVisibility().internalName);
            tNbt.setString("CollisionRule", team.getCollisionRule().name);
            NBTTagList players = new NBTTagList();
            Iterator mIter = team.getMembershipCollection().iterator();

            while(mIter.hasNext()) {
                String name = (String)mIter.next();
                players.appendTag(new NBTTagString(name));
            }

            tNbt.setTag("Players", players);
            list.appendTag(tNbt);
        }

        return list;
    }


    protected void fillInDisplaySlots(NBTTagCompound nbt) {
        NBTTagCompound slots = new NBTTagCompound();
        boolean notEmpty = false;

        for(int i = 0; i < 19; ++i) {
            ScoreObjective score = this.scoreboard.getObjectiveInDisplaySlot(i);
            if (score != null) {
                slots.setString("slot_" + i, score.getName());
                notEmpty = true;
            }
        }

        if (notEmpty) {
            nbt.setTag("DisplaySlots", slots);
        }
    }


    protected NBTTagList objectivesToNbt() {
        NBTTagList list = new NBTTagList();
        Collection<ScoreObjective> cores = this.scoreboard.getScoreObjectives();
        Iterator var3 = cores.iterator();

        while(var3.hasNext()) {
            ScoreObjective score = (ScoreObjective)var3.next();
            if (score.getCriteria() != null) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("Name", score.getName());
                nbt.setString("CriteriaName", score.getCriteria().getName());
                nbt.setString("DisplayName", score.getDisplayName());
                nbt.setString("RenderType", score.getRenderType().getRenderType());
                list.appendTag(nbt);
            }
        }

        return list;
    }


    protected NBTTagList scoresToNbt() {
        NBTTagList list = new NBTTagList();
        Collection<Score> scores = this.scoreboard.getScores();
        Iterator var3 = scores.iterator();

        while(var3.hasNext()) {
            Score score = (Score)var3.next();
            if (score.getObjective() != null) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("Name", score.getPlayerName());
                nbt.setString("Objective", score.getObjective().getName());
                nbt.setIntegerU("Score", score.getScorePoints());
                nbt.setBoolean("Locked", score.isLocked());
                list.appendTag(nbt);
            }
        }
        return list;
    }


    @Override
    public boolean shouldCopyNbtOnAsync() {
        return false;
    }
}
