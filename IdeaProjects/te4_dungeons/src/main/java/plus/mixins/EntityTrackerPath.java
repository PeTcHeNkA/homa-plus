package plus.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.gen.utl.Buffer;
import java.util.Set;


@Mixin(EntityTracker.class)
public class EntityTrackerPath {

    @Final
    @Shadow
    private Set<EntityTrackerEntry> entries;
    @Final
    @Shadow
    private WorldServer world;

    private void test(){
        //EntityPlayer
        //net.minecraft.client.gui.
    }


    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        ci.cancel();
        Buffer<EntityPlayerMP> buffer = new Buffer<>();

        EntityTrackerEntry[] entries0 = entries.toArray(new EntityTrackerEntry[0]);

        for (EntityTrackerEntry entry : entries0) {
            entry.updatePlayerList(world.playerEntities);

            if (entry.playerEntitiesUpdated) {
                Entity entity = entry.getTrackedEntity();

                if (entity instanceof EntityPlayerMP) {
                    buffer.put((EntityPlayerMP)entity);
                }
            }
        }

        while (buffer.hasNext()) {
            EntityPlayerMP entityplayermp = buffer.flush();
            for (EntityTrackerEntry tracker : entries0) {
                if (tracker.getTrackedEntity() != entityplayermp) {
                    tracker.updatePlayerEntity(entityplayermp);
                }
            }
        }
    }


    @Inject(method = "sendLeashedEntitiesInChunk", at = @At("HEAD"), cancellable = true)
    public void sendLeashedEntitiesInChunk(EntityPlayerMP player, Chunk chunkIn, CallbackInfo ci) {
        ci.cancel();
        EntityTrackerEntry[] entries0 = entries.toArray(new EntityTrackerEntry[0]);
        Buffer<Entity> buffer = new Buffer<>();
        Buffer<Entity> buffer1 = new Buffer<>();

        for (EntityTrackerEntry entitytrackerentry : entries0) {
            Entity entity = entitytrackerentry.getTrackedEntity();

            if (entity != player && entity.chunkCoordX == chunkIn.x && entity.chunkCoordZ == chunkIn.z) {
                entitytrackerentry.updatePlayerEntity(player);

                if (entity instanceof EntityLiving && ((EntityLiving)entity).getLeashHolder() != null) {
                    buffer.put(entity);
                }

                if (!entity.getPassengers().isEmpty()) {
                    buffer1.put(entity);
                }
            }
        }

        while (buffer.hasNext()){
            Entity entity1 = buffer.flush();
            player.connection.sendPacket(new SPacketEntityAttach(entity1,
                    ((EntityLiving)entity1).getLeashHolder()));

        }
        while (buffer1.hasNext()){
            Entity entity1 = buffer1.flush();
            player.connection.sendPacket(new SPacketSetPassengers(entity1));
        }
    }
}