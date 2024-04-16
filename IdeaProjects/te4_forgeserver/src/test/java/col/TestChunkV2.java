package col;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import plus.fserver.utls.debug.DebugEntity;

import java.util.ArrayList;
import java.util.Iterator;


public class TestChunkV2 {

    public static void main(String[] args) {
        ChunkSlimMobContainer mobs = new ChunkSlimMobContainer();

        mobs.addEntity(new DebugEntity("A1").setPos(10,7,10));
        mobs.addEntity(new DebugEntity("AX").setPos(3,3,3));
        mobs.addEntity(new DebugEntity("A2").setPos(10,15,10));
        mobs.addEntity(new DebugEntity("A3").setPos(10,63,10));
        mobs.addEntity(new DebugEntity("A4").setPos(10,65,10));
        mobs.addEntity(new DebugEntity("A5").setPos(10,120,10));

        ArrayList<Entity> list = new ArrayList<>();

        mobs.getEntitiesWithinAABB(null,
                new AxisAlignedBB(2,0,2, 15, 30, 15),
                list, null
        );

//        for (Entity entity:list){
//            System.out.println(entity);
//        }

        Iterator<ClassInheritanceMultiMap<Entity>> iter = mobs.getEntityLists();
        while (iter.hasNext()){
            ClassInheritanceMultiMap<Entity> res = iter.next();
            //System.out.println(res);
            for (Entity entity:res){
                System.out.println(entity);
            }
        }

    }


}
