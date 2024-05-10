package net.minecraftforge.fml.common.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;
import plus.fserver.world.SpawnEntityFactory;
import java.util.function.Function;


public class EntityEntry extends Impl<EntityEntry> {
    private Class<? extends Entity> cls;
    private String name;
    private EntityEggInfo egg;
    Function<World, ? extends Entity> factory;

    public EntityEntry(Class<? extends Entity> cls, String name) {
        this.cls = cls;
        this.name = name;
        init();
    }


    protected void init() {
        factory = SpawnEntityFactory.make(cls);
    }


    public Class<? extends Entity> getEntityClass(){
        return this.cls;
    }


    public String getName(){
        return this.name;
    }


    public EntityEggInfo getEgg(){
        return this.egg;
    }


    public void setEgg(EntityEggInfo egg) {
        this.egg = egg;
        if (this.getRegistryName() != null)
            EntityList.ENTITY_EGGS.put(this.getRegistryName(), egg);
    }


    public Entity newInstance(World world) {
        return this.factory.apply(world);
    }
}
