package plus.common.spship.entity;

import plus.client.ISpREntity;


public interface ILivingBlock extends ISpREntity {
    default LivingBlock.Mem newMem(){return null;}
    default void onClientUpdate(){}
    default void onClientInit(){}
}