package plus.lyn.utl;

import plus.tson.utl.Te4HashMap;


public class UnLinker<T> extends Te4HashMap<T,T> {

    public T unLink(T src){
        T eLink;
        if((eLink = get(src)) == null){
            fput(src, src);
            return src;
        } else return eLink;
    }
}