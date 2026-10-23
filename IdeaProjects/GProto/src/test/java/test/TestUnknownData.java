package test;


public class TestUnknownData {
    public static final String PRE = "C:\\Games\\GF2Exilium\\GF2 Game\\GF2_Exilium_Data\\LocalCache\\Data\\Table/".replace('\\', '/');

    private static String src(String name){
        return PRE + name+".bytes";
    }

    public static void main(String[] args) throws Exception {
        //UPack pack = new UPack(src("StoryData"));

        //System.out.println(pack.tryFindStrIDS());
//        System.out.println(pack);
    }
}
