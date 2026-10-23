public class test {
    private static int seed = 1;
    public static int nextInt(int maxIndex){
        seed = (seed * 111119 + 31);
        if(seed<0)seed = -seed;
        return seed % maxIndex;
    }
    public static void main(String[] args){
        for(int i=1;i<100;++i){
            System.out.println(nextInt(10000));
        }
    }
}

