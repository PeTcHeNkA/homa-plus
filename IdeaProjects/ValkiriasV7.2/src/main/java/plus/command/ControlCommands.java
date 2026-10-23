package plus.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plus.item.BaseInfo;
import plus.item.BoostStats;
import plus.chest.reward.RewardBoxFolder;
import plus.util.cmd.CommandArgs;
import plus.util.ConfigHelper;
import plus.block.teleport.GuiTeleport;
import plus.system.ModuleLoader;
import plus.block.*;
import plus.ValkiriaManager;
import plus.util.cmd.YandeReCommandExucator;
import plus.valcirias.data.ClassStats;
import plus.valcirias.pages.ClassManager;
import plus.valcirias.pages.ClassSet;
import plus.valcirias.Valkiria;
import plus.Main;
import plus.util.cmd.SubCommand;
import plus.util.cmd.YandeReCommand;
import plus.util.cfg.BasicSector;
import plus.util.cfg.Config;
import plus.util.FastRandom;
import plus.util.Reloadable;
import plus.quest.Quest;
import plus.quest.QuestSession;
import java.util.*;


public class ControlCommands extends YandeReCommand {


    public ControlCommands(Config c) {
        super("valkirias", "yanderepowered", "valkirias utilites", new ArrayList<>());
        boolean enableSystemCommands = c.getOrSetBoolean("main.enable_system_commands", true);
        if(enableSystemCommands) {
            addSubCommand(new SubCommand("admin") {
                private final List<String> notPerms = Collections.singletonList("Hey! This command node only for Admins!");

                @Override
                public void exec(CommandSender commandSender, CommandArgs args) {
                    if (commandSender.isOp()) {
                        super.exec(commandSender, args);
                    } else {
                        commandSender.sendMessage("Hey! This command node only for Admins!");
                    }
                }

                @Override
                public List<String> exexTabComplate(CommandSender commandSender, String label, CommandArgs args) {
                    if (commandSender.isOp()) {
                        return super.exexTabComplate(commandSender, label, args);
                    } else {
                        return notPerms;
                    }
                }

                @Override
                public SubCommand[] injectSubCommands() {
                    return new SubCommand[]{
                            new SubCommand("prime") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return new ArrayList<>(Main.floader.getValciriaNames());
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs args) {
                                    if (Bukkit.getPlayer(s) == null) {
                                        commandSender.sendMessage("This player is not exist!");
                                    } else {
                                        boolean b;
                                        if(args == null){
                                            b = true;
                                        } else b = "true".equalsIgnoreCase(args.get());

                                        commandSender.sendMessage(String.format("Valkiria [%s] used premium mode[%s]", s, b));
                                        Valkiria v = Main.floader.getValkiria(s);
                                        v.setPremium(b);
                                    }
                                }
                            },
                            new SubCommand("set_bp") {
                                @Override
                                public YandeReCommandExucator[] injectSubCommands() {
                                    return new YandeReCommandExucator[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return Arrays.asList(String.valueOf(ClassStats.MAX_BATTLE_PASS));
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    Valkiria v = Main.floader.getValkiria(commandSender.getName());
                                    v.setCountBattlePass(Integer.parseInt(string));
                                }
                            },
                            new SubCommand("open_visual_tp") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    if(commandSender instanceof Player player){
                                        try {
                                            GuiTeleport.get(string).open(player, 0,0);
                                        } catch (Exception e){
                                            player.sendMessage(e.getMessage());
                                        }
                                    }
                                }
                            },
                            new SubCommand("reloadable_reload") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    Reloadable.reloadAll();
                                    if(commandSender instanceof Player) commandSender.sendMessage("Reloaded");
                                }
                            },
                            new SubCommand("quests") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                        return new SubCommand[]{new SubCommand("reload") {
                                            @Override
                                            public SubCommand[] injectSubCommands() {
                                                return new SubCommand[0];
                                            }

                                            @Override
                                            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                                return null;
                                            }

                                            @Override
                                            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                                Quest.reload();
                                                commandSender.sendMessage("Reloaded.");
                                            }
                                        }, new SubCommand("clear") {
                                            @Override
                                            public SubCommand[] injectSubCommands() {
                                                return new SubCommand[0];
                                            }
                                            private final List<String> subCommands = Arrays.asList("quest", "history");
                                            @Override
                                            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                                return subCommands;
                                            }
                                            @Override
                                            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                                Valkiria v = Main.floader.getValkiria(commandSender.getName());
                                                if(string.equals("quest")) {
                                                    v.getQuestSession().completeActiveQuest();
                                                } else if(string.equals("history")){
                                                    v.getQuestSession().completeActiveQuest();
                                                    v.setQuestSession(new QuestSession());
                                                    v.getQuestSession().setPlayer(v.toPlayer());
                                                    v.getQuestSession().onPostLoad();
                                                }
                                            }
                                        }, new SubCommand("set") {
                                            @Override
                                            public SubCommand[] injectSubCommands() {
                                                return new SubCommand[0];
                                            }

                                            @Override
                                            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                                return null;
                                            }

                                            @Override
                                            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                                Valkiria v = Main.floader.getValkiria(commandSender.getName());
                                                v.getQuestSession().completeActiveQuest();
                                                Quest quest = Quest.valueOf(Integer.parseInt(string));
                                                v.getQuestSession().attemptTakeQuest(quest);
                                            }
                                        }
                                    };
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    Reloadable.reloadAll();
                                    if(commandSender instanceof Player) commandSender.sendMessage("Reloaded");
                                }
                            },
                            new SubCommand("export_loc") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    if(commandSender instanceof Player player){
                                        Config c = new Config(Main.instance, "exported_locations.yml");
                                        String name = string;
                                        if(name == null){
                                            name = "location_"+ FastRandom.r.nextInt(1000);
                                        }
                                        c.set(name,player.getLocation());
                                        c.saveChanges();
                                        commandSender.sendMessage("| Saved as "+name);
                                    }
                                }
                            },

                            new SubCommand("redirect") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    if (Bukkit.getPlayer(s) == null) {
                                        commandSender.sendMessage("This player is not exist!");
                                    } else {
                                        String server = list.get();
                                        if (server!=null)
                                            Main.floader.bd.Redirect(s, server);
                                        else commandSender.sendMessage("Server node is not defined!");
                                    }
                                }
                            },
                            new SubCommand("get_item") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    try {
                                        return Main.generator.getCashedItemsId();
                                    } catch (NullPointerException e){
                                        return null;
                                    }
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    try {
                                        ValkiriaManager.addOrDropItem((Player) commandSender,
                                                Main.generator.buildStandartItem(s)
                                        );
                                    } catch (Exception e) {
                                        commandSender.sendMessage("Error!");
                                        commandSender.sendMessage(e.getMessage());
                                    }
                                }
                            },
                            new SubCommand("open_random_chest") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    Player target = null;
                                    if (list.size() > 0) {
                                        target = Bukkit.getPlayer(list.get(0));
                                    }
                                    if (target == null) target = (Player) commandSender;
                                    try {
                                        target.openInventory(ModuleLoader.ChestC.getChest(s).getRandomChest());
                                    } catch (Exception e) {
                                        commandSender.sendMessage("Error!");
                                        commandSender.sendMessage(e.getMessage());
                                    }
                                }
                            },
                            new SubCommand("get_artifact") {
                                private final List<String> hargs = Collections.singletonList("chiaw&13&1&-1 1&0&0&4&0&0&3");

                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return hargs;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    try {
                                        BaseInfo info = new BaseInfo(s.replace(";", "&"));
                                        BoostStats stats = null;
                                        if(list.size()>0){
                                            stats = new BoostStats(list.get(0).replace(";", "&"));
                                        }
                                        boolean isFrame = false;
                                        if(list.size()>1){
                                            isFrame = (list.get(1).equalsIgnoreCase("true") || list.get(1).equalsIgnoreCase("t"));
                                        }

                                        ItemStack item = Main.generator.getBoostable(info, stats, isFrame); //ValkiriasCORE.generator.generateNonInitArtifact(Integer.parseInt(s), Integer.parseInt(list.get(0)), Integer.parseInt(list.get(1)));
                                        ((Player) commandSender).getInventory().addItem(item);
                                    } catch (Exception e) {
                                        commandSender.sendMessage("Error!");
                                        commandSender.sendMessage(e.getMessage());
                                    }
                                }
                            },
                            new SubCommand("get_ninit") {
                                private final List<String> hargs = Collections.singletonList("id;10;1;-1 0;0;0;0;0;0;0");

                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return hargs;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    try {
                                        BaseInfo info = new BaseInfo(s.replace(";", "&"));
                                        BoostStats stats = null;
                                        if(list.size()>0){
                                            stats = new BoostStats(list.get(0).replace(";", "&"));
                                        }
                                        ItemStack item = Main.generator.getNIItem(info, stats);
                                        ((Player) commandSender).getInventory().addItem(item);
                                    } catch (Exception e) {
                                        commandSender.sendMessage("Error!");
                                        commandSender.sendMessage(e.getMessage());
                                    }
                                }
                            },
                            new SubCommand("get_extra") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    //ModuleLoader.floader.bd.itemCoder.encode(args[3]);
                                    try {
                                        if(commandSender instanceof Player p){
                                            p.getInventory().addItem(Main.floader.bd.itemCoder.encode(string));
                                        }
                                    } catch (Exception e){
                                        commandSender.sendMessage(e.getMessage()+"\n"+e.getCause().getMessage());
                                    }
                                }
                            },

                            new SubCommand("find_chest") {
                                private final List<String> hargs = Collections.singletonList("100");

                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return hargs;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    Player player = (Player) commandSender;
                                    Location loc = player.getLocation();
                                    int maxDist = Integer.parseInt(s);
                                    for (Location l : StationManager.stations.keySet()) {
                                        int dis = (int) l.distance(loc);
                                        if (dis < maxDist) {
                                            InteractableBlock b = StationManager.stations.get(l);
                                            String name = "uncnown";
                                            if (b instanceof LootBox) name = "Chest";
                                            if (b instanceof PowerStation) name = "Energy";
                                            if (b instanceof CraftTable || b instanceof ModifitycationTable)
                                                name = "Craft/Mod";

                                            player.sendMessage(String.format("| [%s]: (%s blocks) X%s, Y%s, Z%s", name, dis, l.getX(), l.getY(), l.getZ()));
                                        }
                                    }
                                }
                            },
                            new SubCommand("stations") {
                                private final List<String> hargs = Arrays.asList(
                                        "cr_station",
                                        "cr_chest",
                                        "cr_alias world;0;0;0->world;0;0;0",
                                        "cr_alias",
                                        "cr_craft_st",
                                        "cr_mod_st",
                                        "cr_visual_tp"
                                );

                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return hargs;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    Player player = (Player) commandSender;
                                    if (s.equals("cr_station")) {
                                        Location loc = player.getTargetBlock(null, 10).getLocation();
                                        StationManager.addStationInConfig(loc, list.get(0));
                                    }
                                    if (s.equals("cr_chest")) {
                                        Location loc = player.getTargetBlock(null, 10).getLocation();
                                        StationManager.addLootBoxInConfig(loc, list.get(0), list.get(1));
                                    }
                                    if (s.equals("cr_alias")) {
                                        if(list.size()>2){
                                            String[] s2 = list.get(1).replace(">", "").split("-");
                                            StationManager.addAliaseInConfig(
                                                    list.get(0),
                                                    BasicSector.encodeLoc(s2[0]),
                                                    BasicSector.encodeLoc(s2[1])
                                            );
                                        } else StationManager.addAliaseInConfig(list.get(0), player);
                                    }
                                    //addTableInConfig mod
                                    if (s.equals("cr_craft_st")) {
                                        StationManager.addTableInConfig(player.getTargetBlock(null, 10).getLocation(), list.get(0), "craft");
                                    }
                                    if (s.equals("cr_mod_st")) {
                                        StationManager.addTableInConfig(player.getTargetBlock(null, 10).getLocation(), list.get(0), "mod");
                                    }
                                    //
                                    if(s.equals("cr_visual_tp")){
                                        StationManager.addVisualTeleportInCfg(player.getTargetBlock(null, 10).getLocation(), list.get(0), list.get(1));
                                    }
                                }
                            },
                            new SubCommand("reward") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[]{
                                            new SubCommand("putAlias") {
                                                @Override
                                                public SubCommand[] injectSubCommands() {
                                                    return new SubCommand[0];
                                                }

                                                @Override
                                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                                    return null;
                                                }

                                                @Override
                                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                                    if(commandSender instanceof Player pl)
                                                        RewardBoxFolder.i.putToConfigAlias(pl, string);
                                                }
                                            },
                                            new SubCommand("putReward") {
                                                @Override
                                                public SubCommand[] injectSubCommands() {
                                                    return new SubCommand[0];
                                                }

                                                @Override
                                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                                    return null;
                                                }

                                                @Override
                                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                                    if(commandSender instanceof Player pl)
                                                        RewardBoxFolder.i.putToConfigRewardBox(pl, string);
                                                }
                                            }
                                    };
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    commandSender.sendMessage("Empty args!");
                                }
                            },
                            new SubCommand("ii_reload") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    //.generator.c.Reload();
                                    Main.generator.reloadCashe();
                                    commandSender.sendMessage("Reloaded!");
                                }
                            },
                            new SubCommand("export_location") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    if(commandSender instanceof Player p){
                                        Location l = p.getLocation();
                                        p.sendMessage(String.format("%s;%s;%s;%s", l.getWorld().getName(), l.getX(), l.getY(), l.getZ()));
                                    } else commandSender.sendMessage("Command only to player!");
                                }
                            },
                            new SubCommand("clear_valciria_cache") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                                    Main.floader.ClearHash();
                                }
                            },

                            new SubCommand("test") {
                                @Override
                                public SubCommand[] injectSubCommands() {
                                    return new SubCommand[0];
                                }

                                @Override
                                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                                    return null;
                                }

                                @Override
                                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                                    Player p = (Player) commandSender;
                                    BaseInfo info = Main.generator.getBaseInfo(p.getInventory().getItemInMainHand());
                                    BoostStats stats = Main.generator.getBoostStats(p.getInventory().getItemInMainHand());
                                    p.sendMessage(info.toString()+"="+stats.toString());
                                }
                            }
                    };
                }

                @Override
                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                    return null;
                }

                @Override
                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                    commandSender.sendMessage("Args is empty!");
                }
            });
        }

        boolean enableCraftModule = c.getOrSetBoolean("main.enable_craft_module", true);

        if(enableCraftModule){
            addSubCommand(new CraftCommands("craft", Main.instance));
        }

        addSubCommand(new SubCommand("combat") {
            @Override
            public SubCommand[] injectSubCommands() {
                return new SubCommand[0];
            }

            @Override
            public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                return null;
            }

            @Override
            public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                Valkiria v = Main.floader.getValkiria(commandSender.getName());
                v.toggleCombatModeWithCheck();
            }
        });


        addSubCommand(new SubCommand("reward") {
            @Override
            public SubCommand[] injectSubCommands() {
                return new SubCommand[0];
            }

            @Override
            public List<String> onTabComplate(CommandSender commandSender, String string, CommandArgs args) {
                return null;
            }

            @Override
            public void onCommand(CommandSender commandSender, String string, CommandArgs args) {
                Valkiria v = Main.floader.getValkiria(commandSender.getName());
                if(v.getReward()==null)return;
                String rewardCode = v.getReward();
                if(rewardCode==null || rewardCode.equalsIgnoreCase("none")){
                    commandSender.spigot().sendMessage(ConfigHelper.emptyReward);
                } else {
                    ItemStack[] items = Main.floader.bd.itemCoder.decodeItems(rewardCode);
                    v.setReward(null);
                    if (items == null) return;
                    for (ItemStack i : items) {
                        if (i != null) ValkiriaManager.addOrDropItem(v.toPlayer(), i);
                    }
                }
            }
        });


        boolean enableClassGui = c.getOrSetBoolean("main.enable_class_gui", true);

        if(enableClassGui){

            addSubCommand(new SubCommand("class") {
                private final ClassManager m = new ClassManager(Main.instance);
                private final List<String> args = m.getClassSets();
                private final List<String> needPrime = Arrays.asList("Hey! This class set only for premium!");
                
                @Override
                public SubCommand[] injectSubCommands() {
                    return new SubCommand[0];
                }


                @Override
                public List<String> onTabComplate(CommandSender commandSender, String s, CommandArgs list) {
                    if(s==null)return args;
                    ClassSet cs = m.getClassSet(s);
                    if(cs==null)return args;
                    if(cs.isPremiumSet()){
                        if(!Main.floader.getValkiria(commandSender.getName()).isPremium()) return needPrime;
                    }
                    return null;
                }


                @Override
                public void onCommand(CommandSender commandSender, String s, CommandArgs list) {
                    ClassSet cs = m.getClassSet(s);
                    if(cs==null){
                        commandSender.sendMessage("Wrong class set name!");
                        return;
                    }
                    if(commandSender instanceof Player p){
                        if(cs.isPremiumSet()){
                            if(Main.floader.getValkiria(commandSender.getName()).isPremium()){
                                cs.Open(p);
                            }
                        } else cs.Open(p);
                    } else commandSender.sendMessage("This only for player!");
                }
            });
        }
    }
}