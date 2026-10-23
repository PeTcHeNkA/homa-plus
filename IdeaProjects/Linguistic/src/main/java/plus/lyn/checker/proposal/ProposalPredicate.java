package plus.lyn.checker.proposal;

import it.unimi.dsi.fastutil.booleans.BooleanByteImmutablePair;
import plus.lyn.WordBuilder;
import plus.lyn.checker.word.WordPredicate;
import plus.lyn.unit.Proposal;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;


public interface ProposalPredicate {
    ProposalPredicate TRUE = (cursor, proposal) -> true;
    ProposalMulti    MULTI = new ProposalMulti();
    ProposalNot    SINGLE  = new ProposalNot(MULTI);
    ProposalGen   GEN_GIRL = new ProposalGen();
    ProposalNot    GEN_BOY = new ProposalNot(GEN_GIRL);

    boolean isSuccess(int cursor, Proposal proposal);


    static ProposalPredicate ofTson(WordBuilder builder, TsonObj obj){
        if(obj == null)return TRUE;
        if(obj.isList()){
            TsonList list = obj.getList();
            if(list.size() == 0)return TRUE;
            if(list.size() == 1)return ofTson(builder, list.get(0));

            ProposalPredicate group = new ProposalAnd(
                    ofTson(builder, list.remove(0)), ofTson(builder, list.remove(0))
            );
            while (list.size() > 0){
                group = new ProposalAnd(group, ofTson(builder, list.remove(0)));
            }
        } else if (obj.isMap()) {
            TsonMap map = obj.getMap();
            ProposalPredicate predicate = null;
            ProposalPredicate predicate2;
            if(map.containsKey("not")){
                predicate = new ProposalNot(ofTson(builder, map.get("not")));
            }
            if(map.containsKey("and")){
                predicate2 = ofTson(builder, map.get("and"));
                if(predicate != null){
                    predicate = new ProposalAnd(predicate, predicate2);
                } else {
                    predicate = predicate2;
                }
            }

            for (String key: map.keySet()){
                if(key.startsWith("at-")){
                    WordPredicate wordPredicate = WordPredicate.ofTson(builder, map.get(key));
                    String[] subKeys = key.split("-");
                    BooleanByteImmutablePair pair = ProposalCheckWord.argsOf(subKeys[1], subKeys.length==2?null:subKeys[2]);

                    return new ProposalCheckWord(wordPredicate, pair.rightByte(), pair.leftBoolean());

//                    switch (subKeys[1]){
//                        case "start":
//                            return new ProposalCheckWord(wordPredicate, 0, false);
//                        case "end":
//                            return new ProposalCheckWord(wordPredicate, -1, false);
//                        case "b":
//                        case "before":
//                            return new ProposalCheckWord(wordPredicate, -Integer.parseInt(subKeys[2]), true);
//                        case "c":
//                        case "cursor":
//                            if(subKeys.length == 2)
//                                return new ProposalCheckWord(wordPredicate, 0, true);
//                        case "after":
//                            return new ProposalCheckWord(wordPredicate, Integer.parseInt(subKeys[2]), true);
//                        case "pos":
//                        case "position":
//                            return new ProposalCheckWord(wordPredicate, Integer.parseInt(subKeys[2]), false);
//                        default:
//                            return new ProposalCheckWord(wordPredicate, Integer.parseInt(subKeys[1]), false);
//                    }
                }
            }

            if(map.containsKey("or")){
                predicate2 = ofTson(builder, map.get("or"));
                if(predicate != null){
                    predicate = new ProposalOr(predicate, predicate2);
                } else {
                    predicate = predicate2;
                }
            }
            if(predicate != null)return predicate;
        } else if (obj.isCustom()) {
            return (ProposalPredicate) obj.getField();
        } else if (obj.isString()) {
            String key = obj.getStr();
//            int idx = key.indexOf(":");
//            String subKey = null;
//            if(idx != -1){
//                subKey = key.substring(idx+1);
//                key = key.substring(0, idx);
//            }

            switch (key){
                case "multi" : return MULTI;
                case "single": return SINGLE;
                case "maiden":
                case "gen-girl":
                case "girly" : return GEN_GIRL;
                case "gen-boy":return GEN_BOY;
            }
        }
        throw new IllegalArgumentException();
    }
}















