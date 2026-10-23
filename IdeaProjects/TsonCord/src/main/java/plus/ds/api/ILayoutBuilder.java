package plus.ds.api;

import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import plus.ds.IrisSystem;
import plus.ds.msg.menu.MenuController;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


@FunctionalInterface
public interface ILayoutBuilder extends IMsgAction {
    LayoutComponent toLayout(IMessageEvent event);

    @Override
    default void apply(MessageCreateBuilder msfBuilder, IMessageEvent event) {
        msfBuilder.addOneComponent(toLayout(event));
    }


    static ILayoutBuilder build(IrisSystem.GuildProgram system, TsonObj obj){
        if(obj.isMap()) {
            TsonMap map = obj.getMap();
            TsonObj tp0 = map.get("type");
            if (tp0 == null || !tp0.isString()) throw new RuntimeException();
            String type = tp0.getStr();
            switch (type) {
                case "menu":
                case "sel-menu":
                case "selected_menu":
                    return MenuController.build(system, map);
                case "plugin":
                case "plug":
                    try {
                        return (ILayoutBuilder) map.getField("layout");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return event -> null;
                    }
            }
        } else if(obj.isCustom()){
            return (ILayoutBuilder) obj.getField();
        }
        System.out.println("LAYOUT TYPE NOT EXIST! ("+obj+")");
        return event -> null;
    }
}
