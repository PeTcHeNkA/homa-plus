package plus.proto;

import com.google.protobuf.InvalidProtocolBufferException;
import java.io.File;
import java.util.*;
import java.util.function.Function;


public class GroupScanner {
    private final String root;
    private Set<String> exclude = Collections.EMPTY_SET;
    private EPack pack;

    public GroupScanner(String root){
        this.root = root;
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

        UPack pack1 = new UPack(file);
        pack1.tryFindStrIDS(new UPack.IntConsumer() {
            int index = 0;
            @Override
            public void apply(int id) {
                ELine line = pack.getLineById(id);
                if(line != null){
                    line.viewSortIdx = index;
                    line.classes.add(
                            finalGroupName);
                }
                index += 1;
            }
        });
    }


    public void export(String exportRoot){
        HashMap<String, ArrayList<ELine>> groups = new HashMap<>();
        int size = pack.size();

        for (int i = 0; i < size; i++){
            ELine line = pack.getLineByIndex(i);
            String clazz = line.getPriorityClazz();
            ArrayList<ELine> gr = groups.computeIfAbsent(clazz, NEW_LIST);
            gr.add(line);
        }

        groups.forEach((name, eLines) -> {
            eLines.sort(E_LINE_COMPARATOR);
            JsonUtils.writeFile(exportRoot+"/"+name+".json", new ELineSaveIter(eLines), JsonUtils.Type.UTF8);
        });
    }


    private static class ELineSaveIter implements Iterable<Map.Entry<String, String>>, Map.Entry<String, String>, Iterator<Map.Entry<String, String>>{
        private final ArrayList<ELine> eLines;
        private ELine current, next;
        int index = 0;

        private ELineSaveIter(ArrayList<ELine> eLines) {
            this.eLines = eLines;
            if(!eLines.isEmpty()) {
                current = eLines.get(0);
                if (eLines.size() >= 2)
                    next = eLines.get(1);
                index = 1;
            }
        }


        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return this;
        }


        @Override
        public String getKey() {
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
