package plus.command;

import org.bukkit.command.CommandSender;
import plus.Main;
import plus.util.ConfigHelper;
import plus.util.cmd.CommandArgs;
import plus.util.cmd.SubCommand;
import plus.util.cmd.YandeReCommand;
import plus.util.cmd.YandeReCommandExucator;
import plus.valcirias.Party;
import plus.valcirias.Valkiria;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PartyCommands extends YandeReCommand {

    private final List<String> warnYouReadyInParty = Arrays.asList("You're already in party!");
    private final List<String> warnNullParty = Arrays.asList("You don't have party!");
    private final List<String> warnYouNotOwner = Arrays.asList("Only leader can do it!");
    private final List<String> warnYouSure = Arrays.asList("Are you sure?");
    private final List<String> warnNotLeave = Arrays.asList("You can't leave from party!");

    public PartyCommands() {
        super("party", "yanderepowered", "party utilities", Arrays.asList("pt", "pty"));
        addSubCommand(new SubCommand("create") {
            @Override
            public YandeReCommandExucator[] injectSubCommands() {
                return new YandeReCommandExucator[0];
            }


            @Override
            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty() != null){
                    return warnYouReadyInParty;
                } else return null;
            }


            @Override
            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty() != null){
                    valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyNotCreated);
                } else {
                    valkiria.setParty(new Party(valkiria));
                }
            }
        });

        addSubCommand(new SubCommand("invite") {
            @Override
            public YandeReCommandExucator[] injectSubCommands() {
                return new YandeReCommandExucator[0];
            }

            @Override
            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty() == null || !valkiria.getParty().isOwner(valkiria)){
                    return warnYouNotOwner;
                } else return new ArrayList<>(Main.floader.getValciriaNames());
            }


            @Override
            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria owner = Main.floader.getValkiria(commandSender.getName());
                if(owner.getParty()==null){
                    owner.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyIsNull);
                    return;
                }
                if(owner.getParty().isOwner(owner)){
                    Valkiria user = Main.floader.getValkiria(string);
                    if(user == null || !user.toPlayer().isOnline()){
                        owner.toPlayer().spigot().sendMessage(ConfigHelper.messagePlayerOffline);
                        return;
                    }
                    owner.getParty().invite(user);
                } else owner.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyYouNotOwner);
            }
        });

        addSubCommand(new SubCommand("leave") {
            @Override
            public YandeReCommandExucator[] injectSubCommands() {
                return new YandeReCommandExucator[0];
            }

            @Override
            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty()==null){
                    return warnNullParty;
                }
                if(valkiria.getParty().isOwner(valkiria)){
                    return warnNotLeave;
                }
                return warnYouSure;
            }

            @Override
            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty()==null){
                    valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyIsNull);
                } else {
                    valkiria.getParty().leave(valkiria);
                }
            }
        });

        addSubCommand(new SubCommand("join") {
            @Override
            public YandeReCommandExucator[] injectSubCommands() {
                return new YandeReCommandExucator[0];
            }

            @Override
            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty() != null){
                    return warnYouReadyInParty;
                } else return new ArrayList<>(Main.floader.getValciriaNames());
            }


            @Override
            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty() != null){
                    valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyRedyJoinded);
                } else {
                    Valkiria partyOwner = Main.floader.getValkiria(string);
                    if(partyOwner == null || !partyOwner.toPlayer().isOnline()){
                        valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePlayerOffline);
                        return;
                    }
                    Party party = partyOwner.getParty();
                    if(party==null){
                        valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyNotExist);
                        return;
                    }
                    party.attemptToJoin(valkiria);
                }
            }
        });
        addSubCommand(new SubCommand("confirm") {
            @Override
            public YandeReCommandExucator[] injectSubCommands() {
                return new YandeReCommandExucator[0];
            }

            @Override
            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                return new ArrayList<>(Main.floader.getValciriaNames());
            }

            @Override
            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(string==null)return;
                if(valkiria.getParty() != null){
                    Party.confirmJoin(valkiria.getParty().getOwner().toPlayerName(), valkiria, string);
                } else {
                    Party.confirmJoin(string, valkiria, valkiria.toPlayerName());
                }
            }
        });

        addSubCommand(new SubCommand("kick") {
            @Override
            public YandeReCommandExucator[] injectSubCommands() {
                return new YandeReCommandExucator[0];
            }


            @Override
            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty()==null){
                    return warnNullParty;
                }
                if(!valkiria.getParty().isOwner(valkiria)){
                    return warnYouNotOwner;
                }
                return new ArrayList<>(valkiria.getParty().getMemberList());
            }


            @Override
            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty()==null){
                    valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyNotExist);
                } else {
                    Valkiria user = Main.floader.getValkiria(string);
                    if(user == null || !user.toPlayer().isOnline()){
                        valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePlayerOffline);
                        return;
                    }
                    valkiria.getParty().kickUser(valkiria, user);
                }
            }
        });

        addSubCommand(new SubCommand("remove") {
            @Override
            public YandeReCommandExucator[] injectSubCommands() {
                return new YandeReCommandExucator[0];
            }

            @Override
            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty()==null){
                    return warnNullParty;
                }
                if(!valkiria.getParty().isOwner(valkiria)){
                    return warnYouNotOwner;
                }
                return warnYouSure;
            }

            @Override
            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria valkiria = Main.floader.getValkiria(commandSender.getName());
                if(valkiria.getParty()==null){
                    valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyNotExist);
                    return;
                }
                valkiria.getParty().remove(valkiria);
            }
        });

    }
}