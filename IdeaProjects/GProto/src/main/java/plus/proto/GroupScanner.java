package plus.proto;

import com.google.protobuf.InvalidProtocolBufferException;
import plus.proto.tparsers.IntConsumer;
import plus.proto.tparsers.IntFuncExecutor;
import plus.proto.tparsers.ReadFuncRegistry;
import java.io.File;
import java.util.*;
import java.util.function.Function;


public class GroupScanner {
    private final String root;
    private Set<String> exclude = Collections.EMPTY_SET;
    private EPack pack;

    public GroupScanner(String root){
        this.root = root;

        addExclude("actionnpc");
        addExclude("aiconfig");
    }


    public String getRoot(){
        return root;
    }


    public GroupScanner addExclude(String str){
        if(exclude == Collections.EMPTY_SET){
            exclude = new HashSet<>();
        }
        exclude.add(str);
        return this;
    }


    public GroupScanner readPack(String path){
        this.pack = new EPack();
        try {
            pack.read(new File(path), false);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        return this;
    }


    public EPack getPack(){
        return pack;
    }


    public GroupScanner scanUsages(){
        for (File file:new DeepFileIter(new File(this.root))){
            if(file == null)continue;
            if(exclude.contains(file.getName()))continue;
            if(!file.getName().endsWith(".bytes"))continue;
            try {
                scanFile(file);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return this;
    }


    private void scanFile(File file) throws Exception {
        String groupName = file.getName().toLowerCase();
        String finalGroupName = groupName.substring(0, groupName.length()-10);

        if(exclude.contains(finalGroupName))return;

        IntConsumer func = new IntConsumer() {
            int index = 0;
            public void setIndex(int index) {
                this.index = index;
            }
            public int index() {
                return index;
            }
            public String gefGroup() {
                return finalGroupName;
            }
            public void apply(String group, int id) {
                ELine line = pack.getLineById(id);
                if (line != null) {
                    line.viewSortIdx = index;
                    line.classes.add(group);
                }
                index += 1;
            }
        };

        IntFuncExecutor readFunc = ReadFuncRegistry.create(finalGroupName);
        readFunc.readFrom(file);
        readFunc.apply(func);
    }


    private static final String[] subGroups = new String[]{
            "darkzone", "skill", "mail", "weapon", "activity", "achievement", "battle",
            "collection", "cloth", "control", "dungeon", "guide", "guild", "item", "kittycafe", "tpss",
            "unlock", "weekly", "stsp", "sim", "rogue", "rich", "player", "shortcut", "store", "warmup",
            "nrt", "love", "handmode", "enemy", "event", "festival", "information", "language", "rouge",
            "supply", "bp", "element", "difficulty", "tutorial", "stage", "amoactivity", "back", "candycatch",
            "character", "combat", "command", "mod", "chapter", "idcard"
    };
    private static final String[] shortSubGroups = new String[]{
            "monopoly", "sandbox", "repository", "handmade", "highpvp", "squad", "story",
            "mngame", "mnshoot",
    };

    public String getClazzName(ELine line){
        String clazz = line.getPriorityClazz();

        if(clazz.startsWith("gun")){
            return "weapon/"+substrSmart(clazz, 3);
        }
        if(clazz.startsWith("dz")){
            return "darkzone/"+substrSmart(clazz, 2);
        }

        for (String sub : subGroups){
            if(clazz.startsWith(sub))
                return sub+"/"+substrSmart(clazz, sub.length());
        }

        for (String sub : shortSubGroups){
            if(clazz.startsWith(sub))
                return sub;
        }

        return clazz;
    }


    private static String substrSmart(String str, int start){
        int len = str.length();
        if(start >= len)return "common";
        return str.substring(start);
    }


    public void export(String exportRoot, boolean writeId){
        HashMap<String, ArrayList<ELine>> groups = new HashMap<>();
        int size = pack.size();

        for (int i = 0; i < size; i++){
            ELine line = pack.getLineByIndex(i);
            String clazz = getClazzName(line);
            ArrayList<ELine> gr = groups.computeIfAbsent(clazz, NEW_LIST);
            gr.add(line);
        }

        groups.forEach((name, eLines) -> {
            eLines.sort(E_LINE_COMPARATOR);
            JsonUtils.writeFile(exportRoot+"/"+name+".json", new ELineSaveIter(eLines, writeId), JsonUtils.Type.UTF8);
        });
    }


    private class ELineSaveIter implements Iterable<Map.Entry<String, String>>, Map.Entry<String, String>, Iterator<Map.Entry<String, String>>{
        private final ArrayList<ELine> eLines;
        private ELine current, next;
        int index = 0;
        private final boolean writeId;

        private ELineSaveIter(ArrayList<ELine> eLines, boolean writeId) {
            this.eLines = eLines;
            this.writeId = writeId;
            if(!eLines.isEmpty()) {
                current = next = eLines.get(0);
                index = 1;
            }
        }


        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return this;
        }


        @Override
        public String getKey() {
            if(writeId){
//                LangPackV2.Line[] lines = pack.getLines(current.src);
//                if(lines != null && lines.length != 1)
                    return "#ID:"+current.id+"|"+current.src;
            }
            return current.src;
        }


        @Override
        public String getValue() {
            return current.getActual();
        }


        @Override
        public String setValue(String value) {
            return current.actual = value;
        }


        @Override
        public boolean hasNext() {
            return next != null;
        }


        @Override
        public Map.Entry<String, String> next() {
            if(current == null)return null;
            current = next;
            if(eLines.size() > index) {
                next = eLines.get(index);
                ++index;
            } else {
                next = null;
            }
            return this;
        }
    }


    private static final Function<String, ArrayList<ELine>> NEW_LIST = k -> new ArrayList<>();


    public static class DeepFileIter implements Iterator<File>, Iterable<File>{
        Iterator<File> current = null;
        final File[] list;
        int index = 0;

        public DeepFileIter(File file) {
            if(file.isDirectory()){
                list = file.listFiles();
            } else {
                list = new File[]{file};
            }
        }

        public DeepFileIter(File[] list) {
            this.list = list;
        }


        @Override
        public boolean hasNext() {
            return current != null && current.hasNext() || index < list.length;
        }


        @Override
        public File next() {
            if(current != null){
                if(current.hasNext()){
                    return current.next();
                }
            }
            if(list.length == 0)return null;
            File cur = list[index];
            ++index;
            if(cur.isDirectory()){
                current = new DeepFileIter(cur);
                return current.next();
            }
            return cur;
        }


        @Override
        public Iterator<File> iterator() {
            return this;
        }
    }


    public static final Comparator<ELine> E_LINE_COMPARATOR = (o1, o2) -> Integer.compare(o1.viewSortIdx, o2.viewSortIdx);


    public static class ELine extends LangPackV2.Line{
        public final LinkedList<String> classes = new LinkedList<>();
        public int viewSortIdx = 0;

        public ELine(int id, int index, String src) {
            super(id, index, src);
        }

        public void addClazz(String clz){
            if(classes.contains(clz))return;
            classes.add(clz);
        }

        public String getPriorityClazz(){
            if(classes.isEmpty())return "default";
            return classes.get(0);
        }
    }


    public static class EPack extends LangPackV2<ELine>{
        @Override
        public ELine createLine(int id, int index, String src) {
            return new ELine(id, index, src);
        }
    }
}