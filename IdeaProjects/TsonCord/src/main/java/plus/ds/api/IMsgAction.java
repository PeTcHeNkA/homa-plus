package plus.ds.api;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public interface IMsgAction {
    void apply(MessageCreateBuilder msfBuilder, IMessageEvent event);

    static IMsgAction build(TsonMap map){
        TsonObj tp0 = map.get("type");
        if (tp0 == null || !tp0.isString()) throw new RuntimeException();
        String type = tp0.getStr();
        String suffix;
        if(type.contains(":")){
            int i = type.indexOf(":");
            type = type.substring(0,i);
            suffix = type.substring(i);
        } else {
            suffix = null;
        }
        if(suffix!=null) {
            switch (type) {
                case "user":
                    return buildUser(map, suffix);
            }
        }
        return null;
    }


    static IMsgAction buildUser(TsonMap map, String type){
        boolean mtd = true;
        switch (type){
            case "rem_role":
                mtd = false;
            case "set_role":
            case "role":
                final boolean finalMtd = mtd;
                new IMsgAction() {
                    private final long ID = Long.parseLong(map.getStr("id"));
                    @Override
                    public void apply(MessageCreateBuilder msfBuilder, IMessageEvent event) {
                        Guild guild = event.getChannel().asGuildMessageChannel().getGuild();
                        Role role = guild.getRoleById(ID);
                        if(finalMtd) {
                            guild.addRoleToMember(event.getMessage().getAuthor(), role);
                        } else {
                            guild.removeRoleFromMember(event.getMessage().getAuthor(), role);
                        }
                    }
                };
        }
        return null;
    }
}






