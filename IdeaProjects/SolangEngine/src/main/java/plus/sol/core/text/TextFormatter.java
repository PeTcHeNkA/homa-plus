package plus.sol.core.text;

import plus.sol.api.IEnvironment;
import plus.sol.api.IVarProxy;
import plus.sol.core.math.Mather;
import plus.sol.reader.waiter.ToEndWaiter;
import plus.sol.utls.flux.StrFlux;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;


public class TextFormatter<U> {
    public final IVarProxy<U, ?>[] format;
    public TextFormatter(Function<String, IVarProxy<U, ?>> builder, String msg){
        StrFlux flux = new StrFlux(msg);

        ArrayList<IVarProxy<U, ?>> f1 = new ArrayList<>();
        ToEndWaiter waiter = new ToEndWaiter('%');
        boolean text = true;

        while (flux.hasNext()){
            char c = flux.next();
            if(waiter.wait(c)){
                String val = waiter.getValue();
                if(text){
                    f1.add(new IVarProxy.Constant<>(val));
                } else {
                    f1.add(Mather.build(builder, new ArrayList<>(Arrays.asList(val.split(" ")))));
                }
                waiter.reset();
                text = !text;
            }
        }
        String val = waiter.getValue();
        if(!val.isEmpty()) {
            if (text) {
                f1.add(new IVarProxy.Constant<>(val));
            } else {
                f1.add(Mather.build(builder, new ArrayList<>(Arrays.asList(val.split(" ")))));
            }
        }
        format = f1.toArray(new IVarProxy[0]);
    }

    public TextFormatter(IVarProxy<U, ?>[] format){
        this.format = format;
    }


    public String format(U user, IEnvironment<U> env){
        StringBuilder builder = new StringBuilder(128);
        for(IVarProxy<U, ?> g:format){
            builder.append(g.getValue(user, env));
        }
        return builder.toString();
    }
}








