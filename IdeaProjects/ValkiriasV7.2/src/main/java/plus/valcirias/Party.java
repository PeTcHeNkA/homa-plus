package plus.valcirias;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import plus.util.ConfigHelper;
import java.util.HashMap;
import java.util.Set;


public class Party {

    private static final HashMap<String, HashMap<String, JoinRequest>> joinRequests = new HashMap<>();
    private final Valkiria owner;
    private final HashMap<String, Valkiria> members = new HashMap<>();

    public Party(Valkiria owner) {
        this.owner = owner;
        owner.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyCreated);
        joinRequests.put(owner.toPlayerName(), new HashMap<>());
    }


    public static class JoinRequest{
        public final Party party;
        public final Valkiria user;
        public final Valkiria confirmer;

        public JoinRequest(Party party, Valkiria user, Valkiria confirmer){
            this.party = party;
            this.user = user;
            this.confirmer = confirmer;
            TextComponent message = new TextComponent();
            if(party.isOwner(confirmer)) {
                message.setText(
                    String.format(ConfigHelper.partyJoinConfirmFormat, user.toPlayerName()));
                message.setClickEvent(
                        new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/party confirm "+user.toPlayerName()));
            } else {
                message.setText(
                        String.format(ConfigHelper.partyInviteConfirmFormat, party.getOwner().toPlayerName()));
                message.setClickEvent(
                        new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/party confirm "+party.getOwner().toPlayerName()));
            }
            confirmer.toPlayer().spigot().sendMessage(message);
        }


        public void confirm(Valkiria confirmer){
            if(this.confirmer != confirmer){
                confirmer.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyYouNotOwner);
                return;
            }
            if(party.members.size()<ConfigHelper.partyMaxSize) {
                String message = String.format(ConfigHelper.partyMemberJoinFormat, user.toPlayerName());

                party.owner.toPlayer().sendMessage(message);

                for(Valkiria v:party.members.values()){
                    v.toPlayer().sendMessage(message);
                }
                joinRequests.get(party.owner.toPlayerName())
                        .remove(user.toPlayerName()).party.members.put(user.toPlayerName(), user);
                user.setParty(party);
                user.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyJoinUser);
            } else {
                confirmer.toPlayer().spigot().sendMessage(ConfigHelper.partyFullFormat);
            }
        }
    }


    public boolean isOwner(Valkiria valkiria){
        return owner==valkiria;
    }


    public Valkiria getOwner(){
        return owner;
    }


    public boolean isParty(String player){
        return members.containsKey(player) || owner.toPlayerName().equals(player);
    }


    public boolean isParty(Valkiria v){
        return isParty(v.toPlayerName());
    }


    public boolean isParty(Player player){
        return isParty(player.getName());
    }


    public void attemptToJoin(Valkiria valkiria){
        valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyAttemptToJoinUser);
        if(valkiria.getParty()!=null) valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyRedyJoinded);
        else joinRequests.get(owner.toPlayerName()).put(valkiria.toPlayerName(), new JoinRequest(this, valkiria, owner));
    }


    public void invite(Valkiria valkiria){
        if(valkiria.getParty()!=null) {
            if(isParty(valkiria)){
                owner.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyInParty);
            } else owner.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyOtherParty);
        } else {
            owner.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyAttemptToJoinUser);
            joinRequests.get(owner.toPlayerName()).put(valkiria.toPlayerName(), new JoinRequest(this, valkiria, valkiria));
        }
    }


    public void kickUser(Valkiria confirmer, Valkiria player){
        if(owner != confirmer){
            confirmer.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyYouNotOwner);
            return;
        }
        Valkiria user = members.remove(player.toPlayerName());
        if(user==null){
            confirmer.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyPlayerNotExist);
            return;
        }
        user.setParty(null);
        if(user.toPlayer().isOnline()){
            user.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyKick);
        }
        String message = String.format(ConfigHelper.partyMemberKickedFormat, user.toPlayerName());
        confirmer.toPlayer().sendMessage(message);

        for(Valkiria member:members.values()){
            member.toPlayer().sendMessage(message);
        }
    }


    public void leave(Valkiria valkiria){
        if(owner == valkiria){
            valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyNotRemoved);
            return;
        }
        members.remove(valkiria.toPlayerName());
        String message = String.format(ConfigHelper.partyMemberLeaveFormat, valkiria.toPlayerName());
        owner.toPlayer().sendMessage(message);

        for(Valkiria member:members.values()){
            member.toPlayer().sendMessage(message);
        }
        valkiria.setParty(null);
    }


    public void remove(Valkiria confirmer){
        if(owner != confirmer){
            confirmer.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyYouNotOwner);
            return;
        }
        owner.setParty(null);
        owner.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyRemoved);
        for(Valkiria member:members.values()){
            member.setParty(null);
            member.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyRemoved);
        }
        joinRequests.remove(owner.toPlayerName());
    }


    public static void confirmJoin(String ownerPartyName, Valkiria confirmer, String user){
        JoinRequest request = joinRequests.get(ownerPartyName).get(user);
        if(request==null){
            confirmer.toPlayer().spigot().sendMessage(ConfigHelper.messagePartyInviteIsNull);
            return;
        }
        request.confirm(confirmer);
    }


    public Set<String> getMemberList(){
        return members.keySet();
    }
}