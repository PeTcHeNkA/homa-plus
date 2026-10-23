package net.minecraft.scoreboard;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.tson.utl.Te4HashSet;


public class ScorePlayerTeam extends Team {
    private final Scoreboard scoreboard;
    private final String name;
    private final Set<String> membershipSet = new Te4HashSet<>();
    private String displayName;
    private String prefix = "";
    private String suffix = "";
    private boolean allowFriendlyFire = true;
    private boolean canSeeFriendlyInvisibles = true;
    private Team.EnumVisible nameTagVisibility;
    private Team.EnumVisible deathMessageVisibility;
    private TextFormatting color;
    private Team.CollisionRule collisionRule;

    public ScorePlayerTeam(Scoreboard scoreboard, String name) {
        this.nameTagVisibility = EnumVisible.ALWAYS;
        this.deathMessageVisibility = EnumVisible.ALWAYS;
        this.color = TextFormatting.RESET;
        this.collisionRule = CollisionRule.ALWAYS;
        this.scoreboard = scoreboard;
        this.name = name;
        this.displayName = name;
    }


    @Override
    public String getName() {
        return this.name;
    }


    public String getDisplayName() {
        return this.displayName;
    }


    public void setDisplayName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        } else {
            this.displayName = name;
            this.scoreboard.broadcastTeamInfoUpdate(this);
        }
    }


    public boolean add(Entity entity){
        if(entity instanceof EntityPlayer){
            return add(entity.getName());
        } else {
            return add(entity.getCachedUniqueIdString());
        }
    }


    public boolean add(String player){
        return scoreboard.addPlayerToTeam(player, name);
    }


    @Override
    public Set<String> getMembershipCollection() {
        return this.membershipSet;
    }


    public String getPrefix() {
        return this.prefix;
    }


    public void setPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix cannot be null");
        } else {
            this.prefix = prefix;
            this.scoreboard.broadcastTeamInfoUpdate(this);
        }
    }


    public String getSuffix() {
        return this.suffix;
    }


    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.scoreboard.broadcastTeamInfoUpdate(this);
    }


    @Override
    public String formatString(String name) {
        return this.getPrefix() + name + this.getSuffix();
    }


    public static String formatPlayerName(@Nullable Team team, String name) {
        return team == null ? name : team.formatString(name);
    }


    public boolean getAllowFriendlyFire() {
        return this.allowFriendlyFire;
    }


    public void setAllowFriendlyFire(boolean b) {
        this.allowFriendlyFire = b;
        this.scoreboard.broadcastTeamInfoUpdate(this);
    }


    @Override
    public boolean getSeeFriendlyInvisiblesEnabled() {
        return this.canSeeFriendlyInvisibles;
    }


    public void setSeeFriendlyInvisiblesEnabled(boolean b) {
        this.canSeeFriendlyInvisibles = b;
        this.scoreboard.broadcastTeamInfoUpdate(this);
    }


    @Override
    public Team.EnumVisible getNameTagVisibility() {
        return this.nameTagVisibility;
    }


    @Override
    public Team.EnumVisible getDeathMessageVisibility() {
        return this.deathMessageVisibility;
    }


    public void setNameTagVisibility(Team.EnumVisible visible) {
        this.nameTagVisibility = visible;
        this.scoreboard.broadcastTeamInfoUpdate(this);
    }


    public void setDeathMessageVisibility(Team.EnumVisible visible) {
        this.deathMessageVisibility = visible;
        this.scoreboard.broadcastTeamInfoUpdate(this);
    }


    @Override
    public Team.CollisionRule getCollisionRule() {
        return this.collisionRule;
    }



    public void setCollisionRule(Team.CollisionRule rule) {
        this.collisionRule = rule;
        this.scoreboard.broadcastTeamInfoUpdate(this);
    }


    public int getFriendlyFlags() {
        int flags = 0;
        if (this.getAllowFriendlyFire())            flags |= 1;
        if (this.getSeeFriendlyInvisiblesEnabled()) flags |= 2;
        return flags;
    }


    @SideOnly(Side.CLIENT)
    public void setFriendlyFlags(int flags) {
        this.setAllowFriendlyFire((flags & 1) > 0);
        this.setSeeFriendlyInvisiblesEnabled((flags & 2) > 0);
    }


    public void setColor(TextFormatting color) {
        this.color = color;
    }


    @Override
    public TextFormatting getColor() {
        return this.color;
    }
}
