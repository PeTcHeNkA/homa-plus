package plus.utl.audio;

import org.vosk.Model;
import org.vosk.Recognizer;
import plus.utl.collections.CircularWordList;
import javax.sound.sampled.*;
import java.io.IOException;


/**
 * Базовая система по ИИ-обработке аудио-канала в слова
 * @author HomaPlus
 */
public class AudioAITextReader {
    private final Recognizer recognizer;
    private final CircularWordList words;
    private Thread thread;

    public AudioAITextReader(Model model, int wordBuf, int rate) throws IOException {
        recognizer = new Recognizer(model, rate);
        words = createWordList(wordBuf);
    }


    public AudioAITextReader(String filename, int wordBuf, int rate) throws IOException {
         this(new Model(filename), wordBuf, rate);
    }


    protected CircularWordList createWordList(int wordBuf) {
        return new CircularWordList(wordBuf);
    }


    public void start(String audioCode) throws Exception {
        if (thread != null) {
            throw new Exception("Already started");
        }
        AudioHandler handler = new AudioHandler(audioCode);

        thread = new Thread(() -> {
            try {
                handler.startHandle();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            byte[] buf = new byte[handler.getBufferLen()];

            while (thread != null) {
                try {
                    int count = handler.getAudioStream().read(buf);
                    if(count == 0){
                        thread.sleep(50);
                        continue;
                    }

                    if(recognizer.acceptWaveForm(buf, count)){
                        String result = AIAnswer.getResult(recognizer.getFinalResult()).trim();
                        if(!result.isEmpty()){
                            words.addWords(result);
                        }
                        recognizer.reset();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            handler.stopHandle();
        });
        thread.start();

    }


    public  void stop() {
        thread = null;
    }


    public CircularWordList getWords() {
        return words;
    }
}