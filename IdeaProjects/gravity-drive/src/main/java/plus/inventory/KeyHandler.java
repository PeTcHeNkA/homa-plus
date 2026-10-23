package plus.inventory;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class KeyHandler {

    /*Создаем бинд. Указываем название - Open Inventory
       Кнопка поумолчанию, если игрок ничего не менял будет H
       Название категории - Custom Inventory Keys*/
    public static KeyBinding openKey = new KeyBinding("Open Inventory", Keyboard.KEY_H, "Custom Inventory Keys");

    //Регистрируем событие и бинд
    public static void register() {
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
        ClientRegistry.registerKeyBinding(openKey);
    }

    //Событие, которое срабатывает при нажатии игроком кнопки на клавиатуре
    @SubscribeEvent
    public void onKey(KeyInputEvent event) {
        //если нажали на нашу кнопку Н то отправляем пакет на сервер с просьбой открыть инвентарь
        if (openKey.isPressed()) {
            NetworkHandler.network.sendToServer(new OpenInventoryMessage());
        }
    }

}