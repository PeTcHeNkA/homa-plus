//import com.sun.media.sound.WaveFileWriter;
import org.vosk.Model;

import javax.sound.sampled.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TestAI2 {
    public static void main(String[] args) throws Exception {

        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);


        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

        line.open(format);

        line.start();

        System.out.println("Recording...");
        //byte[] buffer = new byte[4096];
        //int bytesRead;

        //WaveFileWriter wr = new WaveFileWriter();

        //int index = 0;
        //OutputStream sampleOut = new BufferedOutputStream(new FileOutputStream("./test.out"));

//        while (index < 100) {
//            bytesRead = line.read(buffer, 0, buffer.length);
//
//            //sampleOut.write(buffer, 0, bytesRead);
//            // Process the audio data in 'buffer' as needed
//
//            index += 1;
//        }

        //sampleOut.close();


        //wr.write(line.)

    }
}
