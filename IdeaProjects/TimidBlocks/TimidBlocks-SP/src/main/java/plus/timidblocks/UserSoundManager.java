package plus.timidblocks;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.vosk.Model;
import org.vosk.Recognizer;
import plus.tson.TsonMap;
import plus.tson.utl.Te4HashMap;
import plus.utl.audio.AIAnswer;
import plus.utl.collections.CircularArrayQueue;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


/**
 * Подсистема управления аудио-потоками и распознования сказанных блоков
 * @author HomaPlus
 */
public class UserSoundManager {
    private final ThreadPoolExecutor pool;
    private final TimidBlocks plugin;
    private final Te4HashMap<UUID, Session>  sessions = new Te4HashMap<>();
    private final int wordBuf;
    private final int rate;
    private final Model model;
    private Te4HashMap<String, Collection<Material>> wordData = new Te4HashMap<>();

    public UserSoundManager(TimidBlocks plugin, String modelPath, int wordBuf, int rate, int threads, int maxThreads) {
        this.plugin = plugin;

        File file0 = new File(plugin.getDataFolder(), modelPath);

        if(!file0.isDirectory()) {
            throw  new IllegalArgumentException("The model path isn't a directory!");
        }
        if(!file0.exists()) {
            try {
                file0.mkdirs();
            } catch (Exception ex) {}
        }
        try {
            this.model = new Model(file0.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.wordBuf = wordBuf;
        this.rate = rate;
        this.pool = new ThreadPoolExecutor(threads = Math.max(1, threads), Math.max(threads, maxThreads), 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }


    /**
     * Загружает карту материалов на ключевые слова, потокобезопасно
     */
    public void loadWordData(TsonMap map){
        if(map == null) return;
        Te4HashMap<String, Collection<Material>> wordData = new Te4HashMap<>();
        map.forEach((key, value) -> {
            Material material = Material.getMaterial(key.toUpperCase());
            if(material == null || material == Material.AIR){
                plugin.getLogger().warning("[TimidBlocks] Could not find material for key: " + key);
            } else {
                if(value.isString()){
                    for (String keyWord: value.getStr().split(";")) {
                        add(wordData, material, keyWord);
                    }
                }
            }
        });
        this.wordData = wordData;
    }


    private static void add(Te4HashMap<String, Collection<Material>> wordData, Material material, String keyWord){
        Collection<Material> materials = wordData.get(keyWord);
        if(materials == null){
            materials = Collections.singleton(material);
            wordData.put(keyWord, materials);
        } else {
            if(materials.size() == 1){
                Collection<Material> materialsNew = new ArrayList<>(2);
                materialsNew.addAll(materials);
                materialsNew.add(material);

                materials = materialsNew;

                wordData.put(keyWord, materials);
                return;
            }
            if(materials.size() == 4){
                Collection<Material> materialsNew = EnumSet.noneOf(Material.class);

                materialsNew.addAll(materials);
                materialsNew.add(material);

                materials = materialsNew;

                wordData.put(keyWord, materials);
                return;
            }
            materials.add(material);
        }
    }


    /**
     * Аудио-сессия конкретного игрока
     * Использует нативные указатели, использовать с осторожностью, чтобы не допустить утечек памяти
     */
    private static class Session {
        final Recognizer recognizer;
        final CircularArrayQueue<String> words;
        final UUID uuid;
        boolean locked = false, alive = true;

        Session(UUID uuid, Model model, int wordBuf, int rate) throws IOException {
            this.uuid = uuid;
            recognizer = new Recognizer(model, rate);
            //recognizer.setPartialWords(true);
            words = new CircularArrayQueue<>(wordBuf);
        }


        /**
         * @return используется ли сессия прямо сейчас?
         */
        public boolean isLocked() {
            return locked;
        }


        public void handleData(UserSoundManager manager, byte[] buf) {
            if(!alive) return;
            synchronized (this) {
                locked = true;

                Recognizer recognizer;
                AIAnswer aiAnswer;

                if ((recognizer = this.recognizer).acceptWaveForm(buf, buf.length)) {
                    aiAnswer = AIAnswer.getResultRaw(recognizer.getResult());
                } else {
                    aiAnswer = AIAnswer.getResultRaw(recognizer.getPartialResult());
                }

                {
                    CircularArrayQueue<String> words;
                    String result;
                    boolean reset;

                    if(aiAnswer.partial != null && !aiAnswer.partial.isEmpty()){
                        words = new CircularArrayQueue<>(0);
                        this.words.copyTo(words);
                        result = aiAnswer.partial;
                        reset = false;
                    } else {
                        words = this.words;
                        result = aiAnswer.text;
                        reset = true;
                    }

                    if (result != null && !result.isEmpty()) {
                        if(manager.plugin.isDebug()){
                            String name;
                            Player player = manager.plugin.getServer().getPlayer(uuid);
                            if(player != null){
                                name = player.getName();
                            } else {
                                name = uuid.toString();
                            }
                            String prefix = aiAnswer.partial == null ? "S" : "P";

                            manager.plugin.getLogger().warning("User ["+name+"] say ("+prefix+"): ["+result+"]");
                        }

                        String[] words2 = result.split(" ");
                        int count = 0;

                        StringBuilder sb = null;

                        while (count < words2.length) {
                            if(sb == null){
                                sb = new StringBuilder();
                            } else {
                                sb.setLength(0);
                            }
//                            int lim = manager.wordQueue;
//                            while (lim-- > 0)
                            words.add(words2[count++]);

                            if(processWords(manager, sb, words)){
                                reset = true;
                                words.clear();
                            }
                        }
                        if(reset){
                            recognizer.reset();
                        }
                    }
                }
                locked = false;
            }
        }


        private boolean processWords(UserSoundManager manager, StringBuilder builder, CircularArrayQueue<String> list) {
            boolean flag = false;
            final int mMax = list.size();
            for (int min = 0; min < mMax; min++) {
                int curFWords = -1;
                for (int max = mMax; max > min; max--) {
                    int curWords = 0;
                    for (int index = min; index < max; index++) {
                        String s = list.getAtHead(index);
                        if(s != null){
                            builder.append(s).append(' ');
                            curWords++;
                        } else break;
                    }
                    if(curWords == curFWords){
                        builder.setLength(0);
                        continue;
                    } else {
                        curFWords = curWords;
                    }

                    if(builder.length() != 0){
                        builder.setLength(builder.length() - 1);

                        flag |= processWord(manager, builder.toString());
                        builder.setLength(0);
                    }
                }
            }
            return flag;
        }


        private boolean processWord(UserSoundManager manager, String key) {
            Collection<Material> blocks = manager.wordData.get(key);
            if (blocks != null) {
                manager.onSayBlock(uuid, blocks);
                return true;
            }
            return false;
        }


        /**
         * Закрывает сессию и освобождает нативные указатели
         */
        public void close(){
            synchronized (this) {
                locked = true;
                recognizer.close();
                locked = false;
                alive = false;
            }
        }
    }


    /**
     * Отслеживание аудио-данных от игрока
     */
    public void onSound(Player player, byte[] sound) {
        UUID uuid;
        Session session = sessions.get(uuid = player.getUniqueId());
        if(session == null){
            try {
                sessions.fput(uuid, session = new Session(uuid, model, wordBuf, rate));
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(session != null) {
            Session finalSession = session;
            pool.execute(() -> finalSession.handleData(UserSoundManager.this, sound));
        }
    }


    /**f
     * Вызывается, когда игрок с указанным uuid "говорит" блок
     */
    public void onSayBlock(UUID plUUID, Collection<Material> blocks) {
        Player player = Bukkit.getPlayer(plUUID);
        if(player == null){
            plugin.getLogger().warning("Player ["+plUUID+"] is not online]");
            return;
        }
        if(plugin.isDebug()){
            plugin.getLogger().warning("User ["+player.getName()+"] say block: ["+getName(blocks)+"], start removing at ["+player.getChunk().getX()+":"+player.getChunk().getZ()+"]");
        }

        if(plugin.isAllPlayers()){
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for(Player p : new ArrayList<>(Bukkit.getOnlinePlayers())){
                    remBlocksForPlayer(p, blocks);
                }
            });
        } else {
            remBlocksForPlayer(player, blocks);
        }
    }


    private static String getName(Collection<Material> materials){
        if(materials == null || materials.isEmpty()) return "NULL/AIR";
        if(materials.size() == 1){
            return materials.iterator().next().name();
        }
        StringBuilder sb = new StringBuilder();
        for(Material material : materials){
            sb.append(material.name()).append(';');
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }


    private void remBlocksForPlayer(Player player, Collection<Material> blocks){
        int range = plugin.getRemRange();
        if(range == 0){
            clearBlocksInChunk(player, blocks);
        } else {
            clearBlocksInRange(player, blocks, range);
        }
    }


    /**
     * Очищает все сессии
     */
    public void clearAll() {
        new ArrayList<>(sessions.keySet()).forEach(this::clearSession);
    }


    /**
     * Очищает сессию для конкретного игрока
     */
    public void clearSession(UUID uuid){
        Session session = sessions.remove(uuid);

        if(session != null){
            if(session.isLocked())
                pool.execute(session::close);
            else
                session.close();
        }
    }


    /**
     * Быстро очищает указанный блок в чанке, где находится игрок
     * (1 этап - создать снапшот и поставить в очередь)
     */
    protected void clearBlocksInChunk(Player player, Collection<Material> block){
        Bukkit.getScheduler().runTask(plugin, () -> {
            Chunk chunk = player.getChunk();
            ChunkSnapshot snap0 = chunk.getChunkSnapshot(false, false, false);

            pool.execute(() -> clearBlocksInChunkSub(chunk, snap0, block));
        });
    }


    protected void clearBlocksInRange(Player player, Collection<Material> blocks, int range){
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location loc = player.getLocation();

            int deltaMin = range >> 1;

            int minX = loc.getBlockX() - deltaMin;
            int minZ = loc.getBlockZ() - deltaMin;

            int maxX = minX + range + 16;
            int maxZ = minZ + range + 16;

            World world = player.getWorld();

            for (int x = minX; x <= maxX; x += 16) {
                for (int z = minZ; z <= maxZ; z += 16) {
                    pool.execute(new RemTask(
                            world.getChunkAt((x >> 4), (z >> 4)), blocks,
                            minX, minZ, maxX - 16, maxZ - 16
                    ));
                }
            }
        });
    }


    public ThreadPoolExecutor getPool(){
        return pool;
    }


    private class RemTask implements Runnable {
        private final Chunk chunk;
        private final ChunkSnapshot snapshot;
        private final Collection<Material> blocks;
        private final int minX, minZ, maxX, maxZ;

        private RemTask(Chunk chunk, Collection<Material> blocks, int minX, int minZ, int maxX, int maxZ) {
            this.chunk = chunk;
            this.snapshot = chunk.getChunkSnapshot(false, false, false);
            this.blocks = blocks;
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
        }

        @Override
        public void run() {
            clearBlocksInRange(chunk, snapshot, blocks, minX, minZ, maxX, maxZ);
        }
    }


//    private static int calcMaxH(Chunk chunk, ChunkSnapshot snap, Material[] blocks){
//        int maxH =  chunk.getWorld().getMaxHeight();
//        int lim = maxH;
//
////        BlockData[] data = new BlockData[blocks.length];
////        for(int i = 0; i < blocks.length; i++){
////            Material mat = blocks[i];
////            if(!mat.isLegacy())return lim;
////            if(mat.getData() != MaterialData.class)return lim;
////
////            data[i] = mat.createBlockData();
////        }
////        //Material.SANDSTONE_STAIRS.
////
////        for(int y = maxH>>4; y < 0; y--){
////            boolean hasANy = false;
////            for(BlockData bd : data){
////                if(snap.contains(bd)){
////                    hasANy = true;
////                }
////            }
////            if(!hasANy){
////                lim = (y * 16) + 16;
////                break;
////            }
////        }
//        return lim;
//    }


    protected void clearBlocksInRange(Chunk chunk, ChunkSnapshot snap, Collection<Material> blocks, int minX, int minZ, int maxX, int maxZ){
        int min = chunk.getWorld().getMinHeight(); //todo check it maybe dont exists

        CordList list = new CordList();

        int lim = chunk.getWorld().getMaxHeight(); //todo maybe faster?
        //int lim = calcMaxH(chunk, snap, blocks);



            for (int x = 0; x < 16; x++){
                if(!isBetween(minX, maxX, (snap.getX() * 16) + x))continue;

                for (int z = 0; z < 16; z++){
                    if(!isBetween(minZ, maxZ, (snap.getZ() * 16) + z))continue;

                    for (int y = min; y < lim; y++){
                        if (blocks.contains(snap.getBlockType(x, y, z)))
                            list.add(x, y, z);
                    }
                }
            }


        plugin.getServer().getScheduler().runTask(plugin, () -> {
            list.forEach((x, y, z) -> chunk.getBlock(x, y, z).setType(Material.AIR));
        });
    }


    private static boolean isBetween(int min, int max, int val){
        return val >= min && val <= max;
    }


    /**
     * Быстро очищает указанный блок в чанке, где находится игрок
     * (2 этап - обход снапшота и применение изменений
     */
    protected void clearBlocksInChunkSub(Chunk chunk, ChunkSnapshot snap, Collection<Material> blocks){
        clearBlocksInRange(chunk, snap, blocks, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
//
//
//    private static class MatList{
//        private Material[] blocks;
//    }


    /**
     * Быстрый и компактный список координат блоков в диапазоне (x 0-15, y +-32767, z 0-15)
     * Х: 32-24 (1 байт )
     * Y: 0-16  (2 байта)
     * Z: 16-14 (1 байт )
     */
    public static class CordList extends CordNode {
        CordNode current = this;

        /**
         * Добавить координаты в список
         */
        public void add(int x, int y, int z){
            int num = ((x & 15) << 24) | ((z & 15) << 16) | (((y & 0xFFFF) + 0x7FFF) & 0xFFFF);

            if(current.add0(num)){
                CordNode next0 = new CordNode();
                current.next = next0;
                current = next0;
                //this.next = next0;
            }
        }


        public boolean isEMpty(){
            return current == this && cursor == 0;
        }

        /**
         * Выполнить координатную функцию для каждого элемента в списке
         */
        public void forEach(CordFunction func){
            CordNode cur = this;
            while(cur != null){
                int[] data = cur.data;
                for(int i = 0, c = cur.cursor; i < c; i++){
                    int num = data[i];

                    func.apply((num >> 24) & 15, (num & 0xFFFF) - 0x7FFF, (num >> 16) & 15);
                }

                cur = cur.next;
            }
        }
    }


    /**
     * Узел списка координат блоков
     */
    private static class CordNode{
        int[] data = new int[64];
        int cursor;
        CordNode next;

        boolean add0(int num){
            data[cursor++] = num;
            return cursor == 64;
        }
    }


    @FunctionalInterface
    public interface CordFunction {
        void apply(int x, int y, int z);
    }
}