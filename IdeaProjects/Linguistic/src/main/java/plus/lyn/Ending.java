package plus.lyn;

import java.util.Arrays;


public class Ending {
    public final IdxString str;
    public final Case[] cases;
    public final int[] weights;
    public final boolean maybeSingle;
    public final boolean maybeMulti;

    Ending(IdxString str, Case[] cases, int[] weights) {
        this.str = str;
        this.cases = cases;
        this.weights = weights;

        boolean single = false, multy = false;

        l1: for (Case cs:cases){
            if(!single) {
                for (IdxString str0 : cs.endingsSingle) {
                    if(str0.index == str.index){
                        single = true;
                        if(multy) break l1;
                        else break;
                    }
                }
            }
            if(!multy){
                for (IdxString str0 : cs.endingsMulti) {
                    if(str0.index == str.index){
                        multy = true;
                        if(single) break l1;
                        else break;
                    }
                }
            }
        }
        maybeSingle = single;
        maybeMulti  = multy;
    }


    public boolean ensureMulti(){
        return maybeSingle && !maybeMulti || !maybeSingle && maybeMulti;
    }


    public int length(){
        return str.value.length();
    }


    public String toUTF8Str(){
        return str.toUTF8Str();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ending ending = (Ending) o;

        if (maybeSingle != ending.maybeSingle) return false;
        if (maybeMulti != ending.maybeMulti) return false;
        if (!str.equals(ending.str)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(cases, ending.cases)) return false;
        return Arrays.equals(weights, ending.weights);
    }


    @Override
    public int hashCode() {
        int result = str.hashCode();
        result = 31 * result + Arrays.hashCode(cases);
        result = 31 * result + Arrays.hashCode(weights);
        result = 31 * result + (maybeSingle ? 1 : 0);
        result = 31 * result + (maybeMulti ? 1 : 0);
        return result;
    }
}
