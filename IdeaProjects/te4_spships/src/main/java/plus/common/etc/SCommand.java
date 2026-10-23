package plus.common.etc;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SCommand implements ICommand {
    private final int level;
    private final String name;

    protected SCommand(int level, String name) {
        this.level = level;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + getName();
    }

    @Override
    public List<String> getAliases() {
        return Collections.EMPTY_LIST;
    }


    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        if(sender instanceof EntityPlayerSP) {
            UserListOpsEntry userlistopsentry = server.getPlayerList().getOppedPlayers()
                    .getEntry(((EntityPlayerSP) sender).getGameProfile());
            boolean result;
            if (userlistopsentry != null) {
                result = userlistopsentry.getPermissionLevel() >= level;
            } else {
                result = server.getOpPermissionLevel() >= level;
            }
            if(!result){
                sender.sendMessage(new TextComponentString("Not allowed."));
            }
            return result;
        } else {
            return true;
        }
    }


    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }


    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}