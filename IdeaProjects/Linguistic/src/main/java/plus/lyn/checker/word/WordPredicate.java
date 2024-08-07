package plus.lyn.checker.word;

import plus.lyn.Case;
import plus.lyn.Word;
import plus.lyn.WordBuilder;
import plus.lyn.WordPartIndex;
import plus.tson.TsonFunc;
import plus.tson.TsonList;
import plus.tson.TsonMap;
import plus.tson.TsonObj;
import plus.tson.utl.FuncCompiler;


public interface WordPredicate {
    WordPredicate TRUE = word -> true;
    WordCase[] wordCases = new WordCase[]{
            new WordCase(0), new WordCase(1), new WordCase(2),
            new WordCase(3), new WordCase(4), new WordCase(5)
    };
    WordMulti W_MULTI  = new WordMulti(true);
    WordMulti W_SINGLE = new WordMulti(false);

    boolean isSuccess(Word word);


    static WordPredicate ofTson(WordBuilder builder, TsonObj obj){
        if(obj.isList()){
            TsonList list = obj.getList();
            if(list.size() == 0)return TRUE;
            if(list.size() == 1)return ofTson(builder, list.get(0));

            WordAnd group = new WordAnd(
                    ofTson(builder, list.remove(0)), ofTson(builder, list.remove(0))
            );
            while (list.size() > 0){
                group = new WordAnd(group, ofTson(builder, list.remove(0)));
            }
        } else if (obj.isString()) {
            String key = obj.getStr();
            int idx = key.indexOf(":");
            String subKey = null;
            if(idx != -1){
                subKey = key.substring(idx+1);
                key = key.substring(0, idx);
            }

            switch (key){
                case "case"  : return wordCases[Case.Type.ofString(subKey).id];
                case "equal" : return new WordEqual(builder, subKey);
                case "multi" : return W_MULTI;
                case "single": return W_SINGLE;
                case "part"  : return WordPart.getByName(subKey);
                case "unsafe": return WordPart.getByPart(WordPartIndex.UNSAFE);
            }
        } else if (obj.isMap()) {
            TsonMap map = obj.getMap();
            WordPredicate predicate = null;
            WordPredicate predicate2 = null;
            if(map.containsKey("not")){
                predicate = new WordNot(ofTson(builder, map.get("not")));
            }
            if(map.containsKey("and")){
                predicate2 = ofTson(builder, map.get("and"));
                if(predicate != null){
                    predicate = new WordAnd(predicate, predicate2);
                } else {
                    predicate = predicate2;
                }
            }
            if(map.containsKey("or")){
                predicate2 = ofTson(builder, map.get("or"));
                if(predicate != null){
                    predicate = new WordOr(predicate, predicate2);
                } else {
                    predicate = predicate2;
                }
            }
            if(predicate != null)return predicate;
        } else if (obj.isCustom()) {
            Object raw = obj.getField();

            if(raw instanceof TsonFunc.Frame){
                TsonFunc.Frame frame = (TsonFunc.Frame) raw;
                raw = TsonFunc.COMPILER.compile(frame);
            }

            if(raw instanceof TsonFunc){
                TsonFunc func = (TsonFunc) raw;
                return word -> (boolean)func.call(word);
            }
        }
        throw new IllegalArgumentException();
    }
}
