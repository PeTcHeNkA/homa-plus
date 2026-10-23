package plus;

public class Region1 implements Region{
    public final short X_1; //0
    public final short Y_1; //1
    public final short Z_1; //2
    public final short X_2; //3
    public final short Y_2; //4
    public final short Z_2; //5


    public Region1(String rawValue){
        this(rawValue.split("\\^"));
    }


    private Region1(String[] args){
        this(
                Integer.parseInt(args[0]),
                Integer.parseInt(args[0]),
                Integer.parseInt(args[0]),
                Integer.parseInt(args[0]),
                Integer.parseInt(args[0]),
                Integer.parseInt(args[0]),
                Integer.parseInt(args[0])
        );
    }


    public Region1(int... cords){
        if(cords[0]>cords[3]){
            X_2 = (short) cords[0];
            X_1 = (short) cords[3];
        } else {
            X_1 = (short) cords[0];
            X_2 = (short) cords[3];
        }

        if(cords[1]>cords[4]){
            Y_2 = (short) cords[1];
            Y_1 = (short) cords[4];
        } else {
            Y_1 = (short) cords[1];
            Y_2 = (short) cords[4];
        }

        if(cords[2]>cords[5]){
            Z_2 = (short) cords[2];
            Z_1 = (short) cords[5];
        } else {
            Z_1 = (short) cords[2];
            Z_2 = (short) cords[5];
        }
    }


    @Override
    public boolean containsArea(Region1 r){
        //return (X_1<r.X_1 && X_2>r.X_2) && (Z_1<r.Z_1 && Z_2>r.Z_2) && (Y_1<r.Y_1 && Y_2>r.Z_2);
        return (X_1<=r.X_1 && X_2>=r.X_2) && (Z_1<=r.Z_1 && Z_2>=r.Z_2) && (Y_1<=r.Y_1 && Y_2>=r.Z_2);
    }


    @Override
    public boolean containsCords(int x, int y, int z){
        //return x>X_1 && x<X_2 && z>Z_1 && z<Z_2 && y>Y_1 && y<Y_2;
        return x>=X_1 && x<=X_2 && z>=Z_1 && z<=Z_2 && y>=Y_1 && y<=Y_2;
    }


    public boolean inBoarder(Region1 r){
        return isBetween(X_1, r.X_1, X_2) || isBetween(X_1, r.X_2, X_2) ||
                isBetween(Z_1, r.Z_1, Z_2) || isBetween(Z_1, r.Z_2, Z_2) ||
                isBetween(Y_1, r.Y_1, Y_2) || isBetween(Y_1, r.Y_2, Y_2);
    }


    private static boolean isBetween(int a, int x, int b){
        return (x > a && x < b) || (x < a && x > b);
    }


    @Override
    public int typeRelationOf(Region1 r){
        if(containsArea(r))return 1;
        if(inBoarder(r))return 0;
        return -1;
    }


    @Override
    public String toString(){
        return String.format("| from [%s, %s, %s] to [%s, %s, %s]", X_1, Y_1, Z_1, X_2, Y_2, Z_2);
    }
}