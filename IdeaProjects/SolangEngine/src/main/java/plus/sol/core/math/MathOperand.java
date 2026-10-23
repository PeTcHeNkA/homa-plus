package plus.sol.core.math;


public abstract class MathOperand {

    public static MathOperand build(String data){
        if(data.equals("+")){
            return new MathOperand() {
                @Override
                public Number calc(Number n1, Number n2) {
                    return n1.floatValue()+n2.floatValue();
                }
            };
        } else if(data.equals("*")){
            return new MathOperand() {
                @Override
                public Number calc(Number n1, Number n2) {
                    return n1.floatValue()*n2.floatValue();
                }
            };
        } else if(data.equals("/")){
            return new MathOperand() {
                @Override
                public Number calc(Number n1, Number n2) {
                    return n1.floatValue()/n2.floatValue();
                }
            };
        } else if(data.equals("//")){
            return new MathOperand() {
                @Override
                public Number calc(Number n1, Number n2) {
                    return (int)(n1.floatValue()/n2.floatValue());
                }
            };
        } else if(data.equals("%")){
            return new MathOperand() {
                @Override
                public Number calc(Number n1, Number n2) {
                    return n1.floatValue()%n2.floatValue();
                }
            };
        }
        return null;
    }


    public abstract Number calc(Number n1, Number n2);
}
