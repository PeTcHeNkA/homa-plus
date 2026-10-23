package plus.utl.collections;


/**
 * Замкнутая очередь строковых слов
 * @author HomaPlus
 */
public class CircularWordList extends CircularArrayQueue<String> {
    protected boolean modified = false;

    protected CircularWordList(){}

    public CircularWordList(int size){
        super(size);
    }


    /**
     * Добавляет слова, разделенные пробелом
     */
    public void addWords(String str) {
        synchronized(this){
            String[] words = str.split(" ");
            for (String word : words) {
                super.add(word);
            }
            modified = true;
        }
    }


    /**
     * Возвращает, был ли изменена очередь, с момента последней проверки
     */
    public boolean isModified(){
        synchronized(this){
            return modified;
        }
    }


    /**
     * Сбросить статус "модифицированности"
     */
    public synchronized void resetModifyState(){
        synchronized(this){
            modified = false;
        }
    }


    @Override
    public void clear() {
        synchronized (this) {
            super.clear();
            modified = true;
        }
    }
}