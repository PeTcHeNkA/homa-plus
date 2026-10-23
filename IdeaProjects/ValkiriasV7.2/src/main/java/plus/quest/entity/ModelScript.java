package plus.quest.entity;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;
import plus.util.cfg.BasicSector;


public abstract class ModelScript {

    protected static ModelScript build(String data, ActorScript s){
        String[] args = data.split(":");
        switch (args[0]){
            case "anim":
                String[] d2 = args[1].replace(" ", "").split(";");

                return new ModelScript() {
                    private final String animation = d2[0];
                    private final int in = Integer.parseInt(d2[1]);
                    private final int out = Integer.parseInt(d2[2]);
                    private final double speed = Double.parseDouble(d2[3]);
                    @Override
                    public void run(Te4ModelledEntity ent) {
                        ent.playAnimation(animation, in, out, speed);
                    }
            };
            case "sleep":
                return new ModelScript() {
                    private final int count = (Integer.parseInt(args[1].strip())/s.ticRate)+1;
                    private final int time = s.ticRate;
                    @Override
                    public void run(Te4ModelledEntity ent) {
                        try {
                            for(int i=0;i<count;i++) {
                                ent.parentTick();
                                Thread.sleep(time);
                            }
                        } catch (InterruptedException e) { e.printStackTrace(); }
                    }
                };
            case "move":
                return new ModelScript() {
                    private final Location loc = BasicSector.encodeLoc(args[1].strip());
                    @Override
                    public void run(Te4ModelledEntity/*extends MEModeledEntity*/ ent) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                ent.e.teleport(loc);
                            }
                        }.runTaskLater(Main.instance, 0);
                    }
                };
        }

        return null;
    }
    public abstract void run(Te4ModelledEntity ent);
}