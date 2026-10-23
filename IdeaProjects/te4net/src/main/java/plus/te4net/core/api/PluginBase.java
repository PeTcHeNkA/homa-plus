package plus.te4net.core.api;

import plus.tson.TsonMap;
import plus.util.PluginClassLoader;
import plus.util.SimpleSrcGetter;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@PublicUsage
public interface PluginBase extends SimpleSrcGetter {
    default TsonMap getTson(String name){
        return new TsonMap(new String(getSrc(name)));
    }


    default File getSrcDir(){
        return new File(PluginClassLoader.getSrcPath(this.getClass()));
    }


    default boolean isRoot(){
        return false;
    }


}
