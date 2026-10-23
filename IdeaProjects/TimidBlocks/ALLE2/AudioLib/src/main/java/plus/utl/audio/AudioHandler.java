package plus.utl.audio;

import javax.sound.sampled.*;


/**
 * Базовая система для управления микрофоном
 * @author HomaPlus
 */
public class AudioHandler {
    public static String DEFAULT_CONFIG = "22050,16,1,true,false";
    private TargetDataLine line;
    private int buffLen;
    public AudioFormat format;
    AudioInputStream audioStream;

    //44100,16,2,true,true,4000
    public AudioHandler(String config) throws Exception {
        try {
            format = parseAudioFormat(config);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);

            buffLen = (int) format.getFrameRate();
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid input config -> ["+config+"]! Use [44100,16,2,true,true,4000] (rate, bits, channels, signed, big endian, buffer len)", e);
        }
    }


    public static AudioFormat parseAudioFormat(String config) {
        String[] args = config.split(",");
        return new AudioFormat(Int(args, 0), Int(args, 1), Int(args, 2), Bool(args, 3), Bool(args, 4));
    }


    public AudioHandler startHandle() throws Exception {
        line.open(format);
        line.start();
        audioStream = new AudioInputStream(line);
        return this;
    }


    public TargetDataLine getLine() {
        return line;
    }


    public void stopHandle() {
        try {
            line.close();
            line.stop();
            audioStream.close();
            audioStream = null;
        } catch (Exception e) {}
    }


    public AudioInputStream getAudioStream() {
        return audioStream;
    }


    public int getBufferLen() {
        return buffLen;
    }


    public static int Int(String[] args, int idx) {
        return Integer.parseInt(args[idx]);
    }


    public static boolean Bool(String[] args, int idx) {
        return args[idx].equalsIgnoreCase("true");
    }
}
