package plus.mcapi;

import plus.te4net.core.auth.User;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class McUser extends User {
    public final String uuid;

    public McUser(String login, String password, String name, String uuid, byte[] data) {
        super(login, password, name, data);
        this.uuid = uuid;
    }


    public static String toBase64(String src){
        return new String(Base64.getEncoder().encode(src.getBytes(StandardCharsets.UTF_8)),StandardCharsets.UTF_8);
    }


    public TsonMap properties(){
        TsonObj obj = get("properties");
        if(obj==null) put("properties", obj = new TsonMap());
        return obj.getMap();
    }


    public TsonObj textures(){
        TsonMap pps = properties();
        TsonObj obj = pps.get("textures");
        if(obj==null) pps.put("textures", obj = new TsonMap());
        return obj;
    }


    private TsonMap getTextures(){
        TsonMap map = new TsonMap();
        map.put("textures", textures());
        map.put("signatureRequired", false);
        map.put("profileName", name);
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        map.put("profileId", uuid);
        return map;
    }


    public void setSkin(String url, boolean slim){
        textures().getMap().put("SKIN", newTex(url, slim));
    }


    private TsonMap newTex(String url, boolean slim){
        TsonMap texture = new TsonMap();
        texture.put("url", url);
        if(slim){
            TsonMap metadata = new TsonMap();
            metadata.put("model", "slim");
            texture.put("metadata", metadata);
        }
        return texture;
    }


    public String getSkinUrl(){
        TsonObj skin = textures().getMap().get("SKIN");
        if(skin==null)return null;
        return skin.getMap().getStr("url");
    }


    public String toGameProfile(){
        TsonMap map = new TsonMap();
        map.put("id", uuid);
        map.put("name", name);
        TsonMap props;
        map.put("properties", props = properties());
        TsonList txList = new TsonList();
        txList.add(toBase64(getTextures().toJsonObj()));
        props.put("textures", txList);

        return map.toJsonObj();
    }
}