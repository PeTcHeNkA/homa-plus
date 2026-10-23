import plus.utl.audio.AudioAITextReader;
import plus.utl.audio.AudioHandler;
import plus.utl.collections.CircularArrayQueue;
import plus.utl.collections.CircularWordList;
import java.util.logging.Logger;


public class TestAI3 {
    public static void main(String[] args) throws Exception {
        AudioAITextReader reader = new AudioAITextReader("model/ru_sm022", 10, 22050);
        reader.start(AudioHandler.DEFAULT_CONFIG);

        while (true){
            CircularWordList words = reader.getWords();
            synchronized (words){
                if(words.isModified()){
                    Logger.getGlobal().info(getLast3(words));
                    words.resetModifyState();
                }
            }
            Thread.sleep(5000);
        }
    }


    public static String getLast3(CircularArrayQueue<String> queue){
        return getS(queue, -3) +", " + getS(queue, -2)+", " + getS(queue, -1);
    }


    public static String getS(CircularArrayQueue<String> queue, int idx){
        String result = queue.getAtTail(idx);
        if(result == null){
            return "______";
        }
        return result;
    }
}
