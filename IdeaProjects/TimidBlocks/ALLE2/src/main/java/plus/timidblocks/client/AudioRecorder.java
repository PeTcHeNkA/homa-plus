package plus.timidblocks.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import plus.utl.audio.AudioHandler;


/**
 * Записывает аудио с микрофона и отправляет на сервер
 * @author HomaPlus
 */
public class AudioRecorder {
    private AudioHandler handler;
    private Thread thread;

    public AudioRecorder(String config) throws Exception {
        this.handler = new AudioHandler(config);
    }


    public void start() throws Exception {
        handler.startHandle();

        thread = new Thread(() -> {
            while (thread != null) {
                byte[] data = new byte[handler.getBufferLen()];
                int count = handler.getLine().read(data, 0, data.length);
                ClientPlayNetworking.send(new AudioDataPayload(data, count));
            }
        });
        thread.start();
    }


    public void stop() throws Exception {
        handler.stopHandle();
        try {
            thread.interrupt();
        } catch (Exception e) {}
        thread = null;
    }
}