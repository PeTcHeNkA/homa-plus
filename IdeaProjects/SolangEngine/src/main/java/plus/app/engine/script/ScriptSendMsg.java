package plus.app.engine.script;

import plus.app.engine.module.AppVarProvider;
import plus.app.engine.module.User;
import plus.sol.api.IEnvironment;
import plus.sol.api.IVarProxy;
import plus.sol.core.text.TextFormatter;
import plus.sol.utls.ILinkable;
import plus.sol.utls.Logger;
import plus.sol.utls.ScriptInitArgs;


public class ScriptSendMsg implements IAppScript, ILinkable<ScriptSendMsg> {
    private final TextFormatter<User> formatter;
    private final IEnvironment<User> env;

    public ScriptSendMsg(IEnvironment<User> env, String msg){
        formatter = new TextFormatter<>(AppVarProvider::get, msg);
        this.env = env;
    }


    public ScriptSendMsg(ScriptInitArgs<User> args) {
        this(args.environment, (String) args.args[0]);
    }


    public ScriptSendMsg(TextFormatter<User> formatter, IEnvironment<User> env){
        this.formatter = formatter;
        this.env = env;
    }


    @Override
    public boolean run(User user) {
        switch (user.getType()){
            case USER:
            case ADMIN:
            case SYSTEM:
                Logger.log(user.getName()+" ->"+formatter.format(user, env));
                break;
        }
        return true;
    }


    @Override
    public ScriptSendMsg link(ScriptSendMsg other) {
        if((formatter.format[formatter.format.length-1] instanceof IVarProxy.Constant)
            && (other.formatter.format[0] instanceof IVarProxy.Constant)){

            int shift = formatter.format.length-1;
            IVarProxy[] items = new IVarProxy[shift+other.formatter.format.length];

            System.arraycopy(formatter.format, 0, items, 0, shift);
            System.arraycopy(other.formatter.format, 0, items, shift, other.formatter.format.length);

            items[shift] = new IVarProxy.Constant(formatter.format[shift]
                            .getValue(null,null)+ "\n"+ items[shift].getValue(null,null)
            );
            return new ScriptSendMsg(new TextFormatter<>(items), other.env);
        } else {
            int shift = formatter.format.length;
            IVarProxy[] items = new IVarProxy[shift+other.formatter.format.length+1];
            System.arraycopy(formatter.format, 0, items, 0, shift);
            System.arraycopy(other.formatter.format, 0, items, shift+1, other.formatter.format.length);

            items[shift] = new IVarProxy.Constant("\n");

            return new ScriptSendMsg(new TextFormatter<>(items), other.env);
        }
    }
}