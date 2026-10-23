import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.*;

public class TestAI {
    public static void main(String[] args) throws Exception {

        LibVosk.setLogLevel(LogLevel.DEBUG);
        int n = 0;
        try (Model model = new Model("model/ru_sm022");
             InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream("src\\test\\resources\\test.wav")));
             Recognizer recognizer = new Recognizer(model, 16000)) {
            //recognizer.setPartialWords(true);

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                System.out.println(n++);
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    System.out.println("R|"+recognizer.getResult());
                } else {
                    System.out.println("P|"+recognizer.getPartialResult());
                }
                //System.out.println("R|"+recognizer.getResult());
            }

            //System.out.println(recognizer.getFinalResult());
        } catch (Exception e) {
            throw e;
            //Files.write(new File("error.txt").toPath(), e.getMessage().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }

        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream("src\\test\\resources\\test.wav")));

        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        try {
            clip.open(ais);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start playing the audio
        clip.start();

        Thread.sleep(2000*10);
        //AudioSystem.
    }
}
