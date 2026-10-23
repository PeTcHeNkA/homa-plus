package plus.mobs.ai;

import plus.mobs.ai.model.*;
import plus.mobs.frame.AbcMob;
import plus.mobs.utl.LogicalExpression;
import plus.mobs.utl.MobRegManager;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public class AiBuilder {
    private AbcMob.IEntityApplicator base;
    private MobRegManager manager;

    public AiBuilder(MobRegManager manager, TsonMap map){
        this.manager = manager;
        add(map);
    }

    private LogicalExpression<AbcMob> createExpression(String name, String exp){
        String code = manager.expressionFrame.replace("%CLASS_NAME%", name)
                .replace("%EXPRESSION%", exp);
        try {
            return (LogicalExpression<AbcMob>)
                    manager.compiler.load(manager.compiler.compile(name, code),"jlogic."+name).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void add(TsonMap map){
        TsonList list = map.getList("normal");
        for(TsonObj obj:list){
            add(getOrNew(obj.getStr()));
        }
        if(map.containsKey("low-hp")){
            TsonMap aiLowHp = map.getMap("low-hp");
            base = new AiCombinedModel(
                    base,
                    new AiLowHealthModel(
                            new AiBuilder(manager, aiLowHp).build(),
                            aiLowHp.getFloat("min-hp")
                    )
            );
        }
        if(map.containsKey("logical")){
            TsonMap aiLogical = map.getMap("logical");
            LogicalExpression<AbcMob> exp = createExpression(map.getStr("name"),map.getStr("code"));
            base = new AiCombinedModel(
                    base,
                    new AiLogicalModel(new AiBuilder(manager, aiLogical).build(), exp)
            );
        }
    }


    private AbcMob.IEntityApplicator getOrNew(String data){
        if(manager.aiModelCache.containsKey(data)){
            return manager.aiModelCache.get(data);
        } else {
            AbcMob.IEntityApplicator app = createOfKey(data.split(":"));
            manager.aiModelCache.put(data, app);
            return app;
        }
    }


    protected AbcMob.IEntityApplicator createOfKey(String... params){
        switch (params[0]){
            default:
            case "default":
            case "revenge-only":
                return new AiDefaultModel(
                        params.length>1 && params[1].equalsIgnoreCase("true"));
            case "aggressive":
                return new AIAgressiveModel();
            case "peaceful":
                return new AiPeacefulModel();
            case "like-item":
                return new AiLikeItemModel(params[1].replace("#",":"));
            case "clear":
                return new AiClearModel();
        }
    }


    protected void setBase(AbcMob.IEntityApplicator base) {
        if(this.base != null)return;
        this.base = base;
    }


    public void add(AbcMob.IEntityApplicator model){
        if(base!=null) {
            base = new AiCombinedModel(base, model);
        } else {
            setBase(model);
        }
    }


    public AbcMob.IEntityApplicator build(){
        return base;
    }
}
