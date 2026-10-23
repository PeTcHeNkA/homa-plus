package plus.item;

import java.util.Objects;

public class BaseInfo {

    public final String ID;//ссылка на инструкцию сборки
    public final int POWER;//сила артефакта (визуальный фон)
    public final int TYPE;//оружие, артефакт, предмет  1, 2, 3 соотв.
                            //4 - еда
                            //5 - энергетич.
                            //не индитифиц = 6
                            //7 - лутбокс
    public final int CLASS;//класс оружия, слот артефакта


    public BaseInfo(String arg){
        if(arg.contains("&")) {
            String[] args = arg.split("&");
            ID = args[0];
            POWER = Integer.parseInt(args[1]);
            if(args.length>3) {
                TYPE = Integer.parseInt(args[2]);
                CLASS = Integer.parseInt(args[3]);
            } else {
                if(args.length==3){
                    TYPE = Integer.parseInt(args[2]);
                } else {
                    TYPE = 3;
                }
                CLASS = -1;
            }
        } else {
            ID = arg;
            POWER = -1;
            TYPE = 3;
            CLASS = -1;
        }
    }

    public BaseInfo(String id, int... args){
        ID = id;
        POWER = args[0];
        TYPE = args[1];
        CLASS = args[2];
    }

    public String toString(){
        if(POWER == -1 && TYPE == 3 && CLASS == -1) return ID;
        return ID+"&"+POWER+"&"+TYPE+"&"+CLASS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseInfo baseInfo = (BaseInfo) o;
        return ID.equals(baseInfo.ID) && TYPE == baseInfo.TYPE && baseInfo.POWER >= POWER && (CLASS == -1 || CLASS == baseInfo.CLASS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, POWER, TYPE, CLASS);
    }
}