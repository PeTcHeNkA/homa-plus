package asm;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import plus.fserver.api.inject.FieldAccessor;


public class TestAsm implements FieldAccessor<Entity,World> {
    public String field;

    public World get(Entity entity){
        return entity.world;
    }

    public void set(Entity entity, World world){
        entity.world = world;
    }
}