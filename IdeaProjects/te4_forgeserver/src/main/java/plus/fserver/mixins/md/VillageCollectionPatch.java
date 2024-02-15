package plus.fserver.mixins.md;

import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;


@Mixin(VillageCollection.class)
public abstract class VillageCollectionPatch extends WorldSavedData {
    @Shadow private int tickCounter;
    @Shadow @Final private List<Village> villageList;

    public VillageCollectionPatch(String name) {
        super(name);
    }

    @Shadow protected abstract void removeAnnihilatedVillages();
    @Shadow protected abstract void dropOldestVillagerPosition();
    @Shadow protected abstract void addNewDoorsToVillageOrCreateVillage();

    /**
     * @author Te4hno
     * @reason Speedy
     */
    @Overwrite
    public void tick() {
        ++this.tickCounter;
        for (Village village : this.villageList) {
            village.tick(this.tickCounter);
        }
        this.removeAnnihilatedVillages();

        if(this.tickCounter%30==0)
            this.dropOldestVillagerPosition();

        this.addNewDoorsToVillageOrCreateVillage();

        if (this.tickCounter % 400 == 0)
            this.markDirty();
    }
}
