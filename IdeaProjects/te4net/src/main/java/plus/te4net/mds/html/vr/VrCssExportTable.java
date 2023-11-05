package plus.te4net.mds.html.vr;

import plus.te4net.mds.util.CharNum;
import java.util.HashMap;
import java.util.Map;


public class VrCssExportTable {
    private final HashMap<String,String> data = new HashMap<>();
    private int num = 0;

    public void addAll(HashMap<String,String> table){
        data.putAll(table);
    }


    //TODO
    public void addAll(String data){
        String[] lines = data.split(";");
        for (String line:lines){
            String[] kv = line.split(":");
            this.data.put(kv[0],kv[1]);
        }
    }


    //TODO
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String,String>etr:data.entrySet()){
            b.append(etr.getKey()).append(':').append(etr.getValue()).append(';');
        }
        b.setLength(b.length()-1);
        return b.toString();
    }


    public String getOrNew(String orig){
        String res = data.get(orig);
        if(res!=null)return res;
        do {
            res = "_" + CharNum.Default.getCharNumOf(num);
            if (res.length() >= orig.length()) return res;
            ++num;
        } while (data.containsKey(res));
        data.put(orig, res);
        return res;
    }


    public String get(String orig){
        return data.getOrDefault(orig, orig);
    }
}