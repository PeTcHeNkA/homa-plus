package asm;

import net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer;
import plus.tson.TsonFile;

import java.io.File;

public class TestEVENT {
    public static void main(String[] args) {
        String name = "net.minecraftforge.event.entity.EntityJoinWorldEvent";
        String path = "build/classes/java/main/"+name.replace('.','/')+".class";
        //String path = "build/classes/java/main/net/minecraftforge/event/entity/EntityJoinWorldEvent.class";


        EventSubscriptionTransformer tr = new EventSubscriptionTransformer();

        byte[] bytes = TsonFile.readFile(new File(path));

        byte[] result = tr.transform(name, name, bytes);

        TsonFile.write(new File("EntityJoinWorldEvent.class"), result);
    }
}
