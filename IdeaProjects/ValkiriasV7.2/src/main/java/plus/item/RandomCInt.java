package plus.item;

import java.util.Random;

public class RandomCInt {
    private static final Random r = new Random();
    private final int[] slots;
    public RandomCInt(String v){
        String[] slotsr = v.replace(" ", "").split(";");
        slots = new int[slotsr.length];
        for(int i = 0;i< slots.length;i++){
            slots[i] = Integer.parseInt(slotsr[i]);
        }
    }
    public int getRandomSlot(){
        return slots[r.nextInt(slots.length)];
    }
}