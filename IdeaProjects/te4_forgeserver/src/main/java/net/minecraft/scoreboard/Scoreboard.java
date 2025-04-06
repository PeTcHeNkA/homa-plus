package net.minecraft.scoreboard;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import plus.fserver.utls.collections.ArrayListE6;
import plus.tson.utl.Te4HashMap;


public class Scoreboard {
    private final Map<String, ScoreObjective> scoreObjectives = new Te4HashMap<>();
    private final Map<IScoreCriteria, List<ScoreObjective>> scoreObjectiveCriterias = new Te4HashMap<>();
    private final Map<String, Map<ScoreObjective, Score>> entitiesScoreObjectives = new Te4HashMap<>();
    private final ScoreObjective[] objectiveDisplaySlots = new ScoreObjective[19];
    private final Map<String, ScorePlayerTeam> teams = new Te4HashMap<>(3);
    private final Te4HashMap<String, ScorePlayerTeam> teamMemberships = new Te4HashMap<>(3);
    private static String[] displaySlots;

    @Nullable
    public ScoreObjective getObjective(String name) {
        return this.scoreObjectives.get(name);
    }


    public ScoreObjective addScoreObjective(String name, IScoreCriteria criteria) {
        if (name.length() > 16) {
            throw new IllegalArgumentException("The objective name '" + name + "' is too long!");
        } else {
            ScoreObjective scoreobjective = this.getObjective(name);

            if (scoreobjective != null) {
                throw new IllegalArgumentException("An objective with the name '" + name + "' already exists!");
            } else {
                scoreobjective = new ScoreObjective(this, name, criteria);
                List<ScoreObjective> list = this.scoreObjectiveCriterias.get(criteria);

                if (list == null) {
                    list = new ArrayListE6<>();
                    this.scoreObjectiveCriterias.put(criteria, list);
                }
                list.add(scoreobjective);
                this.scoreObjectives.put(name, scoreobjective);
                this.onScoreObjectiveAdded(scoreobjective);
                return scoreobjective;
            }
        }
    }


    public Collection<ScoreObjective> getObjectivesFromCriteria(IScoreCriteria criteria) {
        Collection<ScoreObjective> collection = this.scoreObjectiveCriterias.get(criteria);
        return collection == null ? Collections.EMPTY_LIST : new ArrayList<>(collection);
    }


    public boolean entityHasObjective(String name, ScoreObjective objective) {
        Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(name);

        if (map == null) {
            return false;
        } else {
            Score score = map.get(objective);
            return score != null;
        }
    }


    public Score getOrCreateScore(String username, ScoreObjective objective) {
        if (username.length() > 40) {
            throw new IllegalArgumentException("The player name '" + username + "' is too long!");
        } else {
            Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(username);

            if (map == null) {
                map = new Te4HashMap<>(3);
                this.entitiesScoreObjectives.put(username, map);
            }

            Score score = map.get(objective);

            if (score == null) {
                score = new Score(this, objective, username);
                map.put(objective, score);
            }
            return score;
        }
    }


    public Collection<Score> getSortedScores(ScoreObjective objective) {
        List<Score> list = new ArrayListE6<>();

        for (Map<ScoreObjective, Score> map : this.entitiesScoreObjectives.values()) {
            Score score = map.get(objective);
            if (score != null) {
                list.add(score);
            }
        }
        list.sort(Score.SCORE_COMPARATOR);
        return list;
    }


    public Collection<ScoreObjective> getScoreObjectives() {
        return this.scoreObjectives.values();
    }


    public Collection<String> getObjectiveNames() {
        return this.entitiesScoreObjectives.keySet();
    }


    public void removeObjectiveFromEntity(String name, ScoreObjective objective) {
        if (objective == null) {
            Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.remove(name);
            if (map != null) {
                this.broadcastScoreUpdate(name);
            }
        } else {
            Map<ScoreObjective, Score> map2 = this.entitiesScoreObjectives.get(name);

            if (map2 != null) {
                Score score = map2.remove(objective);

                if (map2.size() < 1) {
                    Map<ScoreObjective, Score> map1 = this.entitiesScoreObjectives.remove(name);

                    if (map1 != null) {
                        this.broadcastScoreUpdate(name);
                    }
                } else if (score != null) {
                    this.broadcastScoreUpdate(name, objective);
                }
            }
        }
    }


    public Collection<Score> getScores() {
        Collection<Map<ScoreObjective, Score>> collection = this.entitiesScoreObjectives.values();
        List<Score> list = new ArrayList<>();

        for (Map<ScoreObjective, Score> map : collection) {
            list.addAll(map.values());
        }
        return list;
    }


    public Map<ScoreObjective, Score> getObjectivesForEntity(String name) {
        Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(name);
        if (map == null) {
            map = new Te4HashMap<>();
        }
        return map;
    }


    public void removeObjective(ScoreObjective objective) {
        this.scoreObjectives.remove(objective.getName());

        for (int i = 0; i < 19; ++i) {
            if (this.getObjectiveInDisplaySlot(i) == objective) {
                this.setObjectiveInDisplaySlot(i, null);
            }
        }
        List list = this.scoreObjectiveCriterias.get(objective.getCriteria());

        if (list != null) {
            list.remove(objective);
        }
        for (Map<ScoreObjective, Score> map : this.entitiesScoreObjectives.values()) {
            map.remove(objective);
        }
        this.onScoreObjectiveRemoved(objective);
    }


    public void setObjectiveInDisplaySlot(int objectiveSlot, ScoreObjective objective) {
        this.objectiveDisplaySlots[objectiveSlot] = objective;
    }


    @Nullable
    public ScoreObjective getObjectiveInDisplaySlot(int slotIn) {
        return this.objectiveDisplaySlots[slotIn];
    }


    @Nullable
    public ScorePlayerTeam getTeam(String teamName) {
        return this.teams.get(teamName);
    }


    public ScorePlayerTeam createTeam(String name) {
        if (name.length() > 16) {
            throw new IllegalArgumentException("The team name '" + name + "' is too long!");
        } else {
            ScorePlayerTeam team = this.getTeam(name);

            if (team != null) {
                throw new IllegalArgumentException("A team with the name '" + name + "' already exists!");
            } else {
                team = newTeam(name);
                this.teams.put(name, team);
                this.broadcastTeamCreated(team);
                return team;
            }
        }
    }


    protected ScorePlayerTeam newTeam(String name){
        return new ScorePlayerTeam(this, name);
    }


    public void removeTeam(ScorePlayerTeam playerTeam) {
        this.teams.remove(playerTeam.getName());

        for (String s : playerTeam.getMembershipCollection()) {
            this.teamMemberships.remove(s);
        }
        this.broadcastTeamRemove(playerTeam);
    }


    public boolean addPlayerToTeam(String player, String newTeam) {
        ScorePlayerTeam scoreplayerteam;
        if (player.length() > 40) {
            throw new IllegalArgumentException("The player name '" + player + "' is too long!");
        } else if ((scoreplayerteam = this.getTeam(newTeam)) == null) {
            return false;
        } else {
            this.removePlayerFromTeams(player);

            this.teamMemberships.fput(player, scoreplayerteam);
            scoreplayerteam.getMembershipCollection().add(player);
            return true;
        }
    }


    public boolean removePlayerFromTeams(String playerName) {
        ScorePlayerTeam scoreplayerteam = this.getPlayersTeam(playerName);

        if (scoreplayerteam != null) {
            this.removePlayerFromTeam(playerName, scoreplayerteam);
            return true;
        } else {
            return false;
        }
    }


    public void removePlayerFromTeam(String username, ScorePlayerTeam playerTeam) {
        if (this.getPlayersTeam(username) != playerTeam) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + playerTeam.getName() + "'.");
        } else {
            this.teamMemberships.remove(username);
            playerTeam.getMembershipCollection().remove(username);
        }
    }


    public Collection<String> getTeamNames() {
        return this.teams.keySet();
    }


    public Collection<ScorePlayerTeam> getTeams() {
        return this.teams.values();
    }


    @Nullable
    public ScorePlayerTeam getPlayersTeam(String username) {
        return this.teamMemberships.get(username);
    }

    public void onScoreObjectiveAdded(ScoreObjective scoreObjectiveIn) {}


    public void onObjectiveDisplayNameChanged(ScoreObjective objective) {}


    public void onScoreObjectiveRemoved(ScoreObjective objective) {}


    public void onScoreUpdated(Score scoreIn) {}


    public void broadcastScoreUpdate(String scoreName) {}


    public void broadcastScoreUpdate(String scoreName, ScoreObjective objective) {}


    public void broadcastTeamCreated(ScorePlayerTeam playerTeam) {}


    public void broadcastTeamInfoUpdate(ScorePlayerTeam playerTeam) {}


    public void broadcastTeamRemove(ScorePlayerTeam playerTeam) {}


    public static String getObjectiveDisplaySlot(int id) {
        switch (id) {
            case 0:
                return "list";
            case 1:
                return "sidebar";
            case 2:
                return "belowName";
            default:

                if (id >= 3 && id <= 18) {
                    TextFormatting textformatting = TextFormatting.fromColorIndex(id - 3);

                    if (textformatting != null && textformatting != TextFormatting.RESET) {
                        return "sidebar.team." + textformatting.getFriendlyName();
                    }
                }
                return null;
        }
    }


    public static int getObjectiveDisplaySlotNumber(String name) {
        if ("list".equalsIgnoreCase(name)) {
            return 0;
        }
        else if ("sidebar".equalsIgnoreCase(name)) {
            return 1;
        } else if ("belowName".equalsIgnoreCase(name)) {
            return 2;
        } else {
            if (name.startsWith("sidebar.team.")) {
                String s = name.substring("sidebar.team.".length());
                TextFormatting textformatting = TextFormatting.getValueByName(s);

                if (textformatting != null && textformatting.getColorIndex() >= 0) {
                    return textformatting.getColorIndex() + 3;
                }
            }
            return -1;
        }
    }


    public static String[] getDisplaySlotStrings() {
        if (displaySlots == null) {
            displaySlots = new String[19];
            for (int i = 0; i < 19; ++i) {
                displaySlots[i] = getObjectiveDisplaySlot(i);
            }
        }
        return displaySlots;
    }


    public void removeEntity(Entity entityIn) {
        if (entityIn != null && !(entityIn instanceof EntityPlayer) && !entityIn.isEntityAlive()) {
            String s = entityIn.getCachedUniqueIdString();
            this.removeObjectiveFromEntity(s, null);
            this.removePlayerFromTeams(s);
        }
    }
}