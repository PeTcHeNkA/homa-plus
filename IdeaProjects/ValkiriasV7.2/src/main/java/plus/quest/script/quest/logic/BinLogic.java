package plus.quest.script.quest.logic;


public abstract class BinLogic {


    public static BinLogic build(String data){
        if(data.charAt(0)=='!'){
            String result = data.substring(1);
            return new BinLogic() {
                private final BinLogic l = BinLogic.build(result);
                @Override
                public boolean checkFor(boolean operand1, boolean operand2) {
                    return !l.checkFor(operand1, operand2);
                }
            };
        }
        if(data.equals("&&")){
            return new BinLogic() {
                @Override
                public boolean checkFor(boolean operand1, boolean operand2) {
                    return operand1 && operand2;
                }
            };
        } else if(data.equals("||")){
            return new BinLogic() {
                @Override
                public boolean checkFor(boolean operand1, boolean operand2) {
                    return operand1 || operand2;
                }
            };
        }
        return null;
    }


    public abstract boolean checkFor(boolean operand1, boolean operand2);
}
