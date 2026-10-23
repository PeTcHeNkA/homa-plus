import plus.utl.audio.AudioHandler;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.AudioFileWriter;
//import com.sun.media.sound.*;

import java.io.File;

public class TestRecord {
    public static void main(String[] args) throws Exception {
        AudioHandler audioHandler = new AudioHandler(AudioHandler.DEFAULT_CONFIG);
        audioHandler.startHandle();

        System.out.println("Recording...");
        //AudioInputStream audioStream = new AudioInputStream(audioHandler.getAudioStream(), audioHandler.format, 1024*100);
        new Thread(()->{
            try {
                Thread.sleep(1000*10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            audioHandler.stopHandle();
        });

        //audioHandler.getAudioStream().

        //audioHandler.getAudioStream().

//        WaveFileWriter wr = new WaveFileWriter();
//
//        wr.write(audioHandler.getAudioStream(), AudioFileFormat.Type.WAVE, new File("test.wav"));
//
//        //var v = AudioFileFormat.Type.WAVE;
//
//        //audioStream.getFormat().sa
//
//        //wd.saveToFile("result.wav", AudioFileFormat.Type.WAVE, soundRecorder.getAudioInputStream());
//
//
//        //audioHandler.getAudioStream().read()

    }
}
