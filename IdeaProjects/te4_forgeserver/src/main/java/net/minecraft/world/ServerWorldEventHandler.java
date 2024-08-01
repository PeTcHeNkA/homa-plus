package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;


public class ServerWorldEventHandler implements IWorldEventListener {
    private final MinecraftServer server;
    private final WorldServer world;

    public ServerWorldEventHandler(MinecraftServer mcServerIn, WorldServer worldServerIn) {
        this.server = mcServerIn;
        this.world = worldServerIn;
    }


    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}


    public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}


    @Override
    public void onEntityAdded(Entity entityIn) {
        this.world.getEntityTracker().track(entityIn);
        if (entityIn instanceof EntityPlayerMP) {
            this.world.provider.onPlayerAdded((EntityPlayerMP)entityIn);
        }
    }


    @Override
    public void onEntityRemoved(Entity entityIn) {
        this.world.getEntityTracker().untrack(entityIn);
        this.world.getScoreboard().removeEntity(entityIn);

        if (entityIn instanceof EntityPlayerMP) {
            this.world.provider.onPlayerRemoved((EntityPlayerMP)entityIn);
        }
    }


    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        this.server.getPlayerList().sendToAllNearExcept(player, x, y, z, volume > 1.0F ? (double)(16.0F * volume) : 16.0D, this.world.provider.getDimension(), new SPacketSoundEffect(soundIn, category, x, y, z, volume, pitch));
    }


    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}


    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        this.world.getPlayerChunkMap().markBlockForUpdate(pos);
    }


    @Override
    public void notifyLightSet(BlockPos pos) {}


    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {}


    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
        this.server.getPlayerList().sendToAllNearExcept(player, blockPosIn.getX(), blockPosIn.getY(), blockPosIn.getZ(), 64.0D, this.world.provider.getDimension(), new SPacketEffect(type, blockPosIn, data, false));
    }


    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {
        this.server.getPlayerList().sendPacketToAllPlayersE(new SPacketEffect(soundID, pos, data, true));
    }


    @Override //redirected to player.sendBlockBreakProgressToTracking()
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        SPacketBlockBreakAnim packet = null;
        for (EntityPlayer player : world.playerEntities) {
            if (player != null && player.getEntityId() != breakerId && player instanceof EntityPlayerMP) {
                double d0 = (double)pos.getX() - player.posX;
                double d1 = (double)pos.getY() - player.posY;
                double d2 = (double)pos.getZ() - player.posZ;

                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
                    if(packet == null)packet = new SPacketBlockBreakAnim(breakerId, pos, progress);
                    ((EntityPlayerMP) player).connection.netManager.sendPacketE(packet);
                }
            }
        }
    }
}