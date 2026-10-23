package plus;

import java.util.ArrayList;
import java.util.List;


public class Sector1 implements Sector{

    private final Region1 parent;
    private final Sector subRegions;
    private final Sector boarder;
    private final Sector other;

    public Sector1(Region1... regions){
        int[] betterCenterIndexes = {0, 1};

        int betterCenter = regions.length;

        int maxCenter = regions.length/2;

        int r1i = -1;
        for(Region1 r1:regions){
            ++r1i;
            int r2i = -1;
            for(Region1 r2:regions){
                ++r2i;
                int count = 0;
                if(r1 == r2)continue;

                Region1 b1 = new Region1(r1.X_1, r1.Y_1, r1.Z_1, r2.X_2, r2.Y_2, r2.Z_2);

                for(Region1 t:regions){
                    if(t.containsArea(b1))++count;
                }
                int index;
                if(count>maxCenter)index = count - maxCenter;
                else index = maxCenter-count;
                if(betterCenter>index){
                    betterCenter = index;
                    betterCenterIndexes = new int[]{r1i, r2i};
                }
            }
        }
        Region1 r1 = regions[betterCenterIndexes[0]];
        Region1 r2 = regions[betterCenterIndexes[1]];

        parent = new Region1(r1.X_1, r1.Y_1, r1.Z_1, r2.X_2, r2.Y_2, r2.Z_2);

        List<Region1> subRegs = new ArrayList<>();
        List<Region1> boarder = new ArrayList<>();
        List<Region1> others = new ArrayList<>();

        for(Region1 r:regions){
            int t = r.typeRelationOf(parent);

            if(t==1) subRegs.add(r);
            else if(t==0) boarder.add(r);
            else others.add(r);
        }


        this.subRegions = Sector.buildSector(subRegs);
        System.out.println("------------------");
        System.out.println(subRegions);
        System.out.println("---- sub regs ----");
        for (Region1 er:subRegs){
            System.out.println(er);
        }
        this.boarder = new SmallSector(boarder.toArray(boarder.toArray(new Region1[0])));//Sector.buildSector(boarder);
        this.other = Sector.buildSector(others);
        System.out.println("------------------");
        System.out.println(other);
        System.out.println("---- other ----");
        for (Region1 er:others){
            System.out.println(er);
        }
    }


    @Override
    public boolean containsArea(Region1 r) {
        return parent.containsArea(r);
    }


    @Override
    public boolean containsCords(int x, int y, int z) {
        return parent.containsCords(x, y, z);
    }


    @Override
    public int typeRelationOf(Region1 r) {
        return parent.typeRelationOf(r);
    }


    @Override
    public Region getRegionOfCords(int x, int y, int z) {
        if(subRegions.containsCords(x, y, z)){
            return subRegions.getRegionOfCords(x, y, z);
        } else if(boarder.containsCords(x, y, z)){
            return boarder.getRegionOfCords(x, y, z);
        } else return other.getRegionOfCords(x, y, z);
    }


    @Override
    public String toString(){
        return parent.toString();
    }

}