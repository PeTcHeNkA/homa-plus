package plus.common.mashine.furnace;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import plus.common.block.SimpleOre;
import plus.common.mashine.inv.EnergyDeviceTileEntity;
import plus.common.mashine.inv.SimpleCraftMap;
import plus.common.mashine.item.BasicItem;


public class SpFurnaceTileEntity extends EnergyDeviceTileEntity {
    private static final int[] SLOTS_INPUT = new int[] {2, 3};
    private static final int[] SLOTS_OUTPUT = new int[] {4, 5};
    private static final SimpleCraftMap furnaceCrafts = new SimpleCraftMap() {
        @Override
        public void onInit() {
            add( Blocks.COBBLESTONE,   Blocks.STONE                        );
            add( Blocks.SAND,          Blocks.GLASS                ,2);
            add( Blocks.IRON_ORE,      Items.IRON_INGOT            ,2);
            add( Blocks.GOLD_ORE,      Items.GOLD_INGOT            ,2);
            add( Blocks.CLAY,          Blocks.HARDENED_CLAY                );
            add( Items.CLAY_BALL,      Items.BRICK                         );
            add( Blocks.DIAMOND_ORE  , Items.DIAMOND               ,2);
            add( Blocks.DIAMOND_ORE  , Items.DIAMOND               ,2);
            add( Blocks.HARDENED_CLAY, Blocks.STAINED_HARDENED_CLAY        );
            add( SimpleOre.COPPER_ORE, BasicItem.COPPER_INGOT      ,2);
            add( SimpleOre.ALUMINE_ORE,BasicItem.ALUMINE_INGOT     ,2);
            add( SimpleOre.MEFRIL_ORE, BasicItem.MEFRIL_INGOT              );
            add( SimpleOre.AETHER_ORE, BasicItem.AETHER_CRYSTAL    ,4);
        }
    };


    public SpFurnaceTileEntity() {
        super(6);
    }


    @Override
    public Container createContainer(InventoryPlayer playerInv, EntityPlayer playerIn) {
        return new SpFurnaceContainer(playerInv, this);
    }


    @Override
    public void update(){
        if(isFurnaceCanCraft(2,4,5) || isFurnaceCanCraft(3,5,4)){
            if(!useEnergyIfCan())return;
            if(onSuccessProgress(100)){
                completeCraft(2, 4, 5);
                completeCraft(3, 5, 4);
            }
        } else {
            resetProgress();
        }
    }


    private void completeCraft(int from, int to, int alt){
        ItemStack item = stacks.get(from);
        Item item0 = item.getItem();
        if(item0 != Items.AIR){
            ItemStack itemResult = stacks.get(to);
            int count = itemResult.getCount();

            SimpleCraftMap.ItemGen gen = furnaceCrafts.get(item0);
            if(count == 0){
                stacks.set(to, gen.create());
                takeOneItem(item);
            } else {
                if(itemResult.getItem() == gen.get()){
                    if(count+gen.getCount()<=64) {
                        itemResult.setCount(count + gen.getCount());
                        takeOneItem(item);
                    } else {
                        ItemStack itemResult2 = stacks.get(alt);
                        int stCount = itemResult2.getCount();

                        int addToAlt = (count+gen.getCount())-64;

                        if(stCount==0){
                            itemResult.setCount(64);
                            itemResult2 = new ItemStack(gen.get());
                            itemResult2.setCount(addToAlt);
                            stacks.set(alt, itemResult2);
                            takeOneItem(item);
                        } else {
                            if (itemResult2.getItem() == gen.get() && stCount+addToAlt<=64) {
                                if(stCount<64){
                                    itemResult.setCount(64);
                                }
                                itemResult2.setCount(stCount+addToAlt);
                                takeOneItem(item);
                            }
                        }
                    }
                } else {
                    itemResult = stacks.get(alt);
                    count = itemResult.getCount();
                    if(count == 0){
                        itemResult = new ItemStack(gen.get());
                        itemResult.setCount(gen.getCount());
                        stacks.set(alt, itemResult);
                        takeOneItem(item);
                    } else {
                        if(itemResult.getItem() == gen.get() && count+gen.getCount()<=64) {
                            itemResult.setCount(count + gen.getCount());
                            takeOneItem(item);
                        }
                    }
                }
            }
        }
    }


    private boolean isFurnaceCanCraft(int from, int to, int alt){
        ItemStack item = stacks.get(from);
        Item item0 = item.getItem();
        if(item0 == Items.AIR)return false;
        ItemStack res1 = stacks.get(to);
        int count = res1.getCount();

        SimpleCraftMap.ItemGen gen = furnaceCrafts.get(item0);
        if(gen==null)return false;

        if(count == 0 || count+gen.getCount() <= 64 && res1.getItem() == gen.get()){
            return true;
        } else {
            res1 = stacks.get(alt);
            count = res1.getCount();
            return count == 0 || count+gen.getCount() <= 64 && res1.getItem() == gen.get();
        }
    }


    @Override
    public String getGuiID() {
        return "minecraft:sp_furnace_gui";
    }


    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "sp_craft_gui";
    }


    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if(side==EnumFacing.DOWN){
            return SLOTS_OUTPUT;
        } else {
            return SLOTS_INPUT;
        }
    }


    @Override
    public int useOnActiveTick() {
        return 3;
    }
}