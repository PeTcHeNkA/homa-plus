package plus.mobs.frame;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import plus.mobs.Te4mobsMain;
import plus.tson.TsonMap;
import static plus.mobs.utl.TsonMobComp.getOrDef;


public class EntityAttributeApplicator implements AbcMob.IEntityApplicator {

    private final double followRange;
    private final double attack;
    private final double armor;
    //private final double attackSpeed;
    private final double knockResist;
    private final double maxHealth;
    private final double speed;


    public EntityAttributeApplicator(TsonMap map){
        followRange = getOrDef(map, "follow-range",20   );
        attack      = getOrDef(map, "attack"      ,4    );
        //attackSpeed = getOrDef(map, "attack-speed",1    );
        armor       = getOrDef(map, "armor"       ,2    );
        knockResist = getOrDef(map, "knock-resist",0    );
        maxHealth   = getOrDef(map, "max-hp"      ,35   );
        speed       = getOrDef(map, "speed"       ,0.23 );
    }


    @Override
    public void apply(AbcMob mob) {
        appAttr(mob, SharedMonsterAttributes.FOLLOW_RANGE        , followRange);
        appAttr(mob, SharedMonsterAttributes.ATTACK_DAMAGE       , attack     );
        //appAttr(mob, SharedMonsterAttributes.ATTACK_SPEED        , attackSpeed);
        appAttr(mob, SharedMonsterAttributes.ARMOR               , armor      );
        appAttr(mob, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, knockResist);
        appAttr(mob, SharedMonsterAttributes.MAX_HEALTH          , maxHealth  );
        appAttr(mob, SharedMonsterAttributes.MOVEMENT_SPEED      , speed      );
    }


    private void appAttr(AbcMob mob, IAttribute attr, double val){
        if(Te4mobsMain.PROD==true){
            mob.getEntityAttribute(attr).setBaseValue(val);
        } else {
            IAttributeInstance attr0 = mob.getEntityAttribute(attr);
            if (attr0 != null) {
                attr0.setBaseValue(val);
            } else {
                System.out.printf("Mob [%s]: Attribute %s not exist!%n", mob, attr.getName());
            }
        }
    }
}