package plus.quest.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.BukkitEntity;
import com.ticxo.modelengine.model.MEModeledEntity;
import com.ticxo.modelengine.model.active.MEActiveModel;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import plus.Main;

import java.util.*;


public class Te4ModelledEntity extends MEModeledEntity {

    private final MEActiveModel m;
    private static final UIDLogger logger = new UIDLogger();
    protected final ArmorStand e;


    protected static ArmorStand buildEntity(Location loc){
        ArmorStand e = (ArmorStand) loc.getWorld()
                .spawnEntity(loc, EntityType.ARMOR_STAND);
        e.setMarker(true);
        logger.write(e.getUniqueId());
        return e;
    }


    public Te4ModelledEntity(ArmorStand e, String modelId) {
        super(new BukkitEntity(e));
        this.e = e;
        m = new MEActiveModel(modelId);
        this.addActiveModelOnInit(m);
        //System.out.println("nen1");
    }


    @Override
    public boolean tick(){
        return true;
    }


    public void parentTick(){
        super.tick();
    }


    @Override
    public double getValue(int ix, String[] id){
        return 0;
    }


    @Override
    public void setNametagVisible(boolean bool){}


    @Override
    public void setNametag(String name){}


    @Override
    public void detectPlayers(){}


    @Override
    public void addPlayer(Player var1){}


    public void addToParentPlayer(Player var1){super.addPlayer(var1);}


    @Override
    public boolean addActiveModel(ActiveModel model) {return true;}


    private void addActiveModelOnInit(ActiveModel model) {

        model.setModeledEntity(this);
        model.generatePartEntities();
        this.models.put(model.getModelId(), model);
        ModelBlueprint blueprint = model.getBlueprint();
        this.width = blueprint.getBoundingBoxWidth();
        this.height = blueprint.getBoundingBoxHeight();
        this.eye = blueprint.getEyeHeight();
        this.getMountHandler().setDriverSeat(model.getMountEntity());
        this.getMountHandler().setPassengerSeats(model.getPassengerEntities());
        this.getEntity().setEntitySize(this.width, this.height, this.eye);

    }


    public void playAnimation(String animation, int in, int out, double speed){
        m.addState(animation, in, out, speed);
    }


    public void remove(){
        UUID id = super.entity.getUniqueId();
        ModelEngineAPI.api.getModelManager().removeModeledEntity(id);
        new BukkitRunnable() {
            @Override
            public void run() {
                e.remove();
            }
        }.runTaskLater(Main.instance, 0);
        logger.remove(id);
    }
}