package plus.fserver.world.addon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;


public class LobbyTeleporter implements ITeleporter {
    private Vec3d lobby = new Vec3d(0,70,0);
    private int distance;
    private GameType gameType;

    public void init(Vec3d lobby, int distance, GameType gameType){
        this.lobby = lobby;
        this.distance = distance;
        this.gameType = gameType;
    }


    public void readFrom(NBTTagCompound nbt){
        NBTTagList pos = nbt.getTagList("lobby", 6);
        GameType gameType = null;
        if(nbt.hasKey("gm")){
            gameType = GameType.getByID(nbt.getInteger("gm"));
        }
        init(
                new Vec3d(pos.getDoubleAt(0), pos.getDoubleAt(1), pos.getDoubleAt(2)),
                nbt.getInteger("distance"), gameType
        );
    }


    @Override
    public void placeEntity(final World world, final Entity entity, final float yaw) {
        entity.rotationYaw = entity.prevRotationYaw = yaw;
        if(entity instanceof EntityPlayerMP){
            EntityPlayerMP player = (EntityPlayerMP) entity;
            if(gameType != null)
                player.setGameType(gameType);
            Vec3d vec = findRandomNearSpawnPos(world);
            player.setPosition(vec.x, vec.y, vec.z);
        }
    }


    private Vec3d findRandomNearSpawnPos(final World world){
        if(distance > 0){
            double dx = world.rand.nextDouble()*distance;
            double dz = world.rand.nextDouble()*distance;
            Vec3d vec = lobby.add(dx, 0, dz);
            BlockPos pos1 = new BlockPos(vec);
            pos1 = world.getChunk(pos1).getPrecipitationHeight(pos1);

            double minY = lobby.y - distance;
            double maxY = lobby.z + distance;
            if(pos1.getY() <= minY && pos1.getY() <= maxY){
                return vec;
            }
        }
        return lobby;
    }


    public NBTTagCompound toNbt(){
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList pos = new NBTTagList(3);
        pos.set(0, new NBTTagDouble(lobby.x));
        pos.set(1, new NBTTagDouble(lobby.y));
        pos.set(2, new NBTTagDouble(lobby.z));
        nbt.setTag("lobby", pos);
        if(gameType != null)
            nbt.setIntegerU("gm", gameType.getID());
        nbt.setIntegerU("distance", distance);
        return nbt;
    }
}