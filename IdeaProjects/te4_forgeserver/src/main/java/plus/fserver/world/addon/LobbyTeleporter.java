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
import plus.fserver.utls.McTsonUtils;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.util.function.Consumer;


public class LobbyTeleporter implements ITeleporter {
    private Vec3d lobby = new Vec3d(0,70,0);
    private int distance;
    private GameType gameType;
    private float yaw = Float.NaN, pith = Float.NaN;

    public void init(Vec3d lobby, int distance, GameType gameType){
        this.lobby = lobby;
        this.distance = distance;
        this.gameType = gameType;
    }

    public void setRotation(float yaw, float pith){
        this.yaw  = yaw;
        this.pith = pith;
    }


    public void readFrom(TsonMap map){
        lobby = McTsonUtils.getVec3d(map.get("pos"));
        if(map.containsKey("yaw"))
            yaw = map.getFloat("yaw");
        if(map.containsKey("yaw"))
            pith = map.getFloat("pith");
        distance = map.getOrDefaultInt("distance", 0);
        if(map.containsKey("game-type"))
            gameType = McTsonUtils.getGameType(map.get("game-type"));
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
        if(nbt.hasKey("yaw")) yaw  = nbt.getFloat("yaw");
        if(nbt.hasKey("pith"))pith = nbt.getFloat("pith");
    }


    @Override
    public void placeEntity(final World world, final Entity entity, float yaw) {
        float yaw0 = this.yaw, pith = this.pith;
        if(!Float.isNaN(yaw0))yaw = yaw0;
        if(!Float.isNaN(pith))
            entity.rotationPitch = entity.prevRotationPitch = pith;

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

        if(!Float.isNaN(yaw))  nbt.setFloat("yaw", yaw);
        if(!Float.isNaN(pith)) nbt.setFloat("pith", pith);
        return nbt;
    }
}