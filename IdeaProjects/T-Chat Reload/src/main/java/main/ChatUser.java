package main;

//import net.md_5.bungee.api.chat.hover.content.Content;
import org.bukkit.entity.Player;

public class ChatUser {

    private final String player;
    private String Name;
    private String NickInfo;

    private String lore;
    private String textprefix;
    private String color;
    private String LastPrivateMessageTarget = null;
    private String Linc;

    private int avatarID = 4097;

    boolean hasPrefix = false;
    boolean hasLorePrefix = false;
    boolean hasLinc = false;

    public ChatUser(String name){
        this.player = name;
        this.Name = name;
        this.NickInfo = String.valueOf((char )avatarID)+" "+Name+TChatReload.NickLoreFormat;
    }
    //public Player toPlayer(){
    //    return player;
    //}
    public String toPlayerName(){
        return player;
    }
    public String getTextprefix(){
        return textprefix;
    }
    public String getLorePrefix(){
        return lore;
    }
    public String getColor(){
        return color;
    }
    public int getAvatarID(){
        return avatarID;
    }
    public String getName(){
        return Name;
    }
    public String getNickInfo(){
        return NickInfo;
    }

    public void setPrefix(String textprefix, String lore){
        this.hasPrefix = true;
        this.textprefix = textprefix.replace('&', '\u00A7');

        if(lore != null && !lore.equals("null")) {
            this.hasLorePrefix = true;
            this.lore = PrefixLoreBuilder(lore);
        }
            //prefix.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(lore)));
    }
    public void setPrefix(String textprefix){
        if(textprefix.equals("null")){
            return;
        }
        this.hasPrefix = true;
        this.textprefix = textprefix.replace('&', '\u00A7');
        //this.NickInfo = String.valueOf((char )avatarID)+" "+Name+TChatReload.NickLoreFormat;
    }

    public void setColor(String color){
        this.color = color.replace('&', '\u00A7');
        this.Name = this.color+player;
        this.NickInfo = String.valueOf((char )avatarID)+" "+Name+TChatReload.NickLoreFormat;

    }
    public void ClearPrefix(){
        hasPrefix = false;
        hasLorePrefix = false;
        textprefix = null;
        lore = null;
    }

    public void setAvatar(int avatar){
        //System.out.println(avatarID);
        this.avatarID = avatar;
        //String format = TChatReload.NickLoreFormat;
        this.NickInfo = String.valueOf((char )avatarID)+" "+Name+TChatReload.NickLoreFormat;
    }
    //public void setAvatarAsDedault(int avatar){
    //    //System.out.println(avatar);
    //    this.avatarID = avatar;
    //    //String format = TChatReload.NickLoreFormat;
    //    this.NickInfo = String.valueOf((char )avatar)+" "+Name+TChatReload.NickLoreFormat;
    //}
    public String PrefixLoreBuilder(String rawplorerefix){
        return rawplorerefix.replace('+', '\n').replace('&', '\u00A7').replace('_', ' ');
    }

    public String getLastMessageTarget() {
        return LastPrivateMessageTarget;
    }

    public void setLastMessageTarget(String lastPrivateMessageTarget) {
        LastPrivateMessageTarget = lastPrivateMessageTarget;
    }

    public void setLinc(String linc) {
        if(linc == null){
            hasLinc = false;
            Linc = null;
        } else {
            hasLinc = true;
            Linc = linc;
        }
    }

    public String getLinc() {
        return Linc;
    }
}
