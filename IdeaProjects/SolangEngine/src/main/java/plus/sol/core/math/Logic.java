package plus.sol.core.math;


public abstract class Logic<T> {
    //не оптимально
    public static boolean isLogic(String str){
        return build(str)!=null;
    }

    public static Logic build(String str){
        if(str.charAt(0)=='!'){
            String result = str.substring(1);
            return new Logic() {
                private final Logic l = build(result);
                @Override
                public boolean checkFor(Object operand1, Object operand2) {
                    return !l.checkFor(operand1, operand2);
                }
            };
        } else if(str.equals("==")){
            return new Logic<Object>() {
                @Override
                public boolean checkFor(Object operand1, Object operand2) {
                    if(operand1==null)return operand2 == null;
                    return operand1.equals(operand2);
                }
            };
        } else if(str.equals(">")){
            return new Logic<Number>() {
                @Override
                public boolean checkFor(Number operand1, Number operand2) {
                    return operand1.floatValue()>operand2.floatValue();
                }
            };
        } else if(str.equals("<")) {
            return new Logic<Number>() {
                @Override
                public boolean checkFor(Number operand1, Number operand2) {
                    return operand1.floatValue() < operand2.floatValue();
                }
            };
        } else if(str.equals(">=") || str.equals("=>")){
            return new Logic<Number>() {
                @Override
                public boolean checkFor(Number operand1, Number operand2) {
                    return operand1.floatValue() >= operand2.floatValue();
                }
            };
        } else if(str.equals("<=") || str.equals("=<")) {
            return new Logic<Number>() {
                @Override
                public boolean checkFor(Number operand1, Number operand2) {
                    return operand1.floatValue() <= operand2.floatValue();
                }
            };
        } else if(str.equalsIgnoreCase("IN")) {
            return new Logic() {
                @Override
                public boolean checkFor(Object operand1, Object operand2) {
                    return String.valueOf(operand2).contains(String.valueOf(operand1));
                }
            };
        }
        throw new UnsupportedOperationException(str);
    }

    public abstract boolean checkFor(T operand1, T operand2);
}