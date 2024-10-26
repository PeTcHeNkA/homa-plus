package net.minecraft.command;

import net.minecraft.util.text.*;
import net.minecraftforge.event.*;
import net.minecraftforge.common.*;
import com.google.common.base.*;
import net.minecraft.entity.*;
import net.minecraft.server.*;
import net.minecraft.util.math.*;
import javax.annotation.*;
import com.google.common.collect.*;
import java.util.*;
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Te4HashSet;


public abstract class CommandHandler implements ICommandManager {
    private final Map<String, ICommand> commandMap;
    private final Set<ICommand> commandSet;

    public CommandHandler() {
        this.commandMap = new Te4HashMap<>();
        this.commandSet = new Te4HashSet<>();
    }


    @Override
    public int executeCommand(final ICommandSender sender, String rawCommand) {
        rawCommand = rawCommand.trim();
        if (rawCommand.startsWith("/")) {
            rawCommand = rawCommand.substring(1);
        }
        String[] astring = rawCommand.split(" ");
        final String s = astring[0];
        astring = dropFirstString(astring);
        final ICommand icommand = this.commandMap.get(s);
        int i = 0;
        try {
            final int j = this.getUsernameIndex(icommand, astring);
            if (icommand == null) {
                final TextComponentTranslation textcomponenttranslation1 = new TextComponentTranslation("commands.generic.notFound", new Object[0]);
                textcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
                sender.sendMessage(textcomponenttranslation1);
            }
            else if (icommand.checkPermission(this.getServer(), sender)) {
                final CommandEvent event = new CommandEvent(icommand, sender, astring);
                if (MinecraftForge.EVENT_BUS.post(event)) {
                    if (event.getException() != null) {
                        Throwables.throwIfUnchecked(event.getException());
                    }
                    return 1;
                }
                if (event.getParameters() != null) {
                    astring = event.getParameters();
                }
                if (j > -1) {
                    final List<Entity> list = EntitySelector.matchEntities(sender, astring[j], Entity.class);
                    final String s2 = astring[j];
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());
                    if (list.isEmpty()) {
                        throw new PlayerNotFoundException("commands.generic.selector.notFound", astring[j]);
                    }
                    for (final Entity entity : list) {
                        astring[j] = entity.getCachedUniqueIdString();
                        if (this.tryExecute(sender, astring, icommand, rawCommand)) {
                            ++i;
                        }
                    }
                    astring[j] = s2;
                }
                else {
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
                    if (this.tryExecute(sender, astring, icommand, rawCommand)) {
                        ++i;
                    }
                }
            }
            else {
                final TextComponentTranslation msg = new TextComponentTranslation("commands.generic.permission", new Object[0]);
                msg.getStyle().setColor(TextFormatting.RED);
                sender.sendMessage(msg);
            }
        }
        catch (CommandException commandexception) {
            final TextComponentTranslation msg = new TextComponentTranslation(commandexception.getMessage(), commandexception.getErrorObjects());
            msg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(msg);
        }
        sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, i);
        return i;
    }


    protected boolean tryExecute(final ICommandSender sender, final String[] args, final ICommand command, final String input) {
        try {
            command.execute(this.getServer(), sender, args);
            return true;
        }
        catch (WrongUsageException wrongusageexception) {
            final TextComponentTranslation msg = new TextComponentTranslation("commands.generic.usage", new Object[] { new TextComponentTranslation(wrongusageexception.getMessage(), wrongusageexception.getErrorObjects()) });
            msg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(msg);
        }
        catch (CommandException commandexception) {
            final TextComponentTranslation msg = new TextComponentTranslation(commandexception.getMessage(), commandexception.getErrorObjects());
            msg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(msg);
        }
        catch (Throwable throwable) {
            final TextComponentTranslation msg = new TextComponentTranslation("commands.generic.exception", new Object[0]);
            msg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(msg);
            MinecraftServer.LOGGER.warn("Couldn't process command: " + input, throwable);
        }
        return false;
    }


    protected abstract MinecraftServer getServer();


    public ICommand registerCommand(final ICommand command) {
        this.commandMap.put(command.getName(), command);
        this.commandSet.add(command);
        for (final String s : command.getAliases()) {
            final ICommand icommand = this.commandMap.get(s);
            if (icommand == null || !icommand.getName().equals(s)) {
                this.commandMap.put(s, command);
            }
        }
        return command;
    }


    private static String[] dropFirstString(final String[] input) {
        final String[] astring = new String[input.length - 1];
        System.arraycopy(input, 1, astring, 0, input.length - 1);
        return astring;
    }


    @Override
    public List<String> getTabCompletions(final ICommandSender sender, final String input, @Nullable final BlockPos pos) {
        final String[] astring = input.split(" ", -1);
        final String s = astring[0];
        if (astring.length == 1) {
            final List<String> list = Lists.newArrayList();
            for (final Map.Entry<String, ICommand> entry : this.commandMap.entrySet()) {
                if (CommandBase.doesStringStartWith(s, (String)entry.getKey()) && entry.getValue().checkPermission(this.getServer(), sender)) {
                    list.add(entry.getKey());
                }
            }
            return list;
        }
        if (astring.length > 1) {
            final ICommand icommand = this.commandMap.get(s);
            if (icommand != null && icommand.checkPermission(this.getServer(), sender)) {
                return icommand.getTabCompletions(this.getServer(), sender, dropFirstString(astring), pos);
            }
        }
        return Collections.emptyList();
    }


    @Override
    public List<ICommand> getPossibleCommands(final ICommandSender sender) {
        final List<ICommand> list = Lists.newArrayList();
        for (final ICommand icommand : this.commandSet) {
            if (icommand.checkPermission(this.getServer(), sender)) {
                list.add(icommand);
            }
        }
        return list;
    }


    @Override
    public Map<String, ICommand> getCommands() {
        return this.commandMap;
    }


    private int getUsernameIndex(@Nullable final ICommand command, final String[] args) throws CommandException {
        if (command == null) return -1;
        for (int i = 0; i < args.length; ++i) {
            if (command.isUsernameIndex(args, i) && EntitySelector.matchesMultiplePlayers(args[i])) {
                return i;
            }
        }
        return -1;
    }
}