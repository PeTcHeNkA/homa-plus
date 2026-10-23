package plus.Character;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;

import com.ticxo.modelengine.api.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class ModelEngineCharacter implements Character{

    private final boolean noDamage;
    private final String Name;
    private final String modelId;
    private UUID id;

    public ModelEngineCharacter(String model, String name, boolean noDamage) {

        this.modelId = model;
        this.Name = name;
        this.noDamage = noDamage;

    }

    public int spawn(Location loc) {
        ArmorStand bukkitTarget = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        id = bukkitTarget.getUniqueId();
        if(noDamage){
            bukkitTarget.setNoDamageTicks(-1);
        }


        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(id);
        String modelId = this.modelId;

        //ConfigManager.AnimationMode mode = ConfigManager.AnimationMode.get("animation");

        if (ModelEngineAPI.api.getModelBlueprint(modelId) == null)
            return -1;
        if (modeledEntity == null)
            modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(bukkitTarget);


        ActiveModel model = ModelEngineAPI.api.getModelManager().createActiveModel(modelId);
        model.setDamageTint(noDamage);

        float width = modeledEntity.getWidth(), height = modeledEntity.getHeight(), eye = modeledEntity.getEye();

        modeledEntity.addActiveModel(model);
        modeledEntity.overrideHitbox(width, height, eye);
        modeledEntity.detectPlayers();


        if (Name != null) {
            bukkitTarget.setCustomName(Name);
            modeledEntity.setNametag(Name);
            modeledEntity.setNametagVisible(true);
        }

        modeledEntity.getMountHandler().setSteerable(false);
        modeledEntity.getMountHandler().setCanCarryPassenger(false);
        modeledEntity.setInvisible(true);

        modeledEntity.lockPitch(false);//check me!
        modeledEntity.lockYaw(false);

        return bukkitTarget.getEntityId();
    }

    @Override
    public void playAnimation(String animation) {
        playAnimation(animation, 1.0);
    }

    @Override
    public void playAnimation(String animation, double speed) {
        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(id);
        if (modeledEntity == null) return;

        ActiveModel model = modeledEntity.getActiveModel(modelId);

        if (speed < 0) {
            model.removeState(animation, true);
        } else {
            model.addState(animation, 0, 1, speed);
        }
    }
    @Override
    public void remove(){
        Bukkit.getEntity(id).remove();
    }
}