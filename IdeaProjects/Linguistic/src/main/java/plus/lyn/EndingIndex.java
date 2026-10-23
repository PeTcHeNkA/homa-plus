package plus.lyn;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import plus.lyn.utl.Statister;
import plus.lyn.utl.UnLinker;
import plus.tson.utl.Te4HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class EndingIndex{
    public static final EndingIndex inst = new EndingIndex();
    public final Int2ObjectOpenHashMap<Ending>[] index;

    private EndingIndex(){
        index = fillIndex(
                WordPartIndex.NOUN,
                WordPartIndex.ADJECTIVE,
                WordPartIndex.VERB,
                WordPartIndex.PARTICIPLE,
                WordPartIndex.DEE_PARTICIPLE,
                WordPartIndex.ADVERB
        );
    }


    private Int2ObjectOpenHashMap<Ending>[] fillIndex(WordPartIndex.SPart... sParts){
        Te4HashMap<IdxString, Statister<Case>>[] temp = new Te4HashMap[3];
        Int2ObjectOpenHashMap<Ending>[] index = new Int2ObjectOpenHashMap[temp.length];
        UnLinker<Ending> eLinker = new UnLinker<>();

        for (int i=0;i<temp.length;i++){
            temp[i] = new Te4HashMap<>();
            index[i] = new Int2ObjectOpenHashMap<>();
        }

        for (WordPartIndex.SPart sPart: sParts){
            for (WordPartIndex.Part part: sPart.parts){
                for (Case cs: part.allCases){
                    for (IdxString str: cs.endingsSingle)
                        add(temp[str.value.length()-1], cs, str);
                    for (IdxString str: cs.endingsMulti)
                        add(temp[str.value.length()-1], cs, str);
                }
            }
        }

        UnLinker<Case[]> caseLinker = new UnLinker<>();
        UnLinker<int[]> weightsLinker = new UnLinker<>();

        for (int i=0;i<temp.length;i++){
            for (Map.Entry<IdxString, Statister<Case>> entry: temp[i].entrySet()){
                IdxString str = entry.getKey();

                Te4HashMap<Case, AtomicInteger> val = entry.getValue();
                Case[] cases = new Case[val.size()];
                int[] weights = new int[val.size()];

                int i2 = 0;
                for (Map.Entry<Case, AtomicInteger> pair: val.entrySet()){
                    cases[i2] = pair.getKey();
                    weights[i2] = pair.getValue().get();
                    ++i2;
                }
                cases = caseLinker.unLink(cases);
                if(weights.length == 1){
                    weights = WordPartIndex.emptyWeights;
                } else {
                    weights = weightsLinker.unLink(weights);
                }

                Ending ending = new Ending(str, cases, weights);
                Ending link = eLinker.get(ending);
                if(link == null) eLinker.fput(link = ending, ending);
                index[i].put(str.index, link);
            }
            index[i].trim();
        }

//        for (Ending ending: eLinker.keySet()){
//            for (Case cs: ending.cases){
//                for (int i=0;i<cs.endingsSingle.length;i++){
//                    //cs.endingsSingle[i] = cs.endingsSingle[i];
//                }
//                for (int i=0;i<cs.endingsMulti.length;i++){
//                    //cs.endingsMulti[i] = cs.endingsMulti[i];
//                }
//            }
//        }
        return index;
    }


    private<T> T unLink0(Te4HashMap<T,T> proxy, T src){
        T eLink = proxy.get(src);
        if(eLink == null){
            proxy.fput(src, src);
            return src;
        } else {
            return eLink;
        }
    }


    private void add(Te4HashMap<IdxString, Statister<Case>> idx, Case cs, IdxString str){
        Statister<Case> prev = idx.get(str);
        if(prev == null)idx.fput(str, prev = new Statister<>());
        prev.feed(cs);
    }
}