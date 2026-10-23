package plus.ds.msg;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import plus.ds.IrisSystem;
import plus.ds.api.IEmbedMsg;
import plus.ds.api.IMessageEvent;
import plus.ds.api.MessageVarProvider;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.format.ConstVarGetter;
import plus.tson.format.IVarGetter;
import plus.tson.format.TextFormatter;
import plus.tson.utl.ConstGetter;
import plus.tson.utl.IGetter;

import java.util.concurrent.ThreadLocalRandom;


public class EmbedMsg implements IEmbedMsg {
    private static final IVarGetter<IMessageEvent> nullGetter = new ConstVarGetter<>(null);
    private final IGetter<IMessageEvent,MessageEmbed.Thumbnail> icoGetter;
    private final IGetter<IMessageEvent,MessageEmbed.ImageInfo> imgGetter;
    private final IVarGetter<IMessageEvent> content;
    private final IVarGetter<IMessageEvent> title;
    private final IVarGetter<IMessageEvent> url;
    private final IGetter<IMessageEvent,Integer> color;

    private EmbedMsg(IrisSystem.GuildProgram program, TsonMap map){
        MessageVarProvider provider = new MessageVarProvider(program);
        icoGetter = buildIcoGetter(provider, map, false);//ico
        imgGetter = buildIcoGetter(provider, map, true);//img
        title = buildStrGetter(provider,map,"title","title");
        url = buildStrGetter(provider,map,"url",null);
        content = buildStrGetter(provider,map,"msg",null);
        color = buildColor(map);
    }


    public static IEmbedMsg build(IrisSystem.GuildProgram program, TsonMap map){
        EmbedMsg em = new EmbedMsg(program, map);
        if(
                em.imgGetter.isConst() &&
                em.imgGetter.isConst() &&
                em.color.isConst() &&
                IVarGetter.isConst(em.content) &&
                IVarGetter.isConst(em.url) &&
                IVarGetter.isConst(em.title)
        ){
            return new IEmbedMsg() {
                private final MessageEmbed msg = em.buildEmbed(null);
                @Override
                public MessageEmbed buildEmbed(IMessageEvent event) {
                    return msg;
                }
            };
        } else {
            return em;
        }
    }


    private IGetter<IMessageEvent,Integer> buildColor(TsonMap map){
        TsonObj color = map.get("color");
        int max, min;
        if(color!=null) {
            if (color.isNumber()) {
                max = color.getInt();
                return new ConstGetter<>(max);
            } else {
                String str = color.getStr();
                if(str.equals("random")){
                    min = 0;
                    max = 16777215;
                } else {
                    String[] args = str.split(":");
                    min = Integer.parseInt(args[0]);
                    max = Integer.parseInt(args[0]);
                }
            }
        } else {
            max = 16777215;
            min = 0;
        }
        return o -> ThreadLocalRandom.current().nextInt(min,max);
    }


    private IVarGetter<IMessageEvent> buildStrGetter(
            MessageVarProvider provider, TsonMap map, String key, String def){
        TsonObj obj = map.get(key);
        if(obj==null){
            if(def==null)return nullGetter;
            return new ConstVarGetter<>(def);
        }
        return TextFormatter.compile(obj.getStr(), provider).simplify();
    }


    private IGetter buildIcoGetter(MessageVarProvider provider, TsonMap map, boolean img){
        TsonObj sIco = map.get(img?"img":"ico");
        if(sIco==null)return e -> null;
        IVarGetter<IMessageEvent> icoGetter;
        IVarGetter<IMessageEvent> sizeGetter;
        if(sIco.isMap()) {
            TsonMap map0 = sIco.getMap();
            icoGetter = TextFormatter.compile(map0.getStr(), provider).simplify();
            sizeGetter = buildStrGetter(provider, map0, "size", "512x512");
        } else if(sIco.isString()){
            icoGetter = TextFormatter.compile(sIco.getStr(), provider).simplify();
            sizeGetter = new ConstVarGetter<>("512x512");
        } else if(sIco.isList()){
            TsonList lst = sIco.getList();
            icoGetter = TextFormatter.compile(lst.get(0).getStr(), provider);
            if(lst.size()>1){
                sizeGetter = TextFormatter.compile(lst.get(1).getStr(), provider).simplify();
            } else {
                sizeGetter = new ConstVarGetter<>("512x512");
            }
        } else {
            throw new RuntimeException("Unsupported type: "+sIco);
        }
        if(IVarGetter.isConst(icoGetter) && IVarGetter.isConst(sizeGetter)){
            return new ConstGetter<>(buildIco(icoGetter, sizeGetter, img, null));
        } else {
            return (IGetter<IMessageEvent, Object>) e -> buildIco(icoGetter, sizeGetter, img, e);
        }
    }


    private Object buildIco(
            IVarGetter<IMessageEvent> icoGetter, IVarGetter<IMessageEvent> sizeGetter, boolean img, IMessageEvent e){
        String[] sz = sizeGetter.get(e).split("x");
        int width = Integer.parseInt(sz[0]);
        int height = Integer.parseInt(sz.length>1?sz[1]:sz[0]);
        if(img){
            return new MessageEmbed.ImageInfo(
                    icoGetter.get(e), icoGetter.get(e), width, height
            );
        } else {
            return new MessageEmbed.Thumbnail(
                    icoGetter.get(e), icoGetter.get(e), width, height
            );
        }
    }


    @Override
    public MessageEmbed buildEmbed(IMessageEvent event) {
        return new MessageEmbed(
                url.get(event),
                title.get(event),
                content.get(event),
                EmbedType.RICH,
                null,
                color.get(event),
                icoGetter.get(event),
                null,
                null,
                null,
                null,
                imgGetter.get(event),
                null
        );
    }
}