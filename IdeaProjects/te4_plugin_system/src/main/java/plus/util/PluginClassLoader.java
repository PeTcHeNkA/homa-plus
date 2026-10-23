package plus.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


public class PluginClassLoader extends URLClassLoader {
    private final String path;
    public PluginClassLoader(File... files) throws MalformedURLException {
        super(toUrls(files), PluginClassLoader.class.getClassLoader());
        if(files.length>0) {
            path = files[0].getPath() + "_/";
        } else {
            path = "";
        }
    }


    private static URL[] toUrls(File... files) throws MalformedURLException {
        URL[] urls = new URL[files.length];
        for(int i=0;i< urls.length;i++){
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }


    public static String getSrcPath(Class c){
        ClassLoader cl = c.getClassLoader();
        if(cl instanceof PluginClassLoader){
            return ((PluginClassLoader) cl).path;
        }
        return "";
    }
}