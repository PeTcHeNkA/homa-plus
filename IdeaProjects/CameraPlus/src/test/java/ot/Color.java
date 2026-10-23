package ot;

public class Color {
    public String ColorOf(String s){
        //      ,"color":"%s"
        return switch (s) {
            case "0" -> "black"; //
            case "1" -> "dark_blue";
            case "2" -> "dark_green";
            case "3" -> "dark_aqua";
            case "4" -> "dark_red";
            case "5" -> "dark_purple";
            case "6" -> "gold";
            case "7" -> "gray";
            case "8" -> "dark_gray";
            case "9" -> "blue";
            case "a" -> "green";
            case "b" -> "aqua";
            case "c" -> "red";
            case "d" -> "light_purple";
            case "e" -> "yellow";
            case "f" -> "white";
            default -> null;
        };
    }//  #7EC45C

    public String vExtra(String s){
        //      ,"%s":true
        return switch (s) {
            case "k" -> "obfuscated"; //
            case "m" -> "strikethrough";
            case "o" -> "italic";
            case "l" -> "bold";
            case "n" -> "underlined";
            default -> null;
        };
    }

    //,"strikethrough":true //зачеркнуть
    //,"underlined":true," подчеркнуть
    //,"italic":true,  наклонить
    //"bold":true, обжирнить
    //,"obfuscated":true, сделать рандом символы
}
