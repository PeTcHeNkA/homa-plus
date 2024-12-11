package plus.fserver.api;

import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import plus.fserver.ElcaCV2;
import plus.fserver.api.placeholders.PlayerVarProvider;
import plus.fserver.server.network.packet.SRawPacketTitle;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.format.TextFormatter;
import static plus.tson.ext.TsonNumUtils.*;


public class TitlePacketBuilder extends ChatPacketBuilder{
    protected SPacketTitle.Type type = SPacketTitle.Type.TITLE;
    protected int fadeIn = 10, display = 50, fadeOut = 10;

    protected TitlePacketBuilder(TextFormatter<IPlayerDataModel> format) {
        super(format);
    }


    public static TitlePacketBuilder of(TsonMap map){
        String type0 = map.getStrSafe("type");
        SPacketTitle.Type type = SPacketTitle.Type.TITLE;
        int fadeIn = 10, display = 50, fadeOut = 10;

        if(type0 != null) switch (type0){
            case "sub":
            case "subtitle":
                type = SPacketTitle.Type.SUBTITLE;
                break;
            case "act":
            case "action":
            case "actionbar":
                type = SPacketTitle.Type.ACTIONBAR;
                break;
            case "times":
                type = SPacketTitle.Type.TIMES;
                break;
            case "clear":
                type = SPacketTitle.Type.CLEAR;
                break;
            case "reset":
                type = SPacketTitle.Type.RESET;
                break;
        }

        fadeIn = calc(map, "fade-in", fadeIn);
        display = calc(map, "display", display);
        fadeOut = calc(map, "fade-out", fadeOut);

        TsonObj obj = map.get("msg");

        TitlePacketBuilder builder = of(TextComponentAuto.compFromTson(obj));
        if(obj.isMap()){
            if(obj.getMap().getOrDefaultBool("async", false))
                builder.asyncRender(true);
        }
        return builder.init(type, fadeIn, display, fadeOut);
    }


    public static TitlePacketBuilder of(String text){
        return of(new TextComponentAuto(text));
    }


    public static TitlePacketBuilder of(ITextComponent comp){
        return new TitlePacketBuilder(TextFormatter.compile(
                ITextComponent.Serializer.componentToJson(comp),
                PlayerVarProvider.PROVIDER)
        );
    }


    public TitlePacketBuilder init(SPacketTitle.Type type, int fadeIn, int display, int fadeOut){
        this.type = type;
        this.fadeIn = fadeIn;
        this.display = display;
        this.fadeOut = fadeOut;
        return this;
    }


    @Override
    public TitlePacketBuilder asyncRender(boolean b) {
        asyncRender = b;
        return this;
    }


    @Override
    public Packet<INetHandlerPlayClient> build(IPlayerDataModel model) {
        if(ElcaCV2.LIBBUILD){
            ITextComponent comp = ITextComponent.Serializer.jsonToComponent(format.get(model));
            if(comp == null)comp = new TextComponentString("");
            return new SPacketTitle(type, comp, fadeIn, display, fadeOut);
        }
        return new SRawPacketTitle(type, format.get(model), fadeIn, display, fadeOut);
    }
}