package plus.common.spship;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;

import static plus.common.spship.item.ItemModule.SP_MODULE;


public enum SPM {
    DAMAGE,
    DAMAGE_PR,
    MAX_SHIELD,
    MAX_SHIELD_PR,
    SPEED,
    SPEED_PR,
    CRIT_RATE,
    CRIT_DMG,
    REGEN_SHIELD,
    MASS,
    ENERGY,
    ENERGY_GEN,
    POTENTIAL
    ;
    public static final int SIZE = SPM.values().length;

    public static int[] create(){
        return new int[SPM.SIZE];
    }


    public static NBTTagIntArray write(int... spm){
        int lastNotNull = spm.length-1;
        for(int i = lastNotNull;i>0;i--){
            if(spm[i]<1){
                lastNotNull = i;
                break;
            }
        }
        int[] copy = new int[lastNotNull];
        System.arraycopy(spm, 0, copy, 0, lastNotNull);

        return new NBTTagIntArray(copy);
    }


    public static int[] read(ItemStack item){
        NBTTagCompound tags = item.getTagCompound();
        if(tags==null){
            return empty;
        }
        return tags.getIntArray("p");
    }


    public static void write(ItemStack item, int... spm){
        NBTTagCompound tags = item.getTagCompound();
        if(tags==null){
            tags = new NBTTagCompound();
            item.setTagCompound(tags);
        }
        tags.setIntArray("p", spm);
    }


    public static int[] read(NBTTagIntArray data){
        return normalize(data.getIntArray());
    }


    public static int[] normalize(int[] spm){
        if(spm.length == SPM.SIZE){
            return spm;
        } else {
            int[] copy = SPM.create();
            System.arraycopy(spm, 0, copy, 0, spm.length);
            return copy;
        }
    }


    public static SPM.V getOf(ItemStack item){
        if(item.getItem() != SP_MODULE)return null;
        NBTTagCompound tags = item.getTagCompound();
        if(tags==null)return null;
        return new SPM.V(tags.getIntArray("p"));
    }

    private static final int[] empty = new int[0];
    public static int[] getSimpleSpm(ItemStack item){
        NBTTagCompound tags = item.getTagCompound();
        if(tags==null)return empty;
        return tags.getIntArray("p");
    }


    public final int I;
    SPM(){
        I = V.num++;
    }
    public static class V {
        private static int num = 0;

        public final int[] SPM;

        public V(){
            SPM = create();
        }


        public V(int[] spm){
            SPM = normalize(spm);
        }


        public void set(SPM spm, int val){
            SPM[spm.I] = val;
        }


        public void setF(SPM spm, float val){
            SPM[spm.I] = (int) (val*100);
        }


        public int get(SPM spm){
            return SPM[spm.I];
        }


        public float getF(SPM spm){
            return SPM[spm.I]/100f;
        }


        public void add(int[] spm){
            for(int i=0;i<spm.length;i++){
                SPM[i] += spm[i];
            }
        }


        public void add(SPM.V spm){
            add(spm.SPM);
        }


        public C compile(){
            return new C(SPM);
        }
    }


    public static class C{
        public final float
        DAMAGE,
        DAMAGE_PR,
        MAX_SHIELD,
        MAX_SHIELD_PR,
        SPEED,
        SPEED_PR,
        CRIT_RATE,
        CRIT_DMG,
        REGEN_SHIELD,
        MASS,
        ENERGY,
        ENERGY_GEN,
        POTENTIAL
        ;
        public C(int... ints) {
            short i = 0;
            DAMAGE        = getIOf(ints, i++);
            DAMAGE_PR     = getIOf(ints, i++);
            MAX_SHIELD    = getIOf(ints, i++);
            MAX_SHIELD_PR = getIOf(ints, i++);
            SPEED         = getIOf(ints, i++);
            SPEED_PR      = getIOf(ints, i++);
            CRIT_RATE     = getIOf(ints, i++);
            CRIT_DMG      = getIOf(ints, i++);
            REGEN_SHIELD  = getIOf(ints, i++);
            MASS          = getIOf(ints, i++);
            ENERGY        = getIOf(ints, i++);
            ENERGY_GEN    = getIOf(ints, i++);
            POTENTIAL     = getIOf(ints, i++);
        }
    }


    public static boolean isEmpty(int... ints){
        if(ints.length != 0) {
            for (int i : ints) {
                if (i != 0) return false;
            }
        }
        return true;
    }


    public static float getIOf(int[] ints, int index){
        return ints.length>index?ints[index]/100f:0;
    }


    public static int getIOfI(int[] ints, int index){
        return ints.length>index?ints[index]:0;
    }
}