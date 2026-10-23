package plus.inventory;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;


public class MInventory implements IInventory {

    //Константа которая представляет кол-во добавленных кастомных слотов. У нас их будет 8
    public static final int INV_SIZE = 8;

    /*Список в котором хранятся предметы. Если мы кладем предмет в слот в кастомном инвентаре, он добавляется в этот список. Размер списка должен
    быть таким же, как и кол-во кастомных слотов, т.е. 8*/
    private NonNullList<ItemStack>inventory = NonNullList.<ItemStack>withSize(INV_SIZE, ItemStack.EMPTY);

    //Конструктор
    public MInventory() {}

    /**
     * Имя инвентаря. Можно потом использовать в GUI
     */
    @Override
    public String getName() {
        return "Inventory";
    }

    /**
     * Имеет ли инвентарь свое кастомное имя. Да, имеет.
     */
    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) :
                new TextComponentTranslation(this.getName());
    }

    /**
     * Возвращает размер инвентаря
     */
    @Override
    public int getSizeInventory() {
        return inventory.size();
    }

    /**
     * Возвращает ItemStack что лежит по указанному индексу
     */
    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.inventory.size() ? (ItemStack)this.inventory.get(index) : ItemStack.EMPTY;
    }

    /**
     * Возвращает список с хранящимися в инвентаре предметами
     * @return Список с хранящимися в инвентаре предметами
     */
    public NonNullList<ItemStack> getStacks(){
        return this.inventory;
    }

    /**
     * Удаляет определенное количество элементов из слота инвентаря и возвращает их в виде нового списка.
     */
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, index, count);
        if (!itemstack.isEmpty()) {
            this.markDirty();
        }
        return itemstack;
    }

    /**
     * Удаляет стак из указанного слота и возвращает его.
     */
    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (!this.inventory.get(index).isEmpty()) {
            ItemStack itemstack = this.inventory.get(index);
            this.inventory.set(index, ItemStack.EMPTY);
            return itemstack;
        }else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Добавляет стак в указанный слот
     */
    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
    }

    /**
     * Возвращает true если весь инвентарь пустой. Если не пустой - возвращает false
     */
    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Возвращает максимальный размер стака в слоте
     */
    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    /**
     * Для тайл энтити. Нам он не надо
     */
    @Override
    public void markDirty() { }

    /**
     * Может ли игрок ипользовать этот инвентарь
     */
    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    /**
     * Может ли автоматика класть предметы в слоты с указанным индексом
     */
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    //Эти три метода ниже пока не нужны
    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    /**
     * Очистка инвентаря
     */
    @Override
    public void clear() {
        this.inventory.clear();
    }

    /**
     * Этот метод будет вызываться при сохранении инфы в НБТ КАПы, чтобы, например при перезаходе предметы не пропали из инвентаря
     * @param compound
     */
    public void writeToNBT(NBTTagCompound compound) {
        ItemStackHelper.saveAllItems(compound, this.inventory);
    }

    /**
     * Этот метод будет вызываться при чтении инфы из НБТ КАПы. Достаем из НБТ КАПы стаки и заполняем инвентарь
     * @param compound
     */
    public void readFromNBT(NBTTagCompound compound) {
        this.inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.inventory);
    }

    /**
     * Копируем стаки из другого инвентаря в этот. Используется в PlayerEvent.Clone Event, да бы сохранить содержимое инвентаря, например когда игрок проходит через портал в Энд
     * @param inv
     */
    public void copy(MInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            inventory.set(i, (stack.isEmpty() ? ItemStack.EMPTY : stack.copy()));
        }
    }
}