package plus.space.common.proxy;

import org.lwjgl.input.Keyboard;
import plus.space.client.event.KeyHandler;
import plus.space.client.gui.GuiStarMap;

public class ClientProxy extends CommonProxy{
    @Override
    public void preInit(boolean flag) {
//        plus.space.server.SolarMapServerProvider.init(network, 1);
//        new KeyHandler(GuiStarMap::open, "spworlds", "gui", Keyboard.KEY_N);
    }
}
