package plus.chest.reward;

import plus.Main;
import plus.valcirias.Valkiria;


public interface RewardBox {

    void reward(Valkiria v);
    String getRedirectServer();

    default void rewardS(Valkiria v){
        reward(v);
        if(getRedirectServer() != null) {
            Main.floader.bd.Redirect(v.toPlayer().getName(), getRedirectServer());
        }
    }
}