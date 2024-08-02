package net.minecraft.command.server;

import net.minecraft.server.*;
import net.minecraft.util.text.*;
import net.minecraft.command.*;
import com.mojang.authlib.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.*;
import javax.annotation.*;
import java.util.*;
import net.minecraft.server.management.*;


public class CommandBanPlayer extends CommandBase {
    @Override
    public String getName() {
        return "ban";
    }


    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }


    @Override
    public String getUsage(final ICommandSender sender) {
        return "commands.ban.usage";
    }


    @Override
    public boolean checkPermission(final MinecraftServer server, final ICommandSender sender) {
        return server.getPlayerList().getBannedPlayers().isLanServer() && super.checkPermission(server, sender);
    }


    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 1 || args[0].length() <= 0) {
            throw new WrongUsageException("commands.ban.usage");
        }
        final GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(args[0]);
        if (profile == null) {
            throw new CommandException("commands.ban.failed", args[0]);
        }
        String msg = null;
        if (args.length >= 2) {
            msg = getChatComponentFromNthArg(sender, args, 1).getUnformattedText();
        }
        final UserListBansEntry entry = new UserListBansEntry(profile, null, sender.getName(), null, msg);
        (server.getPlayerList().getBannedPlayers()).addEntry(entry);
        final EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[0]);
        if (player != null) {
            if(server.getNetworkSystem().hasFirewall()){
                server.getNetworkSystem().softBan(player, 600_000);
            }
            player.connection.disconnect(new TextComponentTranslation("multiplayer.disconnect.banned", new Object[0]));
        }
        notifyCommandListener(sender, this, "commands.ban.success", args[0]);
    }


    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
        if (args.length >= 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}