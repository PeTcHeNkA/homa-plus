package plus.entity;


import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.BukkitEntity;
import com.ticxo.modelengine.model.MEModeledEntity;
import com.ticxo.modelengine.model.active.MEActiveModel;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class Te4ModelledEntity extends MEModeledEntity {


    public Te4ModelledEntity(Entity entity, String modelId) {
        super(new BukkitEntity(entity));
        this.addActiveModel(new MEActiveModel(modelId));
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

        //model.addState(defaultState, 0, 0, 1);
    }


    public void playAnimation(Player player){
        parentTick();
        super.addPlayer(player);
        parentTick();
        super.removePlayer(player);

        ModelEngineAPI.api.getModelManager().removeModeledEntity(super.entity.getUniqueId());
    }
}