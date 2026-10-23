package plus.fserver.plugin;

import com.google.common.io.*;
import java.util.*;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import plus.tson.utl.uns.UnsafeUtils;


public class TweakAccessTransformer extends AccessTransformer {
    public TweakAccessTransformer() throws Exception {
        final Class<?> clazz = Class.forName("net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer");

        long off = UnsafeUtils.UNSAFE.staticFieldOffset(clazz.getDeclaredField("embedded"));
        Map<String, String> embedded = UnsafeUtils.get(clazz, off);

        for (final Map.Entry<String, String> e : embedded.entrySet()) {
            this.processATFile(CharSource.wrap(e.getValue()));
        }
    }


    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
//        if(transformedName.equals("net.minecraft.server.MinecraftServer")){
//            //FMLDeobfuscatingRemapper.INSTANCE.mapFieldName()
//            //serverThread
//        }
        return super.transform(name, transformedName, bytes);
    }
}