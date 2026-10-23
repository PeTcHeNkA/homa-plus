package plus.quest.parser;

import plus.quest.motion.BasicPos;
import plus.quest.motion.Motion;
import java.util.List;


public interface Parser {
    Motion[] parse(List<String> rawData, BasicPos lookTarget, int time, boolean invertLook);


    static Parser valueOf(String s){
        return LinearParser.getInstance();
    }
}