package plus.util.Chat;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class DynamicJsonMessage {

    public TextComponent[] format;

    public List<Integer> markers1 = new ArrayList<>();
    public List<Integer> markers2 = new ArrayList<>();

    public DynamicJsonMessage(List<String> format){
        this.format = ChatParser.Parse(format);

        int index = 0;
        for(TextComponent c:this.format){
            if(c.getText().contains("%s")){
                markers1.add(index);

            }
            if(c.getClickEvent() != null) {
                if (c.getClickEvent().getValue().contains("%s")) {
                    markers2.add(index);
                }
            }
            index+=1;
        }
    }

    public TextComponent[] toMessage(Object... args){
        TextComponent[] s = format.clone();
        try {
            int sellectedArg = 0;
            for(int i:markers1){
                s[i].setText(String.format(String.format(  s[i].getText(), args[sellectedArg].toString()  )));
                sellectedArg+=1;
            }
            for(int i:markers2){
                s[i].setClickEvent(
                        new ClickEvent(s[i].getClickEvent().getAction(), s[i].getClickEvent().getValue())
                );
                sellectedArg+=1;
            }
            return s;
        } catch (Exception e){
            return s;
        }
    }
}
