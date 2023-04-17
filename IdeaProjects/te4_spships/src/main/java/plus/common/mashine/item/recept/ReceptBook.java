package plus.common.mashine.item.recept;

import net.minecraft.item.ItemStack;
import java.util.HashMap;
import java.util.Map;


public abstract class ReceptBook<T extends ICraftMatrix, R extends ReceptProcessor> implements IReceptIndexator {
    protected final HashMap<ReseptKey, R> recepts = new HashMap<>();
    protected final HashMap<Integer, ReseptKey> indexes = new HashMap<>();
    protected final int controlSize;
    private int lastReceptID = 0;

    protected ReceptBook(int controlSize) {
        this.controlSize = controlSize;
    }

    @Override
    public int nextReceptId(){
        return ++lastReceptID;
    }


    public ReseptKey getById(int id){
        return indexes.get(id);
    }


    public R getResult(ReseptKey key){
        return recepts.get(key);
    }


    public static ItemStack[] normalize(ItemStack[] items, int size){
        if(items.length < size) {
            ItemStack[] copy = new ItemStack[size];
            System.arraycopy(items, 0, copy, 0, items.length);
            for(int i=copy.length-(size-items.length); i<copy.length;i++){
                copy[i] = ItemStack.EMPTY;
            }
            return copy;
        } else {
            return items;
        }
    }


    public ReceptBook<T, R> add(R processor, ItemStack... key){
        return add(nextReceptId(), processor, key);
    }


    public ReceptBook<T, R> add(int id, R processor, ItemStack... key){
        ReseptKey rk;
        if(key.length == controlSize){
            rk = new ReseptKey(this, key);
        } else {
            rk = new ReseptKey(this, normalize(key, controlSize));
        }
        recepts.put(rk, processor);
        if(id>lastReceptID){
            lastReceptID=id;
        }
        indexes.put(id, rk);
        return this;
    }


    public boolean isValidCraft(T matrix){
        ReseptKey key = matrix.toKey(this);
        ReceptProcessor processor = recepts.get(key);
        return processor != null && processor.valid(key);
    }


    public ItemStack craftPreview(T matrix){
        ReseptKey key = matrix.toKey(this);
        ReceptProcessor processor = recepts.get(key);
        if(processor==null || !processor.valid(key))return ItemStack.EMPTY;
        return processor.calcResult(key);
    }


    public void spendItems(T matrix){
        ReseptKey key = matrix.toKey(this);
        ReceptProcessor processor = recepts.get(key);
        if(processor==null || !processor.valid(key))return;
        processor.spendItems(key, matrix);
    }
}