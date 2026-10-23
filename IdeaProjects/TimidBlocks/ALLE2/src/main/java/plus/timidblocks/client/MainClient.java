package plus.timidblocks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import plus.utl.audio.AudioHandler;


/**
 * Мод на пугливые блоки
 * Если сказать название блока в слух - он пропадет (необходимо установить связанный плагин на сервер)
 * @author HomaPlus
 * Разработано по заказу Nuko02. Не подлежит перепродаже
 */
public class MainClient implements ClientModInitializer {
    public static final Identifier TIMID_BLOCKS_NET_ID = Identifier.of("timidblocks","sound_chan");
    private AudioRecorder recorder;

    @Override
    public void onInitializeClient() {
        AudioDataPayload.init();
        initEvents();
    }


    public void initEvents(){
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if(client.isInSingleplayer())return; //todo maybe add offline options?

            PlayerEntity player = client.player;
            try {
                recorder = new AudioRecorder(AudioHandler.DEFAULT_CONFIG);
                recorder.start();
                player.sendMessage(Text.of("Audio recording started"), false);
            } catch (Exception e) {
                player.sendMessage(Text.of("Critical error starting audio system, see console"), false);

                try {
                    recorder.stop();
                }  catch (Exception ex) {}

                e.printStackTrace();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, sender) -> {
            try {
                if(recorder != null){
                    recorder.stop();
                }
            }  catch (Exception ex) {}
        });
    }
}