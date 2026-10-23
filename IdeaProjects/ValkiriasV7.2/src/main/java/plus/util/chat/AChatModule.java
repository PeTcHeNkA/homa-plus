package plus.util.chat;

import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.chat.TextComponent;

public class AChatModule {

    private List<String> format;
    private String label;

    public AChatModule(String label, List<String> s){
        format = s;
    }
    public List<TextComponent> applyFormat(String label, String value){
        List<String> aLabels = Arrays.asList(label.split(";"));
        
        String wLabel = label;
        
        while (wLabel.contains("$")){
            if(!aLabels.isEmpty()) {
                wLabel = wLabel.replaceFirst("$", aLabels.get(0));
                aLabels.remove(0);
            }
        }

        TextComponent t = new TextComponent(wLabel);
        //"String.format()".re
        return null;
    }
}
