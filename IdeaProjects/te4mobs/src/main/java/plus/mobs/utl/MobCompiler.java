package plus.mobs.utl;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModClassLoader;
import javax.tools.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import static java.util.Collections.singletonList;


public class MobCompiler {
    public Class<?> load(Path path, String name){
        try {

            ModClassLoader loader = Loader.instance().getModClassLoader();
            loader.addFile(path.toFile());
            return loader.loadClass("exmob."+name);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    public Path compile(String className, String code){
        SimpleJavaFileObject javObj = new SimpleJavaFileObject(
                URI.create("string:///" + className.replace('.', '/') +
                        JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE) {

            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return code;
            }
        };
        return compile(javObj);
    }


    public Path compile(SimpleJavaFileObject javaObj){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File file = new File("temp");
        if(!file.exists()){
            file.mkdirs();
        }
        Path output = file.toPath();

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, null,null,
                Arrays.asList("-d", output.toAbsolutePath().toString()),
                null, singletonList(javaObj)
        );

        boolean result = task.call();
        if (!result) throw new RuntimeException("Compilation error!");
        return output;
    }
}