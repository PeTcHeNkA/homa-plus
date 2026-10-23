package plus.quest.memory;

import java.util.HashMap;


public class QuestMemory {

    private final HashMap<String, AbstractMemorySector> memory = new HashMap<>();


    public QuestMemory(){}
    public QuestMemory(String data){
        if(data.equals("empty"))return;
        try {
            for (String dataLine : data.split(",")) {
                memory.put(
                        dataLine.substring(0, dataLine.indexOf("=")),
                        encodeLine(dataLine.substring(dataLine.indexOf("=") + 1))
                );
            }
        } catch (Exception e){
            System.out.println("| Error in memory encode! "+data+"\n| "+e.getMessage());
        }
    }


    public void addMemorySection(String key, AbstractMemorySector sector){
        memory.put(key, sector);
    }


    public void addAll(QuestMemory data){
        for(String key:data.Sections().keySet()){
            memory.put(key, data.getSection(key));
        }
    }


    public AbstractMemorySector getSection(String key){
        return memory.get(key);
    }


    public HashMap<String, AbstractMemorySector> Sections(){
        return memory;
    }


    public String toString(){
        if(memory.isEmpty())return "empty";
        StringBuilder s = new StringBuilder();
        for(String key:memory.keySet()){
            Object o = memory.get(key).getValue();
            s.append(key).append("=").append(parseClass(o.getClass())).append(":").append(o.toString()).append(",");
        }
        return s.toString();
    }


    public void clear(){
        memory.clear();
    }


    public static String parseClass(Class clazz){
        String value = clazz.getSimpleName().toLowerCase();
        return switch (value) {
            case "integer" -> "int";
            case "boolean" -> "bool";
            default -> "str";
        };
    }


    public static AbstractMemorySector encodeLine(String data){
        return parseMemory(
                data.substring(0, data.indexOf(":")),
                data.substring(data.indexOf(":")+1)
        );
    }


    public static AbstractMemorySector parseMemory(String type, String value){
        type = type.toLowerCase();
        switch (type){
            case "int":return new MemorySector<>(value, Integer::parseInt);
            case "bool":new MemorySector<>(value, data -> data.equalsIgnoreCase("true"));
            default: return new MemorySector<>(value);
        }
    }
}