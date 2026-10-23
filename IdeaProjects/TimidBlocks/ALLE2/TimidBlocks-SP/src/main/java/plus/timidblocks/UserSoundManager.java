package plus.timidblocks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.vosk.Model;
import org.vosk.Recognizer;
import plus.tson.TsonMap;
import plus.tson.utl.Te4HashMap;
import plus.utl.audio.AIAnswer;
import plus.utl.collections.CircularArrayQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;


/**
 * Подсистема управления аудио-потоками и распознования сказанных блоков
 * @author HomaPlus
 */
public class UserSoundManager {
    private final ThreadPoolExecutor pool;
    private final TimidBlocks plugin;
    private final Te4HashMap<UUID, Session>  sessions = new Te4HashMap<>();
    private final int wordBuf, wordQueue, rate;
    private final Model model;
    private Te4HashMap<String, Material> wordData = new Te4HashMap<>();

    public UserSoundManager(TimidBlocks plugin, String modelPath, int wordBuf, int wordQueue, int rate, int threads, int maxThreads) {
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
        this.wordQueue = wordQueue;
        this.rate = rate;
        this.pool = new ThreadPoolExecutor(threads = Math.max(1, threads), Math.max(threads, maxThreads), 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }


    /**
     * Загружает карту материалов на ключевые слова, потокобезопасно
     */
    public void loadWordData(TsonMap map){
        if(map == null) return;
        Te4HashMap<String, Material> wordData = new Te4HashMap<>();
        map.forEach((key, value) -> {
            Material material = Material.getMaterial(key.toUpperCase());
            if(material == null || material == Material.AIR){
                plugin.getLogger().warning("[TimidBlocks] Could not find material for key: " + key);
            } else {
                if(value.isString()){
                    for (String keyWord: value.getStr().split(";")) {
                        wordData.fput(keyWord, material);
                    }
                }
            }
        });
        this.wordData = wordData;
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
                if ((recognizer = this.recognizer).acceptWaveForm(buf, buf.length)) {
                    String result = AIAnswer.getResult(recognizer.getFinalResult()).trim();
                    l1: if (!result.isEmpty()) {
                        CircularArrayQueue<String> words = this.words;

                        if(manager.plugin.isDebug()){
                            String name;
                            Player player = manager.plugin.getServer().getPlayer(uuid);
                            if(player != null){
                                name = player.getName();
                            } else {
                                name = uuid.toString();
                            }
                            manager.plugin.getLogger().warning("[TimidBlocks] User ["+name+"] say: ["+result+"]");
                        }

                        String[] words2 = result.split(" ");
                        int count = 0;

                        while (count < words2.length) {
                            int lim = manager.wordQueue;
                            for (;count < words2.length; count++) {
                                words.add(words2[count]);
                                if(lim-- == 0)break;
                            }

                            int cnt = words.size();

                            Te4HashMap<String, Material> wordData = manager.wordData;
                            StringBuilder sb = new StringBuilder();

                            while (cnt > 0) {
                                for (int i = cnt; i > 0; i--) {
                                    String word = words.getAtTail(cnt-i);
                                    if(word != null) {
                                        sb.append(word).append(' ');
                                    } else break;
                                }
                                if(sb.length() > 0){
                                    sb.setLength(sb.length()-1);

                                    Material block = wordData.get(sb.toString());

                                    if(block != null) {
                                        manager.onSayBlock(uuid, block);
                                        words.clear();
                                    }
                                    sb.setLength(0);
                                }
                                --cnt;
                            }
                        }
                    }
                    recognizer.reset();
                }
                locked = false;
            }
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


    /**
     * Вызывается, когда игрок с указанным uuid "говорит" блок
     */
    public void onSayBlock(UUID plUUID, Material block) {
        Player player = Bukkit.getPlayer(plUUID);
        if(player == null){
            plugin.getLogger().warning("Player ["+plUUID+"] is not online]");
            return;
        }
        if(plugin.isDebug()){
            plugin.getLogger().warning("[TimidBlocks] User ["+player.getName()+"] say block: ["+block.name()+"], start removing at ["+player.getChunk().getX()+":"+player.getChunk().getZ()+"]");
        }

        //todo
        clearBlocksInChunk(player, block);
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
    protected void clearBlocksInChunk(Player player, Material block){
        Bukkit.getScheduler().runTask(plugin, () -> {
            Chunk chunk = player.getChunk();
            ChunkSnapshot snap0 = chunk.getChunkSnapshot(true, false, false);

            pool.execute(() -> clearBlocksInChunkSub(chunk, snap0, block));
        });
    }


    /**
     * Быстро очищает указанный блок в чанке, где находится игрок
     * (2 этап - обход снапшота и применение изменений
     */
    protected void clearBlocksInChunkSub(Chunk chunk, ChunkSnapshot snap, Material block){
        int min = chunk.getWorld().getMinHeight(); //todo check it maybe dont exists

        CordList list = new CordList();

        for (int x = 0; x < 16; x++){
            for (int z = 0; z < 16; z++){
                for (int y = min, lim = snap.getHighestBlockYAt(x, z); y < lim; y++){
                    if(snap.getBlockType(x, y, z) == block){
                        list.add(x, y, z);
                    }
                }
            }
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            list.forEach((x, y, z) -> chunk.getBlock(x, y, z).setType(Material.AIR));
        });
    }


    /**
     * Быстрый и компактный список координат блоков в диапазоне (x 0-15, y +-32767, z 0-15)
     * Х: 32-24 (1 байт )
     * Y: 0-16  (2 байта)
     * Z: 16-14 (1 байт )
     */
    private static class CordList extends CordNode {
        CordNode current = this;

        /**
         * Добавить координаты в список
         */
        void add(int x, int y, int z){
            int num = ((x & 15) << 24) | ((z & 15) << 16) | (((y & 0xFFFF) + 0x7FFF) & 0xFFFF);

            if(current.add0(num)){
                CordNode next0 = new CordNode();
                current.next = next0;
                current = next0;
                //this.next = next0;
            }
        }

        /**
         * Выполнить координатную функцию для каждого элемента в списке
         */
        void forEach(CordFunction func){
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
    interface CordFunction {
        void apply(int x, int y, int z);
    }
}