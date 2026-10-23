package plus.inventory;


import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class MInventoryContainer extends Container {

    //Немного кода из ванилы
    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
    private final EntityPlayer thePlayer;

    /**
     * Конструктор
     * @param playerInventory Инвентарь игрока
     * @param cInventory Кастомный инвентарь
     * @param player Игрок
     */
    public MInventoryContainer(InventoryPlayer playerInventory, MInventory cInventory, EntityPlayer player) {

        this.thePlayer = player;
        //Добавляем 8 кастомных слотов. Аргументы: игрок, инвентарь к которому они относятся, индекс слота, х координата, у координата
        this.addSlotToContainer(new MSlot(player, cInventory, 0, 87, 8));
        this.addSlotToContainer(new MSlot(player, cInventory, 1, 87, 26));
        this.addSlotToContainer(new MSlot(player, cInventory, 2, 87, 44));
        this.addSlotToContainer(new MSlot(player, cInventory, 3, 87, 62));
        this.addSlotToContainer(new MSlot(player, cInventory, 4, 109, 8));
        this.addSlotToContainer(new MSlot(player, cInventory, 5, 109, 26));
        this.addSlotToContainer(new MSlot(player, cInventory, 6, 109, 44));
        this.addSlotToContainer(new MSlot(player, cInventory, 7, 109, 62));

        //Все что ниже можно взять из net.minecraft.inventory.ContainerPlayer;
        //Добавляем ванильные слоты для брони
        for (int k = 0; k < 4; ++k){
            final EntityEquipmentSlot entityequipmentslot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(playerInventory, 36 + (3 - k), 8, 8 + k * 18){

                public int getSlotStackLimit(){
                    return 1;
                }

                public boolean isItemValid(ItemStack stack){
                    return stack.getItem().isValidArmor(stack, entityequipmentslot, thePlayer);
                }

                public boolean canTakeStack(EntityPlayer playerIn){
                    ItemStack itemstack = this.getStack();
                    return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
                }
                @Nullable
                @SideOnly(Side.CLIENT)
                public String getSlotTexture(){
                    return ItemArmor.EMPTY_SLOT_NAMES[entityequipmentslot.getIndex()];
                }
            });
        }

        //Добавляем 27 ванильных слотов инвентаря игрока
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {

                this.addSlotToContainer(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        //А так же добавляем 9 ванильных слотов в хотбар
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }
    }

    /**
     * Этот метод срабатывает когда игрок зажимает Шифт и кликает на слот с целью переместить предмет.
     * Здесь мы должны задать откуда и куда будут перемещаться предметы из слота по которому кликнули
     * @param index Индекс слота, на который кликнул игрок
     */
    @Nullable
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {

        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.inventorySlots.get(index);
        //Если слот существует и он не пуст
        if (slot != null && slot.getHasStack()){
            //Достаем стак из слота
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            //Взаимодействие
            //Если индекс слота меньше 12, т.е. игрок уликнул на кастомный слот или слот брони
            if (index < 12){
                //Пытаемся переместить стак в ПЕРВЫЙ свободный слот в хотбаре или инвентаре, т.е. между 12 и 47 слотом
                if (!this.mergeItemStack(itemstack1, 12, 48, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            }

            //Здесь наоборот. Если игрок кликнул на слот в инвентаре или хотбаре
            else if (index > 11){
                //Если это броня, то ее надо переместить в первый подходящий для нее слот между 8 и 11 индексом
                if(itemstack1.getItem() instanceof ItemArmor){
                    //тут один момент. Почему передаем 12 а не 11? Потому что не включительно. Т.е. между 8 и 12 слотом не включительно
                    if (!this.mergeItemStack(itemstack1, 8, 12, false)){
                        return ItemStack.EMPTY;
                    }
                }else
                    //Если это не броня и мы в инвентаре но не в хотбаре(т.е. между 12 и 38 слотом), то помещаем предмет в хотбар, т.е. между 39 и 47 слотом
                    if (index >= 12 && index < 39){
                        if (!this.mergeItemStack(itemstack1, 39, 48, false)){
                            return ItemStack.EMPTY;
                        }
                    }else
                        //Если мы в хотбаре(т.е. между 39 и 47 слотом) то пытаемся переместить предмет в инвентарь(т.е. между в ПЕРВЫМ свободным слотом в инвентаре, т.е. между 12 и 38 слотом)
                        if (index >= 39 && index < 48 && !this.mergeItemStack(itemstack1, 12, 39, false)){
                            return ItemStack.EMPTY;
                        }
            }
            //Остальные простые проверки
            if (itemstack1.getCount() == 0){
                slot.putStack(ItemStack.EMPTY);
            }
            else{
                slot.onSlotChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()){
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, itemstack1);
        }
        return itemstack;
    }


    /**
     * Может ли игрок взаимодействовать с инвентарем?
     */
    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

}