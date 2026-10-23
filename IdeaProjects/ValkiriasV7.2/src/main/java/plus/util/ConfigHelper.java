package plus.util;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plus.util.cfg.BasicSector;
import plus.util.cfg.Sector;
import plus.util.chat.ChatHelper;
import plus.Main;
import plus.util.cfg.Config;
import java.util.*;


public class ConfigHelper{

    public static Config c;
    public static Config cItems;

    public static String V_BOOST_ATTACK;
    public static String V_BOOST_ATTACK_PR;
    public static String V_BOOST_SHIELD;
    public static String V_BOOST_SHIELD_PR;
    public static String V_BOOST_CRIT_POWER;
    public static String V_BOOST_CRIT_RATE;
    public static String V_BOOST_ENERGY;
    public static String BOOST_SHIELD;
    public static String BOOST_ATTACK;
    public static String BOOST_ENERGY;
    public static String BOOST_CRIT_POWER;
    public static String BOOST_CRIT_RATE;
    public static String BOOST_ATTACK_PR;
    public static String BOOST_SHIELD_PR;
    public static String V_EXPERIENCE;
    public static String V_LEVEL;
    public static String V_ATTACK;
    public static String V_CRIT_POWER;
    public static String V_CRIT_RATE;
    public static String V_ENERGY;
    public static String V_SHIELD;
    public static String A_QUALITY_NORMAL;
    public static String A_QUALITY_RARE;
    public static String A_QUALITY_EPIC;
    public static String A_QUALITY_LEGENDARY;

    public static String craftsView;
    public static String valkiriaInfo;
    public static String valkiriaPoints;
    public static String valkiriaArtifacts;
    public static String valkiriaBackPack;
    public static String valkiriaWeapons;
    public static String nickFormat;
    public static String loreFormat;
    public static String advantureItemLoreFormat;
    public static String advantureItemLoreFormat4;
    public static String advantureItemLoreFormat56;
    public static String emptyStation_Format;
    public static String fullShield_Format;
    public static String valkitiaUpgradeArts;
    public static String valkiriaGetArtifact;
    public static String stListClasses;
    public static String stClassConfirm;
    public static String statistickIco;
    public static String statistickIcoMain;
    public static String regenShield_Format;
    public static String spawnBoxFormat;
    public static String findBoxFormat;
    public static String dropExpFormat;
    public static String lvlUpFormat;
    public static String battlePassMaximumFormat;
    public static String battlePassNormalLoreFormat;

    public static int artifactsButtonCmd;
    public static int battlePassCmd;
    public static int battlePassEmptyCmd;
    public static int partyMaxSize;

    public static ItemStack folderButton;
    public static Inventory listClasses;
    public static ItemStack valkiriaBg;
    public static ItemStack valkiriaWeapon;
    public static ItemStack oneClasssBg;
    public static ItemStack upgradeArtsBG;
    public static ItemStack upgradeArtsNull;
    public static ItemStack betterCraftGuiNull;
    public static ItemStack betterCraftGuiRecpt;
    public static ItemStack attackPoints;
    public static ItemStack critPoints;
    public static ItemStack energyPoints;
    public static ItemStack shieldPoints;
    public static ItemStack freePoints;
    public static ItemStack nullSlot;
    public static ItemStack boostPoints;
    public static ItemStack artifactsGUI;

    public static TextComponent Global_pefix;
    public static TextComponent Local_pefix;
    public static TextComponent SenderPrivate_pefix;
    public static TextComponent TargetPrivate_pefix;
    public static TextComponent[] messageDisableCombat;
    public static TextComponent[] messageExistsWeapon;
    public static TextComponent[] messagePlayerOffline;
    public static TextComponent[] messageMuted;
    public static TextComponent[] messageNoPerms;
    public static TextComponent[] empetyChest;
    public static TextComponent[] helpAvatar;
    public static TextComponent[] emptyReward;
    public static TextComponent[] messageColorEdited;
    public static TextComponent[] messageNextUsageKill;
    public static TextComponent[] messageHasRewards;
    public static TextComponent[] messageOnlyPremium;

    public static TextComponent[] messagePartyJoinUser;
    public static TextComponent[] messagePartyAttemptToJoinUser;
    public static TextComponent[] messagePartyKick;
    public static TextComponent[] messagePartyCreated;
    public static TextComponent[] messagePartyNotCreated;
    public static TextComponent[] partyFullFormat;
    public static TextComponent[] messagePartyYouNotOwner;
    public static TextComponent[] messagePartyRedyJoinded;
    public static TextComponent[] messagePartyIsNull;
    public static TextComponent[] messagePartyNotExist;
    public static TextComponent[] messagePartyInviteIsNull;
    public static TextComponent[] messagePartyPlayerNotExist;
    public static TextComponent[] messagePartyNotRemoved;
    public static TextComponent[] messagePartyRemoved;
    public static TextComponent[] messagePartyOtherParty;
    public static TextComponent[] messagePartyInParty;
    public static String partyJoinConfirmFormat;
    public static String partyInviteConfirmFormat;
    //public static String partyInviteFormat;
    public static String partyMemberJoinFormat;
    public static String partyMemberLeaveFormat;
    public static String partyMemberKickedFormat;


    public static void Initialize(){
        List<String> em = Arrays.asList("string1", "string2");
        c = Main.cfg;
        cItems = Main.instance.getOrCreateYandeReConfig("items.yml");//items.yml

        BOOST_ATTACK = c.getOrSetString("main.attackboost", "value: %s", true);
        BOOST_ATTACK_PR = c.getOrSetString("main.attackboost_pr", "value: %s", true);
        BOOST_SHIELD = c.getOrSetString("main.shieldboost", "value: %s", true);
        BOOST_SHIELD_PR = c.getOrSetString("main.shieldboost_pr", "value: %s", true);
        BOOST_CRIT_POWER = c.getOrSetString("main.critdamage", "value: %s", true);
        BOOST_CRIT_RATE = c.getOrSetString("main.critrate", "value: %s", true);
        BOOST_ENERGY = c.getOrSetString("main.energyboost", "value: %s", true);
        V_BOOST_ATTACK = c.getOrSetString("main.v_attackboost", "value: %s", true);
        V_BOOST_ATTACK_PR = c.getOrSetString("main.v_attackboost_pr", "value: %s", true);
        V_BOOST_SHIELD = c.getOrSetString("main.v_shieldboost", "value: %s", true);
        V_BOOST_SHIELD_PR = c.getOrSetString("main.v_shieldboost_pr", "value: %s", true);
        V_BOOST_CRIT_POWER = c.getOrSetString("main.v_critdamage", "value: %s", true);
        V_BOOST_CRIT_RATE = c.getOrSetString("main.v_critrate", "value: %s", true);
        V_BOOST_ENERGY = c.getOrSetString("main.v_energyboost", "value: %s", true);
        V_EXPERIENCE = c.getOrSetString("main.v_experience", "value: %s", true);
        V_LEVEL = c.getOrSetString("main.v_level", "value: %s", true);
        V_ATTACK = c.getOrSetString("main.v_damage", "value: %s", true);
        V_CRIT_POWER = c.getOrSetString("main.v_critpw", "value: %s", true);
        V_CRIT_RATE = c.getOrSetString("main.v_critrt", "value: %s", true);
        V_ENERGY = c.getOrSetString("main.v_energy", "value: %s", true);
        V_SHIELD = c.getOrSetString("main.v_shield", "value: %s", true);
        A_QUALITY_NORMAL = c.getOrSetString("artifacts.quality_normal", "norm", true);
        A_QUALITY_RARE = c.getOrSetString("artifacts.quality_rare", "rare", true);
        A_QUALITY_EPIC = c.getOrSetString("artifacts.quality_epic", "epyc", true);
        A_QUALITY_LEGENDARY = c.getOrSetString("artifacts.quality_legendary", "legend", true);

        craftsView = c.getOrSetString("menus.crafts", "value: %s", true);
        valkiriaInfo = c.getOrSetString("menus.valkirias_info", "value: %s", true);
        findBoxFormat = c.getOrSetString("main.findBoxFormat", "value: %s", true);
        spawnBoxFormat = c.getOrSetString("main.spawnBoxFormat", "value: %s", true);
        valkiriaPoints = c.getOrSetString("menus.valkirias_points", "value: %s", true);
        valkiriaWeapons = c.getOrSetString("menus.valkirias_weapons", "value: %s", true);
        valkiriaBackPack = c.getOrSetString("menus.valkirias_backpack", "value: %s", true);
        valkiriaArtifacts = c.getOrSetString("menus.valkirias_artifacts", "value: %s", true);
        valkitiaUpgradeArts = c.getOrSetString("menus.valkirias_upgradearts", "value: %s", true);
        valkiriaGetArtifact = c.getOrSetString("menus.valkirias_getartifact", "value: %s", true);
        advantureItemLoreFormat = c.getOrSetString("main.adventureitemformat", "value: %s", true);
        advantureItemLoreFormat4 = c.getOrSetString("main.adventureitemformat4", "value: %s", true);
        advantureItemLoreFormat56 = c.getOrSetString("main.adventureitemformat56", "value: %s", true);

        nickFormat = c.getOrSetString("chat.nickformat", "value: %s %s %s", true);
        loreFormat = c.getOrSetString("chat.icoformat", "format")
                .replace('+','\n').replace('&', '\u00A7');

        Global_pefix = ChatHelper.Parse(c.getStringList("chat.globalchat"))[0];
        Local_pefix = ChatHelper.Parse(c.getStringList("chat.loacalchat"))[0];
        SenderPrivate_pefix = ChatHelper.Parse(c.getOrSetStringList("chat.senderprivateprefix", em))[0];
        TargetPrivate_pefix = ChatHelper.Parse(c.getOrSetStringList("chat.targetprivateprefix", em))[0];

        critPoints = generateMenuItem("menus.crit_points");
        freePoints = generateMenuItem("menus.free_points");
        attackPoints = generateMenuItem("menus.attack_points");
        energyPoints = generateMenuItem("menus.energy_points");
        shieldPoints = generateMenuItem("menus.shield_points");
        artifactsGUI = generateMenuItem("menus.artifacts_gui");

        battlePassNormalLoreFormat = BasicSector.parseColor(
                String.join("\n", c.getOrSetStringList("menus.battle_pass_lore",
                Arrays.asList("%count% / %max%", "%sec%, %min%, %hor%, %day%")
        )));
        battlePassMaximumFormat = BasicSector.parseColor(
                String.join("\n", c.getOrSetStringList("menus.battle_max_lore",
                Arrays.asList("%count% / %max%")
        )));

        partyMaxSize = c.getOrSetInt("main.max_party_size", 5);
        battlePassCmd = c.getOrSetInt("menus.battle_pass_cmd", 1);
        artifactsButtonCmd = c.getOrSetInt("menus.artifacts_button_cmd", 1);
        battlePassEmptyCmd = c.getOrSetInt("menus.battle_pass_empty_cmd", 1);

        helpAvatar = ChatHelper.Parse(c.getOrSetStringList("messages.helpavatar", em));
        empetyChest = ChatHelper.Parse(c.getOrSetStringList("messages.empetychest", em));
        emptyReward = ChatHelper.Parse(c.getOrSetStringList("messages.empty_reward", em));
        messageMuted = ChatHelper.Parse(c.getOrSetStringList("messages.youmoted", em));
        messageNoPerms = ChatHelper.Parse(c.getOrSetStringList("messages.noperms", em));
        messageHasRewards = ChatHelper.Parse(c.getOrSetStringList("messages.has_rewards", em));
        messageColorEdited = ChatHelper.Parse(c.getOrSetStringList("messages.color_edited", em));
        messageExistsWeapon = ChatHelper.Parse(c.getOrSetStringList("messages.exsistweapon", em));
        messageDisableCombat = ChatHelper.Parse(c.getOrSetStringList("messages.disablecombat", em));
        messagePlayerOffline = ChatHelper.Parse(c.getOrSetStringList("messages.playerofline", em));
        messageNextUsageKill = ChatHelper.Parse(c.getOrSetStringList("messages.notenergy", em));
        messageOnlyPremium = ChatHelper.Parse(c.getOrSetStringList("messages.only_premium",
                Collections.singletonList("allowed only for premium!")));

        messagePartyKick = ChatHelper.Parse(c.getOrSetStringList("messages.party_kick", em));
        messagePartyJoinUser = ChatHelper.Parse(c.getOrSetStringList("messages.party_join", em));
        messagePartyAttemptToJoinUser = ChatHelper.Parse(c.getOrSetStringList("messages.party_attempt_to_join", em));
        messagePartyCreated = ChatHelper.Parse(c.getOrSetStringList("messages.party_created", em));
        messagePartyNotCreated = ChatHelper.Parse(c.getOrSetStringList("messages.party_not_created", em));
        partyFullFormat = ChatHelper.Parse(c.getOrSetStringList("messages.party_full", em));
        messagePartyYouNotOwner = ChatHelper.Parse(c.getOrSetStringList("messages.party_not_owner", em));
        messagePartyRedyJoinded = ChatHelper.Parse(c.getOrSetStringList("messages.party_redy_joined", em));
        messagePartyIsNull = ChatHelper.Parse(c.getOrSetStringList("messages.party_is_null", em));
        messagePartyNotExist = ChatHelper.Parse(c.getOrSetStringList("messages.party_not_exist", em));
        messagePartyInviteIsNull = ChatHelper.Parse(c.getOrSetStringList("messages.party_invite_is_null", em));
        messagePartyPlayerNotExist = ChatHelper.Parse(c.getOrSetStringList("messages.party_player_is_not_exist", em));
        messagePartyNotRemoved = ChatHelper.Parse(c.getOrSetStringList("messages.party_not_removed", em));
        messagePartyRemoved = ChatHelper.Parse(c.getOrSetStringList("messages.party_removed", em));
        messagePartyOtherParty = ChatHelper.Parse(c.getOrSetStringList("messages.party_other_party", em));
        messagePartyInParty = ChatHelper.Parse(c.getOrSetStringList("messages.party_in_party", em));

        partyJoinConfirmFormat = c.getOrSetString("message_formats.party_join_confirm", "value: %s", true);
        partyInviteConfirmFormat = c.getOrSetString("message_formats.party_invite_confirm", "value: %s", true);
        //partyInviteFormat = c.getOrSetString("message_formats.party_invite", "value: %s", true);
        partyMemberJoinFormat = c.getOrSetString("message_formats.party_member_join", "value: %s", true);
        partyMemberLeaveFormat = c.getOrSetString("message_formats.party_member_leave", "value: %s", true);
        partyMemberKickedFormat = c.getOrSetString("message_formats.party_kicked", "value: %s", true);

        lvlUpFormat = c.getOrSetString("message_formats.lvl_up", "You up %s lvl", true);
        statistickIco = c.getOrSetString("menus.stats_bg", "value: %s", true);
        stListClasses = c.getString("menus.classlist");
        stClassConfirm = c.getString("menus.classconfirm");
        dropExpFormat = c.getOrSetString("message_formats.drop_exp", "You get %s exp", true);
        fullShield_Format = c.getOrSetString("message_formats.fullshield", "value: %s", true);
        statistickIcoMain = c.getOrSetString("menus.stats_bg_main", "value: %s", true);
        regenShield_Format = c.getOrSetString("message_formats.regenshield", "value: %s", true);
        emptyStation_Format = c.getOrSetString("message_formats.empetystation", "value: %s", true);

        nullSlot = generateMenuItem("menus.nullobject");
        valkiriaBg = generateMenuItem("menus.valkirias_bg");
        boostPoints = generateMenuItem("menus.points_bg");
        oneClasssBg =  generateMenuItem("menus.oneclass_bg");
        folderButton = generateMenuItem("menus.floader_button");
        upgradeArtsBG = generateMenuItem("menus.upgradearts_bg");
        valkiriaWeapon = generateMenuItem("menus.valkirias_weapons_bg");
        betterCraftGuiNull = generateMenuItem("menus.bettercraft_null");
        betterCraftGuiRecpt = generateMenuItem("menus.bettercraft_recept");

        upgradeArtsNull = upgradeArtsBG.clone();
        ItemMeta m = upgradeArtsNull.getItemMeta();
        m.setCustomModelData(c.getInt("menus.nullobject.cmd"));
        upgradeArtsNull.setItemMeta(m);



        listClasses = Bukkit.createInventory(null,45, stListClasses);
        listClasses.setItem(36, generateMenuItem("menus.classlist_bg"));
    }


    public static String getConfigStrValue(String key){
        String result;
        result = c.getString(key);//.replace('&', '\u00A7');
        return result;
    }


    public static TextComponent getTextComponent(String patch){
        return ChatHelper.Parse(c.getStringList(patch))[0];
    }


    public static ItemStack generateMenuItem(String patch){
        return generateMenuItem(patch, Material.DIAMOND);
    }


    public static ItemStack generateMenuItem(String patch, Material material){
        return generateMenuItem(c, patch, material);
    }


    public static ItemStack generateMenuItem(Sector c, String patch, Material material){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setCustomModelData(c.getInt(patch+".cmd"));

        List<String> lores = c.getStringList(patch + ".lore");
        for(int i = 0;i<lores.size();i++){
            lores.set(i, BasicSector.parseColor(lores.get(i)));
        }
        if(!lores.isEmpty()){
            meta.setDisplayName(lores.get(0));
            lores.remove(0);
            meta.setLore(lores);

        } else {
            meta.setDisplayName(" ");
        }
        item.setItemMeta(meta);
        return item;
    }
}