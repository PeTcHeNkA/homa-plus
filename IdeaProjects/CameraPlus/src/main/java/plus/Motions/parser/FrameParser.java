package plus.Motions.parser;

import java.util.List;

public class FrameParser implements Parser{

    private boolean hasStartArguments = false;

    protected List<String> data;
    protected double[] startPos;
    protected double[] lookTarget;
    protected int time;
    protected boolean invertLook = false;

    public FrameParser(){}

    public FrameParser(List<String> data, double[] startPos, double[] lookTarget, int time){
        hasStartArguments = true;
        this.data = data;
        this.startPos = startPos;
        this.lookTarget = lookTarget;
        this.time = time;
    }

    public void setInvertLook(boolean newValue){
        invertLook = newValue;
    }

    @Override
    public MotionContainer parse(List<String> data, double[] startPos, double[] lookTarget, int time) {
        return null;
    }
    @Override
    public MotionContainer parse(){
        if(hasStartArguments){
            return parse(data, startPos, lookTarget, time);
        } else throw new NullPointerException("Start arguments is null!");
    }

}
