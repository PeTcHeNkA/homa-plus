package plus.Motions.parser;

import java.util.List;

public interface Parser {
    MotionContainer parse(List<String> data, double[] startPos , double[] lookTarget, int time);
    MotionContainer parse();
    void setInvertLook(boolean newValue);
}
