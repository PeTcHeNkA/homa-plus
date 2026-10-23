import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

public class TestE1 {
    public static void main(String[] args) throws Exception {
        // Установка параметров аудио (44.1 кГц, моно)
        AudioFormat format = new AudioFormat(
                44100,
                16,
                1,
                true,
                true
        );

        // Открытие устройства для записи (микрофон)
        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(
                new DataLine.Info(TargetDataLine.class, format)
        );
        targetDataLine.open(format);
        targetDataLine.start();

        // Создание буфера для эха (0.5 секунды задержки)
        int bufferSize = (int)(44100 * 0.5);  // 0.5 секунды при частоте 44.1 кГц
        byte[] echoBuffer = new byte[bufferSize];

        // Открытие устройства для воспроизведения (наушники)
        SourceDataLine playbackLine = (SourceDataLine) AudioSystem.getLine(
                new DataLine.Info(SourceDataLine.class, format)
        );
        playbackLine.open(format);
        playbackLine.start();

        try {
            // Бесконечный цикл записи и воспроизведения
            while (!Thread.currentThread().isInterrupted()) {
                byte[] buffer = new byte[bufferSize];
                int numBytesRead;

                // Чтение из микрофона
                if ((numBytesRead = targetDataLine.read(buffer, 0, buffer.length)) >= 0) {
                    // Копирование данных в буфер эха
                    System.arraycopy(buffer, 0, echoBuffer, 0, numBytesRead);

                    // Воспроизведение эха с задержкой
                    ByteArrayInputStream bais = new ByteArrayInputStream(echoBuffer, 0, numBytesRead);
                    //byte[] bts = bais.readAllBytes();

                    //playbackLine.write(bts, 0, bts.length);
                }
            }
        } finally {
            targetDataLine.close();
            playbackLine.close();
        }
    }
}