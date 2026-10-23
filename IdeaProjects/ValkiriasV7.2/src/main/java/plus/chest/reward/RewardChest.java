package plus.chest.reward;

import plus.Main;
import plus.chest.SimpleChest;
import plus.valcirias.Valkiria;
import plus.util.cfg.Config;


public class RewardChest extends SimpleChest implements RewardBox{

    private final String server;

    public RewardChest(Config c, String name, boolean isPath) {
        super(c, name, isPath);
        String s = c.getOrSetString("reward_boxes." + name + ".server", "none");
        server = s.equalsIgnoreCase("none")?c.getOrSetString("server_to_redirect", "main"):s;
    }

    public RewardChest(Config c, String name) {
        this(c, name, false);
    }


    @Override
    public void reward(Valkiria v) {
        String codeItems = Main.floader.bd.itemCoder.codeItems(super.dropContent());

        String s1 = v.getReward();
        if(s1 == null){
            v.setReward(codeItems);
        } else {
            if(s1.charAt(s1.length()-1)==';'){
                s1 = s1+codeItems;
            } else {
                s1 = s1+";"+codeItems;
            }
            v.setReward(s1);
        }
    }


    @Override
    public String getRedirectServer() {
        return server;
    }
}
